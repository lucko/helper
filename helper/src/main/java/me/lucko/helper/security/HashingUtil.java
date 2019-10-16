/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.helper.security;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class HashingUtil {

    public static String getHash(String text, String type, int rounds) {
        return rounds > 1 ? getHash(text, type, rounds - 1) : getHash(text, type);
    }

    public static String getHash(String password, String algorithm) {
        algorithm = algorithm.toUpperCase();
        switch (algorithm) {
            case "WHIRLPOOL":
                return whirlpool(password);
            case "BCRYPT":
                return bcrypt(password);
        }
        return getMessageDigestHash(password, algorithm);
    }

    private static String getMessageDigestHash(String password, String algorithm) {
        try {
            final MessageDigest m = MessageDigest.getInstance(algorithm);
            m.update(password.getBytes());
            return String.format("%032x", new BigInteger(1, m.digest()));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static boolean isEqual(String string1, String string2) {
        return MessageDigest.isEqual(string1.getBytes(StandardCharsets.UTF_8), string2.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateSalt(int length, String charSet) {
        final SecureRandom sr = new SecureRandom();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(charSet.toCharArray()[sr.nextInt(charSet.length())]);
        return sb.toString();
    }

    public static String generateBcryptSalt() {
        return BCRYPT.gensalt();
    }

    public static String generateBcryptSalt(int log_rounds) {
        return BCRYPT.gensalt(log_rounds);
    }

    public static String generateBcryptSalt(int log_rounds, SecureRandom random) {
        return BCRYPT.gensalt(log_rounds, random);
    }

    public static String md5(String password) {
        return getMessageDigestHash(password, "MD5");
    }

    public static String sha1(String password) {
        return getMessageDigestHash(password, "SHA-1");
    }

    public static String sha224(String password) {
        return getMessageDigestHash(password, "SHA-224");
    }

    public static String sha256(String password) {
        return getMessageDigestHash(password, "SHA-256");
    }

    public static String sha384(String password) {
        return getMessageDigestHash(password, "SHA-384");
    }

    public static String sha512(String password) {
        return getMessageDigestHash(password, "SHA-512");
    }

    public static String whirlpool(String password) {
        return WHIRLPOOL.toHash(password);
    }

    public static String bcrypt(String password) {
        return BCRYPT.hashpw(password, BCRYPT.gensalt());
    }

    public static String bcrypt(String password, int log2Rounds) {
        return BCRYPT.hashpw(password, BCRYPT.gensalt(log2Rounds));
    }

    public static String bcrypt(String password, String salt) {
        return BCRYPT.hashpw(password, salt);
    }

    public static boolean bcryptCompare(String password, String hash) {
        return BCRYPT.checkpw(password, hash);
    }

    private HashingUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
