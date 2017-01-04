/**
 * Copyright (c) 2016.
 */
package biz.neustar.clouds.chat.service;

/**
 * Author: kvats Date: Dec 15, 2016 Time: 3:45:08 PM
 */
public interface PasswordManager {
    /**
     * Creates a new hashed password
     * 
     * @param password
     *            to be hashed
     * @param salt
     *            the randomly generated salt
     * @return a hashed password
     */
    byte[] hashPassword(final String password, final byte[] salt);

    /**
     * Expected password
     * 
     * @param password
     *            to be verified
     * @param salt
     *            the generated salt
     * @param hash
     *            the generated hash
     * @return true if password matches, false otherwise
     */
    boolean validatePassword(final String password, final byte[] salt, final byte[] hash);
}
