package net.cosmocat.marketplace.database;

import net.cosmocat.marketplace.TestContainersBaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class DatabaseSetupTest extends TestContainersBaseTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseContainerIsRunning() {
        assertTrue(postgresContainer.isRunning(), "PostgreSQL container should be running");
    }

    @Test
    void testLiquibaseMigrationsCreatedAllTables() {
        List<String> tables = jdbcTemplate.query("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name", (rs, rowNum) -> rs.getString("table_name"));

        assertNotNull(tables, "Tables list should not be null");
        assertTrue(tables.contains("categories"), "categories table should exist");
        assertTrue(tables.contains("category_tags"), "category_tags table should exist");
        assertTrue(tables.contains("products"), "products table should exist");
        assertTrue(tables.contains("cosmo_cats"), "cosmo_cats table should exist");
        assertTrue(tables.contains("users"), "users table should exist");
        assertTrue(tables.contains("orders"), "orders table should exist");
        assertTrue(tables.contains("order_items"), "order_items table should exist");

        assertTrue(tables.contains("databasechangelog"), "Liquibase tracking table should exist");
        assertTrue(tables.contains("databasechangeloglock"), "Liquibase lock table should exist");
    }

    @Test
    void testProductsTableHasCorrectColumns() {
        List<String> columns = jdbcTemplate.query("SELECT column_name FROM information_schema.columns WHERE table_name = 'products' ORDER BY ordinal_position", (rs, rowNum) -> rs.getString("column_name"));

        assertNotNull(columns, "Columns list should not be null");
        assertTrue(columns.contains("id"), "products should have id column");
        assertTrue(columns.contains("name"), "products should have name column");
        assertTrue(columns.contains("price"), "products should have price column");
        assertTrue(columns.contains("category_id"), "products should have category_id column");
        assertTrue(columns.contains("sku"), "products should have sku column");
    }

    @Test
    void testUniqueConstraintsExist() {
        Integer skuConstraintCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.table_constraints " + "WHERE constraint_type = 'UNIQUE' AND table_name = 'products' AND constraint_name LIKE '%sku%'", Integer.class);

        Integer emailConstraintCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.table_constraints " + "WHERE constraint_type = 'UNIQUE' AND table_name = 'users' AND constraint_name LIKE '%email%'", Integer.class);

        assertTrue(skuConstraintCount > 0, "Products table should have unique constraint on SKU");
        assertTrue(emailConstraintCount > 0, "Users table should have unique constraint on email");
    }

    @Test
    void testForeignKeysExist() {
        List<String> foreignKeys = jdbcTemplate.query("SELECT constraint_name FROM information_schema.table_constraints " + "WHERE constraint_type = 'FOREIGN KEY' AND table_name IN ('products', 'orders', 'order_items', 'category_tags') " + "ORDER BY constraint_name", (rs, rowNum) -> rs.getString("constraint_name"));

        assertNotNull(foreignKeys, "Foreign keys list should not be null");
        assertFalse(foreignKeys.isEmpty(), "Database should have foreign key constraints");

        assertTrue(foreignKeys.stream().anyMatch(fk -> fk.contains("products")), "Should have FK from products table");

        assertTrue(foreignKeys.stream().anyMatch(fk -> fk.contains("orders")), "Should have FK from orders table");

        assertTrue(foreignKeys.stream().anyMatch(fk -> fk.contains("order_items")), "Should have FK from order_items table");
    }

    @Test
    void testIndexesExist() {
        List<String> indexes = jdbcTemplate.query("SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND indexname LIKE 'idx_%' ORDER BY indexname", (rs, rowNum) -> rs.getString("indexname"));

        assertNotNull(indexes, "Indexes list should not be null");
        assertFalse(indexes.isEmpty(), "Database should have performance indexes");

        assertTrue(indexes.stream().anyMatch(idx -> idx.contains("category")), "Should have index on category");
        assertTrue(indexes.stream().anyMatch(idx -> idx.contains("products")), "Should have index on products");
    }
}
