package group.bison.data.data_layer.intake.domain.model;

import lombok.Data;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by diaobisong on 2020/7/2.
 */
@Data
public class DataInTakeConfig {

    private String configId;

    private String contentGrok;

    private LinkedHashMap<String, String> staticLabelsMap;

    private List<String> labelKeyList;

    private List<String> groupByKeyList;

    private String segmentType;

    private String segmentKey;

    private String valueKey;

    private String dataTimeFormat;

    private Duration duration;
}
