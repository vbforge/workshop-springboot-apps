package com.vbforge.ras.model;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds everything about one user's current session.
 *
 * JUNIOR NOTE — Why Serializable?
 * Spring stores session data in HTTP session (server memory by default).
 * If you ever move to Redis-backed sessions (for scaling), Spring needs to
 * serialize this object. Implementing Serializable now is defensive practice.
 *
 * JUNIOR NOTE — Why LinkedHashMap?
 * We store answers as questionId -> answerText.
 * LinkedHashMap preserves insertion order — so when we iterate to build
 * the AI prompt, answers come out in the order the user answered them.
 * A regular HashMap would give random order each time.
 */
@Data
public class SessionData implements Serializable {

    // Tracks which question the user is currently on (0-indexed internally, 1-indexed in UI)
    private int currentQuestionIndex = 0;

    // Map of questionId -> user's answer text
    private Map<Integer, String> answers = new LinkedHashMap<>();

    // The AI-generated result, populated after all questions are answered
    private String aiResult;

    // Flag: has the user finished all questions?
    public boolean isCompleted() {
        return aiResult != null;
    }

    // Add or update an answer
    public void addAnswer(int questionId, String answer) {
        answers.put(questionId, answer);
    }

    // Move to next question
    public void advance() {
        currentQuestionIndex++;
    }
}
