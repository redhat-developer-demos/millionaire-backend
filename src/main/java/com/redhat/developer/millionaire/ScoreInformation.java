package com.redhat.developer.millionaire;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ScoreInformation {
    


    @Inject
    ContestState contestState;

    long timeBetweenQuestions;

    @PostConstruct
    public void init() {
        timeBetweenQuestions = contestState.getCurrentContest().get().timeBetweenQuestions.getSeconds();
    }

    private Map<String, Long> score = new ConcurrentHashMap<>();

    public void increment(String user) {

        long points = System.currentTimeMillis() - contestState.getQuestionTime();
        // to avoid negative points
        points = Math.max(points, 0);
        long finalScore = points == 0 ? 0 : timeBetweenQuestions - (points/1000);

        this.score.computeIfPresent(user, (k, v) -> v + finalScore);
        this.score.computeIfAbsent(user, k -> finalScore);
    }

    public void fail(String user) {
        this.score.computeIfPresent(user, (k, v) -> v + 0);
        this.score.computeIfAbsent(user, k -> 0L);
    }

    public Map<String, Long> getScore() {
        return score;
    }

}