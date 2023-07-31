package com.joaomarcos.pokeapimiddleware.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joaomarcos.pokeapimiddleware.entities.DTO.BasicPokemonDTO;
import com.joaomarcos.pokeapimiddleware.entities.DTO.PokemonListDTO;
import com.joaomarcos.pokeapimiddleware.entities.DTO.attributes.SpriteSmallDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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
}
