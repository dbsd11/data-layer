package group.bison.data.data_layer.drill.service;

import group.bison.data.data_layer.drill.domain.model.DataDrillContext;
import group.bison.data.data_layer.storage.domain.model.MetricsCondition;

import java.util.List;

/**
 * Created by diaobisong on 2020/7/5.
 */
public interface DataFilterService {

    public List<MetricsCondition> filter(DataDrillContext dataDrillContext);
}
