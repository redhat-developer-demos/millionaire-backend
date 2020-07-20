package com.redhat.developer.millionaire;

import java.util.ArrayList;
import java.util.List;

public class QuestionAnswerDTO implements ServerSideEventMessage {
    
    private int correctAnswerIndex;
    private List<Integer> incorrectAnswersIndex = new ArrayList<>();

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public List<Integer> getIncorrectAnswersIndex() {
        return incorrectAnswersIndex;
    }

    public static QuestionAnswerDTO of(Question question) {
        final QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
        final Answer correct = question.correctAnswer;
        final List<Answer> answers = question.answers;

        for (int i = 0; i < answers.size(); i++) {
            final Answer answer = answers.get(i);
            if (answer.id == correct.id) {
                questionAnswerDTO.correctAnswerIndex = i;
            } else {
                questionAnswerDTO.incorrectAnswersIndex.add(i);
            }
        }

        return questionAnswerDTO;
    }

}