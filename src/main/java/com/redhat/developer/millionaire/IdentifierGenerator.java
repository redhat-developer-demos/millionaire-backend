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
            return Base64.getEncoder().encodeToString(id);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}