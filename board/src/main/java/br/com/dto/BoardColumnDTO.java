package br.com.dto;

public record BoardColumnDTO(
        Long id,
        String name,
        String kind,
        int cardsAmount) {
}
