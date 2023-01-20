package com.mate.kiraly.config;

import com.mate.kiraly.dto.TokenValidateDTO;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
public class JwtAuthenticationFilterFactory extends
        AbstractGatewayFilterFactory<JwtAuthenticationFilterFactory.Config> {

    @Autowired
    WebClient.Builder webClientBuilder;

    public JwtAuthenticationFilterFactory() {
        super(Config.class);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus)  {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        return response.setComplete();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = (ServerHttpRequest) exchange.getRequest();

            final List<String> apiEndpoints = List.of("/api/auth/register", "/api/auth/token");

            Predicate<ServerHttpRequest> isApiSecured = r -> apiEndpoints.stream()
                    .noneMatch(uri -> r.getURI().getPath().contains(uri));
            if (isApiSecured.test(request)) {
                if (!request.getHeaders().containsKey("Auth-Token")) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);

                    return response.setComplete();
                }

                final String token = request.getHeaders().getOrEmpty("Auth-Token").get(0);
                return webClientBuilder.build().get().uri("http://AuthService/api/auth/validate")
                        .header("Auth-Token", token).retrieve().bodyToMono(TokenValidateDTO.class).map(
                                tokenValidateDTO -> {
                                    exchange.getRequest()
                                            .mutate()
                                            .header("auth-user-id", String.valueOf(tokenValidateDTO.getUserId()));
                                    return exchange;
                                }).flatMap(chain::filter);
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Put the configuration properties
    }
}
