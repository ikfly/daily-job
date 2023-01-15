package io.iifly.daily;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author zh-hq
 * @Description
 * @date 2022/8/11
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(App.class);
        // System.out.println(context.getBean(JueJinJob.class).task());
    }
}
