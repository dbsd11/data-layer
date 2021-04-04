package group.bison.data.data_layer.drill.service.impl;

import group.bison.data.data_layer.common.utils.HttpClientFactoryUtil;
import group.bison.data.data_layer.common.utils.JSONUtil;
import group.bison.data.data_layer.drill.service.DruidIoSQLQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DruidIoSQLQueryServiceImpl implements DruidIoSQLQueryService, InitializingBean {

    @Value("${druidIo.apiHost:}")
    private String apiHost;

    @Value("${druidIo.apiUser:}")
    private String apiUser;

    @Value("${druidIo.apiUserPassword:}")
    private String apiUserPassword;

    private ClientHttpRequestFactory clientHttpRequestFactory = HttpClientFactoryUtil.getClientHttpRequestFactory();

    @Override
    public String query(String sql) throws Exception {
        String result = null;
        int statusCode = 200;

        //构造请求body参数
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("header", "true");
        requestBodyMap.put("query", sql);
        requestBodyMap.put("resultFormat", "object");

        ClientHttpRequest request = clientHttpRequestFactory.createRequest(new URI(apiHost + "/druid/v2/sql"), HttpMethod.POST);
        request.getBody().write(JSONUtil.bean2Json(requestBodyMap).getBytes());
        request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(String.join("", apiUser, ":", apiUserPassword).getBytes()));
        request.getHeaders().add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

        try (ClientHttpResponse response = request.execute()) {
            statusCode = response.getRawStatusCode();
            result = IOUtils.toString(response.getBody(), "utf-8");
            log.debug("execute query:{} result:{}", sql, result);
        }

        if (statusCode != 200) {
            throw new RuntimeException(result);
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.apiHost = System.getenv("druidIoApiHost");
        this.apiUser = System.getenv("druidIoApiUser");
        this.apiUserPassword = System.getenv("druidIoApiUserPassword");
    }
}
