package com.redhat.developer.millionaire.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.redhat.developer.millionaire.model.Question;

public class ShowQuestionDTO implements ServerSideEventMessage {
    
    public String questionTitle;
    public String questionId;

    public List<ShowAnswerDTO> answers = new ArrayList<>();

    public static class ShowAnswerDTO implements Comparable<ShowAnswerDTO> {
        public Long answerId;
        public String prefix;
        public String answerDescription;

        public ShowAnswerDTO() {
        }

        public ShowAnswerDTO(Long answerId, String prefix, String answerDescription) {
            this.answerId = answerId;
            this.prefix = prefix;
            this.answerDescription = answerDescription;
        }

        @Override
        public int compareTo(ShowAnswerDTO o) {
            return this.prefix.compareTo(o.prefix);
        }

        public String getPrefix() {
            return prefix;
        }

        public String getAnswerDescription() {
            return answerDescription;
        }

    }

    public static ShowQuestionDTO of(Question question) {
        final ShowQuestionDTO showQuestionDTO = new ShowQuestionDTO();
        showQuestionDTO.questionId = question.questionId;
        showQuestionDTO.questionTitle = question.title;
        showQuestionDTO.answers = question.answers.stream()
                                                    .map(a -> new ShowAnswerDTO(a.id, a.prefix, a.description))
                                                    .sorted()
                                                    .collect(Collectors.toList());
        return showQuestionDTO;
    }

}