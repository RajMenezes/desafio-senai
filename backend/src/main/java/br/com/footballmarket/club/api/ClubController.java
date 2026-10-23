package br.com.footballmarket.club.api;

import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.club.domain.ClubSort;
import br.com.footballmarket.club.domain.ValuedClub;
import br.com.footballmarket.shared.config.WebConfig;
import br.com.footballmarket.shared.error.BadRequestException;
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
@RequestMapping("/clubs")
public class ClubController {

    private final ClubService service;

    public ClubController(ClubService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClubResponse> create(@Valid @RequestBody CreateClubRequest request) {
        ValuedClub club = service.create(request.toCommand());
        URI location = URI.create(WebConfig.API_BASE_PATH + "/clubs/" + club.club().getId());
        return ResponseEntity.created(location).body(ClubResponse.from(club));
    }

    @GetMapping
    public List<ClubResponse> list(@RequestParam(required = false) String sort) {
        return service.list(parseSort(sort)).stream().map(ClubResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ClubDetailResponse findById(@PathVariable Integer id) {
        return ClubDetailResponse.from(service.findById(id));
    }

    /**
     * Um só valor, exato: {@code name}, {@code net_worth_asc} ou {@code net_worth_desc}; vazio é recusado.
     */
    private static ClubSort parseSort(String sort) {
        if (sort == null) {
            return ClubSort.DEFAULT;
        }
        return ClubSort.fromParam(sort).orElseThrow(() -> new BadRequestException(
                "Valor inválido para o parâmetro 'sort'. Use name, net_worth_asc ou net_worth_desc."));
    }
}
