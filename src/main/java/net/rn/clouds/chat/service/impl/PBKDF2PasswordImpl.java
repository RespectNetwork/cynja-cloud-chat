/**
 * Copyright (c) 2016.
 */
package net.rn.clouds.chat.service.impl;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import biz.neustar.clouds.chat.service.PasswordManager;

/**
 * Author: kvats Date: Dec 15, 2016 Time: 4:12:25 PM
 */
public class PBKDF2PasswordImpl implements PasswordManager, Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6144633362199268464L;
    private final int ITERATIONS = 1000;
    private final int KEY_LENGTH = 256;

    @Override
    public byte[] hashPassword(String password, byte[] salt) {
        final char[] passwordChars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(passwordChars, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    @Override
    public boolean validatePassword(String password, byte[] salt, byte[] hashPassword) {
        byte[] pwdHash = hashPassword(password, salt);
        Arrays.fill(password.toCharArray(), Character.MIN_VALUE);
        if (pwdHash.length != hashPassword.length) {
            return false;
        }
        for (int i = 0; i < pwdHash.length; i++) {
            if (pwdHash[i] != hashPassword[i])
                return false;
        }
        return true;
    }
}
