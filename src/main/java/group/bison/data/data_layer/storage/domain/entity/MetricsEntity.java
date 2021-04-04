package group.bison.data.data_layer.storage.domain.entity;

import group.bison.data.data_layer.common.enums.SegmentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

/**
 * Created by diaobisong on 2020/7/2.
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class MetricsEntity {

    private String name;

    private String value;

    private SegmentTypeEnum segmentType;

    private String segmentKey;

    private String segmentKeyValue;

    private String groupKey;

    private String groupKeyValue;

    private LinkedHashMap<String, String> labels;

    private LocalDateTime timestamp;
}
