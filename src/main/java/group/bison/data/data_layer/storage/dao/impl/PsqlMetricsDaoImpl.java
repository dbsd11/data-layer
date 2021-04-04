package group.bison.data.data_layer.storage.dao.impl;

import com.alibaba.fastjson.JSON;
import group.bison.data.data_layer.drill.domain.model.PageResult;
import group.bison.data.data_layer.storage.common.CommonJdbcTemplate;
import group.bison.data.data_layer.storage.dao.MetricsDao;
import group.bison.data.data_layer.storage.domain.entity.MetricsEntity;
import group.bison.data.data_layer.storage.domain.model.PsqlMetricsCondition;
import group.bison.data.data_layer.common.tools.SegmentManageTool;
import group.bison.data.data_layer.common.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Created by diaobisong on 2020/7/3.
 */
@Component
@Slf4j
public class PsqlMetricsDaoImpl implements MetricsDao<PsqlMetricsCondition> {

    @Autowired(required = false)
    @Qualifier("psqlJdbcTemplate")
    private CommonJdbcTemplate psqlJdbcTemplate;

    @Override
    public int write(MetricsEntity metricsEntity) {
        String catalog = String.join("", "metrics_", SegmentManageTool.getSegmentList("psql", new SegmentManageTool.SegmentRange(metricsEntity.getSegmentKey(), metricsEntity.getSegmentKeyValue(), null, null)).get(0));
        String table = String.join("", "public.tb_metrics_", StringUtils.defaultString(metricsEntity.getGroupKeyValue(), "").replaceAll("\\s+", ""));

        if (log.isDebugEnabled()) {
            log.debug("insert metrics, catalog:{} table:{} data:{} ", catalog, table, JSON.toJSONString(metricsEntity));
        }

        int insertCount = 0;

        psqlJdbcTemplate.dynamicChangeCatalog(catalog);
        try {
            insertCount = psqlJdbcTemplate.update(String.join("", "insert into ", table, " (name, value, segmentType, segmentKey, segmentKeyValue, groupKey, groupKeyValue, labels, \"timestamp\") values (?, ?, ?, ?, ?, ?, ?, jsonb(?), to_timestamp(?, 'yyyy-MM-dd HH24-MI-SS'));"), new Object[]{
                    metricsEntity.getName(), metricsEntity.getValue(), metricsEntity.getSegmentType().name(), metricsEntity.getSegmentKey(), metricsEntity.getSegmentKeyValue(), metricsEntity.getGroupKey(), metricsEntity.getGroupKeyValue(), JSON.toJSONString(metricsEntity.getLabels()), DateUtil.formatDateTime(DateUtil.fromLocalDateTimeToDate(metricsEntity.getTimestamp()))});
        } finally {
            psqlJdbcTemplate.dynamicChangeCatalog(null);
        }
        return insertCount;
    }

    @Override
    public MetricsEntity read(PsqlMetricsCondition condition) {
        String catalog = String.join("", "metrics_", SegmentManageTool.getSegmentList("psql", new SegmentManageTool.SegmentRange(null, condition.getSegmentKeyValueOptional().orElse(null), condition.getSegmentKeyValueFromOptional().orElse(null), condition.getSegmentKeyValueToOptional().orElse(null))).get(0));

        String sql = condition.toPsql();
        sql = String.join("", sql, " ORDER BY id DESC LIMIT 1 ");

        MetricsEntity metricsEntity = null;

        psqlJdbcTemplate.dynamicChangeCatalog(catalog);
        try {
            metricsEntity = psqlJdbcTemplate.<MetricsEntity>queryForObject(sql, condition.getMetricsEntityMapper());
        } finally {
            psqlJdbcTemplate.dynamicChangeCatalog(null);
        }

        return metricsEntity;
    }

    @Override
    public PageResult<MetricsEntity> readPage(PsqlMetricsCondition condition, PageRequest pageRequest) {
        String catalog = String.join("", "metrics_", SegmentManageTool.getSegmentList("psql", new SegmentManageTool.SegmentRange(null, condition.getSegmentKeyValueOptional().orElse(null), condition.getSegmentKeyValueFromOptional().orElse(null), condition.getSegmentKeyValueToOptional().orElse(null))).get(0));

        PageResult<MetricsEntity> pageResult = null;

        String sql = condition.toPsql();

        long count = 0;

        psqlJdbcTemplate.dynamicChangeCatalog(catalog);
        try {
            String countSql = sql.replace("SELECT *", "SELECT count(1) as count");
            count = psqlJdbcTemplate.<MetricsEntity>queryForRowSet(countSql).getLong("count");
        } finally {
            psqlJdbcTemplate.dynamicChangeCatalog(null);
        }

        if (count == 0 || pageRequest.getPageNumber() == 0) {
            return new PageResult<>(count, 0, Long.valueOf(pageRequest.getPageNumber()), Long.valueOf(pageRequest.getPageSize()), Collections.<MetricsEntity>emptyList());
        }

        sql = String.join("", sql, " ORDER BY id DESC LIMIT ", String.valueOf((pageRequest.getPageNumber() - 1) * pageRequest.getPageSize()), ",", String.valueOf(pageRequest.getPageSize()));

        psqlJdbcTemplate.dynamicChangeCatalog(catalog);
        try {
            List<MetricsEntity> metricsEntityList = psqlJdbcTemplate.<MetricsEntity>query(sql, condition.getMetricsEntityMapper());

            pageResult = PageResult.<MetricsEntity>builder()
                    .count(count)
                    .currentPage(Long.valueOf(pageRequest.getPageNumber()))
                    .pageSize(Long.valueOf(pageRequest.getPageSize()))
                    .data(metricsEntityList)
                    .build();
        } finally {
            psqlJdbcTemplate.dynamicChangeCatalog(null);
        }

        if (pageResult == null) {
            pageResult = new PageResult<>(0L, 0, Long.valueOf(pageRequest.getPageNumber()), Long.valueOf(pageRequest.getPageSize()), Collections.<MetricsEntity>emptyList());
        }

        return pageResult;
    }
}
