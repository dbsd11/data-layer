package group.bison.data.data_layer.soul_api.plugins;

import group.bison.data.data_layer.common.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.dromara.soul.common.constant.Constants;
import org.dromara.soul.common.enums.PluginEnum;
import org.dromara.soul.common.enums.RpcTypeEnum;
import org.dromara.soul.plugin.api.SoulPlugin;
import org.dromara.soul.plugin.api.SoulPluginChain;
import org.dromara.soul.plugin.api.context.SoulContext;
import org.dromara.soul.plugin.api.result.SoulResultEnum;
import org.dromara.soul.plugin.base.utils.SoulResultWarp;
import org.dromara.soul.plugin.base.utils.WebFluxResultUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class MyCookieAuthPlugin implements SoulPlugin {

    @Value("${soul.cookieAuth.host:}")
    private String cookieAuthHost;

    private ClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();

    @Override
    public Mono<Void> execute(ServerWebExchange exchange, SoulPluginChain chain) {
        //有token则依赖网关验证token，放行
        if (exchange.getRequest().getHeaders().containsKey("token")) {
            return chain.execute(exchange);
        }

        MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
        //todo 未登录的情况也要能调用接口应该怎么处理
        if (MapUtils.isEmpty(cookies) || (!cookies.containsKey("_sessiondata"))) {
            Object error = SoulResultWarp.error(SoulResultEnum.SIGN_IS_NOT_PASS.getCode(), SoulResultEnum.SIGN_IS_NOT_PASS.getMsg(), null);
            return WebFluxResultUtils.result(exchange, error);
        }

        String result = null;
        int statusCode = 200;
        try {
            //构造请求body参数
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("cookie", cookies.getFirst("_sessiondata").getValue());

            ClientHttpRequest request = clientHttpRequestFactory.createRequest(new URI(cookieAuthHost + "/group/cookie"), HttpMethod.POST);
            request.getBody().write(JSONUtil.bean2Json(requestBodyMap).getBytes());
            request.getHeaders().add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

            try (ClientHttpResponse response = request.execute()) {
                statusCode = response.getRawStatusCode();
                result = IOUtils.toString(response.getBody(), "utf-8");
                log.debug("execute cookie cookie:{} result:{}", cookies.getFirst("_sessiondata").getValue(), result);
            }
        } catch (Exception e) {
            log.warn("调用cookie auth失败", e);
        }

        if (statusCode != 200) {
            Object error = SoulResultWarp.error(SoulResultEnum.SIGN_IS_NOT_PASS.getCode(), SoulResultEnum.SIGN_IS_NOT_PASS.getMsg(), null);
            return WebFluxResultUtils.result(exchange, error);
        }

        //todo auth user data permission

        return chain.execute(exchange);
    }

    /**
     * 判断是否需要验证cookie
     *
     * @param exchange
     * @return
     */
    @Override
    public Boolean skip(ServerWebExchange exchange) {
        final SoulContext soulContext = exchange.getAttribute(Constants.CONTEXT);
        assert soulContext != null;
        return StringUtils.isEmpty(cookieAuthHost) || (!Objects.equals(RpcTypeEnum.HTTP.getName(), soulContext.getRpcType())
                && !Objects.equals(RpcTypeEnum.SPRING_CLOUD.getName(), soulContext.getRpcType()));
    }

    @Override
    public int getOrder() {
        return PluginEnum.SIGN.getCode();
    }

    @Override
    public String named() {
        return "cookieAuth";
    }
}
