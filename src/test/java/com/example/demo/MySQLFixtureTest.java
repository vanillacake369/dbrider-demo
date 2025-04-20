package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MySQLFixtureTest {

    @Test
    @DisplayName("MYSQL 테스트컨테이너 연결")
    void MYSQL테스트컨테이너연결() throws SQLException {
        // GIVEN
        // WHEN
        MySQLFixture instance = MySQLFixture.getInstance();

        // THEN
        try (Connection connection = DriverManager.getConnection(
            instance.getJdbcUrl(),
            instance.getUsername(),
            instance.getPassword()
        )) {

            // THEN
            assertTrue(connection.isValid(2), "Database connection is not valid");
        }
    }

}