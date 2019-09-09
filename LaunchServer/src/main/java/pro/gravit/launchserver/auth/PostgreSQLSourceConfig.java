package pro.gravit.launchserver.auth;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import com.zaxxer.hikari.HikariDataSource;

import pro.gravit.utils.helper.LogHelper;
import pro.gravit.utils.helper.VerifyHelper;

public final class PostgreSQLSourceConfig implements AutoCloseable {
    public static final int TIMEOUT = VerifyHelper.verifyInt(
            Integer.parseUnsignedInt(System.getProperty("launcher.postgresql.idleTimeout", Integer.toString(5000))),
            VerifyHelper.POSITIVE, "launcher.postgresql.idleTimeout can't be <= 5000");
    private static final int MAX_POOL_SIZE = VerifyHelper.verifyInt(
            Integer.parseUnsignedInt(System.getProperty("launcher.postgresql.maxPoolSize", Integer.toString(3))),
            VerifyHelper.POSITIVE, "launcher.postgresql.maxPoolSize can't be <= 0");

    // Instance
    private String poolName;

    // Config
    private String address;
    private int port;
    private String username;
    private String password;
    private String database;

    // Cache
    private DataSource source;
    private boolean hikari;

    @Override
    public synchronized void close() {
        if (hikari) { // Shutdown hikari pool
            ((HikariDataSource) source).close();
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        if (source == null) { // New data source
            PGSimpleDataSource postgresqlSource = new PGSimpleDataSource();

            // Set credentials
            postgresqlSource.setServerName(address);
            postgresqlSource.setPortNumber(port);
            postgresqlSource.setUser(username);
            postgresqlSource.setPassword(password);
            postgresqlSource.setDatabaseName(database);

            // Try using HikariCP
            source = postgresqlSource;

            //noinspection Duplicates
            try {
                Class.forName("com.zaxxer.hikari.HikariDataSource");
                hikari = true; // Used for shutdown. Not instanceof because of possible classpath error

                // Set HikariCP pool
                HikariDataSource hikariSource = new HikariDataSource();
                hikariSource.setDataSource(source);

                // Set pool settings
                hikariSource.setPoolName(poolName);
                hikariSource.setMinimumIdle(0);
                hikariSource.setMaximumPoolSize(MAX_POOL_SIZE);
                hikariSource.setIdleTimeout(TIMEOUT * 1000L);

                // Replace source with hds
                source = hikariSource;
                LogHelper.info("HikariCP pooling enabled for '%s'", poolName);
            } catch (ClassNotFoundException ignored) {
                LogHelper.warning("HikariCP isn't in classpath for '%s'", poolName);
            }
        }
        return source.getConnection();
    }
}