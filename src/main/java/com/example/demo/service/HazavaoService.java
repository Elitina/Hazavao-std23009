package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Slf4j
public class HazavaoService {

    private final String apiKey = System.getenv("OPENAI_API_KEY");
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HazavaoService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String getDefinition(String teny) throws IOException, InterruptedException {
        String prompt = String.format(
                "Hazavao ny teny '%s' amin'ny teny gasy (malagasy). " +
                        "Omeo ny famaritana fohy sy mazava. " +
                        "Raha tsy fantatrao ny teny dia lazao hoe 'Tsy fantatra ny teny %s'.",
                teny, teny
        );

        String requestBody = createChatGptRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Erreur API OpenAI: {}", response.body());
            throw new RuntimeException("Erreur lors de l'appel à l'API OpenAI: " + response.statusCode());
        }

        return extractDefinitionFromResponse(response.body());
    }

    private String createChatGptRequestBody(String prompt) throws IOException {
        String jsonBody = String.format("""
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {
                        "role": "system",
                        "content": "Ianao dia mpamadika teny gasy mahay. Omeo famaritana fohy sy mazava ny teny rehetra."
                    },
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "max_tokens": 150,
                "temperature": 0.7
            }
            """, prompt.replace("\"", "\\\""));
        return jsonBody;
    }

    private String extractDefinitionFromResponse(String responseBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        if (jsonNode.has("choices") && jsonNode.get("choices").isArray() &&
                jsonNode.get("choices").size() > 0) {

            JsonNode choice = jsonNode.get("choices").get(0);
            if (choice.has("message") && choice.get("message").has("content")) {
                return choice.get("message").get("content").asText().trim();
            }
        }

        throw new RuntimeException("Format de réponse inattendu de l'API OpenAI");
    }
}