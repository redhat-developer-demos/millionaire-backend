package com.redhat.developer.millionaire;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

// This class should use Redis to store statistics of each question.

@ApplicationScoped
public class Statistics {
    
    private Map<String, QuestionCounter> questions = new HashMap<>();

    public void incr(String questionId, String answerId) {
        questions.computeIfAbsent(questionId, k -> new QuestionCounter())
                 .incr(answerId);
    }

    public Optional<QuestionCounter> getQuestionCounter(String questionId) {
        return Optional.ofNullable(questions.get(questionId));
    }

    public Map<String, QuestionCounter> getQuestions() {
        return questions;
    }

    static class QuestionCounter {
        
        public Map<String, Long> answers = new HashMap<>();

        public QuestionCounter incr(String answerId) {
            answers.merge(answerId, 1L, Long::sum);
            return this;
        }
    }

}