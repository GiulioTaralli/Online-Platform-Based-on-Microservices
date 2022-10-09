$(document).ready(function () {

    let app = new Vue ({
        el: '#booking',
        data: {
            subjects:[],
            teachers:[],
            tutorings:[],
            available: false,
            guest: true,
            student: false,
            admin: false,
            bookingMessage: ''
        },
        mounted() {
            this.sessionFunction();
            this.subjectAvailable();
        },
        methods:{
            sessionFunction:function(){
                let self = this;
                $.ajax({
                    url : "http://localhost:8084/getUsername", // Url of backend (can be python, php, etc..)
                    type: "POST", // data type (can be get, post, put, delete)
                    data : {}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        self.username = data;
                    }
                });
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
            getTeachers:function() {
                this.subjectSelected = $('#subjectSelected').find(":selected").text();
                console.log("prova" + this.subjectSelected + "prova");
                let self = this;
                $.ajax({
                    url : "http://localhost:8084/getTeachers", // Url of backend (can be python, php, etc..)
                    type: "GET", // data type (can be get, post, put, delete)
                    data : {subject: this.subjectSelected.replace(/\s/g, ''), email: "false"}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        self.teachers = data;
                    }
                });
            },
            teacherAvailability:function() {
                this.subjectSelected = $('#subjectSelected').find(":selected").text();
                console.log(this.subjectSelected);
                this.teacherSelected = $('#teacherSelected').find(":selected").text();
                console.log(this.teacherSelected);
                let self = this;
                $.ajax({
                    url : "http://localhost:8084/teacherAvailability", // Url of backend (can be python, php, etc..)
                    type: "GET", // data type (can be get, post, put, delete)
                    data : {subject: this.subjectSelected.replace(/\s/g, ''), teacher: this.teacherSelected}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        self.booking = data;
                        for (let i=0; i<self.booking.length; i++) {
                            switch (self.booking[i].date) {
                                case "Monday":
                                    self.setRedCell(0, self.booking[i].hour);
                                    break;
                                case "Tuesday":
                                    self.setRedCell(1, self.booking[i].hour);
                                    break;
                                case "Wednesday":
                                    self.setRedCell(2, self.booking[i].hour);
                                    break;
                                case "Thursday":
                                    self.setRedCell(3, self.booking[i].hour);
                                    break;
                                case "Friday":
                                    self.setRedCell(4, self.booking[i].hour);
                                    break;
                                default:
                                    console.log("Switch error.");
                                    break;
                            }
                        }
                    }
                });
            },
            setRedCell:function (row, hour) {
                let els = document.getElementById('table');
                let index;
                switch (hour) {
                    case "15":
                        index = 1;
                        break;
                    case "16":
                        index = 2;
                        break;
                    case "17":
                        index = 3;
                        break;
                    case "18":
                        index = 4;
                        break;
                    default:
                        console.log("Switch error.");
                        break;
                }
                els.rows[row].cells[index].style.background = "red";
            },
            printInfo:function(res) {
                let self = this;
                self.bookingMessage = res;
            },
            showButton:function () {
                let self = this;
                self.available = true;
            },
            hideButton:function () {
                let self = this;
                self.available = false;
            },
            insertBooking:function (subject, teacher, day, hour) {
                let self = this;
                $.ajax({
                    url : "http://localhost:8084/insertBooking", // Url of backend (can be python, php, etc..)
                    type: "GET", // data type (can be get, post, put, delete)
                    data : {subject: subject.replace(/\s/g, ''), teacher: teacher, day: day, hour: hour}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        if (data === "true") {
                            alert("Your booking has been correctly inserted in the system");
                            self.teacherAvailability();
                            res = "This booking is not available!";
                            self.printInfo(res);
                            self.hideButton();
                            return false;
                        } else {
                            alert("Error");
                            return false;
                        }
                    }
                });
            }
        }
    });

    $("#booking").on('change', '#subjectSelected', function () {
        resetColorCell();
        if($('#subjectSelected').find(":selected").text() !== "Select a subject") {
            $("#teacherSelected").val(0).change();
            app.getTeachers();
        }
    });

    $("#booking").on('change', '#teacherSelected', function () {
        console.log($('#teacherSelected').find(":selected").text() !== "");
        if($('#teacherSelected').find(":selected").text() !== "Select a teacher" &&
            $('#teacherSelected').find(":selected").text() !== "") {
            resetColorCell();
            app.teacherAvailability();
        }
    });

    function resetColorCell () {
        let els = document.getElementById('table');
        for (let i=0; i<5; i++) {
            for (let j=1; j<5; j++) {
                els.rows[i].cells[j].style.background = "transparent";
            }

        }
    }

    $("#booking").on('click', 'td', function () {
        if ($('#subjectSelected').find(":selected").text() !== "Select a subject" && $('#teacherSelected').find(":selected").text() !== "Select a teacher") {
            let els = document.getElementById('table');
            let row_index = $(this).closest("tr").index();
            let col_index = $(this).index();
            let res = "";
            if (col_index !== 0) {
                if (els.rows[row_index].cells[col_index].style.background !== "red") {
                    let day = (document.getElementById('table').rows[row_index].cells[0]).innerHTML;
                    let hour = (document.getElementById('table').rows[row_index].cells[col_index]).innerHTML;
                    res = "You have selected: \nDay: " + day + "\n Hour: " + hour;
                    app.printInfo(res);
                    app.showButton();
                    app.rowSelected = day;
                    app.columnSelected = hour;
                } else {
                    res = "This booking is not available!";
                    app.printInfo(res);
                    app.hideButton();
                }
            } else app.hideButton();

        }
    });

    $("#booking").on('click', '#actionBook', function () {
        let col_index = app.columnSelected.split("-");
        app.insertBooking(app.subjectSelected, app.teacherSelected, app.rowSelected, col_index[0]);

    });
});