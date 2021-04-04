package group.bison.data.data_layer.common.utils;

import group.bison.data.data_layer.storage.config.PostgresqlConfig;
import org.postgresql.jdbc.PgConnection;

import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by diaobisong on 2020/7/3.
 */
public class PgDynamicConnectionUtil {

    private static volatile Set<PgConnection> pgConnectionSet = new HashSet<>(64);

    public static PgConnection dynamicChangeCatalog(PgConnection pgConnection, String catalog) {
        try {
            if (!pgConnectionSet.contains(pgConnection)) {
                Properties properties = PostgresqlConfig.getConnectionProperties(PostgresqlConfig.getUrl());
                properties.put("username", PostgresqlConfig.getUsername());
                properties.put("password", PostgresqlConfig.getPassword());
                pgConnection = (PgConnection) DriverManager.getConnection(PostgresqlConfig.getUrl(), properties);
                pgConnectionSet.add(pgConnection);
            }
        } catch (Exception e) {
        }
        return pgConnection;
    }

//    private static volatile Map<PgConnection, QueryExecutor> queryExecutorMap = new ConcurrentHashMap<>(64);

//    public static void dynamicChangeCatalogV2(PgConnection pgConnection, String catalog) {
//        try {
//            if (!queryExecutorMap.containsKey(pgConnection)) {
//                Field queryExecutorField = PgConnection.class.getDeclaredField("queryExecutor");
//                queryExecutorField.setAccessible(true);
//
//                QueryExecutor oldQueryExecutor = (QueryExecutor) queryExecutorField.get(pgConnection);
//
//                Field pgStreamField = QueryExecutorBase.class.getDeclaredField("pgStream");
//                pgStreamField.setAccessible(true);
//                HostSpec hotSpec = ((PGStream) pgStreamField.get(oldQueryExecutor)).getHostSpec();
//                QueryExecutor newQueryExecutor = new ConnectionFactoryImpl().openConnectionImpl(new HostSpec[]{hotSpec}, pgConnection.getUserName(), catalog, pgConnection.getClientInfo());
//
//                queryExecutorField.set(pgConnection, newQueryExecutor);
//
//                queryExecutorMap.put(pgConnection, newQueryExecutor);
//            }
//        } catch (Exception e) {
//        }
//    }
}
