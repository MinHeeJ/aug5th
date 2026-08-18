package kr.ac.knue.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import kr.ac.knue.cms.security.MenuPermissionInterceptor;

@SpringBootApplication
public class CommonAdminBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommonAdminBackendApplication.class, args);
    }

    @Bean
    WebMvcConfigurer webMvcConfigurer(MenuPermissionInterceptor menuPermissionInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(menuPermissionInterceptor).addPathPatterns("/api/**");
            }
        };
    }
}
