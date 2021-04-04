package group.bison.data.data_layer.soul_api.service;

import group.bison.data.data_layer.soul_api.common.enums.SoulApiEnum;

import java.util.Map;
import java.util.Properties;

public interface SoulApiInvokeService {

    public <R> R invoke(SoulApiEnum soulApiEnum, Properties configMap, Map<String, Object> paramMap);
}
