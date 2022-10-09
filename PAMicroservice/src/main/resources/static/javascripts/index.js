$(document).ready(function () {

    let app = new Vue ({
        el: '#sessionID',
        data: {
            role: '',
            username: '',
            guest: true,
            student: false,
            admin: false
        },
        mounted() {
            this.sessionFunction();
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
            }
        }
    });

});