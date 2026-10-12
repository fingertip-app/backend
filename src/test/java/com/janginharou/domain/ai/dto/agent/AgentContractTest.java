package com.janginharou.domain.ai.dto.agent;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AgentContractTest {
    // Spring's mapper may ignore unknown fields. New contracts must reject them locally.
    private final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Class<?> contract(String name) {
        return assertDoesNotThrow(() -> Class.forName("com.janginharou.domain.ai.dto.agent." + name),
                "Missing version-one contract: " + name);
    }

    private JsonNode fixture(String file) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/contracts/agent-v1/" + file)) {
            assertThat(stream).isNotNull();
            return mapper.readTree(stream);
        }
    }

    private boolean rejects(String name, String json) throws Exception {
        Class<?> type = contract(name);
        try {
            Object value = mapper.readValue(json, type);
            return !validator.validate(value).isEmpty();
        } catch (com.fasterxml.jackson.core.JsonProcessingException | IllegalArgumentException error) {
            return true;
        }
    }

    @Test
    void moneyRoundTripsAsDecimalStringForNumericAndTextInputs() throws Exception {
        for (String amount : new String[]{"50000", "50000.01", "49999.99", "0"}) {
            for (String input : new String[]{amount, "\"" + amount + "\""}) {
                Object value = mapper.readValue("{\"maxPriceKrw\":" + input + "}", contract("AgentConstraints"));
                assertThat(validator.validate(value)).isEmpty();
                assertThat(mapper.valueToTree(value).path("maxPriceKrw").isTextual()).isTrue();
                assertThat(mapper.valueToTree(value).path("maxPriceKrw").asText()).isEqualTo(amount);
            }
        }
    }

    @Test
    void requestAndSearchDefaultsTrimAndValidate() throws Exception {
        Object request = mapper.readValue("{\"query\":\"한지\"}", contract("AgentRequest"));
        assertThat(validator.validate(request)).isEmpty();
        JsonNode serialized = mapper.valueToTree(request);
        assertThat(serialized.path("locale").asText()).isEqualTo("ko");
        assertThat(serialized.path("constraints").path("headCount").asInt()).isEqualTo(1);
        Object search = mapper.readValue("{\"queryTerms\":[\"  한지  \"]}", contract("CatalogSearchRequest"));
        assertThat(validator.validate(search)).isEmpty();
        serialized = mapper.valueToTree(search);
        assertThat(serialized.path("queryTerms").get(0).asText()).isEqualTo("한지");
        assertThat(serialized.path("limit").asInt()).isEqualTo(5);
    }

    @Test
    void sharedExamplesRoundTripWithSameFieldNamesAndEnums() throws Exception {
        String[][] examples = {{"request.json", "AgentRequest"}, {"completed.json", "AgentResponse"},
                {"clarification.json", "AgentResponse"}, {"catalog-item.json", "CatalogExperienceResponse"},
                {"catalog-search.json", "CatalogSearchRequest"},
                {"catalog-detail.json", "CatalogExperienceDetailsResponse"},
                {"culture-search.json", "CultureSearchRequest"}, {"culture-documents.json", "CultureSearchResponse"},
                {"draft.json", "AgentDraft"}};
        for (String[] example : examples) {
            JsonNode raw = fixture(example[0]);
            Object value = mapper.treeToValue(raw, contract(example[1]));
            assertThat(validator.validate(value)).isEmpty();
            assertThat(mapper.readTree(mapper.writeValueAsBytes(value))).isEqualTo(raw);
        }
    }

    @Test
    void rejectsSharedInvalidRequestsWithoutGlobalMapperChanges() throws Exception {
        for (JsonNode example : fixture("invalid-examples.json")) {
            assertThat(rejects(example.path("model").asText(), example.path("payload").toString()))
                    .as(example.path("name").asText()).isTrue();
        }
    }

    @Test
    void exactRawDecimalExamplesRejectUnsupportedStringForms() throws Exception {
        for (JsonNode example : fixture("decimal-wire-examples.json")) {
            String raw = example.path("inputJson").asText();
            if (example.path("valid").asBoolean()) {
                Object value = mapper.readValue(raw, contract("AgentConstraints"));
                assertThat(validator.validate(value)).isEmpty();
                assertThat(mapper.valueToTree(value).path("maxPriceKrw").asText())
                        .isEqualTo(example.path("maxPriceKrw").asText());
            } else {
                assertThat(rejects("AgentConstraints", raw)).as(example.path("name").asText()).isTrue();
            }
        }
    }

    @Test
    void responseRejectsConflictingAndUnsupportedConditions() throws Exception {
        JsonNode raw = fixture("completed.json");
        for (String status : new String[]{"no_match", "clarification_required"}) {
            var changed = raw.deepCopy();
            ((com.fasterxml.jackson.databind.node.ObjectNode) changed).put("status", status);
            assertThat(rejects("AgentResponse", changed.toString())).isTrue();
        }
        var changed = (com.fasterxml.jackson.databind.node.ObjectNode) raw.deepCopy();
        changed.putArray("recommendedExperienceIds").add(102);
        assertThat(rejects("AgentResponse", changed.toString())).isTrue();
        changed = (com.fasterxml.jackson.databind.node.ObjectNode) raw.deepCopy();
        changed.putArray("unsupportedConstraints").add("weekend");
        assertThat(rejects("AgentResponse", changed.toString())).isTrue();
        // ID strings and floats must not be silently coerced.
        changed = (com.fasterxml.jackson.databind.node.ObjectNode) raw.deepCopy();
        changed.putArray("recommendedExperienceIds").add("101");
        assertThat(rejects("AgentResponse", changed.toString())).isTrue();
    }
}
