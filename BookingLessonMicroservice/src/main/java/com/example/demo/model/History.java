package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_history;
    private String date;
    private String hour;
    private String email_student;
    private String email_teacher;
    private int id_course;
    private int state;

    public History() {}

    public History(String date, String hour, String email_student, String email_teacher, int id_course, int state) {
        this.date = date;
        this.hour = hour;
        this.email_student = email_student;
        this.email_teacher = email_teacher;
        this.id_course = id_course;
        this.state = state;
    }
}
