package group.bison.data.data_layer.storage.api;

import group.bison.data.data_layer.drill.domain.model.PageResult;
import group.bison.data.data_layer.storage.domain.entity.MetricsEntity;
import org.springframework.data.domain.PageRequest;

/**
 * Created by diaobisong on 2020/7/2.
 */
public interface MetricsStorageApi<C> {

    public int write(MetricsEntity metricsEntity);

    public MetricsEntity read(C condition);

    public PageResult<MetricsEntity> readPage(C condition, PageRequest page);
}
