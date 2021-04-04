package group.bison.data.data_layer;

import org.dromara.soul.web.configuration.SoulConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.reactive.config.EnableWebFlux;


/**
 * 启动类
 */
@SpringBootApplication(scanBasePackages = "group.bison.data.data_layer", exclude = {WebMvcAutoConfiguration.class, DataSourceAutoConfiguration.class, ErrorMvcAutoConfiguration.class, SoulConfiguration.class})
@EnableWebFlux
@EnableCaching
public class WebApplication {
    private static final Logger log = LoggerFactory.getLogger(WebApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication.run(WebApplication.class, args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
