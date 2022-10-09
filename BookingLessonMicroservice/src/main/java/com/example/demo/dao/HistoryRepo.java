package com.example.demo.dao;

import com.example.demo.model.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface HistoryRepo extends JpaRepository<History, Integer> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value="update history set state=?7 where date=?1 AND hour=?2 AND email_teacher=(SELECT email_teacher FROM teacher WHERE first_name=?3 AND last_name=?4) AND email_student=?5 AND id_course=?6",
            nativeQuery = true
    )
    void confirmLesson(String date, String hour, String firstName, String lastName, String emailStudent, int idCourse, int state);

    @Query(
            value = "SELECT c.name_course, t.first_name, t.last_name, h.date, h.hour, h.state " +
                    "FROM history as h JOIN course as c on (h.id_course=c.id_course) JOIN teacher as t on (h.email_teacher=t.email_teacher) " +
                    "WHERE h.email_student=?1",
            nativeQuery = true
    )
    ArrayList<String> getStudentHistory(String emailStudent);

    @Query(
            value = "SELECT c.name_course, t.first_name, t.last_name, h.email_student, h.date, h.hour, h.state " +
                    "FROM history as h JOIN course as c on (h.id_course=c.id_course) JOIN teacher as t on (h.email_teacher=t.email_teacher)",
            nativeQuery = true
    )
    ArrayList<String> getAllHistory();

    @Query(
            value = "SELECT c.name_course, t.first_name, t.last_name, h.email_student, h.date, h.hour, h.state " +
                    "FROM history as h JOIN course as c on (h.id_course=c.id_course) JOIN teacher as t on (h.email_teacher=t.email_teacher) " +
                    "WHERE h.state=?1",
            nativeQuery = true
    )
    ArrayList<String> getRestrictedHistory(int status);
}
