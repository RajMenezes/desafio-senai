package br.com.footballmarket.market.api;

import br.com.footballmarket.market.application.TransactionFilter;
import br.com.footballmarket.market.domain.TransactionType;
import br.com.footballmarket.shared.error.BadRequestException;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Query param {@code type}: um ou mais de {@code COMPRA} e {@code VENDA}, separados por vírgula,
 * sem espaços, sem repetição e com as maiúsculas exatas. Vazio é recusado.
 */
final class TransactionTypeParam {

    private TransactionTypeParam() {
    }

    static Set<TransactionType> parse(String raw) {
        if (raw == null) {
            return TransactionFilter.ALL_TYPES;
        }
        Set<TransactionType> types = EnumSet.noneOf(TransactionType.class);
        for (String token : raw.split(",", -1)) {
            TransactionType type = Arrays.stream(TransactionType.values())
                    .filter(candidate -> candidate.name().equals(token))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(
                            "Valor inválido para o parâmetro 'type': '" + token + "'. Use COMPRA, VENDA ou COMPRA,VENDA."));
            if (!types.add(type)) {
                throw new BadRequestException("O parâmetro 'type' não pode repetir o tipo '" + type + "'.");
            }
        }
        return types;
    }
}
