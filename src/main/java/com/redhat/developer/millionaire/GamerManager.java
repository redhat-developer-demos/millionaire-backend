package com.redhat.developer.millionaire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.developer.millionaire.ScoreDTO.GamerScore;

@ApplicationScoped
public class GamerManager {

    @Inject
    ScoreInformation scoreInformation;

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