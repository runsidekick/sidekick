package com.runsidekick.api.utils;

import com.runsidekick.api.security.ApiKeyAuthentication;
import jodd.crypt.BCrypt;
import org.springframework.http.HttpHeaders;

public class TestUtils {

    public static ApiKeyAuthentication getMockAuthenticatedUser() {
        ApiKeyAuthentication user = new ApiKeyAuthentication("test");
        return user;
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public static HttpHeaders getHttpHeaders() {
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", "Token DUMMY_TOKEN");
        header.add("Content-Type", "application/json");
        return header;
    }
}
