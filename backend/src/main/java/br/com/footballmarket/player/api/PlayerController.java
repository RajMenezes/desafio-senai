package br.com.footballmarket.player.api;

import br.com.footballmarket.player.application.PlayerQuery;
import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.Position;
import br.com.footballmarket.shared.config.WebConfig;
import br.com.footballmarket.shared.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> register(@Valid @RequestBody RegisterPlayerRequest request) {
        Player player = service.register(request.toCommand());
        URI location = URI.create(WebConfig.API_BASE_PATH + "/players/" + player.getId());
        return ResponseEntity.created(location).body(PlayerResponse.from(player));
    }

    /**
     * Sem {@code defaultValue}: com ele, o Spring trocaria um {@code page=} vazio pelo padrão, e o enunciado
     * pede 400. Os padrões são aplicados em {@link PlayerListRequest#toQuery()}.
     */
    @GetMapping
    public PageResponse<PlayerResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) Condition condition,
            @RequestParam(required = false) Boolean free,
            @RequestParam(name = "club_id", required = false) Integer clubId,
            @RequestParam(name = "min_age", required = false) Integer minAge,
            @RequestParam(name = "max_age", required = false) Integer maxAge,
            @RequestParam(name = "min_value", required = false) BigDecimal minValue,
            @RequestParam(name = "max_value", required = false) BigDecimal maxValue,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit) {
        PlayerQuery query = new PlayerListRequest(search, position, condition, free, clubId,
                minAge, maxAge, minValue, maxValue, sort, page, limit).toQuery();
        return PageResponse.from(service.list(query).map(PlayerResponse::from));
    }

    @GetMapping("/{id}")
    public PlayerResponse findById(@PathVariable Integer id) {
        return PlayerResponse.from(service.findById(id));
    }

    @PutMapping("/{id}")
    public PlayerResponse update(@PathVariable Integer id, @Valid @RequestBody UpdatePlayerRequest request) {
        return PlayerResponse.from(service.update(id, request.toCommand()));
    }

    @PatchMapping("/{id}/condition")
    public PlayerResponse changeCondition(@PathVariable Integer id, @Valid @RequestBody ChangeConditionRequest request) {
        return PlayerResponse.from(service.changeCondition(id, request.condition()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
