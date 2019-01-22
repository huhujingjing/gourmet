package com.hujing.jdbc;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class MySQLjdbc {
    public Connection connection(String jdbcurl, String username, String password) {
        Connection conn = null;
        Properties prop = new Properties();

        try {
            prop.load(this.getClass().getResourceAsStream("/jdbc.properties"));
            String url = prop.getProperty(jdbcurl);
            Class.forName(prop.getProperty("jdbc.driverClassName"));
            conn = DriverManager.getConnection(url, prop.getProperty(username), prop.getProperty(password));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("未找到驱动");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("MySQL操作错误");
            e.printStackTrace();
        } finally {
            return conn;
        }
    }

    public void close(ResultSet rs, Statement st, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (st != null) st.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
