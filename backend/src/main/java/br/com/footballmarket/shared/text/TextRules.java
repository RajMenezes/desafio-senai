package br.com.footballmarket.shared.text;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Definições de texto do enunciado (V4): caractere é um ponto de código Unicode; espaço é U+0020;
 * letra é a categoria Unicode L; dígito vai de 0 a 9; palavra é uma sequência sem espaços com pelo menos uma letra.
 */
public final class TextRules {

    /**
     * Ordem alfabética das Premissas: minúsculas, sem acentos e comparação caractere a caractere pela
     * ordem Unicode. O espaço vem antes do hífen, e o hífen antes das letras ("Ana Lima" antes de "Ana-Maria").
     * Nomes iguais nessa comparação ("Águias FC" e "Aguias FC") empatam; quem usa desempata pelo id.
     */
    public static final Comparator<String> ALPHABETICAL =
            Comparator.comparing(TextNormalizer::fold, TextRules::compareCodePoints);

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

    /**
     * Compara por ponto de código, e não por unidade UTF-16 como {@link String#compareTo}
     * (as duas diferem para caracteres fora do BMP).
     */
    static int compareCodePoints(String a, String b) {
        int i = 0;
        int j = 0;
        while (i < a.length() && j < b.length()) {
            int ca = a.codePointAt(i);
            int cb = b.codePointAt(j);
            if (ca != cb) {
                return Integer.compare(ca, cb);
            }
            i += Character.charCount(ca);
            j += Character.charCount(cb);
        }
        // o que acabou primeiro (prefixo) vem antes
        return Integer.compare(a.length() - i, b.length() - j);
    }
}
