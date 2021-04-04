package group.bison.data.data_layer.storage.domain.model;

import lombok.Data;

@Data
public class PromethusQLMetricsCondition {

    private String promql;
    private String agg;
    private Long startTimestamp;
    private Long endTimestamp;
}
