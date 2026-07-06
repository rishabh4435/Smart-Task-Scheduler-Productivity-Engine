package com.thakur.scheduler.task.security;

import java.util.List;

public class PublicEndpointRegistry {
    private PublicEndpointRegistry() {
        /* This utility class should not be instantiated */
    }

    public static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/signup",
            "/api/auth/login"
    );
}