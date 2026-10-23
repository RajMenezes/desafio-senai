package br.com.footballmarket.club.api;

import br.com.footballmarket.club.application.LineupService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clubs/{id}/lineup")
public class LineupController {

    private final LineupService lineups;

    public LineupController(LineupService lineups) {
        this.lineups = lineups;
    }

    @PutMapping
    public LineupResponse replace(@PathVariable Integer id, @Valid @RequestBody SetLineupRequest request) {
        return LineupResponse.from(lineups.replace(id, request.toCommand()));
    }

    @GetMapping
    public LineupResponse find(@PathVariable Integer id) {
        return LineupResponse.from(lineups.find(id));
    }
}
