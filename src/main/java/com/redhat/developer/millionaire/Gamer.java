package com.redhat.developer.millionaire;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.UniqueConstraint;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Gamer extends PanacheEntity {
    
    @Column(unique = true) public String username;
    @Column public String userId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "gamer_questions",
           joinColumns = { @JoinColumn(name = "fk_gamer")},
           inverseJoinColumns = { @JoinColumn(name = "fk_question")},
           uniqueConstraints = @UniqueConstraint(columnNames = { "fk_gamer", "fk_question" }))
    public Set<Question> answeredQuestions = new HashSet<>();

    public static Gamer findGamerByUserId(String userId) {
        return Gamer.find("userId", userId).singleResult();
    }

}