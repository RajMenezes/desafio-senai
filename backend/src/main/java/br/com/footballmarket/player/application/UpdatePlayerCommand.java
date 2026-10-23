package br.com.footballmarket.player.application;

import br.com.footballmarket.player.domain.Position;

public record UpdatePlayerCommand(String name, Position position, int agility, int age) {
}
