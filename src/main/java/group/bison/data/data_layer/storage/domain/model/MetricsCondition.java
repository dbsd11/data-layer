package group.bison.data.data_layer.storage.domain.model;

import lombok.Data;

import java.util.Map;
import java.util.Optional;

/**
 * Created by diaobisong on 2020/7/5.
 */
@Data
public class MetricsCondition {

    private String metricsName;

    private String groupKeyValue;

    private Optional<String> groupKeyOptional = Optional.empty();

    private Optional<Map<String, String>> labelsMapOptional = Optional.empty();

    private Optional<Long> timestampFromOptional = Optional.empty();

    private Optional<Long> timestampToOptional = Optional.empty();

    private Optional<String> segmentKeyValueOptional = Optional.empty();

    private Optional<String> segmentKeyValueFromOptional = Optional.empty();

    private Optional<String> segmentKeyValueToOptional = Optional.empty();

    private Optional<String> sqlOptional = Optional.empty();
}
