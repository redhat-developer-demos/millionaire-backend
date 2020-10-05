package com.redhat.developer.millionaire.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.redhat.developer.millionaire.Statistics;

public class StatisticsDTO implements ServerSideEventMessage {
    
    public List<StatisticDTO> stats;
    public long percentageOfRightAnswers;
    public long totalAnsweredQuestions;

    public StatisticsDTO(String questionId, String correctAnswerPrefix, Statistics statistics) {
        this.totalAnsweredQuestions = statistics.getTotalAnsweredQuestions();
        this.stats = of(questionId, correctAnswerPrefix, statistics);
        Collections.sort(this.stats);
        percentageOfRightAnswers = calculatePercentageOfRightAnswers(correctAnswerPrefix);
    }

    public static List<StatisticDTO> of(String questionId, String correctAnswerPrefix, Statistics statistics) {
        return statistics.getQuestionCounter(questionId)
                    .map(qc -> qc.answers.entrySet().stream())
                    .orElseGet(() -> Map.<String, Long>of().entrySet().stream())
                    .map(e ->
                            new StatisticDTO(e.getKey(), e.getValue())
                    )
                    .collect(Collectors.toList());
    }

    private long calculatePercentageOfRightAnswers(String correctAnswerPrefix) {
        double total = 0;
        double correct = 0;
        for (StatisticDTO stat : stats) {
            total += stat.count;

            if (stat.answer.equals(correctAnswerPrefix)) {
                correct += stat.count;
            }
        }
        double percentage = correct == 0 ? correct : ((correct/total) * 100);
        return (long) percentage;
    }

    public static class StatisticDTO implements Comparable<StatisticDTO> {
        public String answer;
        public long count;

        public StatisticDTO(String answer, long count) {
            this.answer = answer;
            this.count = count;
        }

        @Override
        public int compareTo(StatisticDTO o) {
            return this.answer.compareTo(o.answer);
        }

    }
}