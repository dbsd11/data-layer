package group.bison.data.data_layer.drill.service.impl;

import group.bison.data.data_layer.domain.model.Metrics;
import group.bison.data.data_layer.drill.domain.model.PageResult;
import group.bison.data.data_layer.drill.domain.model.DataDrillContext;
import group.bison.data.data_layer.drill.service.DataMergeService;
import group.bison.data.data_layer.storage.dao.MetricsDao;
import group.bison.data.data_layer.storage.domain.entity.MetricsEntity;
import group.bison.data.data_layer.storage.domain.model.MetricsCondition;
import group.bison.data.data_layer.storage.domain.model.PsqlMetricsCondition;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by diaobisong on 2020/7/5.
 */
@Service
public class DataMergeServiceImpl implements DataMergeService {

    @Autowired(required = false)
    @Qualifier("psqlMetricsDaoImpl")
    private MetricsDao psqlMetricsDao;

    @Override
    public PageResult<Metrics> merge(List<MetricsCondition> metricsConditionList, DataDrillContext dataDrillContext, PageRequest pageRequest) {
        long count = 0;
        List<MetricsEntity> mergeMetricsEntityList = new LinkedList<>();

        for (MetricsCondition metricsCondition : metricsConditionList) {
            if (pageRequest.getPageNumber() == 0 && pageRequest.getPageSize() == 1) {
                MetricsEntity metricsEntity = metricsCondition instanceof PsqlMetricsCondition ? psqlMetricsDao.read(metricsCondition) : null;
                if (metricsEntity != null) {
                    count += 1;
                    mergeMetricsEntityList.add(metricsEntity);
                }
            } else {
                PageResult<MetricsEntity> metricsEntityPage = metricsCondition instanceof PsqlMetricsCondition ?
                        psqlMetricsDao.readPage(metricsCondition, PageRequest.of(Math.max(pageRequest.getPageNumber() - Long.valueOf(count / pageRequest.getPageSize()).intValue(), 0), pageRequest.getPageSize())) : null;
                count += metricsEntityPage.getCount();
                if (CollectionUtils.isNotEmpty(metricsEntityPage.getData())) {
                    mergeMetricsEntityList.addAll(metricsEntityPage.getData());
                }
            }
        }

        List<Metrics> mergeMetricsList = mergeMetricsEntityList.stream().map(Metrics::fromEntity).collect(Collectors.toList());

        PageResult<Metrics> pageResult = PageResult.<Metrics>builder()
                .count(count)
                .currentPage(Long.valueOf(pageRequest.getPageNumber()))
                .pageSize(Long.valueOf(pageRequest.getPageSize()))
                .data(mergeMetricsList.subList(0, Math.min(mergeMetricsList.size(), pageRequest.getPageSize())))
                .build();
        return pageResult;
    }
}
