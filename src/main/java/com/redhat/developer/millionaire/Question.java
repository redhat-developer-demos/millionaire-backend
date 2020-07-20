package com.redhat.developer.millionaire;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(indexes = {
    @Index(name = "question_id_index", columnList = "questionId")
})
public class Question extends PanacheEntity {
    
    @Column(name = "questionId", nullable = false, unique = true)
    public String questionId;

    @Column(nullable = false)
    public String title;

    @OneToOne
    @JoinColumn(name = "fk_next_question")
    public Question nextQuestion;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "answer_id")
    @OrderBy("prefix ASC")
    public List<Answer> answers = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "fk_correct_answer")
    public Answer correctAnswer;

    public Question getNextQuestion() {
        return nextQuestion;
    }

}