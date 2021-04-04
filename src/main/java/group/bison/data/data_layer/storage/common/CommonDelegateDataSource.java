package group.bison.data.data_layer.storage.common;

import com.alibaba.druid.pool.DruidAbstractDataSource;
import com.alibaba.druid.pool.DruidConnectionHolder;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by diaobisong on 2020/7/3.
 */
public class CommonDelegateDataSource extends DelegatingDataSource {

    private ThreadLocal<String> catalogThreadLocal = new ThreadLocal<>();

    private volatile Map<String, Connection> connectionMap = new ConcurrentHashMap<>(64);

    public CommonDelegateDataSource() {
    }

    public CommonDelegateDataSource(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(null, null);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        if (StringUtils.isEmpty(catalogThreadLocal.get())) {
            return StringUtils.isEmpty(username) ? super.getConnection() : super.getConnection(username, password);
        }

        Connection cachedConnection = connectionMap.get(catalogThreadLocal.get());
        boolean isValid = false;
        try {
            if (cachedConnection != null && cachedConnection.isValid(1000)) {
                isValid = true;
            }
        } catch (Exception e) {
        }
        if (isValid) {
            return cachedConnection;
        }


        connectionMap.remove(catalogThreadLocal.get());

        DataSource dataSource = getTargetDataSource();
        if (dataSource instanceof DruidDataSource) {
            try {
                String oldUrl = ((DruidDataSource) dataSource).getUrl();
                String newUrl = oldUrl.replace("/postgres", String.join("", "/", catalogThreadLocal.get()));

                Field jdbcUrlField = DruidAbstractDataSource.class.getDeclaredField("jdbcUrl");
                jdbcUrlField.setAccessible(true);
                jdbcUrlField.set(dataSource, newUrl);

                DruidPooledConnection druidPooledConnection = new DruidPooledConnection(new DruidConnectionHolder((DruidAbstractDataSource) dataSource, ((DruidDataSource) dataSource).createPhysicalConnection()));

                jdbcUrlField.set(dataSource, oldUrl);

                connectionMap.put(catalogThreadLocal.get(), druidPooledConnection);
            } catch (Exception e) {
            }
        }
        return connectionMap.get(catalogThreadLocal.get());
    }

    public void dynamicChangeCatalog(String catalog) {
        if (StringUtils.isNotEmpty(catalog)) {
            catalogThreadLocal.set(catalog);
        } else {
            catalogThreadLocal.remove();
        }
    }

}
