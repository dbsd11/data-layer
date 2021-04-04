package group.bison.data.data_layer.common.tools;

import group.bison.data.data_layer.drill.domain.model.DataDrillConfig;
import org.apache.commons.collections.MapUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by diaobisong on 2020/7/2.
 */
public class DataDrillConfigTool {

    private static volatile Map<String, DataDrillConfig> dataDrillConfigMap = new ConcurrentHashMap<>();

    public static DataDrillConfig getConfigById(String configId) {
        if (configId == null || MapUtils.isEmpty(dataDrillConfigMap)) {
            return null;
        }

        return dataDrillConfigMap.get(configId);
    }

    public static void updateConfig(String configId, DataDrillConfig dataDrillConfig) {
        if (!dataDrillConfigMap.containsKey(configId)) {
            dataDrillConfigMap.put(configId, dataDrillConfig);
        }
    }
}
