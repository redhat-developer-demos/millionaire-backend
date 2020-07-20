package com.redhat.developer.millionaire;

import java.util.List;

public class ScoreDTO implements ServerSideEventMessage {

    private List<GamerScore> scores;

    public List<GamerScore> getScores() {
        return scores;
    }

    public void add(GamerScore gamerScore) {
        this.scores.add(gamerScore);
    }

    public ScoreDTO() {
    }

    public ScoreDTO(List<GamerScore> scores) {
        this.scores = scores;
    }
    
    public static class GamerScore implements Comparable<GamerScore> {
        public String username;
        public Long score;

        public GamerScore(String username, Long score) {
            this.username = username;
            this.score = score;
        }

        @Override
        public int compareTo(GamerScore o) {
            return this.score.compareTo(o.score);
        }

        
    }

}