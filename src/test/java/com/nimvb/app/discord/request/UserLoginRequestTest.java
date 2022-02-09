package com.nimvb.app.discord.request;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import(ValidationAutoConfiguration.class)
class UserLoginRequestTest {

    @Autowired
    Validator validator;


    @Test
    void ShouldThrownAnExceptionWhenTheProvidedParametersAreNULL(){
        UserLoginRequest userLoginRequest = new UserLoginRequest(null,null);
        final Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(userLoginRequest);
        Assertions.assertThat(violations).isNotNull().hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void ShouldThrownAnExceptionWhenTheProvidedParametersAreBlank(){
        UserLoginRequest userLoginRequest = new UserLoginRequest(" ","   ");
        final Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(userLoginRequest);
        Assertions.assertThat(violations).isNotNull().hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void ShouldThrownAnExceptionWhenTheProvidedParametersAreEmpty(){
        UserLoginRequest userLoginRequest = new UserLoginRequest("","");
        final Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(userLoginRequest);
        Assertions.assertThat(violations).isNotNull().hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void ShouldThrownAnExceptionWhenTheProvidedEmailAddressIsNotValid(){
        UserLoginRequest userLoginRequest = new UserLoginRequest("222","testtest");
        final Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(userLoginRequest);

        Condition<ConstraintViolation<UserLoginRequest>> condition = new Condition<>(violation -> {
            return violation.getMessage().equals("invalid email address");
        },"invalid email address");
        Assertions.assertThat(violations).isNotNull().hasSizeGreaterThanOrEqualTo(1);
        Assertions.assertThat(violations).have(condition);
    }

    @Test
    void ShouldThrownAnExceptionWhenTheProvidedPasswordIsNotValid(){
        UserLoginRequest userLoginRequest = new UserLoginRequest("hello@hello.com","t");
        final Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(userLoginRequest);

        Condition<ConstraintViolation<UserLoginRequest>> condition = new Condition<>(violation -> {
            return violation.getMessage().equals("password length should be greater than 6 characters");
        },"invalid password");
        Assertions.assertThat(violations).isNotNull().hasSizeGreaterThanOrEqualTo(1);
        Assertions.assertThat(violations).have(condition);
    }

    @Test
    void ShouldValidateWithoutAnyExceptionsWhenValidInputIsProvided(){
        UserLoginRequest userLoginRequest = new UserLoginRequest("hello@hello.com","testtest");
        final Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(userLoginRequest);
        Assertions.assertThat(violations).isNotNull().hasSize(0);
    }

}