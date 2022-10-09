package com.example.demo.controller;

import com.example.demo.dao.CFURepo;
import com.example.demo.model.CFUStudents;
import com.example.demo.model.Lesson;
import com.example.demo.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CFUController {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    CFURepo cfuRepo;

    /**
     * Method to confirm a lesson as a completed.
     * It increments the number of the confirmed lesson by one
     * If the user completed 5 new lessons, it will be incremented the number of gained CFU by one
     * Lastly, it calls the Notification microservice to notify the users
     * @param lsn the confirmed lesson
     */
    @RequestMapping(value="/confirmLesson", method= RequestMethod.POST)
    public void confirmLesson(@RequestBody Lesson lsn) {
        String cfuIncrement = "false";
        CFUStudents cfuStudent = cfuRepo.findByEmailStudent(lsn.getEmailStudent());
        int cfu = cfuStudent.getCurrentCFU();
        cfuStudent.setConfirmedLessons(cfuStudent.getConfirmedLessons() + 1);

        int res = cfuStudent.getConfirmedLessons() / 5;
        if(res > cfu) {
            cfuStudent.setCurrentCFU(cfu + 1);
            cfuIncrement = "true";
        }
        cfuRepo.updateStudent(cfuStudent.getCurrentCFU(), cfuStudent.getConfirmedLessons(), cfuStudent.getEmailStudent());

        Map<String, String> values = new HashMap<>();
        values.put("emailStudent", lsn.getEmailStudent());
        values.put("emailTeacher", lsn.getEmailTeacher());
        values.put("currentLessons", "" + cfuStudent.getConfirmedLessons());
        values.put("totCFU", "" + cfuStudent.getCurrentCFU());
        values.put("cfuIncrement", cfuIncrement);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(values,headers);
        restTemplate.exchange("http://localhost:8082/confirmLesson", HttpMethod.POST, entity, Object.class);
    }

    /**
     * Method that creates a new tuple into the database of the new user.
     * It will contain the number of confirmed lessons and the gained CFU
     * Both of them will be initially initialized to zero
     * @param user the new user
     */
    @RequestMapping(value="/createCFU", method= RequestMethod.POST)
    public void createCFUStudent(@RequestBody UserDto user) {
        CFUStudents cfuStud = new CFUStudents(user.getEmail(), 0 ,0);
        cfuRepo.save(cfuStud);
    }
}
