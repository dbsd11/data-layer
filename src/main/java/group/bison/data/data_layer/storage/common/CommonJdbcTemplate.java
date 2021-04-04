package group.bison.data.data_layer.storage.common;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by diaobisong on 2020/7/3.
 */
public class CommonJdbcTemplate extends JdbcTemplate {

    private CommonDelegateDataSource commonDelagateDataSource;

    public CommonJdbcTemplate() {
    }

    public CommonJdbcTemplate(CommonDelegateDataSource commonDelagateDataSource) {
        super(commonDelagateDataSource);
        this.commonDelagateDataSource = commonDelagateDataSource;
    }

    public void dynamicChangeCatalog(String catalog) {
        commonDelagateDataSource.dynamicChangeCatalog(catalog);
    }
}
