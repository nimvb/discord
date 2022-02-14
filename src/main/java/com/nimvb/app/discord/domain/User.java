package com.nimvb.app.discord.domain;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Set;


@AllArgsConstructor
@Data
@Document(collection = "users")
@EqualsAndHashCode
public class User {

    @Indexed(unique = true,background = true)
    @NotNull(message = "username is required")
    @NotBlank(message = "invalid username")
    private final String username;
    @NotNull(message = "password is required")
    @NotBlank(message = "invalid password")
    private String password;
    @Indexed(unique = true,background = true)
    @NotNull(message = "email is required")
    @NotBlank(message = "invalid email")
    @Email
    private final String             email;
    @NonNull
    private final Collection<String> roles;
    
    
    public static Builder builder(){
        return new Builder(null);
    }
    
    public static Builder builder(Validator validator){
        return new Builder(validator);
    }
    
    public static class Builder {

        private final Validator validator;
        private String username;
        private String password;
        private String email;

        public Builder(Validator validator) {

            this.validator = validator;
        }
        

        public Builder withUsername(String username){
            this.username = username;
            return this;
        }
        
        public Builder withPassword(String password){
            this.password = password;
            return  this;
        }
        
        
        public Builder withEmail(String email){
            this.email = email;
            return this;
        }

        /**
         * Build a new user object based on the passed parameters
         * @return a new user object
         * @throws ConstraintViolationException when a valid validator is set and the constructed {@link User} object
         * has any type of validation violations.
         */
        public User build() throws ConstraintViolationException{
            User user = new User(username, password, email);
            if(validator != null){
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                if(!violations.isEmpty()){
                    throw new ConstraintViolationException("constructing a new user has been failed",violations);
                }
            }
            return user;
        }
        
    }
}
