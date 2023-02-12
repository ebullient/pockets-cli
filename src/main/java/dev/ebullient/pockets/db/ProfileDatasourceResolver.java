package dev.ebullient.pockets.db;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import dev.ebullient.pockets.config.PocketsConfig;
import dev.ebullient.pockets.config.PocketsConfigProvider.PocketsConfigHolder;
import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.customized.QuarkusConnectionProvider;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.quarkus.logging.Log;
import io.vertx.ext.web.RoutingContext;

@RequestScoped
@Unremovable
@PersistenceUnitExtension
public class ProfileDatasourceResolver implements TenantResolver {

    public final PocketsConfig config;
    public final RoutingContext context;

    public ProfileDatasourceResolver(PocketsConfigHolder config, Instance<RoutingContext> context) {
        Log.debugf("Initializing ProfileDatasourceResolver with pockets config: %s", config);
        this.config = config.getConfig();
        this.context = context.isResolvable() ? context.get() : null;
    }

    @Override
    public String getDefaultTenantId() {
        return config.getActiveProfile();
    }

    @Override
    public String resolveTenantId() {
        if (context == null) {
            return config.getActiveProfile();
        }
        String path = context.request().path();
        String[] parts = path.split("/");
        return parts[0];
    }

    @Unremovable
    @ApplicationScoped
    @PersistenceUnitExtension
    public static class ProfileTenantConnectionResolver implements Closeable, TenantConnectionResolver {

        private final ConcurrentHashMap<String, AgroalDataSource> dataSources = new ConcurrentHashMap<>();

        public final PocketsConfig config;

        public ProfileTenantConnectionResolver(PocketsConfigHolder config) {
            Log.debugf("Initializing ProfileDatasourceResolver with pockets config: %s", config);
            this.config = config.getConfig();
        }

        @Override
        public ConnectionProvider resolve(String tenantId) {
            return new QuarkusConnectionProvider(getTenantDataSource(tenantId));
        }

        @Override
        public void close() {
            for (Map.Entry<String, AgroalDataSource> entry : dataSources.entrySet()) {
                try {
                    entry.getValue().close();
                } catch (RuntimeException e) {
                    Log.errorf(e, "Could not close datasource %s", entry.getKey());
                }
            }
        }

        private AgroalDataSource getTenantDataSource(String tenantId) {
            return dataSources.computeIfAbsent(tenantId, this::createDataSource);
        }

        private AgroalDataSource createDataSource(String tenantId) {
            try {
                AgroalDataSourceConfigurationSupplier configurationSupplier = new AgroalDataSourceConfigurationSupplier();
                AgroalConnectionPoolConfigurationSupplier connectionPoolConfig = configurationSupplier
                        .connectionPoolConfiguration();
                AgroalConnectionFactoryConfigurationSupplier connectionFactoryConfig = connectionPoolConfig
                        .connectionFactoryConfiguration();

                String jdbcUrl = String.format("jdbc:h2:%s/%s.db", config.getJdbcUrlBase(), tenantId);
                connectionFactoryConfig.jdbcUrl(jdbcUrl);

                return AgroalDataSource.from(configurationSupplier.get());
            } catch (SQLException | RuntimeException e) {
                throw new IllegalStateException("Exception while creating datasource for tenant " + tenantId, e);
            }
        }

    }

}
