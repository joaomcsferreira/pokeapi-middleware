package com.joaomarcos.pokeapimiddleware.entities;

import com.joaomarcos.pokeapimiddleware.entities.DTO.attributes.PokemonEvolutionItem;

public class PokemonEvolutionChain {
    private PokemonEvolutionItem basicEvolution;
    private PokemonEvolutionItem intermediateEvolution;
    private PokemonEvolutionItem finalEvolution;

    public PokemonEvolutionChain() {}

    public PokemonEvolutionChain(PokemonEvolutionItem basicEvolution) {
        this.basicEvolution = basicEvolution;
    }

    public PokemonEvolutionChain(PokemonEvolutionItem basicEvolution, PokemonEvolutionItem intermediateEvolution) {
        this.basicEvolution = basicEvolution;
        this.intermediateEvolution = intermediateEvolution;
    }

    public PokemonEvolutionChain(PokemonEvolutionItem basicEvolution, PokemonEvolutionItem intermediateEvolution, PokemonEvolutionItem finalEvolution) {
        this.basicEvolution = basicEvolution;
        this.intermediateEvolution = intermediateEvolution;
        this.finalEvolution = finalEvolution;
    }

    public PokemonEvolutionItem getBasicEvolution() {
        return basicEvolution;
    }

    public void setBasicEvolution(PokemonEvolutionItem basicEvolution) {
        this.basicEvolution = basicEvolution;
    }

    public PokemonEvolutionItem getIntermediateEvolution() {
        return intermediateEvolution;
    }

    public void setIntermediateEvolution(PokemonEvolutionItem intermediateEvolution) {
        this.intermediateEvolution = intermediateEvolution;
    }

    public PokemonEvolutionItem getFinalEvolution() {
        return finalEvolution;
    }

    public void setFinalEvolution(PokemonEvolutionItem finalEvolution) {
        this.finalEvolution = finalEvolution;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s%n",
                basicEvolution.name(), basicEvolution.url(),
                intermediateEvolution.name(), intermediateEvolution.url(),
                finalEvolution.name(), finalEvolution.url());
    }
}
