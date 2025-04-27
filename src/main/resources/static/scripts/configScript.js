$(document).ready(function () {

    // Load users from database
    LoadUserTable();
    checkConfigStatus();


    // Adding new User function
    $("#addUserForm").click(function (e) {
        e.preventDefault();
        $("#addUserModal").modal('show');

        $("#addUser").submit(function (e) {
            e.preventDefault();
            $("#addUser").prop("disabled", true)
            const newUser = {
                emailAddress: $("#email").val()
            }

            $.ajax({
                type: "POST",
                url: "http://localhost:8080/user",
                data: JSON.stringify(newUser),
                contentType: "application/json",
                success: function () {
                    $("#addUserModal").modal('hide');
                    LoadUserTable();
                    $("#errorMessage").hide().text("");
                    $("#addUser").prop("disabled", false);
                    $("#addUser")[0].reset();
                },
                error: function (xhr) {
                    $("#addUserModal button[type=submit]").prop("disabled", false);
                    if (xhr.responseText.includes("Email already in use")) {
                        $("#errorMessage").text("User with this email already exists").show();
                        $("#addUser").prop("disabled", false);
                    } else {
                        $("#errorMessage").text("Error while saving new user").show()

                    }

                }
            });
        });
    });
});
function LoadUserTable() {
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/users",
        dataType: "json",
        success: function (response) {
            let tableContent = '';
            response.forEach((user, index) => {
                tableContent += `
    <tr>
    <td>${index + 1}</td>
    <td>${user.emailAddress}</td>
    <td>
    <a class="btn btn-danger btn-sm deleteUser" data-email="${user.emailAddress}">Delete</a>
    </td>
    </tr>`;
                $("table tbody").html(tableContent);
            });
            $(".deleteUser").click(function () {
                let email = $(this).data("email");
                deleteUser(email);
            })

        }, error: function (xhr, status, error) {
            console.error("Database error while loading users - Check users in database");
            console.log("Error:" + error + "Status:" + status, xhr)
        }
    });
}

function deleteUser(email) {
    $.ajax({
        type: "DELETE",
        url: "http://localhost:8080/user",
        data: JSON.stringify({ emailAddress: email }),
        contentType: "application/json",
        success: function () {
            LoadUserTable();
        },
        error: function (xhr, error, status) {
            console.error("Error while deleting user:" + error);
            console.log("Deleting error:" + xhr.responseText);
        }
    });
}

function checkConfigStatus() {
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/config",
        dataType: "json",
        success: function (response) {
            document.getElementById("mailHost").textContent = response.mail_host;
            document.getElementById("mailPort").textContent = response.mail_port;
            document.getElementById("mailName").textContent = response.mail_name;
        }, error: function (xhr, status, error) {
            if (xhr.responseText.includes("Application settings not configured")) {
                $("#configurationModal").modal('show');
                checkPassword();
                $("#rootPassword").submit(function (e) {
                    e.preventDefault();
                    updateRootPassword();
                });

            } else {
                console.error("Error occured while loading basic configuration:" + error);
            }

        }
    });
}
function updateRootPassword() {
    const rootPassword = {
        rootPassword: $("#root").val()
    }
    $.ajax({
        type: "POST",
        url: "http://localhost:8080/config/root",
        data: JSON.stringify(rootPassword),
        contentType: "application/json",
        success: function () {
            $("#rootPassowrd")[0].reset();
            $("#configurationModal").modal('hide');
        }, error: function(xhr, status, error){
            console.log("Password was previously configured");
            console.log(JSON.stringify(rootPassword));
        }
    });
}

function updatePasswordValidation(elementId, isValid) {
    const element = $("#" + elementId);
    if (isValid) {
        element.addClass("req-Meet");
    } else {
        element.removeClass("req-Meet");
    }
}
function checkPassword() {
    const input = document.getElementById('root');
    const saveButton = document.querySelector('button[form="rootPassword"]');

    input.addEventListener('input', () => {
        const value = input.value;

        const isLengthValid = value.length >= 6;
        const hasCapitalLetter = /[A-Z]/.test(value);
        const hasDigit = /\d/.test(value);

        updatePasswordValidation("correctLength", isLengthValid);
        updatePasswordValidation("capitalLetter", hasCapitalLetter);
        updatePasswordValidation("oneDigit", hasDigit);

        if (isLengthValid && hasCapitalLetter && hasDigit) {
            saveButton.disabled = false;
        }
        else {
            saveButton.disabled = true;
        }
    });

};