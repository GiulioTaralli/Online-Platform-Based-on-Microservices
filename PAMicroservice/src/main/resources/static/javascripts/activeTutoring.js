$(document).ready(function () {

    let app = new Vue ({
        el: '#activeTutoring',
        data: {
            tutorings:[]
        },
        mounted() {
            this.getRoleSession();
            this.getTutorings();
            if (this.tutorings.length === 0)
                alert("The tutoring list is empty");
        },
        methods:{
            getRoleSession:function(){
                let self1 = this;
                $.ajax({
                    url : "http://localhost:8084/getRole", // Url of backend (can be python, php, etc..)
                    type: "POST", // data type (can be get, post, put, delete)
                    //data : {submit: "getRoleSession"}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        switch (data) {
                            case 'guest':
                                self1.guest = true;
                                self1.student = false;
                                self1.admin = false;
                                break;
                            case 'ROLE_USER':
                                self1.guest = false;
                                self1.student = true;
                                self1.admin = false;
                                break;
                            case 'ROLE_ADMIN':
                                self1.guest = false;
                                self1.student = false;
                                self1.admin = true;
                                break;
                        }
                    }
                });
            },
            getTutorings:function(){
                let self = this;
                //obtain the array of tutorings
                $.ajax({
                    url : "http://localhost:8084/getStudentTutoring", // Url of backend (can be python, php, etc..)
                    type: "GET", // data type (can be get, post, put, delete)
                    data : {}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        self.tutorings = data;
                    }
                });
            },
            confirmTutoring:function (index) {
                let date = this.tutorings[index].date;
                let hour = this.tutorings[index].hour;
                let teacher = this.tutorings[index].teacher;
                let subject = this.tutorings[index].subject;
                let self = this;
                $.ajax({
                    url : "http://localhost:8084/confirmTutoring", // Url of backend (can be python, php, etc..)
                    type: "GET", // data type (can be get, post, put, delete)
                    data : {date: date, hour: hour, teacher: teacher, subject: subject}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        if (data === "true") {
                            alert ("Your booking has been confirmed successfully");
                            self.getTutorings();
                            return false;
                        }
                        else {
                            alert ("Error");
                            return false;
                        }
                    }
                });
            },
            cancelTutoring:function (index) {
                let date = this.tutorings[index].date;
                let hour = this.tutorings[index].hour;
                let teacher = this.tutorings[index].teacher;
                let subject = this.tutorings[index].subject;
                let self = this;
                if (this.admin) {
                    //delete booking by admin
                    let emailStudent = this.tutorings[index].student;
                    $.ajax({
                        url : "http://localhost:8084/deleteTutoringAdmin", // Url of backend (can be python, php, etc..)
                        type: "POST", // data type (can be get, post, put, delete)
                        data : {date: date, hour: hour, teacher: teacher, subject: subject, emailStudent: emailStudent}, // data in json format
                        async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                        success: function(data) {
                            if (data === "true") {
                                alert ("Booking has been cancelled successfully");
                                self.getTutorings();
                                return false;
                            }
                            else {
                                alert ("Error");
                                return false;
                            }
                        }
                    });
                } else {
                    //delete booking by user
                    $.ajax({
                        url : "http://localhost:8084/deleteTutoringStudent", // Url of backend (can be python, php, etc..)
                        type: "POST", // data type (can be get, post, put, delete)
                        data : {date: date, hour: hour, teacher: teacher, subject: subject}, // data in json format
                        async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                        success: function(data) {
                            if (data === "true") {
                                alert ("Your booking has been cancelled successfully");
                                self.getTutorings();
                            }
                            else {
                                alert ("Error");
                                return false;
                            }
                        }
                    });
                }
            }
        }
    });

    $("#sessionID").on('click', '#confirmButton', function () {
        let index = $(this).parent().index();
        app.confirmTutoring(index);
    });

    $("#sessionID").on('click', '#cancelButton', function () {
        let index = $(this).parent().index();
        app.cancelTutoring(index);
    });

    $("#sessionID").on('click', '#cancelButtonAdmin', function () {
        let index = $(this).parent().index();
        app.cancelTutoring(index);
    });

});