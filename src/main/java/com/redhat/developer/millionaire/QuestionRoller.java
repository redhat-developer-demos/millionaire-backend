package com.redhat.developer.millionaire;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import com.redhat.developer.millionaire.dto.GamerCounterDTO;
import com.redhat.developer.millionaire.dto.QuestionAnswerDTO;
import com.redhat.developer.millionaire.dto.ScoreDTO;
import com.redhat.developer.millionaire.dto.ServerSideEventDTO;
import com.redhat.developer.millionaire.dto.ServerSideEventMessage;
import com.redhat.developer.millionaire.dto.ShowQuestionDTO;
import com.redhat.developer.millionaire.dto.StatisticsDTO;
import com.redhat.developer.millionaire.model.Question;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import io.quarkus.arc.Arc;
import io.vertx.core.Vertx;

@ApplicationScoped
public class QuestionRoller {
    
    @Inject
    @Channel("new-question")
    Emitter<String> questions;

    @Inject
    @Channel("admin-stream")
    Emitter<String> adminStream;

    @Inject
    Scheduler quartz;

    @Inject
    QuestionsManager questionsManager;

    @Inject
    GamerManager gamerManager;

    @Inject
    ContestState state;

    @Inject
    Statistics statistics;

    @Inject
    Vertx vertx;

    public ServerSideEventMessage sendNextQuestion() {
        Question nextQuestion = this.questionsManager.nextQuestion();

        final ShowQuestionDTO message = ShowQuestionDTO.of(nextQuestion);
        this.sendToGamers(new ServerSideEventDTO("question", message));
        start();

        return message;
    }


    // Probably I need to create a SSE manger and everything go through there
    public void sendGamerCounter() {
        final GamerCounterDTO gamerCounterDTO = new GamerCounterDTO(statistics.getNumberOfRegisteredUsers());
        this.sendToAdmin(new ServerSideEventDTO("gamercounter", gamerCounterDTO));
    }
    public void sendAnswersCounter() {
        // Just send that a new answer has been registered, let's count at client side, but when question
        // has timed out then the statistics are updated from the server side.
        this.sendToAdmin(new ServerSideEventDTO("answercounter", new ServerSideEventMessage(){}));
    }

    public QuestionAnswerDTO sendRevealAnswers() {
        final Question question = state.getCurrentQuestion().get();
        final QuestionAnswerDTO questionAnswer = QuestionAnswerDTO.of(question);
        this.sendToGamers(new ServerSideEventDTO("reveal", questionAnswer));
        return questionAnswer;
    }

    public ScoreDTO sendEndContest() {
        try {
            ScoreDTO finalScoreDTO = new ScoreDTO(gamerManager.getUsernameScore());
            this.sendToGamers(new ServerSideEventDTO("end", finalScoreDTO));
            return finalScoreDTO;
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void start() {
        final JobDetail job = newJob(SendEndOfTimeQuestion.class)
                                .withIdentity("questions", "millionaire")
                                .build();

        // TODO change
        final Date triggerDate = new Date(System.currentTimeMillis() + (questionsManager.getTimeBetweenQuestionsInSeconds() * 1000));
        final Trigger trigger = newTrigger()
                                        .withIdentity("questionsTrigger", "millionaire")
                                        .startAt(triggerDate)
                                        .build();
            try {
                quartz.scheduleJob(job, trigger);
            } catch (SchedulerException e) {
                throw new IllegalStateException(e);
            }
    }

    void sendToGamers(ServerSideEventDTO serverSideEventDTO) {
        Jsonb jsonb = JsonbBuilder.create();
        String result = jsonb.toJson(serverSideEventDTO);
        System.out.println("sending: " + result);
        questions.send(result);
    }

    void sendToAdmin(ServerSideEventDTO serverSideEventDTO) {
        Jsonb jsonb = JsonbBuilder.create();
        String result = jsonb.toJson(serverSideEventDTO);
        System.out.println("sending: " + result);
        adminStream.send(result);
    }

    public void stop() {
        try {
            quartz.deleteJob(JobKey.jobKey("questions", "millionaire"));
        } catch (SchedulerException e) {
            throw new IllegalStateException(e);
        }
    }

    void sendEndOfTimeQuestion() {
        this.sendToGamers(new ServerSideEventDTO("disable", new ServerSideEventMessage(){}));
        this.sendToAdmin(new ServerSideEventDTO("disable", new StatisticsDTO(state.getCurrentQuestion().get().questionId, statistics)));
    }

    public static class SendEndOfTimeQuestion implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException{
            Arc.container().instance(QuestionRoller.class).get().sendEndOfTimeQuestion();
        }

    }

}