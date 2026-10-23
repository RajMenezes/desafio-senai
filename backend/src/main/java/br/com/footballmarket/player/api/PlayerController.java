package br.com.footballmarket.player.api;

import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.shared.config.WebConfig;
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
import org.springframework.web.bind.annotation.RestController;

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
