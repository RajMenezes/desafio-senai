package br.com.footballmarket.club.application;

/**
 * @param name nome já normalizado pela V4
 */
public record CreateClubCommand(String name, String acronym) {
}
