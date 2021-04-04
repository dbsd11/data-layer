package group.bison.data.data_layer.common.tools;

import group.bison.data.data_layer.intake.domain.model.DataInTakeConfig;
import org.apache.commons.collections.MapUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by diaobisong on 2020/7/2.
 */
public class DataInTakeConfigTool {

    private static volatile Map<String, DataInTakeConfig> dataInTakeConfigMap = new ConcurrentHashMap<>();

    public static DataInTakeConfig getConfigById(String configId) {
        if (configId == null || MapUtils.isEmpty(dataInTakeConfigMap)) {
            return null;
        }

        return dataInTakeConfigMap.get(configId);
    }

    public static void updateConfig(String configId, DataInTakeConfig dataInTakeConfig) {
        if (!dataInTakeConfigMap.containsKey(configId)) {
            dataInTakeConfigMap.put(configId, dataInTakeConfig);
        }
    }
}
