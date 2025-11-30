package net.cosmocat.marketplace.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLTestContainer {

    private static final PostgreSQLContainer<?> INSTANCE;

    static {
        INSTANCE = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        INSTANCE.start();

        Runtime.getRuntime().addShutdownHook(new Thread(INSTANCE::stop));
    }

    private PostgreSQLTestContainer() {
    }

    public static PostgreSQLContainer<?> getInstance() {
        return INSTANCE;
    }
}
