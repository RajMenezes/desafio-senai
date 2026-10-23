package br.com.footballmarket.market.api;

import br.com.footballmarket.market.application.TransactionFilter;
import br.com.footballmarket.market.application.TransactionHistoryService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/clubs/{id}/transactions")
public class TransactionController {

    static final MediaType TEXT_CSV = MediaType.parseMediaType("text/csv");

    private final TransactionHistoryService history;

    public TransactionController(TransactionHistoryService history) {
        this.history = history;
    }

    @GetMapping
    public TransactionHistoryResponse list(@PathVariable Integer id,
                                           @RequestParam(required = false) String type,
                                           @RequestParam(name = "player_id", required = false) Integer playerId) {
        return TransactionHistoryResponse.from(history.history(id, filter(type, playerId)));
    }

    /**
     * RN18. Os bytes já vão em UTF-8 (sem BOM): com {@code String}, o Spring usaria ISO-8859-1 em
     * {@code text/csv}. Sem {@code produces}, para que um erro continue saindo em JSON.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@PathVariable Integer id,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(name = "player_id", required = false) Integer playerId) {
        String csv = history.exportCsv(id, filter(type, playerId));
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("transacoes-clube-" + id + ".csv").build().toString())
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    private static TransactionFilter filter(String type, Integer playerId) {
        return new TransactionFilter(TransactionTypeParam.parse(type), playerId);
    }
}
