package com.redhat.developer.millionaire;

import java.time.Instant;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContestState {
    
    private Contest currentContest;
    private Question currentQuestion;
    private int questionNumber = 0;
    private Instant questionTime;
    
    public void setQuestionTime() {
        this.questionTime = Instant.now();
    }

    public Instant getQuestionTime() {
        return questionTime;
    }

    public Optional<Contest> getCurrentContest() {
        return Optional.ofNullable(this.currentContest);
    }

    public Optional<Question> getCurrentQuestion() {
        return Optional.ofNullable(this.currentQuestion);
    }

    public void startContest(Contest currentContest) {
        this.currentContest = currentContest;
    }

    public boolean isLastQuestion() {
        return this.questionNumber == currentContest.numberOfQuestions;
    }

    public void setCurrentQuestion(Question currentQuestion) {
        this.currentQuestion = currentQuestion;
        this.questionNumber++;
    }

}