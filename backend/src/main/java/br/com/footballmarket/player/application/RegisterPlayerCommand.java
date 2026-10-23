package br.com.footballmarket.player.application;

import br.com.footballmarket.player.domain.Attributes;
import br.com.footballmarket.player.domain.Position;

public record RegisterPlayerCommand(String name, Position position, int agility, int age,
                                    Attributes attributes, int potential) {
}
