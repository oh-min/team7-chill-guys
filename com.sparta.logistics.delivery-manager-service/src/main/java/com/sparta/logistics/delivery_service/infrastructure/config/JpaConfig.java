package com.sparta.logistics.delivery_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new AuditorAware<Long>() {
            @Override
            public Optional<Long> getCurrentAuditor() {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    String userId = attributes.getRequest().getHeader("X-User-Id");
                    if (userId != null) {
                        try {
                            return Optional.of(Long.parseLong(userId));
                        } catch (NumberFormatException e) {
                            return Optional.empty();
                        }
                    }
                }
                return Optional.empty();
            }
        };
    }
}
