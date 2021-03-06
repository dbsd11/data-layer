package group.bison.data.data_layer.drill.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

/**
 * @Author: bison
 * @Date: 20/9/14
 * <p>
 * swagger配置类
 */
@Configuration
@ConditionalOnProperty(value = "common.swagger.enabled", matchIfMissing = true)
@ConditionalOnWebApplication
@EnableSwagger2WebFlux
public class SwaggerConfig {

    @Bean
    public Docket createRestApi(ApiInfo apiInfo, @Value("${common.swagger.basePackage:}") String basePackage) {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.regex("/api/.*"))
                .build();
    }

    @Bean
    public ApiInfo apiInfo(@Value("${common.swagger.title:}") String swaggerTitle, @Value("${common.swagger.description:}") String swaggerDescription) {
        return new ApiInfoBuilder()
                .title(swaggerTitle)
                .description(swaggerDescription)
                .version("1.0")
                .build();
    }
}