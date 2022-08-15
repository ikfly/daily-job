package io.iifly.daily.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Data
@RefreshScope
@Configuration
@EnableScheduling
@ConfigurationProperties("job")
public class JobConf {
    private JueJinProp jueJin;

    @Data
    public static class JueJinProp {
        private String baseApi = "https://api.juejin.cn";
        private String cookie;
        private String taskName;
    }
}
