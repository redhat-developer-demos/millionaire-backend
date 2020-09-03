package com.redhat.developer.millionaire;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IdentifierGenerator {

    public String generateId(String from) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(from.getBytes());
            byte[] id = md.digest();
            String encodedId = Base64.getEncoder().encodeToString(id);
            return encodedId.replace('/', '_');
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}