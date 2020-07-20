package com.redhat.developer.millionaire;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
public class AdminEndpoint {

    @Inject
    IdentifierGenerator identifierGenerator;

    @Inject
    ContestState state;

    @Inject
    ScoreInformation scoreInformation;

    @Inject
    QuestionsManager questionManager;

    @Inject
    GamerManager gamerManager;

    @Inject
    QuestionRoller task;

    @Inject
    UsernameGenerator nameGenerator;

    @Inject @Channel("new-question") Multi<String> streamOfQuestions;

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    public Multi<String> stream() {
        return streamOfQuestions;
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
        return Contest.findByContestId(contestId)
            .map(contest -> {
                state.startContest(contest);
                return Response.ok().build();
            })
            .orElseGet(() -> Response.status(Status.NOT_FOUND)
                            .entity("Contest Id" + contestId + "not found.").build()
            );
    }

    @GET
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser() {
        return state.getCurrentContest()
            .map(c -> {
                    String username = nameGenerator.getName();
                    String userId = identifierGenerator.generateId(username);

                    final Gamer user = new Gamer();
                    user.username = username;
                    user.userId = userId;
                    user.persist();
                    return Response.ok(AccessContestDTO.of(user, c)).build();
            })
            .orElseGet(() -> Response.status(Status.NOT_FOUND)
            .entity("No Running Contest").build());
    }

    @POST
    @Path("/start/{contestId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response startContest(@PathParam("contestId") String contestId) {
        Response response = state.getCurrentContest()
            .filter(c -> c.contestId.equals(contestId))
            .map(c -> {
                task.start();
                return Response.ok().build();
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

    // This will change to SSE or WebSockets 
    @GET
    @Path("/question/{contestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendQuestion(@PathParam("contestId") String contestId) {
        if (!validCurrentContest(contestId)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok().entity(ShowQuestionDTO.of(questionManager.nextQuestion())).build();

    }

    @POST
    @Path("/question/answer/{contestId}/{questionId}/{answer}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response answer(@PathParam("contestId") String contestId, 
                            @PathParam("questionId") String questionId, 
                            @PathParam("answer") Long answer,
                            @NotNull @HeaderParam("userId") String userId) {
        
        if (!validCurrentContest(contestId) || !validCurrentQuestion(questionId)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        final Optional<Answer> isCorrectAnswer = state.getCurrentQuestion()
            .map(q -> q.correctAnswer)
            .filter(a -> a.id.equals(answer));

        if (isCorrectAnswer.isPresent()) {
            scoreInformation.increment(userId);
            return Response.ok().build();
        } else {
            scoreInformation.fail(userId);
            return Response.status(Status.PRECONDITION_FAILED).build();
        }

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

    private boolean validCurrentQuestion(String questionId) {
        final Question currentQuestion = state.getCurrentQuestion().orElse(new Question());
        return questionId.equals(currentQuestion.questionId);
    }

    private boolean validCurrentContest(String contestId) {
        final Contest currentContest = state.getCurrentContest().orElse(new Contest());
        return contestId.equals(currentContest.contestId);
    }
}