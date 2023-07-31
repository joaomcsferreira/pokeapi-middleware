package com.joaomarcos.pokeapimiddleware.entities.DTO.attributes;

public record PokemonStatsDTO(
        Integer healthPoints,
        Integer attack,
        Integer defense,
        Integer specialAttack,
        Integer specialDefense,
        Integer speed
) {
}
