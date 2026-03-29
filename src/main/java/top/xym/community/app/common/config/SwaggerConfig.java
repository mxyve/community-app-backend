package top.xym.community.app.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("智慧社区应用 API")
                        .contact(new Contact().name("xym" ).email("xym.@qq.com"))
                        .version("1.0")
                        .description("智慧社区应用 API 文档")
                        .license(new License().name("Apache 2.0")
                        .url("http://doc.xiaominfo.com"))
                        );
    }

    @Bean
    public GroupedOpenApi sessionApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {"top.xym.community.app.module.session"};
        return GroupedOpenApi.builder()
                .group("2")
                .displayName("Session API")
                .pathsToMatch(paths)
                .packagesToScan(packagedToMatch).build();
    }

    @Bean
    public GroupedOpenApi messageApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {"top.xym.community.app.module.message"};
        return GroupedOpenApi.builder()
                .group("3")
                .displayName("Message API")
                .pathsToMatch(paths)
                .packagesToScan(packagedToMatch).build();
    }

    @Bean
    public GroupedOpenApi communityApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {"top.xym.community.app.module.community"};
        return GroupedOpenApi.builder()
                .group("4")
                .displayName("Community API")
                .pathsToMatch(paths)
                .packagesToScan(packagedToMatch).build();
    }

    @Bean
    public GroupedOpenApi servicesApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {"top.xym.community.app.module.service"};
        return GroupedOpenApi.builder()
                .group("5")
                .displayName("Services API")
                .pathsToMatch(paths)
                .packagesToScan(packagedToMatch).build();
    }

    @Bean
    public GroupedOpenApi myProfileApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {"top.xym.community.app.module.myProfile"};
        return GroupedOpenApi.builder()
                .group("6")
                .displayName("Services API")
                .pathsToMatch(paths)
                .packagesToScan(packagedToMatch).build();
    }
}
