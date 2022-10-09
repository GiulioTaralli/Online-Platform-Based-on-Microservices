package com.example.demo.model;

public class Lesson {
    private int id;
    private String day;
    private String hour;
    private String emailTeacher;
    private String emailStudent;
    private int idDesk;
    private int idCourse;

    public Lesson() {}

    public Lesson(String day, String hour, String emailTeacher, String emailStudent, int idDesk, int idCourse) {
        super();
        this.day = day;
        this.hour = hour;
        this.emailTeacher = emailTeacher;
        this.emailStudent = emailStudent;
        this.idDesk = idDesk;
        this.idCourse = idCourse;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getEmailTeacher() {
        return emailTeacher;
    }

    public void setEmailTeacher(String emailTeacher) {
        this.emailTeacher = emailTeacher;
    }

    public String getEmailStudent() {
        return emailStudent;
    }

    public void setEmailStudent(String emailStudent) {
        this.emailStudent = emailStudent;
    }

    public int getIdDesk() {
        return idDesk;
    }

    public void setIdDesk(int idDesk) {
        this.idDesk = idDesk;
    }

    public int getIdCourse() {
        return idCourse;
    }

    public void setIdCourse(int idCourse) {
        this.idCourse = idCourse;
    }

}
