package io.iifly.daily.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.google.common.collect.Lists;
import io.iifly.daily.conf.JobConf;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.mail.internet.MimeMessage;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zh-hq
 * @Description
 * @date 2022/8/12
 */
@Slf4j
public class Utils {

    public static final String WEATHER_PATTERN = "%s %s 温度:%s℃\n%s 天气：%s  %s~%s℃";

    /**
     * 满足表达式则执行
     *
     * @param expression
     * @param runnable
     */
    public static void isTrue(boolean expression, Runnable runnable) {
        if (expression) {
            runnable.run();
        }
    }

    /**
     * 执行http请求
     *
     * @param request
     * @return
     */
    public static HttpResponse httpExecute(HttpRequest request) {
        StopWatch stopWatch = new StopWatch();
        log.info(request.getUrl() + toParamStr(request.form()) + " ==> " + Optional.ofNullable(request.bodyBytes()).map(String::new).orElse(null));
        stopWatch.start();
        HttpResponse response = request.execute();
        stopWatch.stop();
        log.info(request.getUrl() + " <== (" + stopWatch.getLastTaskTimeMillis() + "ms) " + response.body());
        Assert.isTrue(response.isOk(), response.body());
        return response;
    }

    public static String toParamStr(Map<String, Object> map) {
        if (CollectionUtils.isEmpty(map)) {
            return "";
        }
        List<String> list = Lists.newArrayList();
        map.forEach((k, v) -> {
            list.add(k + "=" + v);
        });
        return "?" + String.join("&", list);
    }


    /**
     * 拼装嵌套 Type
     *
     * @param types
     * @return
     */
    public static Type buildNestedType(Type... types) {
        if (null == types || types.length <= 0) {
            return null;
        }
        Type type = types[types.length - 1];
        for (int i = types.length - 1; i > 0; i--) {
            type = new ParameterizedTypeImpl(new Type[]{type}, null, types[i - 1]);
        }
        return type;
    }

    /**
     * 发送简单邮件
     *
     * @param mailMessage
     * @param javaMailSender
     */
    public static void sendMail(MailMessage mailMessage, JavaMailSender javaMailSender) {
        try {
            if (mailMessage instanceof SimpleMailMessage) {
                javaMailSender.send((SimpleMailMessage) mailMessage);
            } else if (mailMessage instanceof MimeMessage) {
                javaMailSender.send((MimeMessage) mailMessage);
            } else if (mailMessage instanceof MimeMessagePreparator) {
                javaMailSender.send((MimeMessagePreparator) mailMessage);
            } else {
                throw new MailParseException("mail type error!");
            }
            log.info("sendMail is success");
        } catch (MailException e) {
            log.error("sendMail is exception", e);
        }
    }

    public static void pushTemplateMessage(WxMpService wxMpService, WxMpTemplateMessage message) {
        try {
            wxMpService.getTemplateMsgService().sendTemplateMsg(message);
            log.info("pushTemplateMessage is success");
        } catch (Exception e) {
            log.error("pushTemplateMessage is exception", e);
        }
    }


    public static String birthStr(LocalDate birthday) {
        LocalDate now = LocalDate.now();
        int age = now.getYear() - birthday.getYear();
        if (now.getMonthValue() == birthday.getMonthValue() && now.getDayOfMonth() == birthday.getDayOfMonth()) {
            return String.format("宝宝，今天是你%s岁生日！要快乐哦！", age);
        }
        LocalDate nextBirthday = LocalDate.of(now.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth());
        if (now.isAfter(birthday)) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        return String.format("宝宝，还差%s天就是你%s岁生日了哦！", nextBirthday.toEpochDay() - now.toEpochDay(), age + 1);
    }

    public static String loveStr(LocalDate loveDay) {
        LocalDate now = LocalDate.now();
        int loveYear = now.getYear() - loveDay.getYear();
        if (now.getMonthValue() == loveDay.getMonthValue() && now.getDayOfMonth() == loveDay.getDayOfMonth()) {
            return String.format("亲爱的，今天是是我们相恋%s周年纪念日哦！我们已经相恋%天了！", loveYear, now.toEpochDay() - loveDay.toEpochDay());
        }
        LocalDate nextLoveday = LocalDate.of(now.getYear(), loveDay.getMonthValue(), loveDay.getDayOfMonth());
        if (now.isAfter(nextLoveday)) {
            nextLoveday = nextLoveday.plusYears(1);
        }
        return String.format("亲爱的，我们已经相恋%s天了！再过%s天就是我们%s周年纪念日了！",
                now.toEpochDay() - loveDay.toEpochDay(),
                nextLoveday.toEpochDay() - now.toEpochDay(),
                loveYear + 1
        );
    }

    public static Weather todayWeather(JobConf.WeatherProp weather) {
        HttpRequest request = HttpUtil.createGet(weather.getBaseApi())
                .form("unescape", weather.getUnescape())
                .form("version", weather.getVersion())
                .form("appid", weather.getAppid())
                .form("appsecret", weather.getAppsecret())
                .form("ext", weather.getExt())
                .form("city", weather.getCity())
                .form("cityid", weather.getCityid())
                .form("province", weather.getProvince());
        HttpResponse response = Utils.httpExecute(request);
        return JSON.parseObject(response.body(), Weather.class);
    }

    @Data
    public static class Weather {
        private String date;
        private String week;
        private String city;
        private String wea;
        private String tem;
        private String tem2;
        private String tem1;
    }

}
