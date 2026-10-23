package br.com.footballmarket.league.api;

import br.com.footballmarket.league.application.StandingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/standings")
public class StandingsController {

    private final StandingsService standings;

    public StandingsController(StandingsService standings) {
        this.standings = standings;
    }

    @GetMapping
    public List<StandingResponse> standings() {
        return standings.standings().stream().map(StandingResponse::from).toList();
    }
}
