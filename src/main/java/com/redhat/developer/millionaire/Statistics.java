package com.redhat.developer.millionaire;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

// This class should use Redis to store statistics.
// Needs to lock methods (waiting for Quarkus 1.7.0 for better experience)

@ApplicationScoped
public class Statistics {
    
    private long numberOfRegisteredUsers = 0;

    private Map<String, QuestionCounter> questions = new HashMap<>();

    public void reset() {
        this.numberOfRegisteredUsers = 0;
        this.questions.clear();
    }

    public void incrNumberOfUsers() {
        numberOfRegisteredUsers++;
    }

    public long getNumberOfRegisteredUsers() {
        return numberOfRegisteredUsers;
    }

    public long getTotalAnsweredQuestions() {
        return questions.values()
                    .stream()
                    .mapToLong(QuestionCounter::getTotalAnswers)
                    .sum();
    }

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

    public static class QuestionCounter {
        
        public Map<String, Long> answers = new HashMap<>();

        public QuestionCounter incr(String answerId) {
            answers.merge(answerId, 1L, Long::sum);
            return this;
        }

        public long getTotalAnswers() {
            return answers.values()
                            .stream()
                            .mapToLong(Long::longValue)
                            .sum();
        }
    }

}