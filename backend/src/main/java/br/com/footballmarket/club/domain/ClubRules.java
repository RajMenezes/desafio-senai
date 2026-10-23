package br.com.footballmarket.club.domain;

import br.com.footballmarket.shared.error.BadRequestException;
import br.com.footballmarket.shared.text.TextNormalizer;
import br.com.footballmarket.shared.text.TextRules;

import java.util.PrimitiveIterator;
import java.util.regex.Pattern;

/**
 * Formato do nome e da sigla do clube ("Limites de cada campo"). O nome já chega normalizado pela V4.
 */
final class ClubRules {

    static final int NAME_MIN_LENGTH = 3;
    static final int NAME_MAX_LENGTH = 60;
    static final int NAME_MIN_WORDS = 2;
    static final int NAME_MAX_WORDS = 5;
    private static final Pattern ACRONYM = Pattern.compile("[A-Z]{3}");

    private ClubRules() {
    }

    static void requireValidName(String name) {
        int length = TextRules.length(name);
        if (length < NAME_MIN_LENGTH || length > NAME_MAX_LENGTH) {
            throw new BadRequestException("O campo 'name' deve ter entre 3 e 60 caracteres.");
        }
        if (!name.codePoints().allMatch(ClubRules::isAllowedNameCharacter)) {
            throw new BadRequestException("O campo 'name' aceita só letras, espaços, hífen e ponto.");
        }
        long words = TextRules.wordCount(name);
        if (words < NAME_MIN_WORDS || words > NAME_MAX_WORDS) {
            throw new BadRequestException("O campo 'name' deve ter de 2 a 5 palavras.");
        }
    }

    /**
     * Três letras de A a Z, maiúsculas, que aparecem no nome nessa ordem (sem diferenciar maiúsculas e acentos),
     * a primeira sendo a primeira letra do nome, e não todas iguais. Ex.: "Águias FC" aceita AGU, AFC e ASC.
     */
    static void requireValidAcronym(String acronym, String name) {
        if (!ACRONYM.matcher(acronym).matches()) {
            throw new BadRequestException("O campo 'acronym' deve ter exatamente 3 letras maiúsculas de A a Z.");
        }
        if (acronym.chars().distinct().count() == 1) {
            throw new BadRequestException("As três letras do campo 'acronym' não podem ser todas iguais.");
        }
        if (!matchesName(TextNormalizer.fold(acronym), TextNormalizer.fold(name))) {
            throw new BadRequestException("O campo 'acronym' deve começar pela primeira letra do nome "
                    + "e usar letras que aparecem no nome nessa ordem.");
        }
    }

    private static boolean matchesName(String acronym, String foldedName) {
        PrimitiveIterator.OfInt letters = foldedName.codePoints().filter(Character::isLetter).iterator();
        if (!letters.hasNext() || letters.nextInt() != acronym.charAt(0)) {
            return false;
        }
        int matched = 1;
        while (matched < acronym.length() && letters.hasNext()) {
            if (letters.nextInt() == acronym.charAt(matched)) {
                matched++;
            }
        }
        return matched == acronym.length();
    }

    private static boolean isAllowedNameCharacter(int codePoint) {
        return Character.isLetter(codePoint) || codePoint == ' ' || codePoint == '-' || codePoint == '.';
    }
}
