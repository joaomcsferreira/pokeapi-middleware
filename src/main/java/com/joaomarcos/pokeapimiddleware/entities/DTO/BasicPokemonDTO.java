package com.joaomarcos.pokeapimiddleware.entities.DTO;

import com.joaomarcos.pokeapimiddleware.entities.DTO.attributes.SpriteSmallDTO;

import java.util.List;

public record BasicPokemonDTO(String id, String name, String url, List<String> types, SpriteSmallDTO sprite) {
}
