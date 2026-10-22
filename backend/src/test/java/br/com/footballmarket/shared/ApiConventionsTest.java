package br.com.footballmarket.shared;

import br.com.footballmarket.shared.config.JacksonConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ConventionsTestController.class)
@Import(JacksonConfig.class)
class ApiConventionsTest {

    private static final String SAMPLES = "/api/v1/test/samples";

    @Autowired
    private MockMvc mvc;

    private ResultActions postJson(String body) throws Exception {
        return mvc.perform(post(SAMPLES).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static void expectError(ResultActions result, int status) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Nested
    class RequestBody {

        @Test
        void usesSnakeCaseAndIgnoresUnknownOrCalculatedFields() throws Exception {
            postJson("""
                    {"player_name": "Ana", "agility": 50, "club_id": 3, "overall": 99,
                     "id": 7, "playerName": "ignorado", "cash": 1000}
                    """)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.player_name").value("Ana"))
                    .andExpect(jsonPath("$.club_id").value(3))
                    .andExpect(jsonPath("$.cash").value(300.00))
                    .andExpect(content().string(containsString("\"cash\":300.00")));
        }

        @Test
        void acceptsIntegerWrittenWithZeroFraction() throws Exception {
            postJson("""
                    {"player_name": "Ana", "agility": 50.0, "club_id": 3.00}
                    """)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.agility").value(50))
                    .andExpect(jsonPath("$.club_id").value(3));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "{\"player_name\": \"Ana\", \"agility\": \"50\"}",     // texto não vira número
                "{\"player_name\": \"Ana\", \"agility\": 50.5}",       // inteiro não aceita fração
                "{\"player_name\": \"Ana\", \"agility\": true}",
                "{\"player_name\": \"Ana\", \"agility\": 2147483648}", // fora do int
                "{\"player_name\": 123, \"agility\": 50}",             // número não vira texto
                "{\"player_name\": \"Ana\", \"agility\": 50, \"formation\": 31}",
                "{\"player_name\": \"Ana\", \"agility\": 50, \"free\": \"true\"}",
                "{\"player_name\": \"Ana\", \"agility\": 50, \"free\": 1}",
                "{\"player_name\": \"Ana\", \"agility\": 50, \"kind\": \"ata\"}", // listas são case-sensitive
                "{\"player_name\": \"Ana\", \"agility\": 50, \"kind\": 0}",
                "{\"player_name\": \"Ana\", \"agility\": 50, \"kind\": \"\"}",
                "{\"player_name\": \"Ana\", \"agility\": 50, \"player_ids\": [1, \"2\"]}",
                "{\"player_name\": \"Ana\", \"agility\": 50, \"player_ids\": [1, 2.5]}"
        })
        void rejectsWrongTypes(String body) throws Exception {
            expectError(postJson(body), 400);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "{\"agility\": 50}",                              // ausente
                "{\"player_name\": null, \"agility\": 50}",       // null
                "{\"player_name\": \"   \", \"agility\": 50}",    // vazio após normalizar conta como ausente
                "{\"player_name\": \"Ana\"}",
                "{\"player_name\": \"Ana\", \"agility\": null}"
        })
        void rejectsMissingRequiredFields(String body) throws Exception {
            expectError(postJson(body), 400);
        }

        @ParameterizedTest
        @ValueSource(strings = {"[]", "[{\"player_name\": \"Ana\"}]", "\"texto\"", "42", "null", "{", "{\"a\":1} {}", ""})
        void rejectsBodiesThatAreNotJsonObjects(String body) throws Exception {
            expectError(postJson(body), 400);
        }

        @Test
        void rejectsNonJsonContentType() throws Exception {
            expectError(mvc.perform(post(SAMPLES).contentType(MediaType.TEXT_PLAIN).content("Ana")), 400);
        }

        @Test
        void normalizesFreeText() throws Exception {
            postJson("""
                    {"player_name": "  Meia   Zeta ", "agility": 50}
                    """)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.player_name").value("Meia Zeta"));
        }

        @Test
        void validatesSizeAfterNormalization() throws Exception {
            // 14 caracteres brutos, 9 após normalizar (limite de teste: 10)
            postJson("""
                    {"player_name": "  Meia   Zeta ", "agility": 50}
                    """).andExpect(status().isOk());
            expectError(postJson("""
                    {"player_name": "Meia Zeta Alfa", "agility": 50}
                    """), 400);
        }

        @Test
        void rejectsControlCharactersInText() throws Exception {
            expectError(postJson("""
                    {"player_name": "Meia\\tZeta", "agility": 50}
                    """), 400);
        }
    }

    @Nested
    class Dates {

        @Test
        void acceptsBrasiliaInputAndWritesWithSeconds() throws Exception {
            postJson("""
                    {"player_name": "Ana", "agility": 50, "loyalty_until": "2027-03-02T10:15-03:00"}
                    """)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.loyalty_until").value("2027-03-02T10:15:00-03:00"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "\"2027-02-30T10:15-03:00\"",
                "\"2027-03-02T24:00-03:00\"",
                "\"2027-03-02T10:15\"",
                "\"2027-03-02T10:15:00-03:00\"",
                "\"2027-03-02T13:15Z\"",
                "1803993300"
        })
        void rejectsInvalidDates(String date) throws Exception {
            expectError(postJson("{\"player_name\": \"Ana\", \"agility\": 50, \"loyalty_until\": " + date + "}"), 400);
        }
    }

    @Nested
    class PathIds {

        @Test
        void acceptsPositiveInteger() throws Exception {
            mvc.perform(get(SAMPLES + "/2147483647"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2147483647));
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "0", "-1", "1.5", "01", "+1", "2147483648", "99999999999999999999"})
        void invalidIdIsNotFound(String id) throws Exception {
            expectError(mvc.perform(get(SAMPLES + "/" + id)), 404);
        }

        @Test
        void missingResourceIsNotFound() throws Exception {
            expectError(mvc.perform(get(SAMPLES + "/999")), 404);
        }
    }

    @Nested
    class QueryParams {

        @Test
        void absentParametersAreNull() throws Exception {
            mvc.perform(get(SAMPLES))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value("null"));
        }

        @Test
        void acceptsStrictValuesAndIgnoresUnknownParameters() throws Exception {
            mvc.perform(get(SAMPLES)
                            .param("page", "2").param("free", "true").param("kind", "ATA")
                            .param("min_value", "10.55").param("unknown", "x"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value("2"))
                    .andExpect(jsonPath("$.free").value("true"))
                    .andExpect(jsonPath("$.kind").value("ATA"))
                    .andExpect(jsonPath("$.min_value").value("10.55"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "page=", "page=0", "page=01", "page=-1", "page=+1", "page=1.0", "page=abc",
                "free=", "free=TRUE", "free=1", "free=yes",
                "kind=", "kind=ata",
                "min_value=", "min_value=10.555", "min_value=-1", "min_value=10,5", "min_value=.5"
        })
        void rejectsInvalidOrEmptyValues(String query) throws Exception {
            expectError(mvc.perform(get(SAMPLES + "?" + query)), 400);
        }
    }

    @Nested
    class Errors {

        @Test
        void businessRuleIsConflict() throws Exception {
            mvc.perform(post("/api/v1/test/conflict"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message")
                            .value("O custo da compra (50,40) passa do limite de 50% do caixa (50,00)."));
        }

        @Test
        void unexpectedFailureHidesDetails() throws Exception {
            expectError(mvc.perform(get("/api/v1/test/boom")), 500);
            mvc.perform(get("/api/v1/test/boom"))
                    .andExpect(content().string(not(containsString("detalhe interno"))));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/api/v1/inexistente", "/api/v1/test/samples/", "/test/samples", "/"})
        void unknownRouteIsNotFound(String path) throws Exception {
            expectError(mvc.perform(get(path)), 404);
        }
    }

    @Nested
    class Cors {

        @Test
        void allowsOnlyConfiguredOrigin() throws Exception {
            mvc.perform(options(SAMPLES)
                            .header("Origin", "http://localhost:5173")
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        }

        @Test
        void rejectsOtherOrigins() throws Exception {
            mvc.perform(options(SAMPLES)
                            .header("Origin", "http://evil.example")
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        }
    }
}
