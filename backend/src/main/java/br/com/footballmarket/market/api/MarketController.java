package br.com.footballmarket.market.api;

import br.com.footballmarket.market.application.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compra e venda de jogadores. A venda entra na Etapa 6.
 */
@RestController
public class MarketController {

    private final PurchaseService purchases;

    public MarketController(PurchaseService purchases) {
        this.purchases = purchases;
    }

    @PostMapping("/players/{id}/buy")
    public PurchaseResponse buy(@PathVariable Integer id, @Valid @RequestBody BuyPlayerRequest request) {
        return PurchaseResponse.from(purchases.buy(request.toCommand(id)));
    }
}
