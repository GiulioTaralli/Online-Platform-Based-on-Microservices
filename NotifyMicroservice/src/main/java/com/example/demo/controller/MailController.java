package com.example.demo.controller;

import com.example.demo.EmailSenderService;
import com.example.demo.model.Lesson;
import com.example.demo.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class MailController {

    @Autowired
    private EmailSenderService senderService;

    /**
     * Notifies the student and the teacher of the correct booking of the lesson by email
     * @param lsn the confirmed lesson
     */
    @RequestMapping(value="/notifyBooking", method=RequestMethod.POST)
    public void notificationBooking(@RequestBody Lesson lsn) {
        String subject = "Your booking repetition has been confirmed";
        String text = "Your repetition is planned for the day: " + lsn.getDay() + " at: " + lsn.getHour() +
                " with the professor: " + lsn.getEmailTeacher();
        senderService.sendEmail(lsn.getEmailStudent(), subject, text);

        subject = "You have a new repetition";
        text = lsn.getEmailStudent() + " booked a repetition with you the day: " + lsn.getDay() + " at: "
                + lsn.getHour();
        senderService.sendEmail(lsn.getEmailTeacher(), subject, text);
    }

    /**
     * Notifies the student of the correct registration by email
     * @param user the registered user in the system
     */
    @RequestMapping(value="/notifyRegistration", method=RequestMethod.POST)
    public void notificationRegistration(@RequestBody UserDto user) {
        String  subject = "Welcome to the Lessons Platform!";
        String text = user.getFirstName() + " " + user.getLastName() +  " thank you for registering to the platform. \n" +
                "You can access all the services dedicated to the student.";
        senderService.sendEmail(user.getEmail(), subject, text);
    }

    /**
     * Notifies the student and the teacher of the correct confirmation of the lesson
     * @param values a map of values used to create the email
     */
    @RequestMapping(value="/confirmLesson", method=RequestMethod.POST)
    public void confirmLesson(@RequestBody Map<String, String> values) {
        String subject = "The lesson has been confirmed successfully";
        String text = "Your repetition with the professor " + values.get("emailTeacher") + " has been confirmed.\n";
        if (values.get("cfuIncrement").equals("true"))
            text += "You have earned 1 CFU for completing 5 lessons.\n" +
                    "Total confirmed lessons: " + values.get("currentLessons") + "\n" +
                    "Total CFU: " + values.get("totCFU") + "\n\n";
        text += "Thank you for using our service \n\n The University of Turin ";
        senderService.sendEmail(values.get("emailStudent"), subject, text);

        text = "The repetition with the student: " + values.get("emailStudent") + " has been confirmed. \n" +
                "Thank you for using our service \n\n The University of Turin";
        senderService.sendEmail(values.get("emailTeacher"), subject, text);
    }

    /**
     * Notifies the student and the teacher of the correct elimination of the lesson
     * @param values a map of values used to create the email
     */
    @RequestMapping(value="/deleteLesson", method=RequestMethod.POST)
    public void deleteLesson(@RequestBody Map<String, String> values) {
        String subject = "The lesson has been deleted";
        String text = "The repetition with the teacher: " + values.get("teacher") + " has been deleted.";
        senderService.sendEmail(values.get("student"), subject, text);

        text = "The repetition with the student: " + values.get("student") + " has been deleted.";
        senderService.sendEmail(values.get("emailTeacher"), subject, text);
    }
}
