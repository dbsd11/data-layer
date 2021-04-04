package group.bison.data.data_layer.drill.domain.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by diaobisong on 2020/7/5.
 */
@Data
public class MetricsDrillDto implements Serializable {

    private static final long serialVersionUID = -1L;

    private String dataTopic;

    private String dataFrom;

    private Integer fetchSize;

    private String timestampRange;

    private JSONObject paramJSON;

    private String dataDrillConfigId;
}
