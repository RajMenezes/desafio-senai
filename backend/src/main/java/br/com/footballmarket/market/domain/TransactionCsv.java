package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.money.Money;
import br.com.footballmarket.shared.time.BrasiliaTime;

import java.util.List;

/**
 * Exportação do histórico (RN18): separador {@code ;}, vírgula decimal, datas no formato das respostas (RN01),
 * cada linha terminada em LF, inclusive a última.
 */
public final class TransactionCsv {

    static final String HEADER = "data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa";
    private static final char SEPARATOR = ';';
    private static final char QUOTE = '"';

    private TransactionCsv() {
    }

    /**
     * @param transactions já na ordem do histórico (mais recente primeiro)
     */
    public static String write(List<MarketTransaction> transactions) {
        StringBuilder csv = new StringBuilder(HEADER).append('\n');
        for (MarketTransaction transaction : transactions) {
            csv.append(BrasiliaTime.format(transaction.getCreatedAt())).append(SEPARATOR)
                    .append(transaction.getType()).append(SEPARATOR)
                    .append(escape(transaction.getPlayerName())).append(SEPARATOR)
                    .append(transaction.getValue().toBrazilianFormat()).append(SEPARATOR)
                    .append(transaction.getFee().toBrazilianFormat()).append(SEPARATOR)
                    .append(transaction.getPenalty().toBrazilianFormat()).append(SEPARATOR)
                    .append(format(transaction.getProfit())).append(SEPARATOR)
                    .append(transaction.getCashChange().toBrazilianFormat()).append('\n');
        }
        return csv.toString();
    }

    /**
     * Nome com {@code ;} ou aspas vai entre aspas, com cada aspa interna duplicada:
     * {@code Zé "Foguete" Lima} → {@code "Zé ""Foguete"" Lima"}.
     */
    static String escape(String text) {
        if (text.indexOf(SEPARATOR) < 0 && text.indexOf(QUOTE) < 0) {
            return text;
        }
        return QUOTE + text.replace("\"", "\"\"") + QUOTE;
    }

    /**
     * Lucro vazio quando nulo (compras).
     */
    private static String format(Money money) {
        return money == null ? "" : money.toBrazilianFormat();
    }
}
