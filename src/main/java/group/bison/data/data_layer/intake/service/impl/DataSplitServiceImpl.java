package group.bison.data.data_layer.intake.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import group.bison.data.data_layer.domain.model.Metrics;
import group.bison.data.data_layer.common.enums.SegmentTypeEnum;
import group.bison.data.data_layer.intake.domain.model.DataInTakeContext;
import group.bison.data.data_layer.intake.service.DataSplitService;
import group.bison.data.data_layer.common.utils.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by diaobisong on 2020/7/2.
 */
@Service
public class DataSplitServiceImpl implements DataSplitService {

    @Override
    public List<Metrics> split(DataInTakeContext dataInTakeContext, Metrics metrics) {
        if (CollectionUtils.isNotEmpty(dataInTakeContext.getDataInTakeConfig().getGroupByKeyList())) {
            metrics.setGroupKey(String.join(",", dataInTakeContext.getDataInTakeConfig().getGroupByKeyList()));
            metrics.setGroupKeyValue(String.join(",", dataInTakeContext.getDataInTakeConfig().getGroupByKeyList().stream()
                    .map(groupKey -> dataInTakeContext.getContentJSON().getString(groupKey)).collect(Collectors.toList())));
        }

        if (StringUtils.isEmpty(dataInTakeContext.getDataInTakeConfig().getSegmentType()) || SegmentTypeEnum.getByName(dataInTakeContext.getDataInTakeConfig().getSegmentType()) == null
                || StringUtils.isEmpty(dataInTakeContext.getDataInTakeConfig().getSegmentKey())) {
            metrics.setSegmentKey("timestamp");
            metrics.setSegmentType(SegmentTypeEnum.DATETIME);
            metrics.setSegmentKeyValue(String.valueOf(dataInTakeContext.getTimestamp()));
            return Collections.singletonList(metrics);
        }

        metrics.setSegmentKey(dataInTakeContext.getDataInTakeConfig().getSegmentKey());
        metrics.setSegmentType(SegmentTypeEnum.getByName(dataInTakeContext.getDataInTakeConfig().getSegmentType()));

        String segmentsValue = dataInTakeContext.getContentJSON().getString(dataInTakeContext.getDataInTakeConfig().getSegmentKey());
        if (StringUtils.isEmpty(segmentsValue)) {
            return Collections.singletonList(metrics);
        }

        List<Metrics> splitMetricList = new LinkedList<>();
        if (SegmentTypeEnum.DATETIME.name().equalsIgnoreCase(dataInTakeContext.getDataInTakeConfig().getSegmentType())) {
            splitMetricList.addAll(splitDateTimeMetrics(metrics, segmentsValue, dataInTakeContext.getDataInTakeConfig().getDataTimeFormat(), dataInTakeContext.getDataInTakeConfig().getDuration()));
        } else if (SegmentTypeEnum.NUMERIC.name().equalsIgnoreCase(dataInTakeContext.getDataInTakeConfig().getSegmentType())) {
            splitMetricList.addAll(splitNumericMetrics(metrics, segmentsValue, dataInTakeContext.getDataInTakeConfig().getDuration()));
        } else if (SegmentTypeEnum.STRING.name().equalsIgnoreCase(dataInTakeContext.getDataInTakeConfig().getSegmentType())) {
            splitMetricList.addAll(splitStringMetrics(metrics, segmentsValue));
        } else {
            splitMetricList.add(metrics);
        }
        return splitMetricList;
    }

    List<Metrics> splitDateTimeMetrics(Metrics metrics, String segmentsValue, String dateTimeFormat, Duration duration) {
        List<Metrics> splitMetricList = new LinkedList<>();

        Date date = DateUtil.parseDate(segmentsValue, dateTimeFormat);
        if (date != null) {
            metrics.setTimestamp(DateUtil.fromDateToLocalDateTime(date));
        }

        List<Long> timestampList = getTimestampListFromDuration(metrics.getTimestamp().getLong(ChronoField.MILLI_OF_DAY), duration);
        for (Long timestamp : timestampList) {
            Metrics splitMetric = metrics.clone();
            splitMetric.setSegmentKeyValue(String.valueOf(timestamp));
            splitMetric.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
            splitMetricList.add(splitMetric);
        }
        return splitMetricList;
    }

    List<Metrics> splitNumericMetrics(Metrics metrics, String segmentsValue, Duration duration) {
        List<Metrics> splitMetricList = new LinkedList<>();

        List<Long> timestampList = getTimestampListFromDuration(metrics.getTimestamp().getLong(ChronoField.MILLI_OF_DAY), duration);

        String segmentKeyValue = BigDecimal.valueOf(Double.valueOf(segmentsValue)).divide(BigDecimal.valueOf(timestampList.size()), 5).toString();

        for (Long timestamp : timestampList) {
            Metrics splitMetric = metrics.clone();
            splitMetric.setSegmentKeyValue(segmentKeyValue);
            splitMetric.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
            splitMetricList.add(splitMetric);
        }
        return splitMetricList;
    }

    List<Metrics> splitStringMetrics(Metrics metrics, String segmentsValue) {
        List<Metrics> splitMetricList = new LinkedList<>();
        if (segmentsValue.startsWith("[")) {
            JSONArray segmentValueArray = JSON.parseArray(segmentsValue);
            segmentValueArray.forEach(segmentKeyValueObj -> {
                Metrics splitMetric = metrics.clone();
                splitMetric.setSegmentKeyValue(String.valueOf(segmentKeyValueObj));
                splitMetricList.add(splitMetric);
            });
        } else {
            String[] segmentValueArray = segmentsValue.split(",");
            Arrays.asList(segmentValueArray).forEach(segmentKeyValueObj -> {
                Metrics splitMetric = metrics.clone();
                splitMetric.setSegmentKeyValue(String.valueOf(segmentKeyValueObj));
                splitMetricList.add(splitMetric);
            });
        }
        return splitMetricList;
    }

    List<Long> getTimestampListFromDuration(Long timestamp, Duration duration) {
        List<Long> timestampList = new LinkedList<>();
        timestampList.add(timestamp);

        if (duration != null) {
            long minutes = duration.get(ChronoUnit.MINUTES);
            long seconds = duration.get(ChronoUnit.SECONDS);
            if (minutes != 1) {
                for (int i = 1; i <= minutes; i++) {
                    timestampList.add(timestamp - i * 60 * 1000);
                }
            } else if (seconds != 1) {
                for (int i = 1; i <= seconds; i++) {
                    timestampList.add(timestamp - i * 1000);
                }
            }
        }
        return timestampList;
    }
}
