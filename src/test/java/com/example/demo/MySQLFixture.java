package com.example.demo;

import org.testcontainers.containers.MySQLContainer;

/**
 * MySQL TestContainer Bill Pugh Singleton
 */
public class MySQLFixture extends MySQLContainer<MySQLFixture> {

    private static final String IMAGE_VERSION = "mysql:8";

    private MySQLFixture() {
        super(IMAGE_VERSION);
    }

    public static MySQLFixture getInstance() {
        return SingletonHolder.INSTANCE.init("customdb", "root", "testdbsecret");
    }

    public static MySQLFixture getInstance(String databaseName, String username, String password) {
        return SingletonHolder.INSTANCE.init(databaseName, username, password);
    }

    private MySQLFixture init(String databaseName, String username, String password) {
        this
            .withDatabaseName(databaseName)
            .withUsername(username)
            .withPassword(password)
            .withReuse(true);
        this.start();
        return this;
    }



    @Override
    public void stop() {
    }

    private static class SingletonHolder {

        private static final MySQLFixture INSTANCE = createInstance();

        private static MySQLFixture createInstance() {
            return new MySQLFixture();
        }
    }
}