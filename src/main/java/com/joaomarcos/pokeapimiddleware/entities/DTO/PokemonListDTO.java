package com.joaomarcos.pokeapimiddleware.entities.DTO;

import java.util.List;

public record PokemonListDTO(String previous, String next, List<BasicPokemonDTO> list) {
}
