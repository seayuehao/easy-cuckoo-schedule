package org.ecs.schedule;

import org.ecs.schedule.executor.framerwork.CuckooClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class ExampleMainApplication {

    @Value("${netty.port}")
    private int port;

    @Value("${netty.url}")
    private String url;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(ExampleMainApplication.class, args);
    }

    @Bean
    public CuckooClient cuckooClient() {
        CuckooClient cuckooClient = new CuckooClient();
        cuckooClient.setServer(url + ":" + port);
        cuckooClient.setAppName("schedule-agency-example");
        cuckooClient.setClientTag("cuckooClientTagTest1");
        cuckooClient.setApplicationContext(applicationContext);
        return cuckooClient;
    }

}
