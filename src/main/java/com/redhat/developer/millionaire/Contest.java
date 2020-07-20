package com.redhat.developer.millionaire;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(indexes = {
    @Index(name = "contest_id_index", columnList = "contestId")
})
public class Contest extends PanacheEntity {
    
    @Column(name = "contestId", nullable = false, unique = true)
    public String contestId;

    @Column(nullable = false)
    public String name;

    @Column
    public Date creationTime;

    @OneToOne
    @JoinColumn(name = "fk_initialquestion")
    public Question initialQuestion;

    @Column
    public int numberOfQuestions;

    @Column
    Duration timeBetweenQuestions = Duration.ofMinutes(1);

    public static Optional<Contest> findByContestId(String contestId) {
        return Contest.find("contestId", contestId).singleResultOptional();
    }

}