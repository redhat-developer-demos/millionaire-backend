package com.redhat.developer.millionaire;

public class AccessContestDTO {
    
    private String user;
    private String userId;
    private String contestId;

    public String getUser() {
        return user;
    }

    public String getContestId() {
        return contestId;
    }

    public String getUserId() {
        return userId;
    }

    private AccessContestDTO(String userId, String user, String contestId) {
        this.user = user;
        this.userId = userId;
        this.contestId = contestId;
    }

    public static AccessContestDTO of(Gamer user, Contest contest) {
        return new AccessContestDTO(user.userId, user.username, contest.contestId);
    }

}