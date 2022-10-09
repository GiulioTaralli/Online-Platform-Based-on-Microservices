$(document).ready(function () {

    let app = new Vue ({
        el: '#admin',
        data: {
            guest: false,
            student: false,
            admin: true,
            subjects:[],
            teachers:[],
        },
        mounted() {
            this.subjectAvailable();
        },
        methods:{
            subjectAvailable:function() {
                let self = this;
                $.ajax({
                    url : "http://localhost:8084/subjectAvailable", // Url of backend (can be python, php, etc..)
                    type: "GET", // data type (can be get, post, put, delete)
                    data : {}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        self.subjects = data;
                    }
                });
            },
            getRestrictedTeachers:function (subject, email) {
                let self = this;
                if (subject === "All") {
                    $.ajax({
                        url : "http://localhost:8084/getAllTeachers", // Url of backend (can be python, php, etc..)
                        type: "GET", // data type (can be get, post, put, delete)
                        data : {email: email}, // data in json format
                        async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                        success: function(data) {
                            self.teachers = data;
                        }
                    });
                } else {
                    $.ajax({
                        url : "http://localhost:8084/getTeachers", // Url of backend (can be python, php, etc..)
                        type: "GET", // data type (can be get, post, put, delete)
                        data : {subject: subject.replace(/\s/g, ''), email: "true"}, // data in json format
                        async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                        success: function(data) {
                            self.teachers = data;
                        }
                    });
                }
            },
            deleteAssociation:function (index, subject) {
                let teacherElem = this.teachers[index];
                let elem = teacherElem.split(" ");
                $.ajax({
                    url : "http://localhost:8084/deleteAssociation", // Url of backend (can be python, php, etc..)
                    type: "POST", // data type (can be get, post, put, delete)
                    data : {email: elem[2], subject: subject}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function() {
                        alert("Association " + elem[2] + " - " + subject + " correctly deleted");
                        $('.collapse').collapse('hide');
                        $('#subjectSelected4').prop('selectedIndex',0);
                        return false;
                    }
                });
            },
            deleteCourse:function (index) {
                let subject = this.subjects[index];
                $.ajax({
                    url : "http://localhost:8084/deleteCourse", // Url of backend (can be python, php, etc..)
                    type: "GET", // data type (can be get, post, put, delete)
                    data : {subject: subject}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function() {
                        alert("Course " + subject + " correctly deleted");
                        $('.collapse').collapse('hide');
                        return false;
                    }
                });
            },
            deleteTeacher:function (index) {
                let teacherElem = this.teachers[index];
                let elem = teacherElem.split(" ");
                $.ajax({
                    url : "http://localhost:8084/deleteTeacher", // Url of backend (can be python, php, etc..)
                    type: "POST", // data type (can be get, post, put, delete)
                    data : {firstName: elem[0], lastName: elem[1], email: elem[2]}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        alert("Teacher correctly deleted");
                        $('.collapse').collapse('hide');
                        return false;
                    }
                });
            }
        }
    });

    $("#admin").on('click', '#sendTeacherData', function (event) {
        let firstName = document.getElementById("formInputFirstName").value;
        let lastName = document.getElementById("formInputLastName").value;
        let email = document.getElementById("formInputEmail").value;
        if (isValidEmail(email) && !isParameterEmpty(firstName) && !isParameterEmpty(lastName)) {
            $.ajax({
                url : "http://localhost:8084/insertTeacher", // Url of backend (can be python, php, etc..)
                type: "POST", // data type (can be get, post, put, delete)
                data : {firstName: firstName, lastName: lastName, email: email}, // data in json format
                async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                success: function(data) {
                    if (data === "true"){
                        alert("Teacher successfully inserted in the system");
                        $('.collapse').collapse('hide');
                        $('#formInputFirstName').val("");
                        $('#formInputLastName').val("");
                        $('#formInputEmail').val("");
                        event.preventDefault();
                        return false;
                    }
                    else {
                        alert("Error, this teacher is already in the system");
                        event.preventDefault();
                        return false;
                    }
                }
            });
        } else {
            alert("Inputs are not valid");
            event.preventDefault();
            return false;
        }
    });

    $("#admin").on('click', '#sectionDeleteTeacher', function () {
        $('#subjectSelected2').prop('selectedIndex',0);
        app.teachers = [];
        app.subjectAvailable();
    });

    $("#admin").on('click', '#sectionInsertAssociation', function () {
        app.subjectAvailable();
        app.getRestrictedTeachers("All", "false");
    });

    $("#admin").on('click', '#confirmAssociationButton', function (event) {
        if ($('#teacherSelected3').find(":selected").text() !== "Select a teacher" && $('#subjectSelected3').find(":selected").text() !== "Select a subject") {
            let subject = $('#subjectSelected3').find(":selected").text();
            let teacherAux = $('#teacherSelected3').find(":selected").text();
            let aux = teacherAux.split(" ");
            let teacher = aux[1] + " " + aux[2];
            $.ajax({
                url : "http://localhost:8084/confirmAssociation", // Url of backend (can be python, php, etc..)
                type: "POST", // data type (can be get, post, put, delete)
                data : {teacher: teacher, subject: subject.replace(/\s/g, '')}, // data in json format
                async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                success: function(data) {
                    if (data === "true") {
                        alert("Association has been inserted successfully in the system");
                        $('.collapse').collapse('hide');
                        event.preventDefault();
                        $('#teacherSelected3').prop('selectedIndex',0);
                        $('#subjectSelected3').prop('selectedIndex',0);
                        return false;
                    }
                    else {
                        alert("Error, association already in the system");
                        event.preventDefault();
                        return false;
                    }
                }
            });
        }
    });

    $("#admin").on('click', '#sectionDeleteAssociation', function () {
        app.subjectAvailable();
        app.teachers = [];
    });

    $("#admin").on('change', '#subjectSelected2', function () {
        if ($('#subjectSelected2').find(":selected").text() !== "Restrict by subjects") {
            app.getRestrictedTeachers($('#subjectSelected2').find(":selected").text(), "true");
        } else
            app.teachers = [];
    });

    $("#admin").on('click', '#confirmDeleteButton', function () {
        let index = $(this).parent().index();
        app.deleteTeacher(index);
    });

    $("#admin").on('change', '#subjectSelected4', function () {
        if ($('#subjectSelected4').find(":selected").text() !== "Select a subject") {
            app.getRestrictedTeachers($('#subjectSelected4').find(":selected").text());
        } else
            app.teachers = [];
    });

    $("#admin").on('click', '#confirmDeleteAssociation', function () {
        let index = $(this).parent().index();
        app.deleteAssociation(index, document.getElementById("subjectSelected4").value);
    });

    $("#admin").on('click', '#sendCourse', function (event) {
        let subject = document.getElementById("formInputCourse").value;
        if (!isParameterEmpty(subject)) {
            $.ajax({
                url : "http://localhost:8084/insertCourse", // Url of backend (can be python, php, etc..)
                type: "GET", // data type (can be get, post, put, delete)
                data : {subject: subject}, // data in json format
                async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                success: function(data) {
                    if (data === "true") {
                        alert("Course successfully inserted");
                        $('.collapse').collapse('hide');
                        event.preventDefault();
                        $('#formInputCourse').val("");
                        return false;
                    } else {
                        alert("Error, this course is already in the system");
                        event.preventDefault();
                        return false;
                    }
                }
            });
        } else {
            alert("Inputs are not valid");
            event.preventDefault();
            return false;
        }
    });

    $("#admin").on('click', '#sectionDeleteCourse', function () {
        app.subjectAvailable();
    });

    $("#admin").on('click', '#confirmDeleteCourse', function () {
        app.deleteCourse($(this).parent().index());
    });

});

function isParameterEmpty(param) {
    if (typeof param === 'string') {
        if (!param)
            return true;
        else
            return false;
    } else
        return false;
}

function isValidEmail(param) {
    if (!isParameterEmpty(param)) {
        const regularExpr = /^(([^<>()[\]\.,;:\s@\"]+(\.[^<>()[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i;
        let result = param.toLowerCase().match(regularExpr);
        if (result === null)
            return false;
        else
            return true;
    } else
        return false;
}