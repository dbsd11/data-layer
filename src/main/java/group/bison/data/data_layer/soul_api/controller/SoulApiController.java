package group.bison.data.data_layer.soul_api.controller;

import group.bison.data.data_layer.common.utils.JSONUtil;
import group.bison.data.data_layer.soul_api.common.enums.SoulApiEnum;
import group.bison.data.data_layer.soul_api.common.tools.SoulApiPluginDataHandler;
import group.bison.data.data_layer.soul_api.config.LocalSoulConfig;
import group.bison.data.data_layer.soul_api.service.SoulApiInvokeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.soul.client.springmvc.annotation.SoulSpringMvcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * soul api网关请求入口
 * 通过apiId区分请求的是哪个接口
 */
@Api(value = "soul-api")
@RestController
@RequestMapping("/api/data-layer")
@SoulSpringMvcClient(path = "/api/data-layer/**")
@Slf4j
public class SoulApiController {

    @Autowired
    private SoulApiInvokeService soulApiInvokeService;

    /**
     * 调用指定api
     *
     * @param apiId
     * @param serverHttpRequest
     * @return
     */
    @RequestMapping(value = "/{apiId}")
    @ResponseBody
    @ApiOperation(value = "get方式调用指定Api", notes = "get方式调用指定Api")
    @ApiImplicitParams(
            value = {
                    @ApiImplicitParam(paramType = "path", name = "apiId", value = "apiId", required = true, dataType = "String"),
            }
    )
    public Mono<ResponseEntity<?>> request(@PathVariable(value = "apiId", required = true) String apiId, @ApiIgnore ServerHttpRequest serverHttpRequest) throws Exception {
        if (!serverHttpRequest.getHeaders().containsKey(LocalSoulConfig.SOUL_API_HEADER_FORWARD_FROM)) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Please configure your soul-api rightly."));
        }

        Properties apiProperties = SoulApiPluginDataHandler.SoulApiPluginDataCache.getApiProperties(apiId);
        if (apiProperties == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到指定Api, apiId:" + apiId));
        }

        SoulApiEnum soulApiEnum = (SoulApiEnum) apiProperties.get(SoulApiPluginDataHandler.SoulApiPluginDataCache.SOUL_API_ENUM_KEY);

        Map<String, Object> paramMap = new HashMap<>();
        if (MapUtils.isNotEmpty(serverHttpRequest.getQueryParams())) {
            paramMap.putAll(serverHttpRequest.getQueryParams().toSingleValueMap());
        }

        if (serverHttpRequest.getMethod() != HttpMethod.GET) {
            String body = IOUtils.toString(serverHttpRequest.getBody().blockFirst().asInputStream(), "utf-8");
            if (StringUtils.isNotEmpty(body)) {
                paramMap.putAll(JSONUtil.json2Bean(body, HashMap.class));
            }
        }

        Object result = soulApiInvokeService.invoke(soulApiEnum, apiProperties, paramMap);
        return Mono.just(ResponseEntity.ok().body(result));
    }
}
