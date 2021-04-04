package group.bison.data.data_layer.common.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 扁平化处理相关工具类
 * <p>
 * Created by diaobisong on 2019/9/17.
 */
@Slf4j
public class ObjectFlatUtil {

    private ObjectFlatUtil() {
    }


    /**
     * 嵌套结构mapStr转换成扁平化mapStr, 以.分隔
     *
     * @param map
     * @return Map
     */
    public static Map<String,Object> mapFlatMapToMap(JSONObject map) {
        if (ObjectUtils.isEmpty(map)) {
            return null;
        }

        try {
            Map<String, Object> flatMap = new HashMap<>();
            getFlatMap(flatMap, map, "");
            return flatMap;
        } catch (Exception e) {
            log.error("map扁平化处理失败", e);
            return null;
        }
    }


    static void getFlatMap(Map<String, Object> flatMap, JSONObject jsonObject, String prefix) {
        if (jsonObject == null) {
            return;
        }

        //所有嵌套的key最大长度不能超过100个字符，不然可能嵌套过多不再递推解析
        if (prefix.length() > 100) {
            return;
        }

        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String key = String.join("", prefix, StringUtils.isEmpty(prefix) ? "" : ".", entry.getKey());
            Object value = entry.getValue();

            if (value instanceof JSONObject) {
                log.info("开始解析key:{}", key);
                getFlatMap(flatMap, (JSONObject) value, key);
            } else {
                flatMap.put(key, value);
            }
        }
    }
}
