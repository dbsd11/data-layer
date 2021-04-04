package group.bison.data.data_layer.drill.controller;

import group.bison.data.data_layer.drill.service.DruidIoSQLQueryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.soul.client.springmvc.annotation.SoulSpringMvcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Api("druid.io查询服务")
@RestController
@RequestMapping("/api/data-layer/druidio-sql")
@SoulSpringMvcClient(path = "/api/data-layer/druidio-sql/**")
@Slf4j
public class DruidIoSQLQueryController {

    @Autowired
    private DruidIoSQLQueryService druidIoSQLQueryService;

    /**
     * druid.io 单次查询服务
     *
     * @param sql 查询sql
     */
    @RequestMapping(value = "/query", method = {RequestMethod.GET})
    @ResponseBody
    @ApiOperation(value = "查询记录", notes = "返回结果值")
    @ApiImplicitParams(
            value = {
                    @ApiImplicitParam(paramType = "query", name = "sql", value = "查询sql", required = true, dataType = "String"),
            }
    )
    public Mono<ResponseEntity<?>> query(String sql) throws Exception {
        String result = null;
        String errorMsg = null;
        try {
            result = druidIoSQLQueryService.query(sql);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            errorMsg = e.getMessage();
        }
        if (StringUtils.isNotEmpty(errorMsg)) {
            return Mono.just(ResponseEntity.status(500).body(Collections.singletonMap("errorMsg", errorMsg)));
        }

        return Mono.just(ResponseEntity.ok(Collections.singletonMap("result", result)));
    }
}
