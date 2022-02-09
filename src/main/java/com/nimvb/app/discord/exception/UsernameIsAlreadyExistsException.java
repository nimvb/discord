package com.nimvb.app.discord.exception;

public class UsernameIsAlreadyExistsException extends RuntimeException{

    public UsernameIsAlreadyExistsException(String username,Throwable cause) {
        super(username + " exists",cause);
    }
}
