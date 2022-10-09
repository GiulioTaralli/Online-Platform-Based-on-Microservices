package com.example.demo.controller;

import com.example.demo.dao.*;
import com.example.demo.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class Controller {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    LessonsRepo lsnRepo;
    @Autowired
    HistoryRepo hstRepo;
    @Autowired
    TeachersRepo tchRepo;
    @Autowired
    CoursesRepo crsRepo;
    @Autowired
    TeachingRepo thcngRepo;

    /* ACTION'S METHODS */

    /**
     * Method to book a lesson.
     * It creates a Lesson object to save into the active booking and the history table
     * Lastly, it calls the Notification microservice to notify the user
     * @param values a map of values used to create the tuples into the database
     * @return
     */
    @RequestMapping(value = "/bookingLesson", method = RequestMethod.POST)
    @ResponseBody
    public String bookLesson(@RequestBody Map<String, String> values) {
        int id_course = lsnRepo.getCourseID(values.get("subject"));
        String firstName = values.get("teacher").split(" ")[1];
        String lastName = values.get("teacher").split(" ")[2];
        String emailTeacher = lsnRepo.getEmailTeacher(firstName, lastName);

        Lesson lsn = new Lesson(values.get("day"), values.get("hour"), emailTeacher, values.get("emailStudent"), id_course);

        //insert the data of the booking into the active tutoring table
        lsnRepo.save(lsn);

        //insert the data of the booking into the history table
        hstRepo.save(new History(lsn.getDay(), lsn.getHour(), values.get("emailStudent"), emailTeacher, lsn.getIdCourse(), 0));

        //call the notification microservice that sends the email notification to the student
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(lsn,headers);
        restTemplate.exchange("http://localhost:8082/notifyBooking", HttpMethod.POST, entity, Object.class);
        return "true";
    }

    /**
     * Method that recovers the active bookings' user or users.
     * Depending on the role, a different list will be returned.
     * If it is an admin, it will be returned the active bookings of the all users,
     * otherwise, if it is a regular student, it will be returned the active bookings of that student
     * @param values a map of values used to identify the user's role
     * @return the active bookings' list of the user or users
     */
    @RequestMapping(value = "/getStudentTutoring", method = RequestMethod.POST)
    @ResponseBody
    public ArrayList<String> getStudentTutoring(@RequestBody Map<String, String> values) {
        if (values.get("role").equals("ROLE_USER"))
            return lsnRepo.getStudentTutoring(values.get("email"));
         else
            return lsnRepo.getAllTutorings();
    }

    //never used
    @RequestMapping(value="/deleteLesson", method= RequestMethod.POST)
    public void deleteLesson(@RequestBody Lesson lsn) {
        lsnRepo.delete(lsn);
    }

    /**
     * Method that recovers the available courses (called also subjects) into the system
     * @return the list of available courses
     */
    @RequestMapping(value = "/subjectAvailable", method = RequestMethod.GET)
    @ResponseBody
    public ArrayList<String> subjectAvailable() {
        return lsnRepo.subjectAvailable();
    }

    /**
     * Method that recovers the teachers into the system
     * The value to be returned depends on the content of the parameter
     * @param subject the name of the course, based on the content, all professors associated with that course will be returned
     *                if the value is "ALL" it will be return all the teachers into the system
     * @return the teachers' list
     */
    @RequestMapping(value = "/getTeachers", method = RequestMethod.POST)
    @ResponseBody
    public ArrayList<String> getTeachers(@RequestBody String subject) {
        if (!subject.equals("ALL"))
            return lsnRepo.getTeachers(lsnRepo.getCourseID(subject));
        else
            return lsnRepo.getAllTeacher();
    }

    @RequestMapping(value = "/deleteTeacher", method = RequestMethod.POST)
    @ResponseBody
    public void deleteTeacher(@RequestBody Map<String, String> values) {
        tchRepo.delete(new Teacher(values.get("email"), values.get("firstName"), values.get("lastName")));
    }

    @RequestMapping(value = "/confirmTutoring", method = RequestMethod.POST)
    @ResponseBody
    public String confirmTutoring(@RequestBody Map<String, String> values) {
        int idCourse = lsnRepo.getCourseID(values.get("subject"));
        String[] temp = values.get("teacher").split(" ");

        //delete from lesson table
        lsnRepo.deleteLesson(values.get("date"), values.get("hour"), temp[0], temp[1], values.get("student"), idCourse);

        //update from history table
        hstRepo.confirmLesson(values.get("date"), values.get("hour"), temp[0], temp[1], values.get("student"), idCourse, 1);

        //call the CFU microservice
        String emailTeacher = lsnRepo.getEmailTeacher(temp[0], temp[1]);
        Lesson lsn = new Lesson(values.get("date"), values.get("hour"), emailTeacher, values.get("student"), idCourse);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(lsn,headers);
        restTemplate.exchange("http://localhost:8083/confirmLesson", HttpMethod.POST, entity, Object.class);

        return "true";
    }

    @RequestMapping(value = "/deleteCourse", method = RequestMethod.POST)
    @ResponseBody
    public void deleteCourse(@RequestBody String subject) {
        crsRepo.delete(new Course(lsnRepo.getCourseID(subject), subject));
    }

    @RequestMapping(value = "/deleteTutoring", method = RequestMethod.POST)
    @ResponseBody
    public String deleteTutoring(@RequestBody Map<String, String> values) {
        //get idCourse
        int idCourse = lsnRepo.getCourseID(values.get("subject"));
        String[] temp = values.get("teacher").split(" ");
        String emailTeacher = lsnRepo.getEmailTeacher(temp[0], temp[1]);
        values.put("emailTeacher", emailTeacher);

        //delete from lesson table
        lsnRepo.deleteLesson(values.get("date"), values.get("hour"), temp[0], temp[1], values.get("student"), idCourse);

        //update from history table
        hstRepo.confirmLesson(values.get("date"), values.get("hour"), temp[0], temp[1], values.get("student"), idCourse, 2);

        //call the Notification microservice
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(values,headers);
        restTemplate.exchange("http://localhost:8082/deleteLesson", HttpMethod.POST, entity, Object.class);

        return "true";
    }

    @RequestMapping(value = "/insertTeacher", method = RequestMethod.POST)
    @ResponseBody
    public String insertTeacher(@RequestBody Map<String, String> values) {
        tchRepo.save(new Teacher(values.get("email"), values.get("firstName"), values.get("lastName")));
        return "true";
    }

    @RequestMapping(value = "/insertCourse", method = RequestMethod.POST)
    @ResponseBody
    public String insertCourse(@RequestBody String subject) {
        crsRepo.save(new Course(subject));
        return "true";
    }

    @RequestMapping(value = "/confirmAssociation", method = RequestMethod.POST)
    @ResponseBody
    public String confirmAssociation(@RequestBody Map<String, String> values) {
        int idCourse = lsnRepo.getCourseID(values.get("subject"));
        String emailTeacher = lsnRepo.getEmailTeacher(values.get("teacher").split(" ")[0], values.get("teacher").split(" ")[1]);
        thcngRepo.save(new Teaching(emailTeacher, idCourse));
        return "true";
    }

    @RequestMapping(value = "/deleteAssociation", method = RequestMethod.POST)
    @ResponseBody
    public void deleteAssociation(@RequestBody Map<String, String> values) {
        int idCourse = lsnRepo.getCourseID(values.get("subject"));
        thcngRepo.delete(new Teaching(values.get("email"), idCourse));
    }

    @RequestMapping(value = "/teacherAvailability", method = RequestMethod.POST)
    @ResponseBody
    public ArrayList<String> teacherAvailability(@RequestBody String subjectTeacher) {
        String[] temp = subjectTeacher.split(" ");
        //temp[0] -> subject, temp[2] -> first name, temp[3] -> last name
        return lsnRepo.getTutorings(temp[2], temp[3]);
    }

    @RequestMapping(value = "/getHistory", method = RequestMethod.POST)
    @ResponseBody
    public ArrayList<String> getHistory(@RequestBody Map<String, String> values) {
        if (values.get("role").equals("ROLE_USER"))
            return hstRepo.getStudentHistory(values.get("email"));
        else
            return hstRepo.getAllHistory();
    }

    @RequestMapping(value = "/getRestrictedHistory", method = RequestMethod.POST)
    @ResponseBody
    public ArrayList<String> getRestrictedHistory(@RequestBody Map<String, Integer> values) {
        return  hstRepo.getRestrictedHistory(values.get("status"));
    }
}
