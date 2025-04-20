package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.connection.RiderDataSource;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.spring.api.DBRider;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@DBRider
@DBUnit(caseInsensitiveStrategy = Orthography.LOWERCASE)
class AdminRepositoryTest {

    @Container
    static MySQLContainer<?> MY_SQL_FIXTURE = MySQLFixture.getInstance();

    static {
        JdbcDatabaseDelegate jdbcDatabaseDelegate = new JdbcDatabaseDelegate(MY_SQL_FIXTURE, "");
        ScriptUtils.runInitScript(jdbcDatabaseDelegate, "mock/sql/admin.sql");
    }

    private final DatabaseOperation databaseOperation = DatabaseOperation.INSERT;

    @Autowired
    private AdminRepository adminRepository;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_FIXTURE::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_FIXTURE::getUsername);
        registry.add("spring.datasource.password", MY_SQL_FIXTURE::getPassword);
    }

    @Test
    @DisplayName("어드민 조회 시 성공합니다.")
    @DataSet(
        value = "mock/json/admin.json",
        strategy = SeedStrategy.INSERT,
        cleanBefore = true,
        disableConstraints = true,
        cleanAfter = true,
        transactional = true
    )
    void 어드민조회시성공() {
        // GIVEN
        // WHEN
        List<Admin> all = adminRepository.findAll();

        // THEN
        assertFalse(all.isEmpty());
        all.stream().map(Admin::getAdmIdx).forEach(System.out::println);
    }

    @Test
    @DisplayName("데이터베이스실행")
    void 데이터베이스실행() throws SQLException, DatabaseUnitException, IOException {
        // GIVEN
        Connection dbConnection = DriverManager.getConnection(
            MY_SQL_FIXTURE.getJdbcUrl(),
            MY_SQL_FIXTURE.getUsername(),
            MY_SQL_FIXTURE.getPassword()
        );
        ConnectionHolderImpl connectionHolder = new ConnectionHolderImpl(dbConnection);
        DataSetExecutorImpl dataSetExecutor = DataSetExecutorImpl.instance(connectionHolder);
        IDataSet iDataSet = dataSetExecutor.loadDataSet("mock/json/admin.json");

        // WHEN
        RiderDataSource riderDataSource = dataSetExecutor.getRiderDataSource();
        DatabaseConnection dbUnitConnection = riderDataSource.getDBUnitConnection();
        databaseOperation.execute(dbUnitConnection, iDataSet);

        // THEN
        try (
            PreparedStatement stmt = dbConnection.prepareStatement("SELECT adm_idx FROM admin LIMIT 1");
            ResultSet rs = stmt.executeQuery()
        ) {
            boolean hasValue = rs.next();
            assertTrue(hasValue);
            int admIdx = rs.getInt("adm_idx");
            assertTrue(admIdx > 0);
            System.out.println("adm_idx = " + admIdx);
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Query failed due to SQLException: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("데이터베이스연결")
    void 데이터베이스연결() throws SQLException {
        // GIVEN
        // WHEN
        try (Connection connection = DriverManager.getConnection(
            MY_SQL_FIXTURE.getJdbcUrl(),
            MY_SQL_FIXTURE.getUsername(),
            MY_SQL_FIXTURE.getPassword())
        ) {

            // THEN
            assertTrue(connection.isValid(2), "Database connection is not valid");
        }
    }

    @Test
    @DisplayName("JSON 데이터셋 파싱에 성공합니다.")
    void JSON데이터셋파싱성공() throws SQLException, DataSetException, IOException {
        // GIVEN
        Connection dbConnection = DriverManager.getConnection(
            MY_SQL_FIXTURE.getJdbcUrl(),
            MY_SQL_FIXTURE.getUsername(),
            MY_SQL_FIXTURE.getPassword()
        );
        ConnectionHolderImpl connectionHolder = new ConnectionHolderImpl(dbConnection);
        DataSetExecutorImpl dataSetExecutor = DataSetExecutorImpl.instance(connectionHolder);

        // WHEN
        IDataSet iDataSet = dataSetExecutor.loadDataSet("mock/json/admin.json");

        // THEN
        ITableIterator iterator = iDataSet.iterator();
        while (iterator.next()) {
            ITable table = iterator.getTable();
            ITableMetaData tableMetaData = table.getTableMetaData();
            String tableName = tableMetaData.getTableName();
            Column[] primaryKeys = tableMetaData.getPrimaryKeys();
            Column[] columns = tableMetaData.getColumns();
            System.out.println("tableName = " + tableName);
            System.out.println("primaryKeys = " + Arrays.toString(primaryKeys));
            System.out.println("columns = " + Arrays.toString(columns));
        }
    }
}