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

@ExtendWith(SpringExtension.class)
@Import(ValidationAutoConfiguration.class)
class UserRegistrationRequestTest {

    @Autowired
    Validator validator;

    @Test
    void ShouldThrownAnExceptionWhenTheProvidedParametersAreNULL() {
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest(null, null, null);
        final Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userRegistrationRequest);
        Condition<ConstraintViolation<UserRegistrationRequest>> usernameIsRequired = new Condition<>(violation -> {
            return violation.getMessage().equals("username is required");
        }, "username");
        Condition<ConstraintViolation<UserRegistrationRequest>> passwordIsRequired = new Condition<>(violation -> {
            return violation.getMessage().equals("password is required");
        }, "password");
        Condition<ConstraintViolation<UserRegistrationRequest>> emailIsRequired = new Condition<>(violation -> {
            return violation.getMessage().equals("email is required");
        }, "email");
        Assertions.assertThat(violations).isNotNull()
                .hasSizeGreaterThanOrEqualTo(3)
                .haveExactly(1, usernameIsRequired)
                .haveExactly(1, passwordIsRequired)
                .haveExactly(1, emailIsRequired);
    }

    @Test
    void ShouldThrownAnExceptionWhenTheProvidedParametersAreEmpty() {
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest("", "", "");
        final Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userRegistrationRequest);
        Condition<ConstraintViolation<UserRegistrationRequest>> usernameIsRequired = new Condition<>(violation -> {
            return violation.getMessage().equals("username is required");
        }, "username");
        Condition<ConstraintViolation<UserRegistrationRequest>> passwordIsRequired = new Condition<>(violation -> {
            return violation.getMessage().equals("password is required");
        }, "password");
        Condition<ConstraintViolation<UserRegistrationRequest>> emailIsRequired = new Condition<>(violation -> {
            return violation.getMessage().equals("email is required");
        }, "email");
        Assertions.assertThat(violations).isNotNull()
                .hasSizeGreaterThanOrEqualTo(3)
                .haveExactly(1, usernameIsRequired)
                .haveExactly(1, passwordIsRequired)
                .haveExactly(1, emailIsRequired);
    }

    @Test
    void ShouldThrownAnExceptionWhenTheProvidedParametersAreBlank() {
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest(" ", "   ", "  ");
        final Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userRegistrationRequest);
        Condition<ConstraintViolation<UserRegistrationRequest>> usernameIsRequired = new Condition<>(violation -> {
            return violation.getMessage().equals("username is required");
        }, "username");
        Condition<ConstraintViolation<UserRegistrationRequest>> passwordIsRequired = new Condition<>(violation -> {
            return violation.getMessage().equals("password is required");
        }, "password");
        Condition<ConstraintViolation<UserRegistrationRequest>> emailIsRequired = new Condition<>(violation -> {
            return violation.getMessage().equals("email is required");
        }, "email");
        Assertions.assertThat(violations).isNotNull()
                .hasSizeGreaterThanOrEqualTo(3)
                .haveExactly(1, usernameIsRequired)
                .haveExactly(1, passwordIsRequired)
                .haveExactly(1, emailIsRequired);
    }

    @Test
    void ShouldThrownAnExceptionWhenTheProvidedParametersAreNotValid() {
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest("us", "pass", "email");
        final Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userRegistrationRequest);

        Condition<ConstraintViolation<UserRegistrationRequest>> invalidEmail = new Condition<>(violation -> {
            return violation.getMessage().equals("invalid email address");
        }, "invalid-email");

        Condition<ConstraintViolation<UserRegistrationRequest>> invalidPassword = new Condition<>(violation -> {
            final UserRegistrationRequest request = violation.getRootBean();
            return !(request.getPassword().length() >= 6) &&
                    violation.getMessage().equals("password length should be greater than 6");
        }, "invalid-password");

        Condition<ConstraintViolation<UserRegistrationRequest>> invalidUsername = new Condition<>(violation -> {
            final UserRegistrationRequest request = violation.getRootBean();
            return !(request.getUsername().length() >= 3) &&
                    violation.getMessage().equals("username length should be greater than 3");
        }, "invalid-username");


        Assertions.assertThat(violations).isNotNull().hasSize(3);
        Assertions.assertThat(violations)
                .haveExactly(1, invalidUsername)
                .haveExactly(1, invalidEmail)
                .haveExactly(1, invalidPassword);
    }


    @Test
    void ShouldValidateWithoutAnyExceptionsWhenValidParametersAreProvided() {
        UserRegistrationRequest userLoginRequest = new UserRegistrationRequest("username", "password", "email@email.com");
        final Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(userLoginRequest);
        Assertions.assertThat(violations).isNotNull().hasSize(0);
    }
}