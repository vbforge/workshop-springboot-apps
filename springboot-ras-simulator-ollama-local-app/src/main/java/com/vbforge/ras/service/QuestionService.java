package com.vbforge.ras.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.ras.model.Question;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Loads the question bank from questions.json at application startup.
 *
 * JUNIOR NOTE — @PostConstruct:
 * Spring creates the bean, injects all dependencies (@Value fields, ObjectMapper),
 * then calls the method annotated @PostConstruct before the bean is used anywhere.
 * This is the correct place for initialization logic — not the constructor,
 * because injected fields aren't available yet in the constructor body.
 *
 * JUNIOR NOTE — @Value("${ras.questions.file}"):
 * Reads the value from application.yml → ras.questions.file.
 * Spring resolves "classpath:questions.json" to a Resource object automatically.
 * This is dependency injection of configuration — clean and testable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    // Spring injects the Resource from application.yml config
    @Value("${ras.questions.file}")
    private Resource questionsResource;

    // Spring auto-configures ObjectMapper — we just inject it
    private final ObjectMapper objectMapper;

    // Loaded once, read-only after that — no need for thread-safe collection
    private List<Question> questions;

    @PostConstruct
    public void loadQuestions() {
        try {
            // TypeReference tells Jackson the exact generic type to deserialize into
            // Without it, Jackson would return List<LinkedHashMap> — not List<Question>
            questions = objectMapper.readValue(
                    questionsResource.getInputStream(),
                    new TypeReference<List<Question>>() {}
            );
            log.info("Loaded {} questions from {}", questions.size(), questionsResource.getFilename());
        } catch (IOException e) {
            // Fail fast — if questions can't load, the app is broken. Don't swallow this.
            throw new RuntimeException("Failed to load questions from JSON", e);
        }
    }

    public List<Question> getAllQuestions() {
        return questions;
    }

    public int getTotalCount() {
        return questions.size();
    }

    /**
     * Get question by 0-based index.
     * Returns null if index is out of bounds (session is done).
     */
    public Question getByIndex(int index) {
        if (index < 0 || index >= questions.size()) {
            return null;
        }
        return questions.get(index);
    }
}
