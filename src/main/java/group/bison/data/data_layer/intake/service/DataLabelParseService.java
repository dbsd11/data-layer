package group.bison.data.data_layer.intake.service;

import group.bison.data.data_layer.domain.model.Metrics;
import group.bison.data.data_layer.intake.domain.model.DataInTakeContext;

import java.util.LinkedHashMap;

/**
 * Created by diaobisong on 2020/7/2.
 */
public interface DataLabelParseService {

    LinkedHashMap<String, String> parse(DataInTakeContext dataInTakeContext, Metrics metrics);
}
