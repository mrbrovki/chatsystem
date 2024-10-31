package com.example.chatsystem.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtils {
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, String domain) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setDomain(domain);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);
    }
}
