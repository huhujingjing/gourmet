package com.hujing.knowledgegraph;

import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @Author: HUJING
 * @Date: 2018/11/12 13:43
 */
public class PushLogic implements AutoCloseable {

    private static Driver driver;

    public void connect() {
        InputStream inputStream;

        try {
            Properties prop = new Properties();
            inputStream = this.getClass().getClassLoader().getResourceAsStream("bolt.properties");

            if (inputStream != null) {
                prop.load(inputStream);
                String uri = prop.getProperty("bolt.uri");
                String user = prop.getProperty("bolt.user");
                String password = prop.getProperty("bolt.password");

                driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password),
                        Config.build().withMaxConnectionLifetime(30, TimeUnit.MINUTES)
                                .withMaxTransactionRetryTime(5, TimeUnit.SECONDS)
                                .withMaxConnectionPoolSize(50)
                                .withConnectionAcquisitionTimeout(2, TimeUnit.MINUTES)
                                .toConfig()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 操作和计算neo4j
     *
     * @param filds
     */
    public void mathNeo4J(String... filds) {

        Session session = null;
        StringBuffer sb0 = new StringBuffer();
        StringBuffer sb1 = new StringBuffer();
        StringBuffer end = new StringBuffer();

        List<String> names = new ArrayList<>();

        sb0.append("MATCH ");
        try {
            session = driver.session(AccessMode.WRITE);
            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String query = "";
                    for (int i = 0; i < filds.length; i++) {
                        if (i != filds.length - 1) {
                            sb0.append("(fild").append(i).append(")").append("<-[*0..2]-(c:菜名),");
                        } else {
                            sb0.append("(fild").append(i).append(")").append("<-[*0..2]-(c:菜名)");
                        }
                    }
                    sb1.append(" where ");
                    for (int i = 0; i < filds.length; i++) {
                        if (i != filds.length - 1) {
                            sb1.append("fild").append(i).append(".name=").append("\"")
                                    .append(filds[i]).append("\"").append(" and ");
                        } else {
                            sb1.append("fild").append(i).append(".name=").append("\"")
                                    .append(filds[i]).append("\"");
                        }
                    }
                    end.append(sb0).append(sb1).append("and c.main_imfor is not null return c.name,c.main_imfor,c.love order by toint(c.love) desc limit 10");
                    System.out.println(end.toString());
                    query = end.toString();
                    StatementResult run = tx.run(query);
                    String name = null;
                    if (run.hasNext()) {
                        while (run.hasNext()) {
                            Record record = run.next();
                            name = record.get("c.name").toString();
                            String main_imfor = record.get("c.main_imfor").toString();
                            String love = record.get("c.love").toString();
                            names.add(name);

                            System.out.println("菜名:" + name + "\n" + "简介:" + main_imfor + "\n" + "点赞量:" + love);

                            String querymaterial = "MATCH (e {name:" + name + "})-[r:有]->(s) return s.name";
                            StatementResult runmaterial = tx.run(querymaterial);
                            StringBuffer sb1 = new StringBuffer();
                            StringBuffer sb2 = new StringBuffer();
                            //查主食材
                            while (runmaterial.hasNext()) {

                                Record next = runmaterial.next();

                                sb1.append(next.get("s.name"));
                            }
                            System.out.println("主食材:" + sb1.toString().toString().replace("\"\"", ",")
                                    .replace("\"", ""));
                            //查关键字
                            String querykeyword = "MATCH (e {name:" + name + "})-[r:关键字]->(s) return s.name";
                            StatementResult runkeyword = tx.run(querykeyword);
                            while (runkeyword.hasNext()) {
                                Record next = runkeyword.next();
                                sb2.append(next.get("s.name").toString());
                            }
                            System.out.println("关键字:" + sb2.toString().replace("\"\"", ",")
                                    .replace("\"", ""));
                            System.out.println("---------------------------------------------------------------------------");
                        }
                    } else {
                        System.out.println("-----------------------------这个组合暂时没有-------------------------------");
                    }
                    return 1;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public void foodName(String... filds) {
        Session session = null;

        StringBuffer sb1 = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
        try {
            session = driver.session(AccessMode.WRITE);
            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String query = "";
                    String query1 = "";
                    String query2 = "";
                    String name = null;
                    String main_imfor = null;
                    String love = null;
                    StatementResult run = null;
                    for (int i = 0; i < filds.length; i++) {

                        query = "MATCH (e {name:'" + filds[i] + "'}) return e.name,e.main_imfor,e.love";
                        run = tx.run(query);
                        if (run.hasNext()) {
                            while (run.hasNext()) {
                                Record record = run.next();
                                name = record.get("e.name").toString();
                                if (record.get("e.main_imfor").toString() != null) {
                                    main_imfor = record.get("e.main_imfor").toString();
                                }
                                love = record.get("e.love").toString();
                                System.out.println("菜名:" + name + "\n" + "简介:" + main_imfor + "\n" + "点赞量:" + love);

                                //查主食材
                                query1 = "MATCH (e {name:" + name + "})-[r:有]->(s) return s.name";
                                run = tx.run(query1);
                                while (run.hasNext()) {
                                    Record record1 = run.next();
                                    sb1.append(record1.get("s.name").toString());

                                }
                                System.out.println("主食材:" + sb1.toString().toString().replace("\"\"", ",")
                                        .replace("\"", ""));
                                //查关键字
                                query2 = "MATCH (e {name:" + name + "})-[r:关键字]->(s) return s.name";
                                run = tx.run(query2);
                                while (run.hasNext()) {
                                    Record record1 = run.next();
                                    sb2.append(record1.get("s.name").toString());

                                }
                                System.out.println("关键字:" + sb2.toString().toString().replace("\"\"", ",")
                                        .replace("\"", ""));
                                System.out.println("---------------------------------------------------------------------------");
                            }
                        } else {
                            System.out.println("-----------------------------这个菜暂时没有-------------------------------");
                        }
                    }

                    return 1;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }
}
