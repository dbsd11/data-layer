package group.bison.data.data_layer.intake.service.impl;

import group.bison.data.data_layer.domain.model.Metrics;
import group.bison.data.data_layer.intake.domain.model.DataInTakeContext;
import group.bison.data.data_layer.intake.service.DataLabelParseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * Created by diaobisong on 2020/7/2.
 */
@Service
public class DataLabelParseServiceImpl implements DataLabelParseService {

    @Override
    public LinkedHashMap<String, String> parse(DataInTakeContext dataInTakeContext, Metrics metrics) {
        LinkedHashMap<String, String> labelsMap = new LinkedHashMap<>();

        if (CollectionUtils.isNotEmpty(dataInTakeContext.getDataInTakeConfig().getLabelKeyList()) && MapUtils.isNotEmpty(dataInTakeContext.getContentJSON())) {
            dataInTakeContext.getDataInTakeConfig().getLabelKeyList().forEach(labelKey -> labelsMap.put(labelKey, dataInTakeContext.getContentJSON().getString(labelKey)));
        }
        return labelsMap;
    }
}
