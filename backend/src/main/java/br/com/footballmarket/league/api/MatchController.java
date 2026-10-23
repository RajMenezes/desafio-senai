package br.com.footballmarket.league.api;

import br.com.footballmarket.league.application.MatchService;
import br.com.footballmarket.league.application.PlayedMatch;
import br.com.footballmarket.shared.config.WebConfig;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/matches")
public class MatchController {

    private final MatchService matches;

    public MatchController(MatchService matches) {
        this.matches = matches;
    }

    @PostMapping
    public ResponseEntity<MatchResponse> play(@Valid @RequestBody PlayMatchRequest request) {
        request.requireDifferentClubs();
        PlayedMatch played = matches.play(request.homeClubId(), request.awayClubId());
        URI location = URI.create(WebConfig.API_BASE_PATH + "/matches/" + played.match().getId());
        return ResponseEntity.created(location).body(MatchResponse.withDuels(played));
    }

    @GetMapping
    public List<MatchResponse> history(@RequestParam(name = "club_id", required = false) Integer clubId) {
        return matches.history(clubId).stream().map(MatchResponse::summary).toList();
    }

    @GetMapping("/{id}")
    public MatchResponse findById(@PathVariable Integer id) {
        return MatchResponse.withDuels(matches.findById(id));
    }
}
