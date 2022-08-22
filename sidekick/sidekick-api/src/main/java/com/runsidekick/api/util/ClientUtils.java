package com.runsidekick.api.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yasin.kalafat
 */
public final class ClientUtils {

    private ClientUtils() {

    }

    public static List<String> getClientIpAddresses(HttpServletRequest httpRequest, int maxDepth) {
        List<String> clientIpAddresses = new ArrayList<>();
        String forwardedHeaderValue = httpRequest.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedHeaderValue)) {
            String[] forwardedHeaderParts = forwardedHeaderValue.split(",");
            for (String forwardedHeader : forwardedHeaderParts) {
                forwardedHeader = forwardedHeader.trim();
                if (StringUtils.hasText(forwardedHeader)) {
                    clientIpAddresses.add(forwardedHeader);
                }
            }
        }
        clientIpAddresses.add(httpRequest.getRemoteAddr());
        Collections.reverse(clientIpAddresses);
        if (maxDepth < 0 || clientIpAddresses.size() < maxDepth) {
            return clientIpAddresses;
        } else {
            return clientIpAddresses.subList(0, maxDepth);
        }
    }
}
