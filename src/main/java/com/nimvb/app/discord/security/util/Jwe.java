package com.nimvb.app.discord.security.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.Getter;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Jwe {

    private Builder builder;

    public static Builder create() {
        return new Builder();
    }

    public static DecryptionManagerBuilder require(Algorithm algorithm) {
        return new DecryptionManagerBuilder(algorithm);
    }

    protected Jwe(Builder builder) {

        this.builder = builder;
    }

    public static class Algorithm {

        private final JWEHeader    header;
        private final JWEEncrypter encryptor;
        private final JWEDecrypter decrypter;

        protected Algorithm(JWEHeader header, JWEEncrypter encryptor, JWEDecrypter decrypter) {
            this.header = header;
            this.encryptor = encryptor;
            this.decrypter = decrypter;
        }

        public static Algorithm AES128HS256(String secret) throws KeyLengthException, NoSuchAlgorithmException, InvalidKeySpecException {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            SecretKey       secretKey = secretKeyFactory.generateSecret(new PBEKeySpec(secret.toCharArray(), secret.getBytes(StandardCharsets.UTF_8), 10, 256));
            DirectEncrypter encryptor = new DirectEncrypter(secretKey);
            DirectDecrypter decryptor = new DirectDecrypter(secretKey);
            return new Algorithm(new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256), encryptor, decryptor);
        }

    }


    @Getter
    public static class Builder {

        private String              subject;
        private Date                expiredAt;
        private Date                issuedAt;
        private String              issuer;
        private String              audience;
        private Map<String, Object> claims = new HashMap<>();

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withExpiredAt(Date expiredAt) {
            this.expiredAt = expiredAt;
            return this;
        }

        public Builder withIssuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public Builder withAudience(String audience) {
            this.audience = audience;
            return this;
        }

        public Builder withClaim(String key, Object value) {
            claims.put(key, value);
            return this;
        }

        public Builder withIssuedAt(Date issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public String encrypt(Algorithm algorithm) throws JOSEException {
            return new Jwe(this).encrypt(algorithm);
        }
    }

    private String encrypt(Algorithm algorithm) throws JOSEException {
        JWTClaimsSet.Builder claimSetBuilder = new JWTClaimsSet.Builder();
        claimSetBuilder
                .subject(builder.subject)
                .expirationTime(builder.expiredAt)
                .issuer(builder.issuer)
                .issueTime(builder.issuedAt)
                .audience(builder.audience);
        builder.claims.forEach(claimSetBuilder::claim);
        JWTClaimsSet claimsSet = claimSetBuilder.build();

        Payload   payload   = new Payload(claimsSet.toJSONObject());
        JWEObject jweObject = new JWEObject(algorithm.header, payload);

        jweObject.encrypt(algorithm.encryptor);

        return jweObject.serialize();
    }


    public static class DecryptionManagerBuilder {
        private final Algorithm algorithm;

        protected DecryptionManagerBuilder(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        public DecryptionManager build() {
            return new DecryptionManager(this.algorithm);
        }


    }

    public static class DecryptionManager {

        private final Algorithm algorithm;

        protected DecryptionManager(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        public JWTClaimsSet decrypt(String token) throws ParseException, JOSEException {
            JWEObject jweObject = JWEObject.parse(token);
            jweObject.decrypt(algorithm.decrypter);
            Payload payload = jweObject.getPayload();
            return JWTClaimsSet.parse(payload.toJSONObject());
        }
    }

}
