package com.example.demo.dao;

import com.example.demo.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface LessonsRepo extends JpaRepository<Lesson, Integer> {

    @Query(
            value = "SELECT name_course FROM course WHERE TRUE",
            nativeQuery = true
    )
    ArrayList<String> subjectAvailable();

    @Query(
            value = "SELECT id_course FROM course WHERE name_course = ?1",
            nativeQuery = true
    )
    int getCourseID(String subject);

    @Query(
            value = "SELECT ter.first_name, ter.last_name, ter.email_teacher " +
                    "FROM teaching as ting JOIN teacher as ter " +
                    "WHERE ting.id_course = ?1 AND ting.email_teacher = ter.email_teacher;",
            nativeQuery = true
    )
    ArrayList<String> getTeachers(int courseID);

    @Query(
            value = "SELECT ter.first_name, ter.last_name, ter.email_teacher " +
                    "FROM teacher as ter " +
                    "WHERE TRUE",
            nativeQuery = true
    )
    ArrayList<String> getAllTeacher();

    @Query(
            value = "SELECT l.day, l.hour " +
                    "FROM course as c JOIN lesson as l on (c.id_course=l.id_course) JOIN teacher as t on (l.email_teacher=t.email_teacher) " +
                    "WHERE l.email_teacher=(SELECT email_teacher FROM teacher WHERE first_name=?1 AND last_name=?2);",
            nativeQuery = true
    )
    ArrayList<String> getTutorings(/*String subject,*/ String firstName, String lastName);
    //(SELECT id_course FROM course WHERE name_course = ?1) = l.id_course AND

    @Query(
            value = "SELECT email_teacher " +
                    "FROM teacher " +
                    "WHERE first_name=?1 AND last_name=?2",
            nativeQuery = true
    )
    String getEmailTeacher(String firstName, String lastName);

    @Query(
            value = "SELECT c.name_course, t.first_name, t.last_name, l.day, l.hour " +
                    "FROM course as c JOIN lesson as l on (c.id_course=l.id_course) JOIN teacher as t on (l.email_teacher=t.email_teacher) " +
                    "WHERE l.email_student =?1",
            nativeQuery = true
    )
    ArrayList<String> getStudentTutoring(String email);

    @Query(
            value = "SELECT c.name_course, t.first_name, t.last_name, l.day, l.hour, l.email_student " +
                    "FROM course as c JOIN lesson as l on (c.id_course=l.id_course) JOIN teacher as t on (l.email_teacher=t.email_teacher)",
            nativeQuery = true
    )
    ArrayList<String> getAllTutorings();

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM lesson WHERE day=?1 AND hour=?2 AND email_teacher=(SELECT email_teacher FROM teacher WHERE first_name=?3 AND last_name=?4) AND email_student=?5 AND id_course=?6",
            nativeQuery = true)
    void deleteLesson(String date, String hour, String firstName, String lastName, String emailStudent, int idCourse);

}

