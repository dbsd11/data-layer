package group.bison.data.data_layer.intake.api;

import group.bison.data.data_layer.intake.domain.dto.MetricsMessageDto;

/**
 * Created by diaobisong on 2020/7/2.
 */
public interface DataInTakeApi {

    public int intake(MetricsMessageDto metricsMessageDto);
}
