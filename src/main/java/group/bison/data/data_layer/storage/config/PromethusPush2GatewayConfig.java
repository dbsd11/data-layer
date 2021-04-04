package group.bison.data.data_layer.storage.config;

import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.PushGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by diaobisong on 2020/7/5.
 */
@Configuration
@Slf4j
public class PromethusPush2GatewayConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${prometheus.pushgateway.host:localhost:9091}")
    private String pushHost;

    @Bean
    public MetricRegistry dropwizardRegistry() {
        MetricRegistry dropwizardRegistry = new MetricRegistry();
        return dropwizardRegistry;
    }

    @Bean
    public CollectorRegistry prometheusCollectorRegistry(MetricRegistry dropwizardRegistry) {
        CollectorRegistry prometheusCollectorRegistry = new CollectorRegistry();
        prometheusCollectorRegistry.register(new DropwizardExports(dropwizardRegistry));
        return prometheusCollectorRegistry;
    }

    @Bean
    public PushGateway pushGateway() {
        return new PushGateway(pushHost);
    }
}
