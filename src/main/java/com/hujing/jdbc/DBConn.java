package com.hujing.jdbc;

import java.sql.Connection;

public class DBConn {
    private static Connection conn;
    private static MySQLjdbc jdbc;
    private static String url = "jdbc.url";
    private static String user = "jdbc.username";
    private static String pwd = "jdbc.password";

    public void init(String jdbcurl,String username,String password){
        jdbc = new MySQLjdbc();
        conn = jdbc.connection(jdbcurl,username,password);
    }














}
