package com.hujing.gourmet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

//扫描neo4j库操作包路径，一定要有而且要配置正确
@EnableNeo4jRepositories(basePackages = {"com.hujing.repository." })
//扫描neo4j实体类包路径，重申一定要有而且要配置正确
@EntityScan(basePackages = {"com.hujing.entity."})
@SpringBootApplication
public class GourmetApplication {

    public static void main(String[] args) {
        SpringApplication.run(GourmetApplication.class, args);
    }

}

