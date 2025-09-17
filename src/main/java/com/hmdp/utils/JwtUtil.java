package com.hmdp.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

public class JwtUtil {

    private static long tokenExpiration = 60 * 60 * 24 * 1000L; // 1天
    private static String secretKey = "M0PKKI6pYGVWWfDZw90a0lTpGYX1d4AQ";

    public static String createToken(Long userId, String username) {
        SecretKey key = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        String token = Jwts.builder()
                .setSubject("USER_INFO")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .claim("userId", userId)
                .claim("username", username)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return token;
    }

    public static Claims parseToken(String token) {
        SecretKey key = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}