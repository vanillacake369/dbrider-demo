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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@DBRider
@DBUnit(caseInsensitiveStrategy = Orthography.LOWERCASE)
class AdminRepositoryIntegrationTest {

    @Autowired
    private AdminRepository adminRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("관리자조회")
    @DataSet(
        value = "mock/json/admin.json",
        strategy = SeedStrategy.INSERT,
        cleanBefore = true,
        disableConstraints = true,
        cleanAfter = true,
        transactional = true
    )
    void 관리자조회() throws SQLException {
        // GIVEN
        dataSource.getConnection().commit();

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
        Connection dbConnection = dataSource.getConnection();
        ConnectionHolderImpl connectionHolder = new ConnectionHolderImpl(dbConnection);
        DataSetExecutorImpl dataSetExecutor = DataSetExecutorImpl.instance(connectionHolder);
        IDataSet iDataSet = dataSetExecutor.loadDataSet("mock/json/admin.json");

        // WHEN
        RiderDataSource riderDataSource = dataSetExecutor.getRiderDataSource();
        DatabaseConnection dbUnitConnection = riderDataSource.getDBUnitConnection();
        DatabaseOperation.INSERT.execute(dbUnitConnection, iDataSet);

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
    @DisplayName("JDBC관리자조회")
    @DataSet(
        value = "mock/json/admin.json",
        strategy = SeedStrategy.INSERT,
        cleanBefore = true,
        disableConstraints = true,
//        cleanAfter = true,
        transactional = true
    )
    @Transactional
    void JDBC관리자조회() throws SQLException {
        // GIVEN
        Connection connection = dataSource.getConnection();

        // WHEN
        try (
            PreparedStatement stmt = connection.prepareStatement("SELECT adm_idx FROM admin LIMIT 1");
            ResultSet rs = stmt.executeQuery()
        ) {
            // THEN
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
}