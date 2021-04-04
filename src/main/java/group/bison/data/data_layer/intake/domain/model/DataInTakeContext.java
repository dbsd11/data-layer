package group.bison.data.data_layer.intake.domain.model;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by diaobisong on 2020/7/2.
 */
@AllArgsConstructor
@Getter
public class DataInTakeContext {

    private DataInTakeConfig dataInTakeConfig;

    private JSONObject contentJSON;

    private Long timestamp;
}
