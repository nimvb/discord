package com.nimvb.app.discord.exception;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(String username){
        super("user[username=" +username+"] not found");
    }
}
