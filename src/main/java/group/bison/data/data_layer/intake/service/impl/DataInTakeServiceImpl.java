package group.bison.data.data_layer.intake.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import group.bison.data.data_layer.domain.model.Metrics;
import group.bison.data.data_layer.intake.domain.dto.MetricsMessageDto;
import group.bison.data.data_layer.intake.domain.model.DataInTakeConfig;
import group.bison.data.data_layer.intake.domain.model.DataInTakeContext;
import group.bison.data.data_layer.intake.service.DataInTakeService;
import group.bison.data.data_layer.intake.service.DataLabelParseService;
import group.bison.data.data_layer.intake.service.DataSplitService;
import group.bison.data.data_layer.storage.dao.MetricsDao;
import group.bison.data.data_layer.common.tools.DataInTakeConfigTool;
import group.bison.data.data_layer.common.utils.GrokUtil;
import group.bison.data.data_layer.common.utils.MD5Util;
import group.bison.data.data_layer.common.utils.ObjectFlatUtil;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by diaobisong on 2020/7/2.
 */
@Service
public class DataInTakeServiceImpl implements DataInTakeService {

    @Autowired
    private DataLabelParseService dataLabelParseService;

    @Autowired
    private DataSplitService dataSplitService;

    @Autowired(required = false)
    @Qualifier("psqlMetricsDaoImpl")
    private MetricsDao metricsDao;

    @Override
    public int intake(MetricsMessageDto metricsMessageDto) {
        if (metricsMessageDto.getBody() == null) {
            return 0;
        }

        DataInTakeConfig dataInTakeConfig = DataInTakeConfigTool.getConfigById(metricsMessageDto.getBody().getConfigId());
        if (dataInTakeConfig == null) {
            return 0;
        }

        JSONObject contentJSON = parseContentJson(dataInTakeConfig, metricsMessageDto.getBody().getContent());

        Metrics metrics = new Metrics();
        metrics.setName(String.join("#", metricsMessageDto.getTopic(), MD5Util.md5(metricsMessageDto.getFrom())));
        metrics.setValue(contentJSON.getString(dataInTakeConfig.getValueKey()));
        metrics.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(metricsMessageDto.getTimestamp()), ZoneId.systemDefault()));

        DataInTakeContext dataInTakeContext = new DataInTakeContext(dataInTakeConfig, contentJSON, metricsMessageDto.getTimestamp());

        LinkedHashMap<String, String> dataLabelsMap = dataLabelParseService.parse(dataInTakeContext, metrics);
        metrics.setLabels(dataLabelsMap);

        List<Metrics> splitMetricsList = dataSplitService.split(dataInTakeContext, metrics);
        for (Metrics splitMetrics : splitMetricsList) {
            if (metricsDao != null) {
                metricsDao.write(splitMetrics);
            }
        }
        return 0;
    }

    JSONObject parseContentJson(DataInTakeConfig dataInTakeConfig, String content) {
        JSONObject labelJson = new JSONObject();
        try {
            labelJson.putAll(ObjectFlatUtil.mapFlatMapToMap(JSON.parseObject(content)));
        } catch (Exception e) {
        }

        if (labelJson.isEmpty()) {
            try {
                labelJson.putAll(GrokUtil.match(dataInTakeConfig.getContentGrok(), content));
            } catch (Exception e) {
            }
        }

        if (MapUtils.isNotEmpty(dataInTakeConfig.getStaticLabelsMap())) {
            labelJson.putAll(dataInTakeConfig.getStaticLabelsMap());
        }

        return labelJson;
    }
}
