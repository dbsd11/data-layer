package group.bison.data.data_layer.drill.service.impl;

import group.bison.data.data_layer.drill.domain.model.DataDrillConfig;
import group.bison.data.data_layer.drill.domain.model.DataDrillContext;
import group.bison.data.data_layer.drill.service.DataFilterService;
import group.bison.data.data_layer.storage.domain.model.MetricsCondition;
import group.bison.data.data_layer.storage.domain.model.PsqlMetricsCondition;
import group.bison.data.data_layer.common.tools.SegmentManageTool;
import group.bison.data.data_layer.common.utils.MD5Util;
import group.bison.data.data_layer.common.utils.ObjectFlatUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by diaobisong on 2020/7/5.
 */
@Service
public class DataFilterServiceImpl implements DataFilterService {

    @Override
    public List<MetricsCondition> filter(DataDrillContext dataDrillContext) {
        List<MetricsCondition> metricsConditionList = psqlFilter(dataDrillContext);
        return metricsConditionList;
    }

    List<MetricsCondition> psqlFilter(DataDrillContext dataDrillContext) {
        PsqlMetricsCondition psqlMetricsConditon = new PsqlMetricsCondition();

        String metricName = String.join("#", dataDrillContext.getDataTopic(), MD5Util.md5(dataDrillContext.getDataFrom()));
        psqlMetricsConditon.setMetricsName(metricName);

        DataDrillConfig dataDrillConfig = dataDrillContext.getDataDrillConfig();

        if (CollectionUtils.isNotEmpty(dataDrillConfig.getGroupByKeyList()) && MapUtils.isNotEmpty(dataDrillContext.getParamJSON())) {
            String groupKey = dataDrillConfig.getGroupByKeyList().stream().reduce((str1, str2) -> String.join(",", str1, str2)).orElse(null);
            String groupKeyValue = dataDrillContext.getParamJSON().containsKey(groupKey) ? dataDrillContext.getParamJSON().getString(groupKey) :
                    dataDrillConfig.getGroupByKeyList().stream().map(groupByKey -> dataDrillContext.getParamJSON().getString(groupByKey)).reduce((str1, str2) -> String.join(",", str1, str2)).orElse(null);
            psqlMetricsConditon.setGroupKeyOptional(Optional.of(groupKey));
            psqlMetricsConditon.setGroupKeyValue(groupKeyValue);
        }

        if (CollectionUtils.isNotEmpty(dataDrillConfig.getLabelKeyList()) && MapUtils.isNotEmpty(dataDrillContext.getParamJSON())) {
            Map<String, String> labelsMap = new LinkedHashMap<>(dataDrillConfig.getLabelKeyList().size());
            dataDrillConfig.getLabelKeyList().forEach(labelKey -> {
                if (dataDrillContext.getParamJSON().containsKey(labelKey)) {
                    labelsMap.put(labelKey, dataDrillContext.getParamJSON().getString(labelKey));
                }
            });
            psqlMetricsConditon.setLabelsMapOptional(Optional.of(labelsMap));
        }

        if (StringUtils.isNotEmpty(dataDrillConfig.getDynamicSql()) && MapUtils.isNotEmpty(dataDrillContext.getParamJSON())) {
            Map<String, Object> flatParamJSON = ObjectFlatUtil.mapFlatMapToMap(dataDrillContext.getParamJSON());

            String sql = parseDynamicSql(dataDrillConfig.getDynamicSql(), flatParamJSON);
            if (StringUtils.isNotEmpty(sql)) {
                psqlMetricsConditon.setSqlOptional(Optional.of(sql));
            }
        }

        if (StringUtils.isNotEmpty(dataDrillContext.getTimestampRange())) {
            String[] timestampRangeArray = dataDrillContext.getTimestampRange().split("~|-");
            if (timestampRangeArray.length > 0) {
                psqlMetricsConditon.setTimestampFromOptional(Optional.of(Long.valueOf(timestampRangeArray[0])));
            }
            if (timestampRangeArray.length > 1) {
                psqlMetricsConditon.setTimestampToOptional(Optional.of(Long.valueOf(timestampRangeArray[1])));
            }
        }

        List<MetricsCondition> segmentMetricsConditionList = new LinkedList<>();
        if (MapUtils.isNotEmpty(dataDrillContext.getParamJSON())) {
            List<String> segmentList = SegmentManageTool.getSegmentList("psql", new SegmentManageTool.SegmentRange(dataDrillConfig.getSegmentKey(), dataDrillContext.getParamJSON().getString(dataDrillConfig.getSegmentKey()),
                    dataDrillContext.getParamJSON().getString(dataDrillConfig.getSegmentFromKey()), dataDrillContext.getParamJSON().getString(dataDrillConfig.getSegmentToKey())));
            for (String segment : segmentList) {
                PsqlMetricsCondition segmentMetricsCondition = psqlMetricsConditon.clone();
                segmentMetricsCondition.setSegmentKeyValueOptional(Optional.of(segment));
                segmentMetricsConditionList.add(segmentMetricsCondition);
            }
        }

        if (CollectionUtils.isEmpty(segmentMetricsConditionList)) {
            segmentMetricsConditionList.add(psqlMetricsConditon);
        }

        return segmentMetricsConditionList;
    }

    String parseDynamicSql(String sql, Map<String, Object> paramMap) {
        for (Map.Entry<String, Object> paramEntry : paramMap.entrySet()) {
            sql = sql.replaceAll(String.join("", "\\$\\{", paramEntry.getKey(), "\\}"), String.valueOf(paramEntry.getValue()));
        }
        return sql;
    }
}
