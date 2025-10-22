package com.example;

public enum Errors {
    EMPTY_INPUT("Must be at least one character long"),
    INVALID_EMAIL("Email format is invalid"),
    PASSWORD_TOO_SHORT("Password must be at least 8 characters long"),
    USER_NOT_FOUND("User not found"),
    UNKNOWN_ERROR("An unknown error occurred"),
    BAD_TELEPHONE("mobile_phone number must be 10 digits"),
    BAD_ROLE("Role must be Admin, Driver, or User"),
    BAD_STATUS("Status must be confirmed, pending, or cancelled"),
    ILLEGAL_EXCEPTION("The variable %s is incorrect"),
    NOT_FOUND("Could not find any %s matching these requirements"),
    ALREADY_EXSISTS("Could not create %s");

    private String message;

    Errors(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String format(String variable) {
        return String.format(this.message, variable);
    }
}

