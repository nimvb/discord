package com.nimvb.app.discord.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;


@AllArgsConstructor
@Data
@Document(collection = "users")
@EqualsAndHashCode
public class User {

    @Indexed(unique = true, background = true)
    @NotNull(message = "username is required")
    @NotBlank(message = "invalid username")
    private final String      username;
    @NotNull(message = "password is required")
    @NotBlank(message = "invalid password")
    private       String      password;
    @Indexed(unique = true, background = true)
    @NotNull(message = "email is required")
    @NotBlank(message = "invalid email")
    @Email
    private final String      email;
    @NotNull(message = "roles are required")
    private final Set<String> roles;

    public Collection<String> getRoles() {
        if (this.roles != null) {
            return Collections.unmodifiableSet(this.roles);
        }
        return Collections.unmodifiableSet(Collections.emptySet());
    }

    public static Builder builder() {
        return new Builder(null);
    }

    public static Builder builder(Validator validator) {
        return new Builder(validator);
    }

    public static class Builder {

        private final Validator   validator;
        private       String      username;
        private       String      password;
        private       String      email;
        private       Set<String> roles = new HashSet<>();
        
        @AllArgsConstructor
        final class Inspector {
            private final Builder parent;
            public Builder and(){
                return parent;
            }
            
            public Collection<String> rolesSnapshot(){
                return Collections.unmodifiableSet(Set.copyOf(parent.roles));
            }
        }

        public Builder(Validator validator) {

            this.validator = validator;
        }


        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }


        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }
        
        public Inspector inspect(){
            return new Inspector(this);
        }

        public Builder withRole(@NonNull String role) {
            Assert.hasText(role, "invalid role");
            this.roles.add(role);
            return this;
        }

        public Builder withRoles(@NonNull String... roles) {
            this.roles.addAll(Arrays.asList(roles));
            return this;
        }

        public Builder revoke(@NonNull String role) {
            Assert.hasText(role, "invalid role");
            this.roles.remove(role);
            return this;
        }

        public Builder revokeAll() {
            this.roles.clear();
            return this;
        }

        /**
         * Build a new user object based on the passed parameters
         *
         * @return a new user object
         * @throws ConstraintViolationException when a valid validator is set and the constructed {@link User} object
         *                                      has any type of validation violations.
         */
        public User build() throws ConstraintViolationException {
            User user = new User(username, password, email, roles);
            if (validator != null) {
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException("constructing a new user has been failed", violations);
                }
            }
            return user;
        }

    }
}
