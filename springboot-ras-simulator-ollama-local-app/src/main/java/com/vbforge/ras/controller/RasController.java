package com.vbforge.ras.controller;

import com.vbforge.ras.model.Question;
import com.vbforge.ras.model.SessionData;
import com.vbforge.ras.service.OllamaService;
import com.vbforge.ras.service.QuestionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles all HTTP requests and controls the question flow.
 *
 * JUNIOR NOTE — @Controller vs @RestController:
 * @RestController returns JSON/text directly (for REST APIs).
 * @Controller returns view names — Thymeleaf resolves them to HTML templates.
 * We use @Controller because we want Thymeleaf to render pages.
 *
 * JUNIOR NOTE — HttpSession:
 * HTTP is stateless — every request is independent. HttpSession is how Spring
 * keeps data between requests for the same user. Spring stores it server-side
 * and sends a session cookie to the browser to identify returning requests.
 * SessionData lives in the session — it survives across multiple page loads.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RasController {

    private static final String SESSION_KEY = "rasSession";

    private final QuestionService questionService;
    private final OllamaService ollamaService;

    // ─── Welcome Page ─────────────────────────────────────────────────────────

    /**
     * Root URL — shows the welcome/landing page.
     */
    @GetMapping("/")
    public String welcome(Model model) {
        model.addAttribute("totalQuestions", questionService.getTotalCount());
        return "index"; // resolves to templates/index.html
    }

    // ─── Start Session ─────────────────────────────────────────────────────────

    /**
     * User clicks "Start" — creates a fresh session and redirects to first question.
     *
     * JUNIOR NOTE — Post/Redirect/Get (PRG) pattern:
     * After a POST, we always redirect (GET) instead of rendering directly.
     * This prevents the browser from re-submitting the form if the user refreshes.
     * You'll see this pattern everywhere in web apps.
     */
    @PostMapping("/start")
    public String start(HttpSession session) {
        // Always create a fresh session — clears any previous run
        session.setAttribute(SESSION_KEY, new SessionData());
        log.info("New session started: {}", session.getId());
        return "redirect:/question"; // PRG pattern
    }

    // ─── Question Page ─────────────────────────────────────────────────────────

    /**
     * Shows the current question based on where the user is in the flow.
     */
    @GetMapping("/question")
    public String showQuestion(HttpSession session, Model model) {
        SessionData sessionData = getOrCreateSession(session);

        // Guard: if somehow the user navigates here after completion, send to result
        if (sessionData.isCompleted()) {
            return "redirect:/result";
        }

        int index = sessionData.getCurrentQuestionIndex();
        Question currentQuestion = questionService.getByIndex(index);

        // Guard: index out of bounds means all questions answered — show loading screen first
        // The processing.html page shows a spinner, then JS triggers /process in the background
        if (currentQuestion == null) {
            return "processing";
        }

        // Add data to the model — Thymeleaf reads from here in the template
        model.addAttribute("question", currentQuestion);
        model.addAttribute("currentNumber", index + 1);          // 1-based for display
        model.addAttribute("totalQuestions", questionService.getTotalCount());
        model.addAttribute("progressPercent", calculateProgress(index));

        return "question"; // resolves to templates/question.html
    }

    // ─── Submit Answer ─────────────────────────────────────────────────────────

    /**
     * User submits an answer — saves it, advances, redirects to next question.
     *
     * @RequestParam maps the HTML form field named "answer" to this parameter.
     */
    @PostMapping("/answer")
    public String submitAnswer(
            @RequestParam("questionId") int questionId,
            @RequestParam("answer") String answer,
            HttpSession session) {

        SessionData sessionData = getOrCreateSession(session);

        // Basic validation — don't save empty answers
        String trimmed = answer == null ? "" : answer.trim();
        if (trimmed.isEmpty()) {
            log.warn("Empty answer submitted for question {}", questionId);
            return "redirect:/question"; // just re-show the same question
        }

        sessionData.addAnswer(questionId, trimmed);
        sessionData.advance();
        log.debug("Answer saved for question {}, moving to index {}", questionId, sessionData.getCurrentQuestionIndex());

        return "redirect:/question"; // PRG — next question or /process via the GET guard above
    }

    // ─── Process (AI Call) ─────────────────────────────────────────────────────

    /**
     * All questions answered — call the AI and store the result.
     * This is a GET that triggers the Ollama call, then redirects to result.
     *
     * JUNIOR NOTE: In a real app you'd do this asynchronously with a loading
     * page polling for completion. For Phase 1 (placeholder), it's instant.
     * For Phase 2 (real Ollama), the timeout is 120s — the redirect waits.
     * We'll improve UX in Phase 3 with a spinner page.
     */
    @GetMapping("/process")
    public String process(HttpSession session) {
        SessionData sessionData = getOrCreateSession(session);

        // Guard: no answers yet — send back to start
        if (sessionData.getAnswers().isEmpty()) {
            return "redirect:/";
        }

        // Guard: already processed — skip re-calling AI
        if (sessionData.isCompleted()) {
            return "redirect:/result";
        }

        log.info("Processing {} answers, calling AI...", sessionData.getAnswers().size());

        // This blocks until Ollama responds (or times out)
        String result = ollamaService.analyze(sessionData);
        sessionData.setAiResult(result);

        return "redirect:/result";
    }

    // ─── Result Page ───────────────────────────────────────────────────────────

    /**
     * Shows the AI-generated RAS analysis.
     */
    @GetMapping("/result")
    public String showResult(HttpSession session, Model model) {
        SessionData sessionData = getOrCreateSession(session);

        if (!sessionData.isCompleted()) {
            return "redirect:/";
        }

        model.addAttribute("result", sessionData.getAiResult());
        model.addAttribute("answers", sessionData.getAnswers());
        model.addAttribute("questions", questionService.getAllQuestions());

        return "result"; // resolves to templates/result.html
    }

    // ─── Restart ───────────────────────────────────────────────────────────────

    /**
     * Clears the session and starts over.
     */
    @PostMapping("/restart")
    public String restart(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
        log.info("Session cleared, restarting");
        return "redirect:/";
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Gets the existing session data, or creates a new one.
     * Defensive: handles the case where someone visits /question directly.
     */
    private SessionData getOrCreateSession(HttpSession session) {
        SessionData data = (SessionData) session.getAttribute(SESSION_KEY);
        if (data == null) {
            data = new SessionData();
            session.setAttribute(SESSION_KEY, data);
        }
        return data;
    }

    /**
     * Calculates progress as a percentage for the progress bar.
     * index 0 of 6 = 0%, index 5 of 6 = ~83%, etc.
     */
    private int calculateProgress(int currentIndex) {
        int total = questionService.getTotalCount();
        if (total == 0) return 0;
        return (currentIndex * 100) / total;
    }
}
