package org.example.persistence.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class for JDBC database operations
 */
public class JdbcUtils {

    private final Properties jdbcProps;
    private static final Logger logger = LogManager.getLogger();
    private Connection instance = null;

    /**
     * Create a new JDBC utility instance with the given properties
     * @param props JDBC properties (url, user, pass)
     */
    public JdbcUtils(Properties props) {
        this.jdbcProps = props;
    }

    /**
     * Create a new database connection
     * @return A new database connection
     */
    private Connection getNewConnection() {
        logger.traceEntry();

        String url = jdbcProps.getProperty("jdbc.url");
        String user = jdbcProps.getProperty("jdbc.user");
        String pass = jdbcProps.getProperty("jdbc.pass");
        logger.info("Trying to connect to database ... {}", url);
        logger.info("User: {}", user);
        logger.info("Pass: {}", pass);

        Connection con = null;
        try {
            if (user != null && pass != null) {
                con = DriverManager.getConnection(url, user, pass);
            } else {
                con = DriverManager.getConnection(url);
            }
        } catch (SQLException e) {
            logger.error(e);
            System.out.println("Error getting connection " + e);
        }
        return con;
    }

    /**
     * Get a database connection, creating a new one if necessary
     * @return A database connection
     */
    public Connection getConnection() {
        logger.traceEntry();
        try {
            if (instance == null || instance.isClosed()) {
                instance = getNewConnection();
            }
        } catch (SQLException e) {
            logger.error(e);
            System.out.println("Error DB " + e);
        }
        logger.traceExit(instance);
        return instance;
    }
}