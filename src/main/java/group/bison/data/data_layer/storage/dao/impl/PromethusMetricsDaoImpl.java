package group.bison.data.data_layer.storage.dao.impl;

import group.bison.data.data_layer.common.utils.HttpClientFactoryUtil;
import group.bison.data.data_layer.common.utils.NonBlockingInputStreamReadUtil;
import group.bison.data.data_layer.drill.domain.model.PageResult;
import group.bison.data.data_layer.storage.dao.MetricsDao;
import group.bison.data.data_layer.storage.domain.entity.MetricsEntity;
import group.bison.data.data_layer.storage.domain.model.PromethusQLMetricsCondition;
import com.jayway.jsonpath.JsonPath;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static group.bison.data.data_layer.common.utils.HttpClientFactoryUtil.GLOBAL_HTTP_READ_TIMEOUT;

@Component
@Slf4j
public class PromethusMetricsDaoImpl implements MetricsDao<PromethusQLMetricsCondition>, InitializingBean {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${prometheus.pushgateway.intervalInMillis:10000}")
    private Long intervalInMillis;

    @Value("${promethus.api.host}")
    private String apiHost;

    private ClientHttpRequestFactory clientHttpRequestFactory = HttpClientFactoryUtil.getClientHttpRequestFactory();

    @Autowired
    private PushGateway pushGateway;

    @Autowired
    private CollectorRegistry promethusRegistry;

    @Override
    public int write(MetricsEntity metricsEntity) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public MetricsEntity read(PromethusQLMetricsCondition condition) {
        MetricsEntity metricsEntity = null;
        try {
            MetricsEntity queryMetricsEntity = new MetricsEntity();
            Map<String, Double> valueWithLabelMap = StringUtils.isEmpty(condition.getAgg()) ? query(condition.getPromql()) :
                    queryRange(condition.getPromql(), condition.getAgg(), condition.getStartTimestamp(), condition.getEndTimestamp());
            LinkedHashMap<String, String> labelsMap = new LinkedHashMap<>();
            valueWithLabelMap.entrySet().forEach(entry -> labelsMap.put(entry.getKey(), String.valueOf(entry.getValue())));
            queryMetricsEntity.setLabels(labelsMap);
            metricsEntity = queryMetricsEntity;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return metricsEntity;
    }

    @Override
    public PageResult<MetricsEntity> readPage(PromethusQLMetricsCondition condition, PageRequest page) {
        throw new UnsupportedOperationException("");
    }

    private Map<String, Double> query(String query) throws Exception {
        Map<String, Double> valueWithLabelMap = new HashMap<>();

        ClientHttpRequest request = clientHttpRequestFactory.createRequest(new URI(String.format(apiHost + "/query?query=%s", URLEncoder.encode(query, "utf-8"))), HttpMethod.GET);
        try (ClientHttpResponse response = request.execute()) {
            String resultContent = IOUtils.toString(NonBlockingInputStreamReadUtil.readNulIfTimeout(response.getBody(), GLOBAL_HTTP_READ_TIMEOUT), "utf-8");
            log.debug("execute query:{} result:{}", query, resultContent);

            if (response.getRawStatusCode() != 200 || StringUtils.isEmpty(resultContent)) {
                throw new RuntimeException(resultContent);
            }

            String resultType = JsonPath.read(resultContent, "data.resultType");

            if ("vector".equalsIgnoreCase(resultType)) {
                List<String> metricStrList = JsonPath.read(resultContent, "$.data.result[*].metric");
                List<String> valueStrList = JsonPath.read(resultContent, "$.data.result[*].value[1]");
                Iterator<String> metricIterator = metricStrList.iterator();
                Iterator<String> valueIterator = valueStrList.iterator();
                while (metricIterator.hasNext() && valueIterator.hasNext()) {
                    valueWithLabelMap.put(String.valueOf(metricIterator.next()), Double.valueOf(valueIterator.next()));
                }
            }
        }
        return valueWithLabelMap;
    }

    private Map<String, Double> queryRange(String query, String agg, Long startTimestamp, Long endTimestamp) throws Exception {
        Map<String, Double> valueWithLabelMap = new HashMap<>();

        ClientHttpRequest request = clientHttpRequestFactory.createRequest(new URI(String.format(apiHost + "/query_range?query=%s&start=%s&end=%s&step=1000&_=", URLEncoder.encode(query, "utf-8"), startTimestamp, endTimestamp, System.currentTimeMillis())), HttpMethod.GET);
        try (ClientHttpResponse response = request.execute()) {
            String resultContent = IOUtils.toString(NonBlockingInputStreamReadUtil.readNulIfTimeout(response.getBody(), GLOBAL_HTTP_READ_TIMEOUT), "utf-8");
            log.debug("execute range query:{} result:{}", query, resultContent);

            if (response.getRawStatusCode() != 200 || StringUtils.isEmpty(resultContent)) {
                throw new RuntimeException(resultContent);
            }

            String resultType = JsonPath.read(resultContent, "data.resultType");

            if ("matrix".equalsIgnoreCase(resultType)) {
                JSONArray metricStrArray = JsonPath.read(resultContent, "$.data.result[*].metric");
                JSONArray valueStrArrayArray = JsonPath.read(resultContent, "$.data.result[*].values");
                Iterator metricIterator = metricStrArray.iterator();
                Iterator valueArrayListIterator = valueStrArrayArray.iterator();
                double value = 0.0;
                List<JSONArray> valueArrayList = null;
                while (metricIterator.hasNext() && valueArrayListIterator.hasNext()) {
                    valueArrayList = (List<JSONArray>) valueArrayListIterator.next();
                    switch (agg) {
                        case "max":
                            value = valueArrayList.stream().mapToDouble(valueArray -> Double.valueOf(valueArray.get(1).toString())).max().orElse(0.0);
                            break;
                        case "sum":
                            value = valueArrayList.stream().mapToDouble(valueArray -> Double.valueOf(valueArray.get(1).toString())).sum();
                            break;
                        case "none":
                            value = valueArrayList.stream().mapToDouble(valueArray -> Double.valueOf(valueArray.get(1).toString())).findFirst().orElse(0.0);
                        default:
                            break;
                    }
                    valueWithLabelMap.put(String.valueOf(metricIterator.next()), value);
                }
            }
        }

        return valueWithLabelMap;
    }

    public void pushAll() {
        try {
            pushGateway.push(promethusRegistry, applicationName);
        } catch (Exception e) {
            log.error("pushAll失败", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//            try {
//                pushAll();
//            } catch (Exception e) {
//                log.error("scheduled pushAll失败", e);
//            }
//        }, 1000, intervalInMillis, TimeUnit.MILLISECONDS);
    }
}
