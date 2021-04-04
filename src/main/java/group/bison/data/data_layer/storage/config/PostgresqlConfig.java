package group.bison.data.data_layer.storage.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import group.bison.data.data_layer.storage.common.CommonDelegateDataSource;
import group.bison.data.data_layer.storage.common.CommonJdbcTemplate;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * Created by diaobisong on 2020/7/3.
 */
@Configuration
@ConditionalOnProperty(prefix = "postgresql",name = "url",havingValue = "true")
public class PostgresqlConfig {

    private static String driverClassName;

    private static String url=System.getenv("postgresqlHost");

    private static String username=System.getenv("postgresqlUser");;

    private static String password=System.getenv("postgresqlPassword");

    @Bean
    public CommonJdbcTemplate psqlJdbcTemplate() throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDbType("postgresql");
        druidDataSource.setDriverClassName(driverClassName);
        Properties connectionProperties = getConnectionProperties(url);
        druidDataSource.setUrl(url.contains("?") ? url.substring(0, url.indexOf("?")) : url);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        druidDataSource.setConnectionInitSqls(Collections.singletonList("select version()"));
        druidDataSource.setConnectProperties(connectionProperties);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTimeBetweenEvictionRunsMillis(600000L);
        druidDataSource.setMaxWait(30000L);
        druidDataSource.setMaxActive(300);
        druidDataSource.setUseUnfairLock(true);
        druidDataSource.setKeepAlive(true);
        druidDataSource.setEnable(true);
        druidDataSource.setInitialSize(4);
        druidDataSource.setAsyncInit(true);
        druidDataSource.setUseGlobalDataSourceStat(true);
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        druidDataSource.init();

        CommonDelegateDataSource psqlDataSource = new CommonDelegateDataSource(druidDataSource);

        CommonJdbcTemplate psqlJdbcTemplate = new CommonJdbcTemplate(psqlDataSource);
        return psqlJdbcTemplate;
    }

    public static Properties getConnectionProperties(String connectionString) {
        String propertyStr = connectionString.contains("?") ? connectionString.substring(connectionString.indexOf("?") + 1) : null;
        if (StringUtils.isEmpty(propertyStr)) {
            return new Properties();
        } else {
            Properties properties = new Properties();
            String[] propertyKVArray = propertyStr.split("&");
            String[] var5 = propertyKVArray;
            int var6 = propertyKVArray.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                String propertyKV = var5[var7];
                String key = propertyKV.contains("=") ? propertyKV.substring(0, propertyKV.indexOf("=")) : propertyKV;
                String value = propertyKV.contains("=") ? propertyKV.substring(propertyKV.indexOf("=") + 1) : null;
                if (!StringUtils.isEmpty(value)) {
                    properties.put(key, value);
                }
            }

            return properties;
        }
    }

    public static String getDriverClassName() {
        return driverClassName;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    @Value("${postgresql.driverClassName:org.postgresql.Driver}")
    public void setDriverClassName(String driverClassName) {
        PostgresqlConfig.driverClassName = driverClassName;
    }

//    @Value("${postgresql.url:jdbc:postgresql://127.0.0.1:15432/postgres?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&autoReconnect=true&useLocalSessionState=true&stringType=unspecified}")
    public void setUrl(String url) {
        PostgresqlConfig.url = url;
    }

//    @Value("${postgresql.username:postgres}")
    public void setUsername(String username) {
        PostgresqlConfig.username = username;
    }

//    @Value("${postgresql.password:postgres}")
    public void setPassword(String password) {
        PostgresqlConfig.password = password;
    }

    @Component
    public class MetricsEntityConventer implements Converter<PGobject, LinkedHashMap>, InitializingBean {

        @Override
        public LinkedHashMap convert(PGobject pGobject) {
            LinkedHashMap<String, String> labelsMap = JSON.parseObject(pGobject.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
            });
            return labelsMap;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            ((DefaultConversionService) DefaultConversionService.getSharedInstance()).addConverter(PGobject.class, LinkedHashMap.class, this);
        }
    }
}
