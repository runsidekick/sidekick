package com.runsidekick.api.security;

import com.runsidekick.api.service.ApiAuthService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author yasin.kalafat
 */
@RequiredArgsConstructor
public class AuthenticationFilter extends GenericFilterBean {

    private final Logger logger = LogManager.getLogger(getClass());

    private static final String API_KEY = "ApiKey";
    private static final String AUTH_ATTRIBUTE_NAME = "sidekick.auth";

    private final ApiAuthService apiAuthService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (doFilterInternal(request, response)) {
            chain.doFilter(request, response);
        }
    }

    private boolean doFilterInternal(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            auth = (Authentication) request.getAttribute(AUTH_ATTRIBUTE_NAME);
            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
                return true;
            }
        }

        Optional<String> apiKey = Optional.ofNullable(httpRequest.getHeader(API_KEY));

        boolean authenticated = false;
        String errorMessage = null;

        if (apiKey.isPresent()) {
            try {
                if (!apiAuthService.getApiToken().equals(apiKey.get())) {
                    errorMessage = "Invalid API key: " + apiKey.get();
                } else {
                    authenticated = true;
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        if (!authenticated) {
            SecurityContextHolder.clearContext();
            if (apiKey.isPresent()) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
            }
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No API key provided");
            return false;
        }


        auth = new ApiKeyAuthentication(apiKey.get());
        request.setAttribute(AUTH_ATTRIBUTE_NAME, auth);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return true;
    }

}