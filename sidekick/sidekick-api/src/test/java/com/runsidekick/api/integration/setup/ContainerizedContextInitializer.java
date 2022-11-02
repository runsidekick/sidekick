package com.runsidekick.api.integration.setup;

import com.opsgenie.core.util.ExceptionUtil;
import io.thundra.swark.env.utils.MySQLContainerContext;
import io.thundra.swark.env.utils.RedisContainerContext;
import io.thundra.swark.env.utils.TestEnvironmentFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.testcontainers.containers.MySQLContainer;

import java.io.File;

@Slf4j
public class ContainerizedContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private MySQLContainerContext mySQLContainerContext;
    private RedisContainerContext redisContainerContext;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            onContextInit(applicationContext);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        applicationContext.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                onContextClose();
            } else if (event instanceof ContextRefreshedEvent) {
                onContextRefresh(applicationContext);
            }
        });
    }

    private void onContextInit(ConfigurableApplicationContext applicationContext) {
        setupRedis(applicationContext);
        setupMySql(applicationContext);
    }

    private void onContextRefresh(ConfigurableApplicationContext applicationContext) {
        ContainerContextHolder containerContextHolder = applicationContext.getBean(ContainerContextHolder.class);
        containerContextHolder.mySQLContainerContext = mySQLContainerContext;
        containerContextHolder.redisContainerContext = redisContainerContext;
        initMySQL();
    }

    private void onContextClose() {
    }

    private void setupRedis(ConfigurableApplicationContext applicationContext) {
        redisContainerContext = TestEnvironmentFactory.getOrCreateRedis();
        int redisPort = redisContainerContext.getPort();
        TestPropertyValues.of("redis.port=" + redisPort).applyTo(applicationContext);
        redisContainerContext.clearCache();
    }

    private void setupMySql(ConfigurableApplicationContext applicationContext) {
        mySQLContainerContext = TestEnvironmentFactory.getOrCreateMySQL();
        MySQLContainer mysql = mySQLContainerContext.getContainer();
        TestPropertyValues.of(
                "spring.datasource.driver-class-name=" + mysql.getDriverClassName(),
                "spring.datasource.url=" + mysql.getJdbcUrl() + "&useSSL=false",
                "spring.datasource.username=" + mysql.getUsername(),
                "spring.datasource.password=" + mysql.getPassword(),
                "jdbc.driverClassName=" + mysql.getDriverClassName(),
                "jdbc.url=" + mysql.getJdbcUrl() + "&useSSL=false",
                "jdbc.username=" + mysql.getUsername(),
                "jdbc.password=" + mysql.getPassword()
        ).applyTo(applicationContext);
    }

    private void initMySQL() {
        try {
            String rootPath = System.getProperty("user.dir");
            rootPath = refineRootPath(rootPath, "sidekick-api");
            mySQLContainerContext.setCreateTablesSql(new File(rootPath + "mysql/create-tables.sql"));
            mySQLContainerContext.setClearTablesSql(new File(rootPath + "mysql/clear-tables.sql"));
            mySQLContainerContext.setDropTablesSql(new File(rootPath + "mysql/drop-tables.sql"));

            log.info("Loaded SQL scripts from " + rootPath);

            mySQLContainerContext.resetDB();
        } catch (Throwable t) {
            ExceptionUtil.sneakyThrow(t);
        }
    }

    private String refineRootPath(String rootPath, String moduleName) {
        rootPath = rootPath.replace("sidekick/" + moduleName, "");
        rootPath = rootPath.replace("sidekick\\" + moduleName, "");
        return rootPath;
    }

}
