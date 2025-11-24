package org.utn.ba.gatewayservice.configuration;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitingConfig {
  @Bean
  KeyResolver userKeyResolver() {
    return exchange -> ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .flatMap(auth -> {
          if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return Mono.just(jwt.getSubject());
          }
          return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
        })
        .switchIfEmpty(Mono.just("anonymous"));
  }
}
