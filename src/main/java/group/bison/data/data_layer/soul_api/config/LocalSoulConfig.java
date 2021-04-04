package group.bison.data.data_layer.soul_api.config;

import group.bison.data.data_layer.common.utils.JSONUtil;
import group.bison.data.data_layer.soul_api.plugins.MyCookieAuthPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.soul.plugin.api.RemoteAddressResolver;
import org.dromara.soul.plugin.api.SoulPlugin;
import org.dromara.soul.plugin.api.SoulPluginChain;
import org.dromara.soul.plugin.api.dubbo.DubboParamResolveService;
import org.dromara.soul.plugin.base.cache.CommonPluginDataSubscriber;
import org.dromara.soul.plugin.base.handler.PluginDataHandler;
import org.dromara.soul.plugin.httpclient.WebClientPlugin;
import org.dromara.soul.plugin.httpclient.response.WebClientResponsePlugin;
import org.dromara.soul.sync.data.api.PluginDataSubscriber;
import org.dromara.soul.web.config.SoulConfig;
import org.dromara.soul.web.configuration.ErrorHandlerConfiguration;
import org.dromara.soul.web.configuration.SoulConfiguration;
import org.dromara.soul.web.configuration.SoulExtConfiguration;
import org.dromara.soul.web.configuration.SpringExtConfiguration;
import org.dromara.soul.web.dubbo.DefaultDubboParamResolveService;
import org.dromara.soul.web.dubbo.DubboMultiParameterResolveServiceImpl;
import org.dromara.soul.web.filter.CrossFilter;
import org.dromara.soul.web.filter.FileSizeFilter;
import org.dromara.soul.web.filter.TimeWebFilter;
import org.dromara.soul.web.filter.WebSocketParamFilter;
import org.dromara.soul.web.forwarde.ForwardedRemoteAddressResolver;
import org.dromara.soul.web.handler.SoulWebHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.EntityResponse;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.netty.http.client.HttpClient;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SoulConfiguration.
 *
 * @author xiaoyu(Myth)
 */
@Configuration
@ComponentScan(value = "org.dromara.soul", excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SoulConfiguration.class)})
@Import(value = {ErrorHandlerConfiguration.class, SoulExtConfiguration.class, SpringExtConfiguration.class})
@Slf4j
public class LocalSoulConfig {

    public static final String SOUL_API_HEADER_FORWARD_FROM = "forward-from";

    /**
     * init SoulWebHandler.
     *
     * @param plugins this plugins is All impl SoulPlugin.
     * @return {@linkplain SoulWebHandler}
     */
    @Bean("soulWebHandler")
    public SoulWebHandler soulWebHandler(final ObjectProvider<List<SoulPlugin>> plugins) {
        List<SoulPlugin> pluginList = plugins.getIfAvailable(Collections::emptyList);
        final List<SoulPlugin> soulPlugins = pluginList.stream()
                .sorted(Comparator.comparingInt(SoulPlugin::getOrder)).collect(Collectors.toList());
        soulPlugins.forEach(soulPlugin -> log.info("loader plugin:[{}] [{}]", soulPlugin.named(), soulPlugin.getClass().getName()));
        SoulWebHandler soulWebHandler = new SoulWebHandler(soulPlugins);
        return soulWebHandler;
    }

    @Bean("soulApiRouterFunction")
    public RouterFunction soulApiRouterFunction(@org.springframework.beans.factory.annotation.Value("${soul.http.contextPath}") String soulApiContextPath, SoulWebHandler soulWebHandler, CacheManager cacheManager) {
        RouterFunction soulApiFunction = RouterFunctions.route(RequestPredicates.path(soulApiContextPath + "/**"), request -> {
            try {
                ServerWebExchange serverWebExchange = null;
                Field[] fields = request.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().isAssignableFrom(ServerWebExchange.class)) {
                        field.setAccessible(true);
                        serverWebExchange = (ServerWebExchange) field.get(request);
                        break;
                    }
                }

                Field requestHttpHeadersField = HttpHeaders.class.getDeclaredField("headers");
                requestHttpHeadersField.setAccessible(true);

                MultiValueMap<String, String> httpHeaders = (MultiValueMap<String, String>) requestHttpHeadersField.get(serverWebExchange.getRequest().getHeaders());
                httpHeaders.add(SOUL_API_HEADER_FORWARD_FROM, "soul-api");

                //判断是否缓存生效，如失效则直接请求
                String etag = request.headers().header(HttpHeaders.ETAG).stream().reduce(String::join).orElse(null);
                if (StringUtils.isEmpty(etag) || !serverWebExchange.checkNotModified(null, Instant.now())) {
                    return soulWebHandler.handle(serverWebExchange).then(Mono.empty());
                }

                Cache soulApiCache = cacheManager.getCache("soulApiCache");
                final ServerWebExchange finalServerWebExchange = serverWebExchange;

                return CacheMono.<String, Object>lookup(key -> Mono.justOrEmpty(Optional.ofNullable(soulApiCache.get(key)).orElse(() -> null).get()).map(Signal::next), etag)
                        .onCacheMissResume(soulWebHandler.handle(serverWebExchange).then(Mono.fromCallable(() -> {
                            ClientResponse clientResponse = finalServerWebExchange.getAttribute("webHandlerClientResponse");
                            String responseContent = finalServerWebExchange.getAttribute("responseContent");

                            if (clientResponse == null || StringUtils.isEmpty(responseContent)) {
                                return Mono.empty();
                            }

                            Object responseBody = responseContent;
                            if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(clientResponse != null ? clientResponse.headers().asHttpHeaders().getContentType() : null)) {
                                responseBody = JSONUtil.json2Bean(responseContent, HashMap.class);
                            }
                            return EntityResponse.fromObject(responseBody)
                                    .status(clientResponse != null ? clientResponse.statusCode() : HttpStatus.BAD_REQUEST)
                                    .headers(clientResponse != null ? clientResponse.headers().asHttpHeaders() : HttpHeaders.EMPTY)
                                    .build();
                        })))
                        .andWriteWith((key, signal) ->
                                Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> soulApiCache.put(key, value))))
                        .map(response -> {
                            if (finalServerWebExchange.getAttribute("webHandlerClientResponse") != null) {
                                //Do nothing, webHandlerClientPlugin will write content
                                return Mono.<ServerResponse>empty();
                            } else {
                                return (Mono<EntityResponse>) response;
                            }
                        }).transform(response -> response.flatMap(Function.identity()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return Mono.empty();
        });
        return soulApiFunction;
    }

    @Bean("proxyRouterFunction")
    public RouterFunction proxyRouterFunction(SoulWebHandler soulWebHandler) {
        RouterFunction proxyRouterFunction = RouterFunctions.route(RequestPredicates.path("/data-proxy/**"), request -> {
            try {
                ServerWebExchange serverWebExchange = null;
                Field[] fields = request.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().isAssignableFrom(ServerWebExchange.class)) {
                        field.setAccessible(true);
                        serverWebExchange = (ServerWebExchange) field.get(request);
                        break;
                    }
                }

                return soulWebHandler.handle(serverWebExchange).then(Mono.empty());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return Mono.empty();
        });
        return proxyRouterFunction;
    }

    /**
     * Plugin data subscriber plugin data subscriber.
     *
     * @param pluginDataHandlerList the plugin data handler list
     * @return the plugin data subscriber
     */
    @Bean
    public PluginDataSubscriber pluginDataSubscriber(final ObjectProvider<List<PluginDataHandler>> pluginDataHandlerList) {
        return new CommonPluginDataSubscriber(pluginDataHandlerList.getIfAvailable(Collections::emptyList));
    }

    /**
     * Generic param resolve service generic param resolve service.
     *
     * @return the generic param resolve service
     */
    @Bean
    @ConditionalOnProperty(name = "soul.dubbo.parameter", havingValue = "multi")
    public DubboParamResolveService dubboMultiParameterResolveServiceImpl() {
        return new DubboMultiParameterResolveServiceImpl();
    }

    /**
     * Generic param resolve service dubbo param resolve service.
     *
     * @return the dubbo param resolve service
     */
    @Bean
    @ConditionalOnMissingBean(value = DubboParamResolveService.class, search = SearchStrategy.ALL)
    public DubboParamResolveService defaultDubboParamResolveService() {
        return new DefaultDubboParamResolveService();
    }

    /**
     * Remote address resolver remote address resolver.
     *
     * @return the remote address resolver
     */
    @Bean
    @ConditionalOnMissingBean(RemoteAddressResolver.class)
    public RemoteAddressResolver remoteAddressResolver() {
        return new ForwardedRemoteAddressResolver(1);
    }

    /**
     * Cross filter web filter.
     * if you application has cross-domain.
     * this is demo.
     * 1. Customize webflux's cross-domain requests.
     * 2. Spring bean Sort is greater than -1.
     *
     * @return the web filter
     */
    @Bean
    @Order(-100)
    @ConditionalOnProperty(name = "soul.cross.enabled", havingValue = "true")
    public WebFilter crossFilter() {
        return new CrossFilter();
    }

    /**
     * Body web filter web filter.
     *
     * @return the web filter
     */
    @Bean
    @Order(-10)
    @ConditionalOnProperty(name = "soul.file.enabled", havingValue = "true")
    public WebFilter fileSizeFilter() {
        return new FileSizeFilter();
    }


    /**
     * Soul config soul config.
     *
     * @return the soul config
     */
    @Bean
    @ConfigurationProperties(prefix = "soul")
    public SoulConfig soulConfig() {
        return new SoulConfig();
    }

    /**
     * init time web filter.
     *
     * @param soulConfig the soul config
     * @return {@linkplain TimeWebFilter}
     */
    @Bean
    @Order(30)
    @ConditionalOnProperty(name = "soul.filterTimeEnable")
    public WebFilter timeWebFilter(final SoulConfig soulConfig) {
        return new TimeWebFilter(soulConfig);
    }

    /**
     * Web socket web filter web filter.
     *
     * @return the web filter
     */
    @Bean
    @Order(4)
    public WebFilter webSocketWebFilter() {
        return new WebSocketParamFilter();
    }

    @Configuration
    @ConditionalOnProperty(
            name = {"soul.httpclient.strategy"},
            havingValue = "localWebClient"
    )
    static class WebClientConfiguration {
        WebClientConfiguration() {
        }

        @Bean
        public SoulPlugin webClientPlugin(final ObjectProvider<HttpClient> httpClient) {
            WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector((HttpClient) Objects.requireNonNull(httpClient.getIfAvailable()))).build();
            return new WebClientPlugin(webClient);
        }

        @Bean
        public SoulPlugin webClientResponsePlugin() {
            return new WebClientResponsePlugin() {
                @Override
                public Mono<Void> execute(ServerWebExchange exchange, SoulPluginChain chain) {
                    return chain.execute(exchange).then(Mono.fromRunnable(() -> {
                        ClientResponse clientResponse = exchange.getAttribute("webHandlerClientResponse");
                        if (clientResponse != null) {
                            //proxy clientResponse: write responseContent
                            Object proxyClientResponse = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{ClientResponse.class}, new InvocationHandler() {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                    Object value = method.invoke(clientResponse, args);
                                    if (method.getName().equals("body")) {
                                        value = DataBufferUtils.join((Flux<DataBuffer>) value).doOnNext(dataBuffer ->
                                                exchange.getAttributes().put("responseContent", dataBuffer.toString(Charset.forName("utf-8")))
                                        );
                                    }
                                    return value;
                                }
                            });
                            exchange.getAttributes().put("webHandlerClientResponse", proxyClientResponse);
                        }
                    })).then(super.execute(exchange, chain));
                }
            };
        }

        @Bean
        public SoulPlugin signPlugin() {
            return new MyCookieAuthPlugin();
        }
    }
}

