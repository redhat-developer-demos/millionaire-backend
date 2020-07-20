package com.redhat.developer.millionaire;

import javax.persistence.Column;
import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Answer extends PanacheEntity {
    
    @Column(name = "prefix", nullable = false)
    public String prefix;

    @Column
    public String description;

}