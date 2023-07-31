package com.joaomarcos.pokeapimiddleware.entities.DTO;

import com.joaomarcos.pokeapimiddleware.entities.DTO.attributes.PokemonStatsDTO;
import com.joaomarcos.pokeapimiddleware.entities.DTO.attributes.SpriteLargeDTO;
import com.joaomarcos.pokeapimiddleware.entities.DTO.attributes.SpriteSmallDTO;
import com.joaomarcos.pokeapimiddleware.entities.PokemonEvolutionChain;

import java.util.List;

public record AdvancedPokemonDTO(
        String id,
        String name,
        List<String> types,
        SpriteLargeDTO spriteLarge,
        SpriteSmallDTO spriteSmall,
        List<String> abilities,
        PokemonStatsDTO stats,
        Double height,
        Double weight,
        Integer baseExperience,
        String description,
        String color,
        String shape,
        String habitat,
        List<PokemonEvolutionChain> evolutions
) {
}
