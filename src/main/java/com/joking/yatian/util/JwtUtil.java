package com.joking.yatian.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JwtUtil JWT工具类
 * @author Joking7
 * @version 2024/07/17 
**/
@Component
public class JwtUtil {

    @Value("${token.privateKey}")
    private String privateKey;

    /**
     * TODO
     * @date 2024/7/17
     * @methodName getToken 返回根据 {用户id, 用户权限, 时间} 生成的Token
     * @param userId 用户id
     * @param userRole 用户权限
     * @return java.lang.String
     * @author Joing7
     * @throws
     *
    **/
    public String getToken(String userId, String userRole) {
        return JWT
                .create()
                .withClaim("userId" ,userId)
                .withClaim("userRole", userRole)
                .withClaim("timeStamp", System.currentTimeMillis())
                .sign(Algorithm.HMAC256(privateKey));
    }

    /**
     * TODO
     * @date 2024/7/17
     * @methodName parseToken 解析前端返回的token
     * @param token 待解析的token
     * @return java.util.Map<java.lang.String,java.lang.String> {用户id, 用户权限, token生成时间}
     * @author Joing7
     * @throws
     *
    **/
    public Map<String, String> parseToken(String token) {
        HashMap<String, String> map = new HashMap<>();
        DecodedJWT decodedjwt = JWT.require(Algorithm.HMAC256(privateKey))
                .build().verify(token);
        Claim userId = decodedjwt.getClaim("userId");
        Claim userRole = decodedjwt.getClaim("userRole");
        Claim timeStamp = decodedjwt.getClaim("timeStamp");
        map.put("userId", userId.asString());
        map.put("userRole", userRole.asString());
        map.put("timeStamp", timeStamp.asLong().toString());
        return map;
    }
}