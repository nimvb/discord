package com.nimvb.app.discord.domain;

import com.nimvb.app.discord.configuration.MongoConfiguration;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@Import({ValidationAutoConfiguration.class, MongoConfiguration.class})
@TestPropertySource(properties = {
        "spring.mongodb.embedded.version=3.5.5",
        "spring.data.mongodb.auto-index-creation=true"
})
@DirtiesContext()
class UserTest {


    @Autowired
    ReactiveMongoTemplate template;

    @Autowired
    ValidatingMongoEventListener validatingMongoEventListener;

    @Autowired
    Validator validator;

    @Test
    void ShouldReturnANewBuilderWhetherOrNotAValidaValidatorIsPassed(){
        User.Builder builder = new User.Builder(null);
        User.Builder builderWithValidator = new User.Builder(validator);
        Assertions.assertThat(builder).isNotNull();
        Assertions.assertThat(builderWithValidator).isNotNull();
    }
    
    @Test
    void ShouldReturnAUserObjectWhenNULLValidatorIsPassed(){
        User.Builder builderWithNoValidator = new User.Builder(null);
        User         user = builderWithNoValidator.build();
        User nullUser = new User(null,null,null);
        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user).isEqualTo(nullUser);
    }

    @Test
    void ShouldThrownAnExceptionWhenAValidatorAndInvalidUserParametersArePassed(){
        User.Builder builderWithValidator = new User.Builder(validator);
        User testCase = new User(null,null,null);
        Comparator<ConstraintViolation<User>> violationComparator = Comparator.comparing(ConstraintViolation::getMessage);
        Set<ConstraintViolation<User>> violations = new TreeSet<>(violationComparator);
        violations.addAll(validator.validate(testCase));
        Assertions.assertThatThrownBy(() -> {
            User         user = builderWithValidator.build();
        }).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("constructing a new user has been failed")
                .has(new Condition<>(throwable -> {
                    ConstraintViolationException violationException = (ConstraintViolationException) throwable;
                    TreeSet<ConstraintViolation<?>> constraintViolations = new TreeSet(violationComparator);
                    constraintViolations.addAll(violationException.getConstraintViolations());
                    return constraintViolations.toString().equals(violations.toString());
                },"violation equality check"));

    }


    @Test
    void ShouldThrownAnExceptionWhenTheProvidedParametersAreNULL() {
        Condition<ConstraintViolation<User>> username = new Condition<>(violation -> {
            return violation.getRootBean().getUsername() == null &&
                    violation.getMessage().equals("username is required");
        }, "null username");
        Condition<ConstraintViolation<User>> email = new Condition<>(violation -> {
            return violation.getRootBean().getEmail() == null &&
                    violation.getMessage().equals("email is required");
        }, "null email");
        Condition<ConstraintViolation<User>> password = new Condition<>(violation -> {

            return violation.getRootBean().getPassword() == null &&
                    violation.getMessage().equals("password is required");
        }, "null password");
        User user = new User(null, null, null);
        
        final Set<ConstraintViolation<User>> violations = validator.validate(user);
        Assertions.assertThat(violations)
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(3)
                .haveExactly(1, username)
                .haveExactly(1, email)
                .haveExactly(1, password);
    }

    @Test
    void ShouldThrownAnExceptionWhenTheProvidedParametersAreBlank() {

        Condition<ConstraintViolation<User>> username = new Condition<>(violation -> {
            return violation.getRootBean().getUsername().isBlank() &&
                    violation.getMessage().equals("invalid username");
        }, "blank username");
        Condition<ConstraintViolation<User>> email = new Condition<>(violation -> {
            return violation.getRootBean().getEmail().isBlank() &&
                    violation.getMessage().equals("invalid email");
        }, "blank email");
        Condition<ConstraintViolation<User>> password = new Condition<>(violation -> {

            return violation.getRootBean().getPassword().isBlank() &&
                    violation.getMessage().equals("invalid password");
        }, "blank password");


        User user = new User(" ", "   ", " ");
        final Set<ConstraintViolation<User>> violations = validator.validate(user);
        Assertions.assertThat(violations)
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(3)
                .haveExactly(1, username)
                .haveExactly(1, email)
                .haveExactly(1, password);
    }

    @Test
    void ShouldThrownAnExceptionWhenTheProvidedParametersAreEmpty() {
        Condition<ConstraintViolation<User>> username = new Condition<>(violation -> {
            return violation.getRootBean().getUsername().isEmpty() &&
                    violation.getMessage().equals("invalid username");
        }, "empty username");
        Condition<ConstraintViolation<User>> email = new Condition<>(violation -> {
            return violation.getRootBean().getEmail().isEmpty() &&
                    violation.getMessage().equals("invalid email");
        }, "empty email");
        Condition<ConstraintViolation<User>> password = new Condition<>(violation -> {
            return violation.getRootBean().getPassword().isEmpty() &&
                    violation.getMessage().equals("invalid password");
        }, "empty password");


        User user = new User("", "", "");
        final Set<ConstraintViolation<User>> violations = validator.validate(user);
        Assertions.assertThat(violations)
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(3)
                .haveAtLeast(1, username)
                .haveAtLeast(1, email)
                .haveAtLeast(1, password);
    }

    @Test
    void ShouldUsernameHasUniqueConstraintIndex() {

        Predicate<IndexInfo> userNameUniqueIndexCondition = indexInfo -> {
            if (indexInfo.getIndexFields().size() <= 0 || indexInfo.getIndexFields().size() > 1) {
                return true;
            }

            final IndexField target = indexInfo.getIndexFields().stream().findFirst().orElse(null);
            if (target != null) {

                if (target.getKey().equals("username")) {
                    return indexInfo.isUnique();
                }

            }

            return true;
        };

        StepVerifier
                .create(template.indexOps(User.class).getIndexInfo())
                .expectNextMatches(userNameUniqueIndexCondition)
                .expectNextMatches(userNameUniqueIndexCondition)
                .expectNextMatches(userNameUniqueIndexCondition)
                .expectComplete().verify();
    }

    @Test
    void ShouldTestEqualityOfTwoUserObjectsBasedOnAllFieldValuesExceptThePasswordField(){
        User first = new User("username","password","email@email");
        User second = new User("username","password","email@email");
        Assertions.assertThat(first).isEqualTo(second);
    }

    @Test
    void ShouldEmailHasUniqueConstraintIndex() {

        Predicate<IndexInfo> emailUniqueIndexCondition = indexInfo -> {
            if (indexInfo.getIndexFields().size() <= 0 || indexInfo.getIndexFields().size() > 1) {
                return true;
            }

            final IndexField target = indexInfo.getIndexFields().stream().findFirst().orElse(null);
            if (target != null) {

                if (target.getKey().equals("email")) {
                    return indexInfo.isUnique();
                }

            }

            return true;
        };

        StepVerifier
                .create(template.indexOps(User.class).getIndexInfo())
                .expectNextMatches(emailUniqueIndexCondition)
                .expectNextMatches(emailUniqueIndexCondition)
                .expectNextMatches(emailUniqueIndexCondition)
                .expectComplete().verify();
    }




}