package group.bison.data.data_layer.drill.api;

import group.bison.data.data_layer.domain.model.Metrics;
import group.bison.data.data_layer.drill.domain.dto.MetricsDrillDto;

import java.util.List;

/**
 * Created by diaobisong on 2020/7/5.
 */
public interface DataDrillApi {

    public List<Metrics> drill(MetricsDrillDto metricsDrillDto);
}
