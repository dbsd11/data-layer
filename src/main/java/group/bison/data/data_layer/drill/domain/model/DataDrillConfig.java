package group.bison.data.data_layer.drill.domain.model;

import lombok.Data;

import java.util.List;

/**
 * Created by diaobisong on 2020/7/5.
 */
@Data
public class DataDrillConfig {

    private String configId;

    private String segmentKey;

    private String segmentFromKey;

    private String segmentToKey;

    private List<String> groupByKeyList;

    private List<String> labelKeyList;

    private String dynamicSql;
}
