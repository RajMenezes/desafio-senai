package br.com.footballmarket.shared.text;

import java.util.Arrays;

/**
 * Definições de texto do enunciado (V4): caractere é um ponto de código Unicode; espaço é U+0020;
 * letra é a categoria Unicode L; dígito vai de 0 a 9; palavra é uma sequência sem espaços com pelo menos uma letra.
 */
public final class TextRules {

    private TextRules() {
    }

    public static int length(String text) {
        return text.codePointCount(0, text.length());
    }

    public static long wordCount(String text) {
        return Arrays.stream(text.split(" "))
                .filter(token -> token.codePoints().anyMatch(Character::isLetter))
                .count();
    }

    public static boolean containsDigit(String text) {
        return text.chars().anyMatch(c -> c >= '0' && c <= '9');
    }
}
