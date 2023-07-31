package com.joaomarcos.pokeapimiddleware.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joaomarcos.pokeapimiddleware.entities.DTO.AdvancedPokemonDTO;
import com.joaomarcos.pokeapimiddleware.entities.DTO.BasicPokemonDTO;
import com.joaomarcos.pokeapimiddleware.entities.DTO.PokemonListDTO;
import com.joaomarcos.pokeapimiddleware.entities.DTO.attributes.PokemonEvolutionItem;
import com.joaomarcos.pokeapimiddleware.entities.DTO.attributes.PokemonStatsDTO;
import com.joaomarcos.pokeapimiddleware.entities.DTO.attributes.SpriteLargeDTO;
import com.joaomarcos.pokeapimiddleware.entities.DTO.attributes.SpriteSmallDTO;
import com.joaomarcos.pokeapimiddleware.entities.PokemonEvolutionChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "pokemon")
public class PokemonController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${EXTERNAL_URL}")
    private String baseURL;

    @Value("${INTERNAL_URL}")
    private String localBaseUrl;

    private String getAttribute(JsonNode node) {
        return node != null ? node.asText() : null;
    }

    private JsonNode objectResponseData(String url) throws Exception {
        String response = restTemplate.getForObject(url, String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(response);
    }

    private BasicPokemonDTO getBasicInformationPokemon(String apiUrl) throws Exception {
        JsonNode jsonNode = objectResponseData(apiUrl);

        String id = jsonNode.get("id").asText();

        String name = jsonNode.get("name").asText();

        String url = String.format("%s/pokemon/%s/advanced", localBaseUrl, id);

        JsonNode typesNode = jsonNode.get("types");
        List<String> typeNames = new ArrayList<>();

        String spriteSmallNormal = getAttribute(jsonNode.get("sprites").get("front_default"));
        String spriteSmallAnimated = getAttribute(jsonNode
                .get("sprites")
                .get("versions")
                .get("generation-v")
                .get("black-white")
                .get("animated")
                .get("front_default"));

        if (typesNode.isArray()) {
            for (JsonNode typeNode: typesNode) {
                String typeName = typeNode.get("type").get("name").asText();

                typeNames.add(typeName);
            }
        }

        return new BasicPokemonDTO(id, name, url, typeNames, new SpriteSmallDTO(spriteSmallNormal, spriteSmallAnimated));
    }

    private String buildUrlPokemon(String url) throws Exception {
        JsonNode jsonNode = objectResponseData(url);

        return jsonNode.get("id").asText();
    }

    private List<PokemonEvolutionChain> buildEvolutionChain(String apiUrl) throws Exception{
        JsonNode jsonNode = objectResponseData(apiUrl);

        List<PokemonEvolutionChain> evolutions = new ArrayList<>();
        JsonNode evolutionsNode = jsonNode.get("chain");

        JsonNode intermediateEvolutionNode = evolutionsNode.get("evolves_to");

        String basicEvolutionName = evolutionsNode.get("species").get("name").asText();
        String basicEvolutionId = buildUrlPokemon(evolutionsNode.get("species").get("url").asText());
        String basicEvolutionUrl = String.format("%s/pokemon/%s/basic", localBaseUrl, basicEvolutionId);


        if (intermediateEvolutionNode.size() > 0) {
            for (JsonNode intermediateEvolution: intermediateEvolutionNode) {

                String intermediateEvolutionName = intermediateEvolution.get("species").get("name").asText();
                String intermediateEvolutionId = buildUrlPokemon(intermediateEvolution.get("species").get("url").asText());
                String intermediateEvolutionUrl = String.format("%s/pokemon/%s/basic", localBaseUrl, intermediateEvolutionId);

                JsonNode finalEvolutionNode = intermediateEvolution.get("evolves_to");

                if (finalEvolutionNode.size() > 0) {
                    for (JsonNode finalEvolution: finalEvolutionNode) {
                        String finalEvolutionName = finalEvolution.get("species").get("name").asText();
                        String finalEvolutionId= buildUrlPokemon(finalEvolution.get("species").get("url").asText());
                        String finalEvolutionUrl = String.format("%s/pokemon/%s/basic", localBaseUrl, finalEvolutionId);

                        var pokemonEvolutionChain = new PokemonEvolutionChain(
                                new PokemonEvolutionItem(basicEvolutionName, basicEvolutionUrl),
                                new PokemonEvolutionItem(intermediateEvolutionName, intermediateEvolutionUrl),
                                new PokemonEvolutionItem(finalEvolutionName, finalEvolutionUrl));
                        evolutions.add(pokemonEvolutionChain);
                    }
                } else {
                    var pokemonEvolutionChain = new PokemonEvolutionChain(
                            new PokemonEvolutionItem(basicEvolutionName, basicEvolutionUrl),
                            new PokemonEvolutionItem(intermediateEvolutionName, intermediateEvolutionUrl));
                    evolutions.add(pokemonEvolutionChain);
                }
            }
        } else {
            var pokemonEvolutionChain = new PokemonEvolutionChain(new PokemonEvolutionItem(basicEvolutionName, basicEvolutionUrl));
            evolutions.add(pokemonEvolutionChain);
        }

        return evolutions;
    }

    private List<PokemonEvolutionChain> getEvolutionChain(String apiUrl, String name) throws Exception {
        var buildEvolutionChain = buildEvolutionChain(apiUrl);

        List<PokemonEvolutionChain> evolutions = new ArrayList<>();

        for (PokemonEvolutionChain pokemonEvolutionChain: buildEvolutionChain) {
            if (Objects.equals(pokemonEvolutionChain.getBasicEvolution().name(), name)) {
                evolutions = List.copyOf(buildEvolutionChain);
            } else if (Objects.equals(pokemonEvolutionChain.getIntermediateEvolution().name(), name)) {
                evolutions.add(pokemonEvolutionChain);
            } else if (Objects.equals(pokemonEvolutionChain.getFinalEvolution().name(), name)) {
                evolutions = List.of(pokemonEvolutionChain);
            }
        }

        return evolutions;
    }

    @GetMapping(value = "/list")
    public ResponseEntity<PokemonListDTO> getPokemonList(@RequestParam(defaultValue = "12") String limit, @RequestParam(defaultValue = "0") String step) {
        String apiUrl = String.format("%s/pokemon?offset=%s&limit=%s", baseURL, step, limit);

        try {
            JsonNode jsonNode = objectResponseData(apiUrl);

            JsonNode items = jsonNode.get("results");
            List<BasicPokemonDTO> list = new ArrayList<>();

            int totalPokemon = jsonNode.get("count").asInt();

            int currentStep = Integer.parseInt(step);
            int currentLimit = Integer.parseInt(limit);

            int previousStep = currentStep - currentLimit;
            int nextStep = currentStep + currentLimit;


            String previous = previousStep >= 0 ?
                    String.format("%s/pokemon/list?limit=%s&step=%s", localBaseUrl, limit, previousStep) : null;
            String next = nextStep <= totalPokemon ?
                    String.format("%s/pokemon/list?limit=%s&step=%s", localBaseUrl, limit, nextStep) : null;

            for (JsonNode itemNode: items) {
                var pokemonBasic = getBasicInformationPokemon(itemNode.get("url").asText());

                list.add(pokemonBasic);
            }

            return ResponseEntity.ok().body(new PokemonListDTO(previous, next, list));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping(value = "/{id}/basic")
    public ResponseEntity<BasicPokemonDTO> getBasicPokemon(@PathVariable String id) {
        String apiUrl = String.format("%s/pokemon/%s", baseURL , id);

        try {
            var basicPokemon = getBasicInformationPokemon(apiUrl);

            return ResponseEntity
                    .ok()
                    .body(basicPokemon);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping(value = "/{id}/advanced")
    public ResponseEntity<AdvancedPokemonDTO> getAdvancedPokemon(@PathVariable String id) {
        String apiUrl = String.format("%s/pokemon/%s", baseURL, id);

        try {
            JsonNode jsonNode = objectResponseData(apiUrl);

            var basicPokemon = getBasicInformationPokemon(apiUrl);

            String spriteLargeNormal = getAttribute(jsonNode
                    .get("sprites")
                    .get("other")
                    .get("dream_world")
                    .get("front_default"));
            String spriteLargeAlternative = getAttribute(jsonNode
                    .get("sprites")
                    .get("other")
                    .get("official-artwork")
                    .get("front_default"));

            JsonNode abilitiesNode = jsonNode.get("abilities");
            List<String> abilitiesNames = new ArrayList<>();

            if (abilitiesNode.isArray()) {
                for (JsonNode node: abilitiesNode) {
                    String abilityName = node.get("ability").get("name").asText();

                    abilitiesNames.add(abilityName);
                }
            }

            Integer healthPoints = jsonNode.get("stats").get(0).get("base_stat").asInt();
            Integer attack = jsonNode.get("stats").get(1).get("base_stat").asInt();
            Integer defense = jsonNode.get("stats").get(2).get("base_stat").asInt();
            Integer specialAttack = jsonNode.get("stats").get(3).get("base_stat").asInt();
            Integer specialDefense = jsonNode.get("stats").get(4).get("base_stat").asInt();
            Integer speed = jsonNode.get("stats").get(5).get("base_stat").asInt();

            Double height = jsonNode.get("height").asDouble() / 10;
            Double weight = jsonNode.get("weight").asDouble() / 10;

            Integer baseExperience = jsonNode.get("base_experience") != null ?
                    jsonNode.get("base_experience").asInt() : null;

            String species = getAttribute(jsonNode.get("species").get("url"));

            jsonNode = objectResponseData(species);

            String description = "";

            for (JsonNode flavors: jsonNode.get("flavor_text_entries")) {
                if (Objects.equals(flavors.get("language").get("name").asText(), "en")
                        && Objects.equals(flavors.get("version").get("name").asText(), "ruby")) {
                    description = flavors.get("flavor_text").asText();
                }
            }

            String color = getAttribute(jsonNode.get("color").get("name"));

            String shape = getAttribute(jsonNode.get("shape").get("name"));

            String habitat = getAttribute(jsonNode.get("habitat").get("name"));

            String evolutionChainURL = getAttribute(jsonNode.get("evolution_chain").get("url"));

            List<PokemonEvolutionChain> evolutions = getEvolutionChain(evolutionChainURL, basicPokemon.name());

            var advancedPokemon = new AdvancedPokemonDTO(
                    id,
                    basicPokemon.name(),
                    basicPokemon.types(),
                    new SpriteLargeDTO(spriteLargeNormal, spriteLargeAlternative),
                    basicPokemon.sprite(),
                    abilitiesNames,
                    new PokemonStatsDTO(
                            healthPoints,
                            attack,
                            defense,
                            specialAttack,
                            specialDefense,
                            speed
                    ),
                    height,
                    weight,
                    baseExperience,
                    description,
                    color,
                    shape,
                    habitat,
                    evolutions
            );

            return ResponseEntity.ok().body(advancedPokemon);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping(value = "/search/{name}")
    public ResponseEntity<List<BasicPokemonDTO>> getPokemonByName(@PathVariable String name) {
        String apiUrl = String.format("%s/pokemon?offset=%s&limit=%s", baseURL, 0, 833);

        try {
            JsonNode jsonNode = objectResponseData(apiUrl);

            JsonNode pokemonsNode = jsonNode.get("results");

            List<BasicPokemonDTO> matchPokemons = new ArrayList<>();

            for (JsonNode node: pokemonsNode) {
                Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(node.get("name").asText());

                if (matcher.find()) {
                    var pokemonBasic = getBasicInformationPokemon(node.get("url").asText());

                    matchPokemons.add(pokemonBasic);
                }
            }

            return ResponseEntity.ok().body(matchPokemons);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
