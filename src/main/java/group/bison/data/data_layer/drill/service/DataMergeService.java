package group.bison.data.data_layer.drill.service;

import group.bison.data.data_layer.domain.model.Metrics;
import group.bison.data.data_layer.drill.domain.model.DataDrillContext;
import group.bison.data.data_layer.drill.domain.model.PageResult;
import group.bison.data.data_layer.storage.domain.model.MetricsCondition;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Created by diaobisong on 2020/7/5.
 */
public interface DataMergeService {

    public PageResult<Metrics> merge(List<MetricsCondition> metricsConditionList, DataDrillContext dataDrillContext, PageRequest pageRequest);
}
