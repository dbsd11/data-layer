package group.bison.data.data_layer.intake.service;

import group.bison.data.data_layer.domain.model.Metrics;
import group.bison.data.data_layer.intake.domain.model.DataInTakeContext;

import java.util.List;

/**
 * Created by diaobisong on 2020/7/2.
 */
public interface DataSplitService {

    public List<Metrics> split(DataInTakeContext dataInTakeContext, Metrics metrics);
}
