package com.redhat.developer.millionaire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.developer.millionaire.dto.ScoreDTO;
import com.redhat.developer.millionaire.dto.ScoreDTO.GamerScore;
import com.redhat.developer.millionaire.model.Gamer;


@ApplicationScoped
public class GamerManager {

    @Inject
    ScoreInformation scoreInformation;

    public List<ScoreDTO.GamerScore> getUsernameScore(int max) {
        List<GamerScore> usernameScore = getUsernameScore();
        return usernameScore.subList(0, Math.min(usernameScore.size(), max));
    }

    public List<GamerScore> getUsernameScore() {

        final List<GamerScore> usernameScore = new ArrayList<>();
        
        scoreInformation.getScore().forEach((userId, score) -> {
            usernameScore.add(new GamerScore(Gamer.findGamerByUserId(userId).username, score));
        });

        Collections.sort(usernameScore);
        Collections.reverse(usernameScore);

        return usernameScore;
    }

}