package br.com.footballmarket.league.domain;

import br.com.footballmarket.player.domain.Attributes;
import br.com.footballmarket.player.domain.Position;

import java.util.Arrays;
import java.util.function.ToIntFunction;

/**
 * Atributo usado num duelo (RN20, passo 5): o atacante usa o da posição dele; o defensor, sempre a defesa.
 */
public enum DuelAttribute {

    SHOOTING("shooting", Attributes::shooting),
    PASSING("passing", Attributes::passing),
    DRIBBLING("dribbling", Attributes::dribbling),
    DEFENDING("defending", Attributes::defending);

    private final String label;
    private final ToIntFunction<Attributes> reader;

    DuelAttribute(String label, ToIntFunction<Attributes> reader) {
        this.label = label;
        this.reader = reader;
    }

    /**
     * ATA usa finalização, MEI usa passe, e ZAG e LAT usam drible.
     */
    public static DuelAttribute attackOf(Position position) {
        return switch (position) {
            case ATA -> SHOOTING;
            case MEI -> PASSING;
            case ZAG, LAT -> DRIBBLING;
            case GOL -> throw new IllegalArgumentException("O goleiro não ataca");
        };
    }

    /**
     * Nome como na API e no banco ({@code "shooting"}).
     */
    public String label() {
        return label;
    }

    public static DuelAttribute fromLabel(String label) {
        return Arrays.stream(values()).filter(attribute -> attribute.label.equals(label)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Atributo desconhecido: " + label));
    }

    int read(Attributes attributes) {
        return reader.applyAsInt(attributes);
    }
}
