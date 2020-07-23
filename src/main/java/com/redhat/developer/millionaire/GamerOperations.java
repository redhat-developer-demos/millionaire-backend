package com.redhat.developer.millionaire;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;

import io.smallrye.mutiny.Multi;

@Path("/user")
@Transactional
public class GamerOperations {
    
    @Inject
    ContestState state;

    @Inject
    UsernameGenerator nameGenerator;

    @Inject
    IdentifierGenerator identifierGenerator;

    @Inject
    Statistics statistics;

    @Inject
    ScoreInformation scoreInformation;

    @Inject @Channel("new-question") Multi<String> streamOfQuestions;

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    public Multi<String> stream() {
        return streamOfQuestions;
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
    @Path("/question/answer/{contestId}/{questionId}/{answer}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response answer(@PathParam("contestId") String contestId, 
                            @PathParam("questionId") String questionId, 
                            @PathParam("answer") Long answerId,
                            @NotNull @HeaderParam("userId") String userId) {
        

        // Check between margins of correct time 
        if (!validCurrentContest(contestId) || !validCurrentQuestion(questionId)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        if (!isInsideTimeframe()) {
            return Response.status(Status.PRECONDITION_FAILED)
                .entity("Answering a tiemout question").build();
        }

        updateStats(questionId, answerId);
        setQuestionAnsweredyUser(userId);

        final Optional<Answer> isCorrectAnswer = state.getCurrentQuestion()
            .map(q -> q.correctAnswer)
            .filter(a -> a.id.equals(answerId));

        if (isCorrectAnswer.isPresent()) {
            scoreInformation.increment(userId);
            return Response.ok().build();
        } else {
            scoreInformation.fail(userId);
            return Response.status(Status.PRECONDITION_FAILED).build();
        }

    }

    private void updateStats(String questionId, Long answerId) {
        final List<Answer> answers = state.getCurrentQuestion().get().answers;
        for (Answer answer : answers) {
            if (answer.id.equals(answerId)) {
                statistics.incr(questionId, answer.prefix);
                break;
            }
        }
    }

    private void setQuestionAnsweredyUser(String userId) {
        Gamer gamer = Gamer.findGamerByUserId(userId);
        Question question = state.getCurrentQuestion().get();
        gamer.answeredQuestions.add(question);
        gamer.persist();
    }

    private boolean validCurrentContest(String contestId) {
        final Contest currentContest = state.getCurrentContest().orElse(new Contest());
        return contestId.equals(currentContest.contestId);
    }

    private boolean validCurrentQuestion(String questionId) {
        final Question currentQuestion = state.getCurrentQuestion().orElse(new Question());
        return questionId.equals(currentQuestion.questionId);
    }

    private boolean isInsideTimeframe() {
        final Instant endTimeForAnswering = state.getQuestionTime()
                .plus(state.getCurrentContest().get().timeBetweenQuestions);

        return Instant.now().isBefore(endTimeForAnswering);
    }

}