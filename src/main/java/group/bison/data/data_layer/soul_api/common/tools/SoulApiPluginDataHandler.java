package group.bison.data.data_layer.soul_api.common.tools;

import group.bison.data.data_layer.common.utils.JSONUtil;
import group.bison.data.data_layer.soul_api.common.enums.SoulApiEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.soul.common.dto.PluginData;
import org.dromara.soul.common.dto.RuleData;
import org.dromara.soul.common.dto.SelectorData;
import org.dromara.soul.common.dto.convert.DivideUpstream;
import org.dromara.soul.common.dto.convert.rule.DivideRuleHandle;
import org.dromara.soul.common.enums.PluginEnum;
import org.dromara.soul.common.utils.GsonUtils;
import org.dromara.soul.plugin.base.cache.BaseDataCache;
import org.dromara.soul.plugin.base.handler.PluginDataHandler;
import org.dromara.soul.plugin.divide.cache.UpstreamCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SoulApiPluginDataHandler implements PluginDataHandler {

    @Override
    public String pluginNamed() {
        return "soulApi";
    }

    @Override
    public void handlerPlugin(PluginData pluginData) {
        String pluginDataConfig = pluginData.getConfig();
        try {
            Map<String, List<Map>> apiPropertiesMap = JSONUtil.json2Bean(pluginDataConfig, HashMap.class);
            for (Map.Entry<String, List<Map>> apiPropertiesEntry : apiPropertiesMap.entrySet()) {
                if (apiPropertiesEntry.getKey().equalsIgnoreCase("divide") && CollectionUtils.isNotEmpty(apiPropertiesEntry.getValue())) {
                    apiPropertiesEntry.getValue().forEach(divideConfig -> updateDivideConfig(divideConfig));
                    continue;
                }
                SoulApiEnum soulApiEnum = SoulApiEnum.valueOf(apiPropertiesEntry.getKey().toUpperCase());
                apiPropertiesEntry.getValue().forEach(apiProperties -> SoulApiPluginDataCache.cacheApiProperties(soulApiEnum, (String) apiProperties.get("apiId"), new Properties() {{
                    putAll(apiProperties);
                }}));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    void updateDivideConfig(Map divideConfig) {
        if (MapUtils.isEmpty(divideConfig)) {
            return;
        }

        if (divideConfig.containsKey("timeout")) {
            CompletableFuture.runAsync(() -> {
                while (true) {
                    Long timeout = Long.valueOf(divideConfig.get("timeout").toString());
                    List<SelectorData> selectorDataList = BaseDataCache.getInstance().obtainSelectorData(PluginEnum.DIVIDE.getName());
                    if (CollectionUtils.isEmpty(selectorDataList)) {
                        String synKey = "updateDivideTimeoutKey";
                        synchronized (synKey) {
                            try {
                                synKey.wait(2000);
                            } catch (InterruptedException e) {
                            }
                        }
                        continue;
                    }

                    List<RuleData> ruleDataList = selectorDataList.stream().flatMap(selectorData -> BaseDataCache.getInstance().obtainRuleData(selectorData.getId()).stream()).collect(Collectors.toList());
                    ruleDataList.forEach(ruleData -> {
                        if (StringUtils.isEmpty(ruleData.getHandle())) {
                            return;
                        }

                        DivideRuleHandle handle = GsonUtils.getInstance().fromJson(ruleData.getHandle(), DivideRuleHandle.class);
                        handle.setTimeout(timeout);
                        ruleData.setHandle(GsonUtils.getInstance().toJson(handle));
                        BaseDataCache.getInstance().cacheRuleData(ruleData);
                    });

                    log.info("updateDivideConfig timeout:{}", timeout);
                    break;
                }
            });

        }

        if (divideConfig.containsKey("useLocalStream")) {
            CompletableFuture.runAsync(() -> {
                while (true) {
                    String useSelectorName = divideConfig.get("useLocalStream").toString();
                    List<SelectorData> selectorDataList = BaseDataCache.getInstance().obtainSelectorData(PluginEnum.DIVIDE.getName());
                    if (CollectionUtils.isEmpty(selectorDataList)) {
                        String synKey = "updateDivideUseLocalStreamKey";
                        synchronized (synKey) {
                            try {
                                synKey.wait(2000);
                            } catch (InterruptedException e) {
                            }
                        }
                        continue;
                    }

                    SelectorData useLocalStreamSelectorData = selectorDataList.stream().filter(selectorData -> selectorData.getName().equalsIgnoreCase(useSelectorName)).findAny().orElse(null);
                    if (useLocalStreamSelectorData == null) {
                        break;
                    }

                    if (StringUtils.isEmpty(useLocalStreamSelectorData.getHandle())) {
                        DivideUpstream defaultLocalStream = new DivideUpstream();
                        defaultLocalStream.setProtocol("http://");
                        defaultLocalStream.setUpstreamHost("localhost");
                        defaultLocalStream.setUpstreamUrl("127.0.0.1:50000");
                        defaultLocalStream.setWeight(100);
                        useLocalStreamSelectorData.setHandle(GsonUtils.getInstance().toJson(Collections.singletonList(defaultLocalStream)));
                    } else {
                        List<DivideUpstream> upstreamList = GsonUtils.getInstance().fromList(useLocalStreamSelectorData.getHandle(), DivideUpstream.class);
                        upstreamList.forEach(divideUpstream -> {
                            divideUpstream.setProtocol("http://");
                            divideUpstream.setUpstreamHost("localhost");
                            divideUpstream.setUpstreamUrl("127.0.0.1:50000");
                            divideUpstream.setWeight(100);
                        });
                        useLocalStreamSelectorData.setHandle(GsonUtils.getInstance().toJson(upstreamList));
                    }

                    UpstreamCacheManager.getInstance().submit(useLocalStreamSelectorData);

                    log.info("updateDivideConfig useLocalStream:{}", useSelectorName);
                    break;
                }
            });
        }
    }

    public static class SoulApiPluginDataCache {

        public static final String SOUL_API_ENUM_KEY = "soulApiEnum";

        private static final Map<String, Properties> soulApiPropertiesMap = new ConcurrentHashMap<>();

        private static final PathMatcher pathMatcher = new AntPathMatcher();

        static void cacheApiProperties(SoulApiEnum soulApiEnum, String apiId, Properties properties) {
            properties.put("soulApiEnum", soulApiEnum);
            soulApiPropertiesMap.put(apiId, properties);
        }

        public static Properties getApiProperties(String apiId) {
            Properties apiProperties = new Properties();
            soulApiPropertiesMap.entrySet().forEach(entry -> {
                String templateApiId = entry.getKey();
                if (pathMatcher.match(templateApiId, apiId)) {
                    Map<String, String> templateVariables = pathMatcher.extractUriTemplateVariables(templateApiId, apiId);
                    apiProperties.putAll(templateVariables);
                    apiProperties.putAll(entry.getValue());
                }
            });
            return apiProperties;
        }
    }

}
