package com.backend.global.security.util;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityUtil {
    private static String AUTH_PARAM_NAME = "Authorization";
    private static String TOKEN_PREFIX = "Bearer";

    public static String getTokenPrefix() {
        return TOKEN_PREFIX;
    }

    public static String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTH_PARAM_NAME);
        if (StringUtils.isNotEmpty(bearerToken) && StringUtils.isNotBlank(bearerToken)
                && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
