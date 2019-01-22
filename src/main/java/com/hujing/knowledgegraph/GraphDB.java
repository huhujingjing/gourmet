package com.hujing.knowledgegraph;

import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.neo4j.driver.v1.Values.parameters;

public class GraphDB implements AutoCloseable {

    private static Driver driver;

    private static final String entitylbl = "Entity";
    private static final String catelbl = "Category";
    private static final String proplbl = "Property";
    private static Map<String, String> RelaType = new HashMap<>();


    public void Connect() {
        InputStream inputstream;
        try {
            Properties prop = new Properties();

            inputstream = this.getClass().getClassLoader().getResourceAsStream("bolt.properties");

            if (inputstream != null) {
                prop.load(inputstream);
                String uri = prop.getProperty("bolt.uri");
                String user = prop.getProperty("bolt.user");
                String passwd = prop.getProperty("bolt.password");

                driver = GraphDatabase.driver(uri, AuthTokens.basic(user, passwd),
                        Config.build().withMaxConnectionLifetime(30, TimeUnit.MINUTES)
                                .withMaxTransactionRetryTime(5, TimeUnit.SECONDS)
                                .withMaxConnectionPoolSize(50)
                                .withConnectionAcquisitionTimeout(2, TimeUnit.MINUTES)
                                .toConfig()
                );
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }


    public void TransferRecords(String src, String rel, String dest,
                                String fromlbl, String tolbl) {
        try {

            //removeAll();

            AddNode(fromlbl, src);
            AddNode(tolbl, dest);
            AddRelation(fromlbl, src, rel, tolbl, dest);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void AddNode(String lbl, String val) {

        Session session = null;

        try {
            session = driver.session(AccessMode.WRITE);

            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String query = "MERGE (n:" + lbl + "{name:$name}) RETURN n";

                    int result = processNode(tx, query, "name", val);
                    return result;
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }


    public void AddRelation(String fromlbl, String fromval, String relation, String tolbl, String toval) {
        Session session = null;

        try {
            session = driver.session(AccessMode.WRITE);

            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    //MATCH (e:菜名 name:''})-[r]->(c:食材 {name:''}) DELETE r
                    String query = "MATCH (e:" + fromlbl + "{name:$src})-[r]->(c:" +
                            tolbl + "{name:$dest}) DELETE r";

                    int result = processRelation(tx, query, "src", fromval, "dest", toval);
                    return result;
                }
            });

            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String query = "MATCH (e:" + fromlbl + "{name:$src}), (c:" + tolbl + "{name:$dest}) " +
                            "MERGE (e)-[r:" + relation + "]->(c) RETURN e.name, type(r), c.name";

                    int result = processRelation(tx, query, "src", fromval, "dest", toval);
                    return result;
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }

    public void AddFoodProperties(String lbl, String food, String val, String effect) {
        Session session = null;

        try {
            session = driver.session(AccessMode.WRITE);
            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String query = "MATCH (e:" + lbl + ") WHERE  e.name = \'" + food + "\' SET e.material = \'" + val +
                            "\',e.effect=\'" + effect + "\' RETURN e";
                    int result = processNode(tx, query, "", "");
                    return result;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }

    public void AddFoodmakeProperties(String lbl, String food_name, String val, int love) {
        Session session = null;

        try {
            session = driver.session(AccessMode.WRITE);
            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String query = null;
                    if (val.contains("\'")) {
                        String newval = val.replace("\'", "’");

                        query = "MATCH (e:" + lbl + ") WHERE  e.name = \'" + food_name + "\' SET e.main_imfor = \'"
                                + newval + "\',e.love=\'" + love + "\' RETURN e";
                    }else {
                        query = "MATCH (e:" + lbl + ") WHERE  e.name = \'" + food_name + "\' SET e.main_imfor = \'"
                                + val + "\',e.love=\'" + love + "\' RETURN e";
                    }
                    int result = processNode(tx, query, "", "");
                    return result;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }

    public void removeAll() {
        Session session = null;

        try {
            session = driver.session(AccessMode.WRITE);

            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String query = "MATCH (e:" + entitylbl + ") DETACH DELETE e";
                    int result = processNode(tx, query, "", "");
                    return result;
                }
            });

            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String query = "MATCH (c:" + catelbl + ") DETACH DELETE c";
                    int result = processNode(tx, query, "", "");
                    return result;
                }
            });

            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String query = "MATCH (c:" + proplbl + ") DETACH DELETE c";
                    int result = processNode(tx, query, "", "");
                    return result;
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }

    public void removeNode(String name) {
        Session session = null;

        try {
            session = driver.session(AccessMode.WRITE);

            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    String query = "MATCH (e) WHERE e.name='" + name + "' DETACH DELETE e";
                    int result = processNode(tx, query, "", "");

                    return result;
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }


    private int processNode(Transaction tx, String query, String label, String val) {
        tx.run(query, parameters(label, val));
        return 1;
    }

    private int processRelation(Transaction tx, String query, String srclbl,
                                String srcname, String destlbl, String destname) {
        tx.run(query, parameters(srclbl, srcname, destlbl, destname));
        return 1;
    }
}
