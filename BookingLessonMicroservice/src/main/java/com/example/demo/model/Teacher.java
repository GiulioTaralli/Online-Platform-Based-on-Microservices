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
public class Teacher {

    @Id
    private String email_teacher;
    private String first_name;
    private String last_name;

    public Teacher() {}

    public Teacher(String email_teacher, String first_name, String last_name) {
        this.email_teacher = email_teacher;
        this.first_name = first_name;
        this.last_name = last_name;
    }
}
