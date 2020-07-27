package com.redhat.developer.millionaire.dto;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.redhat.developer.millionaire.model.Answer;

public class ContestDTO {
    
    private String name;
    private Duration durationBetweenQuestions;

    private List<QuestionDTO> questions = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Duration getDurationBetweenQuestions() {
        return durationBetweenQuestions;
    }

    public void setDurationBetweenQuestions(Duration durationBetweenQuestions) {
        this.durationBetweenQuestions = durationBetweenQuestions;
    }

    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
    }

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    public void addQuestion(QuestionDTO question) {
        this.questions.add(question);
    }

    

    public static class QuestionDTO implements Comparable<QuestionDTO> {
        private String title;
        private int order;

        private AnswerDTO correctAnswer;
        private List<AnswerDTO> incorrectAnswers = new ArrayList<>();

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public int getOrder() {
            return order;
        }

        public void setCorrectAnswer(AnswerDTO correctAnswer) {
            this.correctAnswer = correctAnswer;
        }

        public AnswerDTO getCorrectAnswer() {
            return correctAnswer;
        }

        public void setIncorrectAnswers(List<AnswerDTO> incorrectAnswers) {
            this.incorrectAnswers = incorrectAnswers;
        }

        public List<AnswerDTO> getIncorrectAnswers() {
            return incorrectAnswers;
        }

        @Override
        public int compareTo(QuestionDTO o) {
            return this.order - o.order;
        }

        @Override
        public String toString() {
            return "QuestionDTO [correctAnswer=" + correctAnswer + ", incorrectAnswers=" + incorrectAnswers + ", order="
                    + order + ", title=" + title + "]";
        }

    }

    public static class AnswerDTO {

        private String description;
        private String prefix;

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        @Override
        public String toString() {
            return "AnswerDTO [description=" + description + ", prefix=" + prefix + "]";
        }

        public Answer toAnswer() {
            Answer answer = new Answer();
            answer.description = this.getDescription();
            answer.prefix = this.getPrefix();
            return answer;
        }

    }

    @Override
    public String toString() {
        return "ContestDTO [durationBetweenQuestions=" + durationBetweenQuestions + ", name=" + name + ", questions="
                + questions + "]";
    }
}