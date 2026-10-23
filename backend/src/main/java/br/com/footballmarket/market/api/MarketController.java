package br.com.footballmarket.market.api;

import br.com.footballmarket.market.application.PurchaseService;
import br.com.footballmarket.market.application.SaleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compra e venda de jogadores.
 */
@RestController
public class MarketController {

    private final PurchaseService purchases;
    private final SaleService sales;

    public MarketController(PurchaseService purchases, SaleService sales) {
        this.purchases = purchases;
        this.sales = sales;
    }

    @PostMapping("/players/{id}/buy")
    public PurchaseResponse buy(@PathVariable Integer id, @Valid @RequestBody BuyPlayerRequest request) {
        return PurchaseResponse.from(purchases.buy(request.toCommand(id)));
    }

    /**
     * Sem corpo: a venda é sempre feita pelo clube atual do jogador, e um corpo enviado é ignorado (V1).
     */
    @PostMapping("/players/{id}/sell")
    public SaleResponse sell(@PathVariable Integer id) {
        return SaleResponse.from(sales.sell(id));
    }
}
