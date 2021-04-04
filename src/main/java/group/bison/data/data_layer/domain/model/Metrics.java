package group.bison.data.data_layer.domain.model;

import group.bison.data.data_layer.storage.domain.entity.MetricsEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * Created by diaobisong on 2020/7/2.
 */
@Data
public class Metrics extends MetricsEntity implements Cloneable {

    @Override
    public Metrics clone() {
        Metrics cloneMetrics = new Metrics();
        BeanUtils.copyProperties(this, cloneMetrics);
        return cloneMetrics;
    }

    public static Metrics fromEntity(MetricsEntity metricsEntity) {
        Metrics metrics = new Metrics();
        BeanUtils.copyProperties(metricsEntity, metrics);
        return metrics;
    }

    public static MetricsEntity toEntity(Metrics metrics) {
        MetricsEntity metricsEntity = new MetricsEntity();
        BeanUtils.copyProperties(metrics, metricsEntity);
        return metricsEntity;
    }
}
