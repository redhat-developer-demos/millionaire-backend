package com.redhat.developer.millionaire;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContestState {
    
    private Contest currentContest;
    private Question currentQuestion;
    private long questionTime;
    
    public void setQuestionTime() {
        this.questionTime = System.currentTimeMillis();
    }

    public long getQuestionTime() {
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

    public void setCurrentQuestion(Question currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

}