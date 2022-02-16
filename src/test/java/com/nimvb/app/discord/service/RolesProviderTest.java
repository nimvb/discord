package com.nimvb.app.discord.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.JsonPathExpectationsHelper;

import java.io.*;


class RolesProviderTest {
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    
    @Test
    void ShouldSerializeTheCommonRolesToTheirProperStringRepresentations() throws IOException {
        RolesProvider.Role admin = RolesProvider.Role.ADMIN;
        RolesProvider.Role user = RolesProvider.Role.USER;
        RolesProvider.Role unknown = RolesProvider.Role.UNKNOWN;
        Assertions.assertThat(admin.toString()).isEqualTo("ROLE_ADMIN");
        Assertions.assertThat(user.toString()).isEqualTo("ROLE_USER");
        Assertions.assertThat(unknown.toString()).isEqualTo("UNKNOWN");
    }

    @Test
    void ShouldSerializeDeserializeCommonRoleObjectCorrectly() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out          = new ByteArrayOutputStream();
        ObjectOutputStream    outputStream = new ObjectOutputStream(out);
        outputStream.writeObject(RolesProvider.Role.ADMIN);
        outputStream.close();
        Assertions.assertThatCode(() -> {
            RolesProvider.Role role = null;
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
            role = (RolesProvider.Role) inputStream.readObject();
            Assertions.assertThat(role).isNotNull();
            Assertions.assertThat(role).isEqualTo(RolesProvider.Role.ADMIN);
        }).doesNotThrowAnyException();
    }

    @Test
    void ShouldSerializeDeserializeAnyRoleObjectCorrectly() throws IOException, ClassNotFoundException {
        final RolesProvider.Role custom = RolesProvider.Role.deserialize("ROLE_CUSTOM");
        ByteArrayOutputStream out          = new ByteArrayOutputStream();
        ObjectOutputStream    outputStream = new ObjectOutputStream(out);
        outputStream.writeObject(custom);
        outputStream.close();
        Assertions.assertThatCode(() -> {
            RolesProvider.Role role = null;
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
            role = (RolesProvider.Role) inputStream.readObject();
            Assertions.assertThat(role).isNotNull();
            Assertions.assertThat(role).isEqualTo(custom);
        }).doesNotThrowAnyException();
    }

    
    @Test
    void ShouldSerializeAnyUnknownRoleToTheCorrectString(){
        String             roleFake = "ROLE_FAKE";
        String serialized = RolesProvider.Role.deserialize(roleFake).toString();
        Assertions.assertThat(serialized).isEqualTo(roleFake);
    }
    
    @Test
    void ShouldDeserializeNULLToTheUnKnownRole(){
        RolesProvider.Role role = RolesProvider.Role.deserialize(null);
        Assertions.assertThat(role).isEqualTo(RolesProvider.Role.UNKNOWN);
    }
    
    @Test
    void ShouldDeserializeTheCommonRolesStringValuesToTheirCorrespondentRoleObject(){
        RolesProvider.Role user = RolesProvider.Role.deserialize("ROLE_USER");
        RolesProvider.Role admin = RolesProvider.Role.deserialize("ROLE_ADMIN");
        RolesProvider.Role unknown = RolesProvider.Role.deserialize("UNKNOWN");
        Assertions.assertThat(unknown).isEqualTo(RolesProvider.Role.UNKNOWN);
        Assertions.assertThat(unknown.getDescription()).isEmpty();
        Assertions.assertThat(admin).isEqualTo(RolesProvider.Role.ADMIN);
        Assertions.assertThat(admin.getDescription()).isEmpty();
        Assertions.assertThat(user).isEqualTo(RolesProvider.Role.USER);
        Assertions.assertThat(user.getDescription()).isEmpty();
    }

    @Test
    void ShouldDeserializeUnknownInputsToTheProperUnknownRole(){
        String             roleFake = "ROLE_FAKE";
        RolesProvider.Role role      = RolesProvider.Role.deserialize(roleFake);
        Assertions.assertThat(role).isEqualTo(RolesProvider.Role.UNKNOWN);
        Assertions.assertThat(role.getRole()).isEqualTo(RolesProvider.Role.UNKNOWN.getRole());
        Assertions.assertThat(role.getDescription()).isEqualTo(roleFake);
    }

    @Test
    void ShouldSerializeDeserializeARoleToJson() throws IOException {
        final RolesProvider.Role user = RolesProvider.Role.USER;
        final StringWriter userWriter = new StringWriter();
        mapper.writeValue(userWriter,user);
        final RolesProvider.Role userResult = mapper.readValue(userWriter.toString(), RolesProvider.Role.class);
        Assertions.assertThat(userResult).isEqualTo(user);
        final RolesProvider.Role fake = RolesProvider.Role.deserialize("ROLE_FAKE");
        final StringWriter fakeWriter = new StringWriter();
        mapper.writeValue(fakeWriter,fake);
        final RolesProvider.Role fakeResult = mapper.readValue(fakeWriter.toString(), RolesProvider.Role.class);
        Assertions.assertThat(fakeResult).isEqualTo(fake);

    }

    @Test
    void ShouldHashCodeBeEqualToRoleValueHashCode(){
        final String roleFake = "ROLE_FAKE";
        final RolesProvider.Role role = RolesProvider.Role.deserialize(roleFake);
        Assertions.assertThat(role.hashCode()).isEqualTo(RolesProvider.Role.UNKNOWN.hashCode());
        Assertions.assertThat(RolesProvider.Role.USER.hashCode()).isEqualTo(RolesProvider.Role.USER.getRole().hashCode());
    }

}