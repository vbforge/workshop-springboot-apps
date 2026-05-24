package com.vbforge.ras.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vbforge.ras.model.Question;
import com.vbforge.ras.model.SessionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Communicates with the local Ollama instance to generate AI analysis.
 *
 * JUNIOR NOTE — how Ollama's REST API works:
 *
 * POST http://ollama:11434/api/generate
 * Body: { "model": "llama3.2:3b", "prompt": "...", "stream": false }
 *
 * Response: { "response": "the AI text here", "done": true, ... }
 *
 * That's it. No auth, no API key — it's your local machine.
 * We set stream:false so we get one complete JSON response instead of
 * a stream of partial chunks (simpler to handle for now).
 */
@Slf4j
@Service
public class OllamaService {

    @Value("${ras.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ras.ollama.model}")
    private String model;

    @Value("${ras.ollama.timeout}")
    private int timeoutSeconds;

    private final QuestionService questionService;
    private final ObjectMapper objectMapper;

    public OllamaService(QuestionService questionService, ObjectMapper objectMapper) {
        this.questionService = questionService;
        this.objectMapper = objectMapper;
    }

    /**
     * Main entry point — builds the prompt and calls Ollama.
     * Falls back to a plain summary if Ollama is unavailable.
     */
    public String analyze(SessionData sessionData) {
        String prompt = buildPrompt(sessionData);
        log.info("Sending prompt to Ollama (model: {}, timeout: {}s)", model, timeoutSeconds);
        log.debug("Prompt:\n{}", prompt);

        try {
            return callOllama(prompt);
        } catch (Exception e) {
            // JUNIOR NOTE — fail gracefully:
            // If Ollama is slow, crashed, or the model isn't loaded,
            // we don't crash the user's session. We return a fallback.
            // Always think: "what does the user see if this breaks?"
            log.error("Ollama call failed: {}", e.getMessage());
            return buildFallback(sessionData);
        }
    }

    /**
     * Calls Ollama's /api/generate endpoint and returns the response text.
     *
     * JUNIOR NOTE — WebClient chain explained:
     *
     * WebClient.builder()          → creates a configured HTTP client
     * .baseUrl(ollamaBaseUrl)      → sets the base URL (http://ollama:11434)
     * .build()                     → builds the client instance
     * .post()                      → HTTP POST
     * .uri("/api/generate")        → appended to base URL
     * .bodyValue(requestBody)      → JSON body (Jackson serializes the ObjectNode)
     * .retrieve()                  → sends the request, prepares to read response
     * .bodyToMono(String.class)    → read response body as a String (async Mono)
     * .timeout(Duration.of...)     → fail if no response within N seconds
     * .block()                     → WAIT here synchronously (blocking call)
     *                                In reactive apps you'd return the Mono instead
     *
     * The response is raw JSON — we parse it with Jackson to extract "response" field.
     */
    private String callOllama(String prompt) {
        // Build the request body as JSON
        // JUNIOR NOTE: ObjectNode is Jackson's way to build JSON programmatically
        // without needing a separate request DTO class
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);  // get full response at once, not streamed chunks

        // Optional: tune the model's creativity
        // temperature 0.7 = balanced (0.0 = deterministic, 1.0 = creative/chaotic)
        ObjectNode options = objectMapper.createObjectNode();
        options.put("temperature", 0.7);
        options.put("num_predict", 600);   // max tokens in response (~450 words)
        requestBody.set("options", options);

        WebClient client = WebClient.builder()
                .baseUrl(ollamaBaseUrl)
                .codecs(config -> config
                        .defaultCodecs()
                        .maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer — AI responses can be large
                .build();

        String rawResponse = client.post()
                .uri("/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

        // Parse the JSON response and extract the "response" field
        return extractResponse(rawResponse);
    }

    /**
     * Parses Ollama's JSON response and returns just the text content.
     *
     * Ollama response looks like:
     * {
     *   "model": "llama3.2:3b",
     *   "response": "Here is your RAS profile...",
     *   "done": true,
     *   "total_duration": 1234567890,
     *   ...
     * }
     *
     * We only care about the "response" field.
     */
    private String extractResponse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode responseNode = root.get("response");
            if (responseNode == null || responseNode.isNull()) {
                log.warn("Ollama returned no 'response' field: {}", rawJson);
                return "The AI returned an unexpected response. Please try again.";
            }
            return responseNode.asText().trim();
        } catch (Exception e) {
            log.error("Failed to parse Ollama response: {}", e.getMessage());
            return "Could not parse the AI response. Please try again.";
        }
    }

    /**
     * Builds the prompt sent to Ollama.
     *
     * JUNIOR NOTE — prompt engineering basics:
     * The quality of AI output depends heavily on how you phrase the prompt.
     * Key principles used here:
     * 1. Give it a ROLE — "You are an expert in..." grounds its persona
     * 2. Give it STRUCTURE — numbered sections tell it exactly what to output
     * 3. Give it the DATA — the user's answers are injected verbatim
     * 4. Give it CONSTRAINTS — "Be concise", "No fluff" shapes the output style
     * 5. Use the word EXACTLY for critical formatting requirements
     *
     * The prompt is the most important thing in this whole service.
     * Bad prompt = generic, useless output.
     * Good prompt = specific, actionable insight.
     */
    private String buildPrompt(SessionData sessionData) {
        List<Question> questions = questionService.getAllQuestions();
        Map<Integer, String> answers = sessionData.getAnswers();

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an expert in neuroscience, goal psychology, and the Reticular Activating System (RAS). ");
        prompt.append("A person has answered a series of deep reflection questions to help program their RAS — ");
        prompt.append("the brain's attention filter that highlights what matters most.\n\n");

        prompt.append("Here are their answers:\n\n");

        // Inject each question and answer pair
        for (Question q : questions) {
            String answer = answers.getOrDefault(q.getId(), "[not answered]");
            prompt.append("Q: ").append(q.getText()).append("\n");
            prompt.append("A: ").append(answer).append("\n\n");
        }

        prompt.append("Based on these answers, provide a structured RAS profile. ");
        prompt.append("Format your response EXACTLY as follows, using these exact headings:\n\n");

        prompt.append("🎯 CORE GOAL\n");
        prompt.append("One clear, powerful sentence stating their core goal.\n\n");

        prompt.append("🔍 WHAT YOUR RAS SHOULD FILTER FOR\n");
        prompt.append("3 specific things they should now notice in their daily environment that signal progress. ");
        prompt.append("Make them concrete and observable.\n\n");

        prompt.append("⚡ YOUR NEXT 3 ACTIONS\n");
        prompt.append("3 specific actions they can take this week, ordered by priority. ");
        prompt.append("Each action on its own line starting with a number.\n\n");

        prompt.append("💬 YOUR FOCUS ANCHOR\n");
        prompt.append("One short phrase (5-10 words) they can repeat daily to prime their RAS. ");
        prompt.append("Make it personal, present tense, and vivid.\n\n");

        prompt.append("🧠 INSIGHT\n");
        prompt.append("2-3 sentences of honest, direct insight about what their answers reveal. ");
        prompt.append("Be specific to THEIR answers, not generic advice.\n\n");

        prompt.append("Be concise. Be specific to their exact answers. No generic motivational fluff. ");
        prompt.append("Write as if you truly read and understood every word they wrote.");

        return prompt.toString();
    }

    /**
     * Fallback response when Ollama is unavailable.
     * Better than showing an error — shows what we CAN extract from their answers.
     */
    private String buildFallback(SessionData sessionData) {
        Map<Integer, String> answers = sessionData.getAnswers();
        return "⚠️ AI analysis unavailable (Ollama may still be loading the model).\n\n" +
               "🎯 CORE GOAL\n" +
               answers.getOrDefault(1, "[not answered]") + "\n\n" +
               "✨ YOUR VISION\n" +
               answers.getOrDefault(2, "[not answered]") + "\n\n" +
               "⚡ YOUR NEXT ACTION\n" +
               answers.getOrDefault(6, "[not answered]") + "\n\n" +
               "Try refreshing in a minute — the model may still be initializing.";
    }
}
