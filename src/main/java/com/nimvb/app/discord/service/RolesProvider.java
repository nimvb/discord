package com.nimvb.app.discord.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

public interface RolesProvider {
    public static final String[] DEFAULT_ROLES = {
            Role.USER.getRole()
    };

    /**
     * Provides the roles based on the defined strategy
     * @return the unmodifiable set of provided roles
     */
    Set<Role> provide();

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    final class Role implements Serializable {

        private static final long serialVersionUID = 1L;

        private Role() {
            role = UNKNOWN.role;
            description = "";
        }

        private Role(Role role) {
            this.role = role.role;
            this.description = role.description;
        }

        public static final     Role   USER    = new Role("ROLE_USER", "");
        public static final     Role   ADMIN   = new Role("ROLE_ADMIN", "");
        public static final     Role   UNKNOWN = new Role("UNKNOWN", "");
        private final transient String role;
        private final transient String description;

        private void writeObject(ObjectOutputStream oos) throws IOException {
            oos.writeUTF(serialize());
        }

        private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException, NoSuchFieldException, IllegalAccessException {
            Role        deserialize      = deserialize(ois.readUTF());
            Class<Role> roleClass        = Role.class;
            Field       roleField        = roleClass.getDeclaredField("role");
            Field       descriptionField = roleClass.getDeclaredField("description");
            roleField.setAccessible(true);
            descriptionField.setAccessible(true);
            roleField.set(this, deserialize.role);
            descriptionField.set(this, deserialize.description);
            roleField.setAccessible(false);
            descriptionField.setAccessible(false);

        }


        /**
         * Deserialize the serialized value to the new {@link Role} object. <br/>
         * <ul>
         *     <li>
         *         If the {@code value} is <code>null</code> then a new {@link Role#UNKNOWN} role will be returned.
         *     </li>
         *     <li>
         *         If the {@code value} is not among the standard roles, then a new {@link Role#UNKNOWN} role with it's
         *         description value set to the serialized value will be returned.
         *     </li>
         * </ul>
         * @param value the serialized value
         * @return a new {@link Role} based on the {@code value}
         */
        public static Role deserialize(String value) {
            if (value == null || value.equalsIgnoreCase(UNKNOWN.role)) {
                return new Role(UNKNOWN);
            }
            if (value.equalsIgnoreCase(USER.role)) {
                return new Role(USER);
            }
            if (value.equalsIgnoreCase(ADMIN.role)) {
                return new Role(ADMIN);
            }
            return new Role(UNKNOWN.role, value);
        }


        /**
         * Serialize the role.
         * If the {@link Role} value is among the standards roles such as {@link Role#USER} then the {@link Role#role}
         * value will be chosen as the serialization value of the {@link Role} object.
         * In case of when the {@link Role#role} value is equal to the {@link Role#UNKNOWN}'s {@link Role#role} value, if the
         * {@link Role#description} is not blank then the serialization value for the current {@link Role} will be the
         * value of the {@link Role#description}, otherwise {@link Role#role} is chosen as the serialization value.
         * @return the serialized value of the current role
         * @throws NotSerializableException when the {@link Role#role} does not follow any of the standards {@link Role}
         * like {@link Role#USER} or {@link Role#UNKNOWN}
         */
        private String serialize() throws NotSerializableException {
            Role role = this;

            if (role.equals(USER)) {
                return USER.role;
            }
            if (role.equals(ADMIN)) {
                return ADMIN.role;
            }
            if (role.equals(UNKNOWN)) {
                if (!role.description.isBlank()) {
                    return role.description;
                }
                return UNKNOWN.role;
            }
            throw new NotSerializableException(role.toString());
        }

        /**
         *
         * @return the serialized value of the current {@link Role}
         */
        @SneakyThrows
        @Override
        public String toString() {
            return serialize();
        }


        /**
         * Serialize the current role object in to it's JSON representation
         * @return the JSON representation of the {@link Role}
         * @throws NotSerializableException if the {@link Role} can not be serialized
         */
        @JsonValue
        public String toJson() throws NotSerializableException {
            return serialize();
        }

        /**
         * Deserialize a JSON string to the {@link Role} object
         * @param json
         * @return the deserialized {@link Role}
         */
        @JsonCreator
        public static Role fromJson(String json){
            return deserialize(json);
        }

        /**
         * Check equality based on the equality of the {@link Role#role}
         * @param o the object for equality check against
         * @return true if equals otherwise false
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Role role1 = (Role) o;

            return getRole().equals(role1.getRole());
        }

        /**
         * Compute hashcode for the current {@link Role} based on the {@link Role#role} hash value
         * @return hashcode of the {@link Role}
         */
        @Override
        public int hashCode() {
            return getRole().hashCode();
        }
    }
}
