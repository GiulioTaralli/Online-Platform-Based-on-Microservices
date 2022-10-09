package com.example.demo.controller;

import com.example.demo.dto.StudentTutoring;
import com.example.demo.dto.Tutoring;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class AuthController {

    private UserService userService;
    private RestTemplate restTemplate = new RestTemplate();

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /*HANDLING PAGE REQUESTS*/
    // handler method to handle home page request
    @GetMapping("/index")
    public String home(){
        return "index";
    }

    @GetMapping("/book")
    public String booking() {
        return "booking";
    }

    @GetMapping("/history")
    public String history() {
        return "history";
    }

    @GetMapping("/activeTutoring")
    public String activeTutoring() {
        return "activeTutoring";
    }

    // handler method to handle login request
    @GetMapping("/activeTutorings")
    public String activeTutorings(){
        return "activeTutoring";
    }

    // handler method to handle login request
    @GetMapping("/login")
    public String login(){
        if (this.isAuthenticated())
            return "redirect:/index";
        else
            return "login";
    }

    // handler method to handle user registration form request
    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        if(this.isAuthenticated())
            return "redirect:/index";
        else {
            // create model object to store form data
            UserDto user = new UserDto();
            model.addAttribute("user", user);
            return "register";
        }
    }

    // handler method to handle list of users
    /*@GetMapping("/users")
    public String users(Model model){
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);

        //block of code to get the session's user (use this to pass email and role to another microservice)
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails)principal).getUsername();
            System.out.println(username);
            Collection<? extends GrantedAuthority> roles = ((UserDetails)principal).getAuthorities();
            System.out.println(roles.toString());

        } else {
            String username = principal.toString();
            System.out.println(username);
        }

        return "users";
    }*/

    @GetMapping("/admin")
    public String admin(){
        return "admin";
    }

    /*BACKEND METHODS*/

    @RequestMapping(value = "/getUsername", method = RequestMethod.POST)
    @ResponseBody
    public String getUsername() {
        return "Welcome " + getEmailSession();
    }

    @RequestMapping(value = "/getRole", method = RequestMethod.POST)
    @ResponseBody
    public String getRole() {
        return getRoleSession();
    }

    // handler method to handle user registration form submit request
    @PostMapping("/register/save")
    public String registration(@ModelAttribute("user") UserDto userDto,
                                          BindingResult result,
                                          Model model){
        User existingUser = userService.findUserByEmail(userDto.getEmail());

        if (existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()) {
            result.rejectValue("email", null,
                    "There is already an account registered with the same email");
        }

        if (result.hasErrors()){
            model.addAttribute("user", userDto);
            return "/register";
        }

        userService.saveUser(userDto);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(userDto,headers);

        //call the CFU microservice to create the tuple for the new student
        restTemplate.exchange("http://localhost:8083/createCFU", HttpMethod.POST, entity, Object.class);

        //call the Notification microservice that notifies the user
        restTemplate.exchange("http://localhost:8082/notifyRegistration", HttpMethod.POST, entity, Object.class);

        return "redirect:/index";
    }

    @RequestMapping(value = "/getStudentTutoring", method = RequestMethod.GET)
    @ResponseBody
    public ArrayList<StudentTutoring> getStudentTutoring() {
        Map<String, String> values = new HashMap<>();
        values.put("email", getEmailSession());
        values.put("role", getRoleSession());

        ResponseEntity<Object> result = callAnotherMicroservice("http://localhost:8081/getStudentTutoring", values);

        ArrayList<String> lst = (ArrayList<String>) result.getBody();
        ArrayList<StudentTutoring> res = new ArrayList<>();
        String[] temp;
        if (values.get("role").equals("ROLE_USER")) {
            for (int i=0; i<lst.size(); i++) {
                temp = lst.get(i).split(",");
                /* temp[0] --> subject, temp[1] --> firstName,
                   temp[2] --> lastName, temp[3] --> day, temp[4] --> hour */
                res.add(new StudentTutoring(temp[3], temp[4], temp[0], temp[1] + " " + temp[2]));
            }
        } else {
            for (int i=0; i<lst.size(); i++) {
                temp = lst.get(i).split(",");
                /* temp[0] --> subject, temp[1] --> firstName,
                   temp[2] --> lastName, temp[3] --> day,
                   temp[4] --> hour, temp[5] --> emailStudent */
                res.add(new StudentTutoring(temp[3], temp[4], temp[0], temp[1] + " " + temp[2], temp[5]));
            }
        }
        return res;
    }

    @RequestMapping(value = "/subjectAvailable", method = RequestMethod.GET)
    @ResponseBody
    public ArrayList<String> subjectAvailable() {
        ResponseEntity<Object> result;
        result = restTemplate.exchange("http://localhost:8081/subjectAvailable", HttpMethod.GET, null, Object.class);
        return (ArrayList<String>) result.getBody();
    }

    @RequestMapping(value = "/confirmTutoring", method = RequestMethod.GET)
    @ResponseBody
    public String confirmTutoring(String date, String hour, String teacher, String subject) {
        Map<String, String> values = new HashMap<>();
        values.put("date", date);
        values.put("hour", hour);
        values.put("teacher", teacher);
        values.put("subject", subject);
        values.put("student", getEmailSession());

        ResponseEntity<Object> result = callAnotherMicroservice("http://localhost:8081/confirmTutoring", values);

        return result.getBody().toString();
    }

    @RequestMapping(value = "/deleteTutoringStudent", method = RequestMethod.POST)
    @ResponseBody
    public String deleteTutoringStudent(String date, String hour, String teacher, String subject) {
        Map<String, String> values = new HashMap<>();
        values.put("date", date);
        values.put("hour", hour);
        values.put("teacher", teacher);
        values.put("subject", subject);
        values.put("student", getEmailSession());
        values.put("role", "user");

        ResponseEntity<Object> result = callAnotherMicroservice("http://localhost:8081/deleteTutoring", values);

        return result.getBody().toString();
    }

    @RequestMapping(value = "/deleteTutoringAdmin", method = RequestMethod.POST)
    @ResponseBody
    public String deleteTutoringAdmin(String date, String hour, String teacher, String subject, String emailStudent) {
        Map<String, String> values = new HashMap<>();
        values.put("date", date);
        values.put("hour", hour);
        values.put("teacher", teacher);
        values.put("subject", subject);
        values.put("student", emailStudent);
        values.put("role", "admin");

        ResponseEntity<Object> result = callAnotherMicroservice("http://localhost:8081/deleteTutoring", values);

        return result.getBody().toString();
    }

    @RequestMapping(value = "/getHistory", method = RequestMethod.POST)
    @ResponseBody
    public ArrayList<StudentTutoring> getHistory() {
        Map<String, String> values = new HashMap<>();
        ArrayList<StudentTutoring> res = new ArrayList<>();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails)principal).getUsername();
            values.put("email", email);
            values.put("role", getRoleSession());

            //call the booking microservice
            ResponseEntity<Object> result = callAnotherMicroservice("http://localhost:8081/getHistory", values);
            ArrayList<String> lst = (ArrayList<String>) result.getBody();

            String[] temp;
            for (int i=0; i<lst.size(); i++) {
                temp = lst.get(i).split(",");
                if (values.get("role").equals("ROLE_USER"))
                    res.add(new StudentTutoring(temp[3], temp[4], temp[0], temp[1] + " " + temp[2], Integer.parseInt(temp[5])));
                else
                    res.add(new StudentTutoring(temp[4], temp[5], temp[0], temp[1] + " " + temp[2], temp[3], Integer.parseInt(temp[6])));
            }
        }
        return res;
    }

    @RequestMapping(value = "/getRestrictedHistory", method = RequestMethod.POST)
    @ResponseBody
    public ArrayList<StudentTutoring> getRestrictedHistory(int status) {
        ArrayList<StudentTutoring> res = new ArrayList<>();
        Map<String, Integer> values = new HashMap<>();
        values.put("status", status);

        //call the booking microservice
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(values,headers);
        ResponseEntity<Object> result = restTemplate.exchange("http://localhost:8081/getRestrictedHistory", HttpMethod.POST, entity, Object.class);
        ArrayList<String> lst = (ArrayList<String>) result.getBody();

        String[] temp;
        for (int i=0; i<lst.size(); i++) {
            temp = lst.get(i).split(",");
            res.add(new StudentTutoring(temp[4], temp[5], temp[0], temp[1] + " " + temp[2], temp[3], Integer.parseInt(temp[6])));
        }

        return res;
    }

    @RequestMapping(value = "/getTeachers", method = RequestMethod.GET)
    @ResponseBody
    public ArrayList<String> getTeachers(String subject, String email) {
        ResponseEntity<Object> result;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(subject,headers);
        result = restTemplate.exchange("http://localhost:8081/getTeachers", HttpMethod.POST, entity, Object.class);
        ArrayList<String> lst = (ArrayList<String>) result.getBody();

        return generateTeachersFormat(lst, email);
    }

    @RequestMapping(value = "/getAllTeachers", method = RequestMethod.GET)
    @ResponseBody
    public ArrayList<String> getAllTeachers(String email) {
        ResponseEntity<Object> result;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>("ALL",headers);
        result = restTemplate.exchange("http://localhost:8081/getTeachers", HttpMethod.POST, entity, Object.class);
        ArrayList<String> lst = (ArrayList<String>) result.getBody();

        return generateTeachersFormat(lst, email);
    }

    @RequestMapping(value = "/deleteTeacher", method = RequestMethod.POST)
    @ResponseBody
    public void deleteTeacher(String firstName, String lastName, String email) {
        Map<String, String> values = new HashMap<>();
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        values.put("email", email);
        callAnotherMicroservice("http://localhost:8081/deleteTeacher", values);
    }

    @RequestMapping(value = "/deleteCourse", method = RequestMethod.GET)
    @ResponseBody
    public void deleteCourse(String subject) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(subject,headers);
        restTemplate.exchange("http://localhost:8081/deleteCourse", HttpMethod.POST, entity, Object.class);
    }

    @RequestMapping(value = "/insertTeacher", method = RequestMethod.POST)
    @ResponseBody
    public String insertTeacher(String firstName, String lastName, String email) {
        Map<String, String> values = new HashMap<>();
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        values.put("email", email);
        ResponseEntity<Object> result = callAnotherMicroservice("http://localhost:8081/insertTeacher", values);
        if (result.getBody().toString().equals("true"))
            return "true";
        else
            return "false";
    }

    @RequestMapping(value = "/confirmAssociation", method = RequestMethod.POST)
    @ResponseBody
    public String confirmAssociation(String teacher, String subject) {
        Map<String, String> values = new HashMap<>();
        values.put("teacher", teacher);
        values.put("subject", subject);
        ResponseEntity<Object> result = callAnotherMicroservice("http://localhost:8081/confirmAssociation", values);
        if (result.getBody().toString().equals("true"))
            return "true";
        else
            return "false";
    }

    @RequestMapping(value = "/deleteAssociation", method = RequestMethod.POST)
    @ResponseBody
    public void deleteAssociation(String email, String subject) {
        Map<String, String> values = new HashMap<>();
        values.put("email", email);
        values.put("subject", subject);
        callAnotherMicroservice("http://localhost:8081/deleteAssociation", values);
    }

    @RequestMapping(value = "/insertCourse", method = RequestMethod.GET)
    @ResponseBody
    public String insertCourse(String subject) {
        ResponseEntity<Object> result;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(subject,headers);
        result = restTemplate.exchange("http://localhost:8081/insertCourse", HttpMethod.POST, entity, Object.class);
        if (result.getBody().toString().equals("true"))
            return "true";
        else
            return "false";
    }

    @RequestMapping(value = "/teacherAvailability", method = RequestMethod.GET)
    @ResponseBody
    public ArrayList<Tutoring> teacherAvailability(String subject, String teacher) {
        ResponseEntity<Object> result;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(subject + " " + teacher,headers);
        result = restTemplate.exchange("http://localhost:8081/teacherAvailability", HttpMethod.POST, entity, Object.class);
        ArrayList<String> lst = (ArrayList<String>) result.getBody();

        ArrayList<Tutoring> res = new ArrayList<>();
        String[] temp;
        for(int i=0; i < lst.size(); i++) {
            temp = lst.get(i).split(",");
            res.add(new Tutoring(temp[0], temp[1]));
        }
        return res;
    }

    @RequestMapping(value = "/insertBooking", method = RequestMethod.GET)
    @ResponseBody
    public String insertBooking(String subject, String teacher, String day, String hour) {
        //block of code to get the session's user (use this to pass email and role to another microservice)
        Map<String, String> values = new HashMap<>();
        values.put("subject", subject);
        values.put("teacher", teacher);
        values.put("day", day);
        values.put("hour", hour);
        values.put("emailStudent", getEmailSession());

        ResponseEntity<Object> result = callAnotherMicroservice("http://localhost:8081/bookingLesson", values);
        return result.getBody().toString();
    }

    /* PRIVATE METHODS */
    /**
     * It checks if the Student/Admin has already been authenticated
     * @return true if it is authenticated, false otherwise
     */
    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * It generates an interpretable output of the teachers' list for the frontend
     * @param lst the list of teacher to format
     * @param email informs if we want the teacher's email in the output
     * @return the formatted teachers' list
     */
    private ArrayList<String> generateTeachersFormat(ArrayList<String> lst, String email) {
        ArrayList<String> res = new ArrayList<>();
        String[] temp;
        for (String s : lst) {
            temp = s.split(",");
            if (email.equals("false"))
                res.add(temp[0] + " " + temp[1]);
            else
                res.add(temp[0] + " " + temp[1] + " " + temp[2]);
        }
        return res;
    }

    /**
     * Private method that calls another microservice
     * @param url the url of the microservice to call
     * @param values a map of values to pass to the microservice
     * @return the response of the microservice (if it has one)
     */
    private ResponseEntity<Object> callAnotherMicroservice(String url, Map<String, String> values) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<Object>(values,headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
    }

    /**
     * Private method to obtain the user's email
     * @return the user's email, or "guest" if the user is not authenticated yet
     */
    private String getEmailSession() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails)
            return ((UserDetails)principal).getUsername();
        else
            return "guest";
    }

    /**
     * Private method to obtain the user's role
     * @return the user's role, or "guest" if the user is not authenticated yet
     */
    private String getRoleSession() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            Collection<? extends GrantedAuthority> roles = ((UserDetails)principal).getAuthorities();
            return (roles.toArray()[0]).toString();
        } else
            return "guest";
    }
}
