package com.example.demo.dto;

import lombok.Getter;

@Getter
public class StudentTutoring extends Tutoring{
    private String subject;
    private String teacher;
    private String student;
    private int status;

    //costruttore generale per ottenere le prenotazioni disponibili lato student
    public StudentTutoring (String date, String hour, String subject, String teacher) {
        super(date, hour);
        this.subject = subject;
        this.teacher = teacher;
    }

    //costruttore generale per ottenere le prenotazioni disponibili lato admin
    public StudentTutoring (String date, String hour, String subject, String teacher, String student) {
        super(date, hour);
        this.subject = subject;
        this.teacher = teacher;
        this.student = student;
    }

    //costruttore visualizzazione storico lato student
    public StudentTutoring (String date, String hour, String subject, String teacher, int status) {
        super(date, hour);
        this.subject = subject;
        this.teacher = teacher;
        this.status = status;
    }

    //costruttore visualizzazione storico lato admin
    public StudentTutoring (String date, String hour, String subject, String teacher, String student, int status) {
        super(date, hour);
        this.subject = subject;
        this.teacher = teacher;
        this.student = student;
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getStudent() {
        return student;
    }
}
