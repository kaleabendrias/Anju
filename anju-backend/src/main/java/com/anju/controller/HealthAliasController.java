package com.anju.controller;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthAliasController {

    private final HealthEndpoint healthEndpoint;

    public HealthAliasController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/actuatur/health")
    public HealthComponent healthAlias() {
        return healthEndpoint.health();
    }
}
