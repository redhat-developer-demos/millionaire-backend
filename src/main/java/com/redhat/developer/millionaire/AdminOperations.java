package com.redhat.developer.millionaire;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.redhat.developer.millionaire.ContestDTO.AnswerDTO;
import com.redhat.developer.millionaire.ContestDTO.QuestionDTO;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;

import io.smallrye.mutiny.Multi;

@Path("/admin")
@Transactional
public class AdminOperations {

    @Inject
    IdentifierGenerator identifierGenerator;

    @Inject
    ContestState state;

    @Inject
    QuestionsManager questionManager;

    @Inject
    GamerManager gamerManager;

    @Inject
    QuestionRoller task;

    @Inject @Channel("admin-stream") Multi<String> streamOfAdmin;

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    public Multi<String> stream() {
        return streamOfAdmin;
    }

    @POST
    @Path("/import")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response importContest(ContestDTO contestDTO) {
        Contest contest = persistContest(contestDTO);
        return Response.status(Status.CREATED).entity(contest.contestId).build();
    }

    private Contest persistContest(ContestDTO contestDTO) {
        final Contest contest = new Contest();
        contest.creationTime = new Date();
        contest.name = contestDTO.getName();
        contest.contestId = identifierGenerator.generateId(contest.name);
        if (contestDTO.getDurationBetweenQuestions() != null) {
            contest.timeBetweenQuestions = contestDTO.getDurationBetweenQuestions();
        }

        List<QuestionDTO> questions = contestDTO.getQuestions();
        Collections.sort(questions);
        Collections.reverse(questions);

        Question previousQuestion = null;
        int numberOfQuestions = 0;
        for (QuestionDTO questionDTO : questions) {
            Question question = new Question();
            question.title = questionDTO.getTitle();
            question.questionId = identifierGenerator.generateId(question.title);

            final AnswerDTO correctAnswerDTO = questionDTO.getCorrectAnswer();
            question.correctAnswer = persistAnswer(correctAnswerDTO);

            question.answers = questionDTO.getIncorrectAnswers().stream()
                        .map(this::persistAnswer)
                        .collect(Collectors.toList());
            question.answers.add(question.correctAnswer);

            question.nextQuestion = previousQuestion;
            question.persist();

            numberOfQuestions++;
            previousQuestion = question;

        }

        contest.initialQuestion = previousQuestion;
        contest.persist();
        contest.numberOfQuestions = numberOfQuestions;

        return contest;
    }

    private Answer persistAnswer(AnswerDTO answerDTO) {
        Answer answer = answerDTO.toAnswer();
        answer.persist();
        return answer;
    }

    @POST
    @Path("/init/{contestId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response initContest(@PathParam("contestId") String contestId) {

        System.out.println("Initializing " + contestId);

        return Contest.findByContestId(contestId)
            .map(contest -> {
                state.startContest(contest);
                return Response.ok().build();
            })
            .orElseGet(() -> Response.status(Status.NOT_FOUND)
                            .entity("Contest Id" + contestId + "not found.").build()
            );
    }



    @POST
    @Path("/start/{contestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startContest(@PathParam("contestId") String contestId) {

        System.out.println("Starting " + contestId);

        Response response = state.getCurrentContest()
            .filter(c -> c.contestId.equals(contestId))
            .map(c -> {
                ServerSideEventMessage nextQuestion = task.sendNextQuestion();
                return Response.ok().entity(nextQuestion).build();
            })
            .orElseGet(() -> Response.status(Status.NOT_FOUND)
                            .entity("Contest Id" + contestId + "not found.").build());
        
        return response;
        
    }

    // Used for testing purposes we need to protect
    @GET
    @Path("/currentquestion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentQuestion() {
        return Response.ok().entity(ShowQuestionDTO.of(state.getCurrentQuestion().get())).build();
    }

    @GET
    @Path("/question/{contestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendQuestion(@PathParam("contestId") String contestId) {

        System.out.println("Sending next question for " + contestId);

        if (!validCurrentContest(contestId)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        if (state.isLastQuestion()) {
            final ServerSideEventMessage end = task.sendEndContest();
            return Response.ok().entity(end).build();
        }

        ServerSideEventMessage nextQuestion = task.sendNextQuestion();
        return Response.accepted().entity(nextQuestion).build();

    }


    @GET
    @Path("/question/answer/reveal/{contestId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response reveal(@PathParam("contestId") String contestId) {

        System.out.println("Revealing Answer " + contestId);

        if (!validCurrentContest(contestId)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        QuestionAnswerDTO revealAnswers = task.sendRevealAnswers();
        return Response.ok().entity(revealAnswers).build();
    }

    @GET
    @Path("/score")
    @Produces(MediaType.APPLICATION_JSON)
    public ScoreDTO getScore() {
        return new ScoreDTO(gamerManager.getUsernameScore());
    }
   
    @POST
    @Path("/end")
    public Response endContest() {
        return Response.ok().build();
    }

    private boolean validCurrentContest(String contestId) {
        final Contest currentContest = state.getCurrentContest().orElse(new Contest());
        return contestId.equals(currentContest.contestId);
    }
}