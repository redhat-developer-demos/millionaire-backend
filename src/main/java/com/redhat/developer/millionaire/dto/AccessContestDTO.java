package com.redhat.developer.millionaire.dto;

import com.redhat.developer.millionaire.model.Contest;
import com.redhat.developer.millionaire.model.Gamer;

public class AccessContestDTO {
    
    private String user;
    private String userId;
    private String contestId;
    private long timeoutInSeconds = 60;

    public String getUser() {
        return user;
    }

    public String getContestId() {
        return contestId;
    }

    public String getUserId() {
        return userId;
    }

    public long getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    private AccessContestDTO(String userId, String user, String contestId, long timeoutInSeconds) {
        this.user = user;
        this.userId = userId;
        this.contestId = contestId;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public static AccessContestDTO of(Gamer user, Contest contest) {
        return new AccessContestDTO(user.userId, user.username, contest.contestId, contest.timeBetweenQuestions.toSeconds());
    }

}