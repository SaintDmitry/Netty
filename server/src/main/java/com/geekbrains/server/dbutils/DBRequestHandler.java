package com.geekbrains.server.dbutils;

import com.geekbrains.common.settings.Settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;

public class DBRequestHandler {

    private static final Logger log = LogManager.getLogger();

    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(Settings.DATABASE);
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.error("DB connect error: " + e);
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("DB disconnect error: " + e);
        }
    }

    public static String authenticate(String login, String password) {
        String sql = String.format("SELECT nickname FROM users WHERE login = '%s' and password = '%s'"
                , login, password);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String nickname = rs.getString(1);
                log.info("Client authentication successful, nickname: " + nickname);
                return nickname;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("DB user authenticate error: " + e);
        }
        return null;
    }

    public static boolean registration(String nickname, String login, String password) {
        String sql = String.format("INSERT INTO users (nickname, login, password) VALUES ('%s', '%s', '%s')"
        , nickname, login, password);
        try {
            return stmt.executeUpdate(sql) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("DB user registration error: " + e);
        }
        return false;
    }
}
