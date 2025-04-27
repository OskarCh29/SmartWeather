let settingsUnlocked = false;
$(document).ready(function () {

    // Check if already configurated
    checkConfigStatus();

    $("#unlockSettings").click(function (e) {
        e.preventDefault();
        if (settingsUnlocked) {
            lockSettings();
        }
        else {
            $("#passwordConfig").modal('show');
        }
    });
    $("#updateConfig").submit(function (e) {
        e.preventDefault();
        validateRootPassword($("#rootConfig").val());

    });



});

function checkConfigStatus() {
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/config",
        dataType: "json",
        success: function (response) {
            document.getElementById("mailHost").textContent = response.mail_host;
            document.getElementById("mailPort").textContent = response.mail_port;
            document.getElementById("mailName").textContent = response.mail_name;
        }, error: function (xhr, status) {
            if (xhr.responseText.includes("Application settings not configured")) {
                $("#configurationModal").modal('show');
                checkPassword();
                $("#rootPassword").submit(function (e) {
                    e.preventDefault();
                    updateRootPassword();
                });
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
            $("#rootPassword")[0].reset();
            $("#configurationModal").modal('hide');
        }, error: function () {
            console.log("Password was previously configured");
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

function validateRootPassword(password) {
    const rootPass = {
        rootPassword: password
    }
    $.ajax({
        type: "POST",
        url: "http://localhost:8080/config/validate",
        data: JSON.stringify(rootPass),
        contentType: "application/json",
        success: function () {
            $("#wrongPassword").hide();
            $("#passwordConfig").modal('hide');
            unlockSettings();

        }, error: function (xhr) {
            if (xhr.responseText.includes("Invalid password")) {
                $("#wrongPassword").text("Password incorrect").show();
            }
        }
    });
}

function unlockSettings() {
    $("#mailHost, #mailPort, #mailName, #mailPassword, #apiKey").prop("disabled", false);

    $("#unlockSettings").removeClass("btn-outline-warning").addClass("btn-outline-success")
        .text("🔒 Lock main settings")

    settingsUnlocked = true;
}

function lockSettings() {
    const configuration = {
        newConfig: {
            "mail_host": $("#mailHost").val(),
            "mail_port": $("#mailPort").val(),
            "mail_name": $("#mailName").val(),
            "mail_pass": $("#mailPassword").val(),
            "api_key": $("#apiKey").val()
        }
    }

    $.ajax({
        type: "POST",
        url: "http://localhost:8080/config",
        data: JSON.stringify(configuration),
        contentType: "application/json",
        success: function () {
            $("#mailHost,#mailPort, #mailName, #mailPassword,#apiKey").prop("disabled", true);
            $("#unlockSettings").removeClass("btn-outline-warning").addClass("btn-outline-success")
                .text("🔓 Unlock main settings");

            settingsUnlocked = false;
            checkConfigStatus();

        }, error: function () {
            console.log("Error");
        }
    });
}

