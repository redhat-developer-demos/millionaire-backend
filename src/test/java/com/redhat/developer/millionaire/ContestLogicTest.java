package com.redhat.developer.millionaire;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import com.redhat.developer.millionaire.ShowQuestionDTO.ShowAnswerDTO;


@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class ContestLogicTest {

    private static String contestId;

    @Test
    @Order(1)
    public void shouldImportData() {
        InputStream contestData = ContestLogicTest.class.getResourceAsStream("/import.json");
        contestId = given()
          .contentType(ContentType.JSON)
          .body(contestData)
          .when().post("/admin/import")
          .andReturn()
          .asString();
        
          assertThat(contestId).isNotEmpty();
    }

    @Test
    @Order(2)
    public void shouldStartAGame() {
        given()
            .when()
            .post("/admin/start/{contestId}", contestId)
            .then()
            .statusCode(200);
    }

    @Test
    @Order(3)
    public void shouldGetQuestion() {
        final String firstQuestion = given()
            .when().get("/admin/question/{contestId}", contestId)
            .andReturn()
            .asString();

        final Jsonb jsonb = JsonbBuilder.create();
        final ShowQuestionDTO question = jsonb.fromJson(firstQuestion, ShowQuestionDTO.class);

        assertThat(question.questionId).isNotEmpty();
        assertThat(question.questionTitle).isEqualTo("What's the name of Java mascot?");
        assertThat(question.answers).isNotNull();

        final List<ShowAnswerDTO> answers = question.answers;
        
        assertThat(answers).hasSize(4);
        assertThat(answers)
            .extracting(ShowAnswerDTO::getPrefix)
            .containsExactly("A", "B", "C", "D");

        assertThat(answers)
            .extracting(ShowAnswerDTO::getAnswerDescription)
            .containsExactly("Tuki", "Duke", "Kuke", "Puki");

    }

    @Test
    @Order(4)
    public void shouldAnswerAQuestionCorrectly() {
        final String currentQuestion = given()
            .when().get("/admin/currentquestion")
            .andReturn()
            .asString();

        final Jsonb jsonb = JsonbBuilder.create();
        final ShowQuestionDTO question = jsonb.fromJson(currentQuestion, ShowQuestionDTO.class);

        final Optional<ShowAnswerDTO> correctAnswer = question.answers.stream().filter(a -> a.answerDescription.equals("Duke")).findFirst();

        given()
            .header("userId", "1111")
            .when()
            .post("/admin/question/answer/{contestId}/{questionId}/{answer}", 
                    contestId, question.questionId, correctAnswer.get().answerId)
            .then()
            .statusCode(200);

    }

    @Test
    @Order(4)
    public void shouldAnswerAQuestionIncorrectly() {
        final String currentQuestion = given()
            .when().get("/admin/currentquestion")
            .andReturn()
            .asString();

        final Jsonb jsonb = JsonbBuilder.create();
        final ShowQuestionDTO question = jsonb.fromJson(currentQuestion, ShowQuestionDTO.class);

        final Optional<ShowAnswerDTO> correctAnswer = question.answers.stream().filter(a -> a.answerDescription.equals("Kuke")).findFirst();

        given()
            .header("userId", "1111")
            .when()
            .post("/admin/question/answer/{contestId}/{questionId}/{answer}", 
                    contestId, question.questionId, correctAnswer.get().answerId)
            .then()
            .statusCode(412);

    }

    @Test
    @Order(5)
    public void shouldGetScore() {
        int score = given()
                        .when().get("/admin/score")
                        .path("1111");

        assertThat(score).isBetween(58, 60);

    }

}