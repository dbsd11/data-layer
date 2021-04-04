package group.bison.data.data_layer.common.tools;

import group.bison.data.data_layer.storage.common.CommonJdbcTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Created by diaobisong on 2020/7/5.
 */
@Component
public class SegmentManageTool {

    private static CommonJdbcTemplate psqlJdbcTemplate;

    public static List<String> getSegmentList(String type, SegmentRange segmentRange) {
        if (type.equalsIgnoreCase("psql")) {
            //todo
        }
        return Collections.singletonList(String.join("", segmentRange.getSegmentKeyValue()));
    }

    @Autowired(required = false)
    @Qualifier("psqlJdbcTemplate")
    public void setPsqlJdbcTemplate(CommonJdbcTemplate psqlJdbcTemplate) {
        SegmentManageTool.psqlJdbcTemplate = psqlJdbcTemplate;
    }

    @AllArgsConstructor
    @Data
    public static class SegmentRange {
        private String segmentKey;
        private String segmentKeyValue;
        private String segmentKeyValueFrom;
        private String segmentKeyValueTo;
    }
}
