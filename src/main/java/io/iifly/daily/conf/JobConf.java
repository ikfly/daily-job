package io.iifly.daily.conf;

import lombok.Data;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;
import java.util.List;

@Data
@RefreshScope
@Configuration("jobConf")
@EnableScheduling
@ConfigurationProperties("job")
public class JobConf {
    private JueJinProp jueJin;

    private WxProp wx;

    private WeatherProp weather;


    @Bean
    public WxMpService wxMpService() {
        //wx 配置
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        wxStorage.setAppId(wx.getAppId());
        wxStorage.setSecret(wx.getSecret());
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);
        return wxMpService;
    }

    @Data
    public static class WxProp {
        private String cron = "0 0 8 * * ?";
        private String appId;
        private String secret;
        private List<String> toUser;
        private String templateId;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate babyBirthDay;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate loveDay;
    }

    @Data
    public static class WeatherProp {
        // https://v0.yiketianqi.com/free/day?appid=43656176&appsecret=I42og6Lm&unescape=1&city=
        private String baseApi = "https://v0.yiketianqi.com/free/day"; // 推荐
        //        private String baseApi = "https://yiketianqi.com/api"; // 备用
        private String unescape = "1";
        private String appid = "43656176";
        private String appsecret = "I42og6Lm";
        private String city;
    }

    @Data
    public static class JueJinProp {
        private String cron = "0 0 8 * * ?";
        private String baseApi = "https://api.juejin.cn";
        private String cookie;
        private String taskName;
        private List<String> simulationAccess;
    }
}
