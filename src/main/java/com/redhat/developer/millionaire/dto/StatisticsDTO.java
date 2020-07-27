package com.redhat.developer.millionaire.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.redhat.developer.millionaire.Statistics;

public class StatisticsDTO implements ServerSideEventMessage {
    
    public List<StatisticDTO> stats;
    public long totalAnsweredQuestions;

    public StatisticsDTO(String questionId, Statistics statistics) {
        this.totalAnsweredQuestions = statistics.getTotalAnsweredQuestions();
        this.stats = of(questionId, statistics);
        Collections.sort(this.stats);
    }

    public static List<StatisticDTO> of(String questionId, Statistics statistics) {
        return statistics.getQuestionCounter(questionId)
                    .map(qc -> qc.answers.entrySet().stream())
                    .orElseGet(() -> Map.<String, Long>of().entrySet().stream())
                    .map(e ->
                            new StatisticDTO(e.getKey(), e.getValue())
                    )
                    .collect(Collectors.toList());
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