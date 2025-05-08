$(document).ready(function () {

    // Load users from database
    LoadUserTable();

    // Adding new User function
    $("#addUserForm").click(function (e) {
        e.preventDefault();
        $("#addUserModal").modal('show');
        $("#errorMessage").hide().text("");
        $("#addUser")[0].reset();
    });

    $("#addUser").submit(function (e) {
        e.preventDefault();
        $("#addUser").prop("disabled", true)

        const newUser = {
            emailAddress: $("#email").val()
        };

        $.ajax({
            type: "POST",
            url: window.API_BASE_URL + "/user",
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
                    $("#errorMessage").text("E-mail format invalid").show()
                }
            }
        });
    });
});


// Load user table function
function LoadUserTable() {
    $.ajax({
        type: "GET",
        url: window.API_BASE_URL + "/user",
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
            });
            $("table tbody").html(tableContent);
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

// Delete user function
function deleteUser(email) {
    $.ajax({
        type: "DELETE",
        url: window.API_BASE_URL + "/user",
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