package group.bison.data.data_layer.soul_api.service;

import group.bison.data.data_layer.common.utils.JSONUtil;
import group.bison.data.data_layer.drill.service.DruidIoSQLQueryService;
import group.bison.data.data_layer.soul_api.common.enums.SoulApiEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@Slf4j
public class SoulApiInvokeServiceImpl implements SoulApiInvokeService {

    @Autowired
    private DruidIoSQLQueryService druidIoSQLQueryService;

    /**
     * api方法调用实现入口 todo 优化使用策略模式
     *
     * @param soulApiEnum
     * @param configMap
     * @param paramMap
     * @param <R>
     * @return
     */
    @Override
    public <R> R invoke(SoulApiEnum soulApiEnum, Properties configMap, Map<String, Object> paramMap) {
        Object invokeResult = null;
        if (soulApiEnum == SoulApiEnum.API_DRUIDIO_SQL) {
            invokeResult = queryDruidIo(configMap, paramMap);
        }
        return (R) invokeResult;
    }

    Map<String, Object> queryDruidIo(Properties configMap, Map<String, Object> paramMap) {
        String druidioSql = configMap.containsKey("druidiosql") ? configMap.getProperty("druidiosql") : null;
        if (StringUtils.isEmpty(druidioSql)) {
            return null;
        }

        druidioSql = decode(druidioSql);

        for (Map.Entry<String, Object> paramEntry : paramMap.entrySet()) {
            druidioSql = druidioSql.contains(getParamValueKey(paramEntry.getKey())) ?
                    druidioSql.replaceAll(getParamRegexKey(paramEntry.getKey()), paramEntry.getValue().toString()) :
                    druidioSql.contains(getParamValueKey1(paramEntry.getKey())) ? druidioSql.replaceAll(getParamRegexKey1(paramEntry.getKey()), paramEntry.getValue().toString()) : druidioSql;
        }

        Map<String, Object> druidIoQueryDataMap = null;
        try {
            String druidIoQueryResultStr = druidIoSQLQueryService.query(druidioSql);
            List queryResultList = StringUtils.isNotEmpty(druidIoQueryResultStr) ? JSONUtil.json2Bean(druidIoQueryResultStr, List.class) : new LinkedList();
            druidIoQueryDataMap = CollectionUtils.isNotEmpty(queryResultList) ? (Map<String, Object>) queryResultList.get(1) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return druidIoQueryDataMap;
    }

    String decode(String sql) {
        String runSql = sql;
        try {
            runSql = new String(Base64.getDecoder().decode(sql));
        } catch (Exception e) {
        }
        return runSql;
    }

    String getParamValueKey(String paramKey) {
        return StringUtils.isEmpty(paramKey) ? null : String.join("", "${", paramKey, "}");
    }

    String getParamRegexKey(String paramKey) {
        return StringUtils.isEmpty(paramKey) ? null : String.join("", "\\$\\{", paramKey, "\\}");
    }

    String getParamValueKey1(String paramKey) {
        return StringUtils.isEmpty(paramKey) ? null : String.join("", "$", paramKey, "");
    }

    String getParamRegexKey1(String paramKey) {
        return StringUtils.isEmpty(paramKey) ? null : String.join("", "\\$", paramKey, "");
    }
}
