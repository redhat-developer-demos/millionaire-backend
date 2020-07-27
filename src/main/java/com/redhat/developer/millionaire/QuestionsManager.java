package com.redhat.developer.millionaire;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.developer.millionaire.model.Question;

@ApplicationScoped
public class QuestionsManager {

    @Inject
    ContestState state;

    public int getTimeBetweenQuestionsInSeconds() {
        return (state.getCurrentContest()
                .map(c -> c.timeBetweenQuestions.toSeconds())
                .orElse(0L)).intValue();
    }

    public int getNumberOfQuestions()  {
        return state.getCurrentContest()
                .map(c -> c.numberOfQuestions)
                .orElse(0);
    }


    public Question nextQuestion() {
        final Question nextQuestion = getNextQuestion();
        state.setCurrentQuestion(nextQuestion);
        state.setQuestionTime();

        return nextQuestion;
    }
 
    private Question getNextQuestion() {
        return state.getCurrentQuestion()
                    .map(Question::getNextQuestion)
                    .orElse(state.getCurrentContest().get().initialQuestion);
    }

}