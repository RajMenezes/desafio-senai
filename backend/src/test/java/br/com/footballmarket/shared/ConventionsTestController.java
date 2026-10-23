package br.com.footballmarket.shared;

import br.com.footballmarket.shared.error.BusinessRuleException;
import br.com.footballmarket.shared.error.NotFoundException;
import br.com.footballmarket.shared.json.NormalizedText;
import br.com.footballmarket.shared.money.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller usado só nos testes para exercitar as convenções transversais da API.
 */
@RestController
class ConventionsTestController {

    enum Kind { GOL, ATA }

    record SampleRequest(
            @NotNull @Size(max = 10) @NormalizedText String playerName,
            @NotNull Integer agility,
            Long clubId,
            Kind kind,
            Boolean free,
            String formation,
            OffsetDateTime loyaltyUntil,
            List<Integer> playerIds) {
    }

    record SampleResponse(String playerName, Integer agility, Long clubId, Kind kind, Boolean free,
                          String formation, OffsetDateTime loyaltyUntil, List<Integer> playerIds, Money cash) {
    }

    @PostMapping("/test/samples")
    SampleResponse create(@Valid @RequestBody SampleRequest request) {
        return new SampleResponse(request.playerName(), request.agility(), request.clubId(), request.kind(),
                request.free(), request.formation(), request.loyaltyUntil(), request.playerIds(), Money.of("300"));
    }

    @GetMapping("/test/samples/{id}")
    Map<String, Object> get(@PathVariable Integer id) {
        if (id == 999) {
            throw new NotFoundException("Jogador não encontrado.");
        }
        return Map.of("id", id);
    }

    @GetMapping("/test/samples")
    Map<String, Object> list(@RequestParam(required = false) Integer page,
                             @RequestParam(required = false) Boolean free,
                             @RequestParam(required = false) Kind kind,
                             @RequestParam(name = "min_value", required = false) BigDecimal minValue) {
        return Map.of(
                "page", String.valueOf(page),
                "free", String.valueOf(free),
                "kind", String.valueOf(kind),
                "min_value", String.valueOf(minValue));
    }

    @PostMapping("/test/conflict")
    void conflict() {
        throw new BusinessRuleException("O custo da compra (50,40) passa do limite de 50% do caixa (50,00).");
    }

    @GetMapping("/test/boom")
    void boom() {
        throw new IllegalStateException("detalhe interno que não pode vazar");
    }
}
