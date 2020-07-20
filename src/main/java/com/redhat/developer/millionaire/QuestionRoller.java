package com.redhat.developer.millionaire;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.KeyMatcher.keyEquals;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.listeners.TriggerListenerSupport;

import io.quarkus.arc.Arc;
import io.vertx.core.Vertx;

@ApplicationScoped
public class QuestionRoller {
    
    @Inject
    @Channel("new-question")
    Emitter<String> questions;

    @Inject
    Scheduler quartz;

    @Inject
    QuestionsManager questionsManager;

    @Inject
    GamerManager gamerManager;

    @Inject
    Vertx vertx;

    public void start() {
        final JobDetail job = newJob(SendEndOfTimeQuestion.class)
                                .withIdentity("questions", "millionaire")
                                .build();

        final SimpleTrigger trigger = newTrigger()
                                        .withIdentity("questionsTrigger", "millionaire")
                                        .startNow()
                                        .withSchedule(
                                            SimpleScheduleBuilder.simpleSchedule()
                                                .withIntervalInSeconds(questionsManager.getTimeBetweenQuestionsInSeconds())
                                                .withRepeatCount(questionsManager.getNumberOfQuestions() - 1))
                                        .build();
        try {
            quartz.getListenerManager()
                    .addTriggerListener(new EndTriggerListener("EndTrigger"),
                                                keyEquals(triggerKey("questionsTrigger", "millionaire")));
            quartz.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new IllegalStateException(e);
        }
    }

    void sendEndOfTimeQuestion() {
        final ShowQuestionDTO message = ShowQuestionDTO.of(this.questionsManager.nextQuestion());
        this.send(new ServerSideEventDTO("question", message));
    }

    void sendEndContest() {
        System.out.println("before");
        try {
            ScoreDTO finalScoreDTO = new ScoreDTO(gamerManager.getUsernameScore());
            System.out.println(finalScoreDTO);
            send(new ServerSideEventDTO("end", finalScoreDTO));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    void send(ServerSideEventDTO serverSideEventDTO) {
        Jsonb jsonb = JsonbBuilder.create();
        String result = jsonb.toJson(serverSideEventDTO);
        System.out.println("sending: " + result);
        questions.send(result);
    }

    public void stop() {
        try {
            quartz.deleteJob(JobKey.jobKey("questions", "millionaire"));
        } catch (SchedulerException e) {
            throw new IllegalStateException(e);
        }
    }

    public class EndTriggerListener extends TriggerListenerSupport {

        private String name;

        public EndTriggerListener(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void triggerComplete(
            Trigger trigger,
            JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
                
                if (triggerInstructionCode == CompletedExecutionInstruction.DELETE_TRIGGER) {
                    final JobDetail job2 = newJob(SendEndContest.class)
                                .withIdentity("end", "millionaire")
                                .build();
                    
                    Date triggerTime = new Date(System.currentTimeMillis() + (questionsManager.getTimeBetweenQuestionsInSeconds() * 1000));

                    final Trigger trigger2 = newTrigger()
                                .withIdentity("endTrigger", "millionaire")
                                .startAt(triggerTime)
                                .build();

                    try {
                        quartz.scheduleJob(job2, trigger2);
                    } catch (SchedulerException e) {
                        throw new IllegalStateException(e);
                    }
                }
        }

    }

    public static class SendEndContest implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("End Trigger");
            Arc.container().instance(QuestionRoller.class).get().sendEndContest();  
        }

    }

    public static class SendEndOfTimeQuestion implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException{
            Arc.container().instance(QuestionRoller.class).get().sendEndOfTimeQuestion();
        }

    }

}