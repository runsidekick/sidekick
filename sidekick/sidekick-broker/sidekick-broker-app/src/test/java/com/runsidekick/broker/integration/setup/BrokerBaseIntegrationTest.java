package com.runsidekick.broker.integration.setup;

import com.runsidekick.broker.SidekickBrokerApplication;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.SessionService;
import com.runsidekick.broker.proxy.TestBrokerListener;
import com.runsidekick.broker.proxy.listener.BrokerListener;
import com.runsidekick.broker.util.Constants;
import io.thundra.swark.env.utils.EnvironmentManager;
import io.thundra.swark.env.utils.MySQLContainerConfig;
import io.thundra.swark.env.utils.MySQLContainerContext;
import io.thundra.swark.env.utils.RedisContainerContext;
import io.thundra.swark.env.utils.TestEnvironmentFactory;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * @Author tolgatakir
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {SidekickBrokerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = BrokerBaseIntegrationTest.TestContextInitializer.class)
public abstract class BrokerBaseIntegrationTest {

    protected static final int WAIT_SECS = 3;
    protected static final int ASSERT_EVENTUALLY_TIMEOUT_SECS = 30;

    protected static final String API_KEY = "test";
    protected static final String USER_TOKEN = "test";
    protected static final String CLIENT = Constants.SESSION_GROUP_ID;
    protected static final String WORKSPACE_ID = Constants.WORKSPACE_ID;

    private final Logger logger = LogManager.getLogger(getClass());

    @Autowired
    protected SessionService sessionService;

    @Autowired
    protected ContainerContextHolder containerContextHolder;

    @Autowired
    protected TestBrokerListener testBrokerListener;

    @Value("${broker.port}")
    public int port;

    public static class TestContextInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        private MySQLContainerContext mySQLContainerContext;
        private RedisContainerContext redisContainerContext;

        @Override
        @SneakyThrows
        public void initialize(ConfigurableApplicationContext applicationContext) {
            onContextInit(applicationContext);

            applicationContext.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    onContextClose(applicationContext);
                } else if (event instanceof ContextRefreshedEvent) {
                    onContextRefresh(applicationContext);
                }
            });
        }

        @SneakyThrows
        private void onContextInit(ConfigurableApplicationContext applicationContext) throws IOException {
            setupMySQL(applicationContext);
            setupRedis(applicationContext);
        }

        @SneakyThrows
        private void onContextRefresh(ConfigurableApplicationContext applicationContext) {
            ContainerContextHolder containerContextHolder = applicationContext.getBean(ContainerContextHolder.class);
            containerContextHolder.mySQLContainerContext = mySQLContainerContext;
            containerContextHolder.redisContainerContext = redisContainerContext;
        }

        @SneakyThrows
        private void onContextClose(ConfigurableApplicationContext applicationContext) {
        }

        private void setupMySQL(ConfigurableApplicationContext applicationContext) throws IOException {
            mySQLContainerContext = TestEnvironmentFactory.getOrCreateMySQL(
                    new MySQLContainerConfig().
                            withScriptTransformer((fileName, script) -> {
                                if ("create-tables.sql".equals(fileName)) {
                                    // On target environment the tracepoint cleanup schedule is 5 minutes
                                    // but we should reduce it to 5 seconds for the tests
                                    script = script.replace(
                                            "ON SCHEDULE EVERY 5 MINUTE",
                                            "ON SCHEDULE EVERY 5 SECOND");
                                }
                                return script;
                            }));

            try {
                String rootPath = System.getProperty("user.dir");
                rootPath = refineRootPath(rootPath, "sidekick-broker-app");
                rootPath = refineRootPath(rootPath, "sidekick-broker-onprem");
                rootPath = refineRootPath(rootPath, "sidekick-broker-saas");
                mySQLContainerContext.setCreateTablesSql(new File(rootPath + "mysql/create-tables.sql"));
                mySQLContainerContext.setClearTablesSql(new File(rootPath + "mysql/clear-tables.sql"));
                mySQLContainerContext.setDropTablesSql(new File(rootPath + "mysql/drop-tables.sql"));
                mySQLContainerContext.resetDB();
            } catch (SQLException e) {
                throw new IOException("Unable to init MySQL", e);
            }

            MySQLContainer mysql = mySQLContainerContext.getContainer();

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    "spring.datasource.driver-class-name=" + mysql.getDriverClassName(),
                    "spring.datasource.url=" + mysql.getJdbcUrl() + "&useSSL=false",
                    "spring.datasource.username=" + mysql.getUsername(),
                    "spring.datasource.password=" + mysql.getPassword(),
                    "jdbc.driverClassName=" + mysql.getDriverClassName(),
                    "jdbc.url=" + mysql.getJdbcUrl() + "&useSSL=false",
                    "jdbc.username=" + mysql.getUsername(),
                    "jdbc.password=" + mysql.getPassword());
        }

        private void setupRedis(ConfigurableApplicationContext applicationContext) {
            redisContainerContext = TestEnvironmentFactory.getOrCreateRedis();
            int redisPort = redisContainerContext.getPort();
            TestPropertyValues.of("redis.port=" + redisPort).applyTo(applicationContext);
            redisContainerContext.clearCache();
        }

        private String refineRootPath(String rootPath, String moduleName) {
            rootPath = rootPath.replace("sidekick/sidekick-broker/" + moduleName, "");
            rootPath = rootPath.replace("sidekick\\sidekick-broker\\" + moduleName, "");
            return rootPath;
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @Before
    public void setup() throws Exception {
        initDataSources();

        preSetup();
        doSetup();
        postSetup();
    }

    protected void preSetup() throws Exception {
        clearSessions();
        clearListeners();
    }

    protected void doSetup() throws Exception {
    }

    protected void postSetup() throws Exception {
    }

    protected void initDataSources() throws Exception {
        initMySQL();
        initES();
        initRedis();
    }

    protected void initMySQL() throws Exception {
        MySQLContainerContext mySQLContainerContext = containerContextHolder.getMySQLContainerContext();
        if (mySQLContainerContext != null) {
            EnvironmentManager.loadAndRunDBScripts(
                    mySQLContainerContext.getConnection(), ClassLoader.getSystemClassLoader(), "data.sql");
        }
    }

    protected void initES() throws Exception {
    }

    protected void initRedis() throws Exception {
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @After
    public void tearDown() throws Exception {
        try {
            preTearDown();
            doTearDown();
            postTearDown();
        } finally {
            clearDataSources();
        }
    }

    protected void preTearDown() throws Exception {
    }

    protected void doTearDown() throws Exception {
        clearSessions();
        clearListeners();
    }

    protected void postTearDown() throws Exception {
    }

    protected void clearSessions() {
        try {
            sessionService.clearSessions();
        } catch (Throwable t) {
            logger.error("Unable to clear sessions", t);
        }
    }

    protected void clearListeners() {
        try {
            testBrokerListener.clearListeners();
        } catch (Throwable t) {
            logger.error("Unable to clear listeners", t);
        }
    }

    protected void clearDataSources() {
        try {
            clearMySQL();
        } catch (Throwable t) {
            logger.error("Unable to clear MySQL", t);
        }
        try {
            clearRedis();
        } catch (Throwable t) {
            logger.error("Unable to clear Redis", t);
        }
    }

    protected void clearMySQL() throws Exception {
        MySQLContainerContext mySQLContainerContext = containerContextHolder.getMySQLContainerContext();
        if (mySQLContainerContext != null) {
            mySQLContainerContext.clearDB();
        }
    }

    private void clearRedis() throws Exception {
        RedisContainerContext redisContainerContext = containerContextHolder.getRedisContainerContext();
        if (redisContainerContext != null) {
            redisContainerContext.clearCache();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    protected CompletableFuture<JSONObject> registerForWaitingClientMessage(String id) {
        CompletableFuture<JSONObject> completableFuture = new CompletableFuture<>();
        testBrokerListener.registerListener(new BrokerListener() {
            @Override
            public void onHandleClientMessage(ChannelInfo channelInfo, JSONObject message) {
                if (id.equals(message.getString("id"))) {
                    completableFuture.complete(message);
                }
            }
        });
        return completableFuture;
    }

    protected CompletableFuture<JSONObject> registerForWaitingAppMessage(String id) {
        CompletableFuture<JSONObject> completableFuture = new CompletableFuture<>();
        testBrokerListener.registerListener(new BrokerListener() {
            @Override
            public void onHandleAppMessage(ChannelInfo channelInfo, JSONObject message) {
                if (id.equals(message.getString("id"))) {
                    completableFuture.complete(message);
                }
            }
        });
        return completableFuture;
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    protected static void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
        }
    }

    protected static void assertConnected(WebSocketClient webSocketClient) {
        boolean connected = webSocketClient.waitUntilConnected(WAIT_SECS, TimeUnit.SECONDS);
        assertTrue("Websocket should be connected", connected);
    }

    protected static void assertClosed(WebSocketClient webSocketClient) {
        boolean closed = webSocketClient.waitUntilClosed(WAIT_SECS, TimeUnit.SECONDS);
        assertTrue("Websocket should be closed", closed);
    }

    protected static void assertEventually(Runnable assertTask) {
        long deadline = System.currentTimeMillis() + (ASSERT_EVENTUALLY_TIMEOUT_SECS * 1000);
        AssertionError assertionError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                assertTask.run();
                assertionError = null;
                break;
            } catch (AssertionError e) {
                assertionError = e;
            }
            sleep(1000);
        }
        if (assertionError != null) {
            throw assertionError;
        }
    }

    protected static String getPasswordOfUser(String email) {
        return email + "$";
    }

    protected static ClientCredentials createEmptyClientCredentials() {
        return new ClientCredentials(null);
    }

    protected ClientCredentials createClientTokenCredentials(String token) {
        return new ClientCredentials(token);
    }

    protected static AppCredentials createEmptyAppCredentials() {
        return new AppCredentials(null, null);
    }

    protected static AppCredentials createAppCredentials(String apiKey, String appInstanceId) {
        return new AppCredentials(apiKey, appInstanceId);
    }

    protected static AppCredentials createAppCredentials(String apiKey, String appInstanceId,
                                                         String appName, String appStage, String appVersion) {
        return new AppCredentials(apiKey, appInstanceId, appName, appStage, appVersion);
    }

    protected static AppCredentials createAppCredentials(String apiKey, String appInstanceId,
                                                         String appName, String appStage, String appVersion,
                                                         Map<String, String> appCustomTags) {
        return new AppCredentials(apiKey, appInstanceId, appName, appStage, appVersion, appCustomTags);
    }

}
