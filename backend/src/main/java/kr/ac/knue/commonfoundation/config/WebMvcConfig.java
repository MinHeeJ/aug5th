package kr.ac.knue.commonfoundation.config;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiError;
import kr.ac.knue.commonfoundation.api.ApiResponse;
import kr.ac.knue.commonfoundation.auth.AuthInterceptor;
import kr.ac.knue.commonfoundation.auth.AuthService;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectProvider<AuthService> authServiceProvider;
    private final ObjectProvider<CurrentUserContext> currentUserContextProvider;

    public WebMvcConfig(ObjectProvider<AuthService> authServiceProvider, ObjectProvider<CurrentUserContext> currentUserContextProvider) {
        this.authServiceProvider = authServiceProvider;
        this.currentUserContextProvider = currentUserContextProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AuthService authService = authServiceProvider.getIfAvailable();
        CurrentUserContext currentUserContext = currentUserContextProvider.getIfAvailable();
        if (authService != null && currentUserContext != null) {
            registry.addInterceptor(new AuthInterceptor(authService, currentUserContext)).addPathPatterns("/api/**");
        }
    }

    @Bean
    RouterFunction<ServerResponse> apiEnvelopeContractProbeRoutes() {
        return RouterFunctions.route()
            .GET("/api/probe/success", request -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.ok(Map.of("value", "정상"))))
            .GET("/api/probe/forbidden", request -> ServerResponse.status(403)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail(new ApiError("FORBIDDEN", "권한이 없습니다.", null))))
            .POST("/api/probe/validate", request -> {
                Map<String, String> fields = new LinkedHashMap<>();
                fields.put("name", "이름을 입력하세요.");
                return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.fail(new ApiError("VALIDATION_ERROR", "입력값을 확인하세요.", fields)));
            })
            .build();
    }

    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }
}
