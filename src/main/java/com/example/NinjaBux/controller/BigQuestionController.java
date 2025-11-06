package com.example.NinjaBux.controller;

import com.example.NinjaBux.domain.BigQuestion;
import com.example.NinjaBux.domain.QuestionAnswer;
import com.example.NinjaBux.dto.AnswerBigQuestionRequest;
import com.example.NinjaBux.dto.BigQuestionResponse;
import com.example.NinjaBux.dto.CreateBigQuestionRequest;
import com.example.NinjaBux.dto.SuggestQuestionRequest;
import com.example.NinjaBux.dto.ReviewQuestionRequest;
import com.example.NinjaBux.service.BigQuestionService.BigQuestionWithNinjaName;
import com.example.NinjaBux.service.BigQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/big-question")
public class BigQuestionController {

    @Autowired
    private BigQuestionService bigQuestionService;

    @GetMapping("/week/{ninjaId}")
    public ResponseEntity<BigQuestionResponse> getThisWeeksQuestion(@PathVariable Long ninjaId) {
        BigQuestionResponse question = bigQuestionService.getThisWeeksQuestion(ninjaId);
        if (question == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(question);
    }

    @PostMapping("/{questionId}/answer")
    public ResponseEntity<QuestionAnswer> submitAnswer(
            @PathVariable Long questionId,
            @RequestBody AnswerBigQuestionRequest request) {
        try {
            QuestionAnswer answer = bigQuestionService.submitAnswer(questionId, request);
            return ResponseEntity.ok(answer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<BigQuestion>> getAllQuestions() {
        return ResponseEntity.ok(bigQuestionService.getAllQuestions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BigQuestion> getQuestionById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bigQuestionService.getQuestionById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<BigQuestion> createQuestion(
            @RequestBody CreateBigQuestionRequest request,
            @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin") String adminUsername) {
        BigQuestion question = bigQuestionService.createQuestion(request, adminUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(question);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BigQuestion> updateQuestion(
            @PathVariable Long id,
            @RequestBody CreateBigQuestionRequest request) {
        try {
            BigQuestion question = bigQuestionService.updateQuestion(id, request);
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        bigQuestionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/past")
    public ResponseEntity<List<BigQuestionResponse>> getPastQuestions() {
        return ResponseEntity.ok(bigQuestionService.getPastQuestions());
    }

    @PostMapping("/suggest")
    public ResponseEntity<BigQuestion> suggestQuestion(@RequestBody SuggestQuestionRequest request) {
        try {
            BigQuestion question = bigQuestionService.suggestQuestion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(question);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-suggestions/{ninjaId}")
    public ResponseEntity<List<BigQuestion>> getMySuggestions(@PathVariable Long ninjaId) {
        return ResponseEntity.ok(bigQuestionService.getMySuggestions(ninjaId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<BigQuestionWithNinjaName>> getPendingSuggestions(
            @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin") String adminUsername) {
        return ResponseEntity.ok(bigQuestionService.getPendingSuggestionsWithNames());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BigQuestion> approveQuestion(
            @PathVariable Long id,
            @RequestBody ReviewQuestionRequest request,
            @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin") String adminUsername) {
        try {
            String admin = request.getAdminUsername() != null ? request.getAdminUsername() : adminUsername;
            BigQuestion question = bigQuestionService.approveQuestion(id, admin);
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<BigQuestion> rejectQuestion(
            @PathVariable Long id,
            @RequestBody ReviewQuestionRequest request,
            @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin") String adminUsername) {
        try {
            String admin = request.getAdminUsername() != null ? request.getAdminUsername() : adminUsername;
            BigQuestion question = bigQuestionService.rejectQuestion(id, admin, request.getReason());
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
