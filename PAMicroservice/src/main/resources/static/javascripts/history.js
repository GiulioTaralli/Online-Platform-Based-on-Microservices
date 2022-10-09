$(document).ready(function () {
    let app = new Vue ({
        el: '#history',
        data: {
            guest: true,
            student: false,
            admin: false,
            histories:[],
        },
        mounted() {
            this.getRoleSession();
            this.getHistory();
            if (this.histories.length === 0)
                alert("The history list is empty");
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
            getHistory:function () {
                let self = this;
                $.ajax({
                    url : "http://localhost:8084/getHistory", // Url of backend (can be python, php, etc..)
                    type: "POST", // data type (can be get, post, put, delete)
                    data : {}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        self.histories = data;
                    }
                });
            },
            restrictionHistory:function (status) {
                let self = this;
                $.ajax({
                    url : "http://localhost:8084/getRestrictedHistory", // Url of backend (can be python, php, etc..)
                    type: "POST", // data type (can be get, post, put, delete)
                    data : {status: status}, // data in json format
                    async : false, // enable or disable async (optional, but suggested as false if you need to populate data afterwards)
                    success: function(data) {
                        self.histories = data;
                        if (self.histories.length === 0)
                            alert("There are no booking with this status");
                    }
                });
            }
        }
    });

    $("#sessionID").on('click', '#restrictionSelected', function () {
        if ($('#restrictionSelected').find(":selected").text() === "All bookings")
            app.getHistory();
        else if ($('#restrictionSelected').find(":selected").text() === "Active bookings")
            app.restrictionHistory(0);
        else if ($('#restrictionSelected').find(":selected").text() === "Confirmed bookings")
            app.restrictionHistory(1);
        else if ($('#restrictionSelected').find(":selected").text() === "Deleted bookings")
            app.restrictionHistory(2);
    });
});