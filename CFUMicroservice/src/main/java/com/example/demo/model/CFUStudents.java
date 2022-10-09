package com.example.demo.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CFUStudents {

    @Id
    private String emailStudent;
    private int currentCFU;
    private int confirmedLessons;

    public CFUStudents() {}

    public CFUStudents(String emailStudent, int currentCFU, int confirmedLessons) {
        this.emailStudent = emailStudent;
        this.currentCFU = currentCFU;
        this.confirmedLessons = confirmedLessons;
    }

    public String getEmailStudent() {
        return emailStudent;
    }

    public void setEmailStudent(String emailStudent) {
        this.emailStudent = emailStudent;
    }

    public int getCurrentCFU() {
        return currentCFU;
    }

    public void setCurrentCFU(int currentCFU) {
        this.currentCFU = currentCFU;
    }

    public int getConfirmedLessons() {
        return confirmedLessons;
    }

    public void setConfirmedLessons(int confirmedLessons) {
        this.confirmedLessons = confirmedLessons;
    }
}
