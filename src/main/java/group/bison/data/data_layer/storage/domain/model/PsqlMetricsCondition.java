package group.bison.data.data_layer.storage.domain.model;

import group.bison.data.data_layer.storage.domain.entity.MetricsEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

/**
 * Created by diaobisong on 2020/7/3.
 */
@Data
public class PsqlMetricsCondition extends MetricsCondition implements Cloneable {

    private static final RowMapper<MetricsEntity> metricsEntityMapper = new BeanPropertyRowMapper(MetricsEntity.class);

    public RowMapper<MetricsEntity> getMetricsEntityMapper() {
        return metricsEntityMapper;
    }

    public String toPsql() {
        if (getSqlOptional().isPresent()) {
            return getSqlOptional().get();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.join("", "SELECT * FROM public.tb_metrics_", StringUtils.defaultString(getGroupKeyValue(), "").replaceAll("\\s+", "")));
        sb.append(" WHERE ");
        sb.append(String.join("", "name=", "'", getMetricsName(), "'"));
        sb.append(String.join("", " AND ", "groupKeyValue=", "'", getGroupKeyValue(), "'"));
        getGroupKeyOptional().ifPresent(groupKey -> sb.append(String.join("", " AND ", "groupKey=", "'", groupKey, "'")));
        getLabelsMapOptional().ifPresent(labelsMap -> labelsMap.entrySet().forEach(labelEntry -> sb.append(String.join("", " AND labels ::jsonb->>", "'", labelEntry.getKey(), "'", "=", "'", labelEntry.getValue(), "'"))));
        getTimestampFromOptional().ifPresent(timestamp -> sb.append(String.join("", " AND ", "timestamp>=to_timestamp(", String.valueOf(timestamp / 1000), ")")));
        getTimestampToOptional().ifPresent(timestamp -> sb.append(String.join("", " AND ", "timestamp<=to_timestamp（", String.valueOf(timestamp / 1000), ")")));
        return sb.toString();
    }

    @Override
    public PsqlMetricsCondition clone() {
        PsqlMetricsCondition copyPsqlMetricsCondition = new PsqlMetricsCondition();
        BeanUtils.copyProperties(this, copyPsqlMetricsCondition);
        return copyPsqlMetricsCondition;
    }
}
