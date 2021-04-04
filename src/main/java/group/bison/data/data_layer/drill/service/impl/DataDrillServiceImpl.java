package group.bison.data.data_layer.drill.service.impl;

import group.bison.data.data_layer.domain.model.Metrics;
import group.bison.data.data_layer.drill.domain.dto.MetricsDrillDto;
import group.bison.data.data_layer.drill.domain.model.DataDrillConfig;
import group.bison.data.data_layer.drill.domain.model.DataDrillContext;
import group.bison.data.data_layer.drill.domain.model.PageResult;
import group.bison.data.data_layer.drill.service.DataDrillService;
import group.bison.data.data_layer.drill.service.DataFilterService;
import group.bison.data.data_layer.drill.service.DataMergeService;
import group.bison.data.data_layer.storage.domain.model.MetricsCondition;
import group.bison.data.data_layer.common.tools.DataDrillConfigTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Created by diaobisong on 2020/7/5.
 */
@Service
public class DataDrillServiceImpl implements DataDrillService {

    @Autowired
    private DataFilterService dataFilterService;

    @Autowired
    private DataMergeService dataMergeService;

    @Override
    public List<Metrics> drill(MetricsDrillDto metricsDrillDto) {
        DataDrillConfig dataDrillConfig = DataDrillConfigTool.getConfigById(metricsDrillDto.getDataDrillConfigId());
        if (dataDrillConfig == null) {
            return Collections.emptyList();
        }

        DataDrillContext dataDrillContext = new DataDrillContext(dataDrillConfig, metricsDrillDto.getParamJSON(), metricsDrillDto.getDataTopic(), metricsDrillDto.getDataFrom(), metricsDrillDto.getTimestampRange());

        List<MetricsCondition> metricsConditionList = dataFilterService.filter(dataDrillContext);

        PageResult<Metrics> mergedMetricsPage = dataMergeService.merge(metricsConditionList, dataDrillContext, (metricsDrillDto.getFetchSize() == null || metricsDrillDto.getFetchSize() <= 1) ? PageRequest.of(0, 1) : PageRequest.of(1, metricsDrillDto.getFetchSize()));

        List<Metrics> mergedMetricsList = mergedMetricsPage.getData();
        for (Metrics metrics : mergedMetricsList) {
            metrics.setName(metrics.getName().replaceAll("#.*", ""));
        }
        return mergedMetricsList;
    }
}
