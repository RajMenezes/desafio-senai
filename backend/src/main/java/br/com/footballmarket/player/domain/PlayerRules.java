package br.com.footballmarket.player.domain;

import br.com.footballmarket.shared.error.BadRequestException;
import br.com.footballmarket.shared.text.TextRules;


 // Limites do jogador que dependem de mais de um campo ou de regras de texto ("Limites de cada campo").
 // Os limites simples (ex.: agilidade de 1 a 100) ficam no Bean Validation dos DTOs de entrada.

final class PlayerRules {

    static final int NAME_MIN_LENGTH = 3;
    static final int NAME_MAX_LENGTH = 100;
    static final int VETERAN_AGE = 35;
    static final int VETERAN_MAX_AGILITY = 85;
    static final int FIXED_POTENTIAL_AGE = 33;

    private PlayerRules() {
    }

    static void requireValidName(String name) {
        int length = TextRules.length(name);
        if (length < NAME_MIN_LENGTH || length > NAME_MAX_LENGTH) {
            throw new BadRequestException("O campo 'name' deve ter entre 3 e 100 caracteres.");
        }
        if (TextRules.containsDigit(name)) {
            throw new BadRequestException("O campo 'name' não pode conter dígitos.");
        }
        if (TextRules.wordCount(name) < 2) {
            throw new BadRequestException("O campo 'name' deve ter pelo menos duas palavras.");
        }
    }

    static void requireAgilityAllowedForAge(int agility, int age) {
        if (age >= VETERAN_AGE && agility > VETERAN_MAX_AGILITY) {
            throw new BadRequestException("Com 35 anos ou mais, o campo 'agility' deve ser no máximo 85.");
        }
    }

    static void requireValidAttributes(Attributes attributes, int potential, int age) {
        if (attributes.sum() > Attributes.MAX_SUM) {
            throw new BadRequestException("A soma dos seis atributos não pode passar de 480.");
        }
        int highest = attributes.highest();
        if (potential < highest) {
            throw new BadRequestException("O campo 'potential' deve ser maior ou igual ao maior dos seis atributos.");
        }
        if (age >= FIXED_POTENTIAL_AGE && potential != highest) {
            throw new BadRequestException(
                    "Com 33 anos ou mais, o campo 'potential' deve ser igual ao maior dos seis atributos.");
        }
    }
}
