package br.com.footballmarket.shared.text;

import br.com.footballmarket.shared.error.BadRequestException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalização de textos livres (nomes e busca), aplicada antes da validação de tamanho e da gravação:
 * remove espaços das pontas, colapsa espaços repetidos e recusa caracteres de controle.
 */
public final class
TextNormalizer {

    private static final Pattern REPEATED_SPACES = Pattern.compile("\\s{2,}");
    private static final Pattern NONSPACING_MARKS = Pattern.compile("\\p{Mn}+");

    private TextNormalizer() {
    }

    /**
     * @return o texto normalizado, ou {@code null} se ficar vazio (texto vazio conta como ausente)
     * @throws BadRequestException se houver caracteres de controle (tabulação, quebra de linha...)
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        if (raw.chars().anyMatch(Character::isISOControl)) {
            throw new BadRequestException("O texto não pode conter caracteres de controle.");
        }
        String normalized = REPEATED_SPACES.matcher(raw.strip()).replaceAll(" ");
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Forma usada para comparar textos sem diferenciar maiúsculas, minúsculas e acentos (V4 e RN07):
     * minúsculas, depois decomposição NFD sem as marcas de acento (categoria Mn, que inclui a cedilha).
     * {@code "João"} e {@code "JOAO"} viram {@code "joao"}.
     */
    public static String fold(String text) {
        String decomposed = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return NONSPACING_MARKS.matcher(decomposed).replaceAll("");
    }
}
