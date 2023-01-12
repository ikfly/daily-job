package io.iifly.daily.util;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.google.common.collect.Lists;
import io.iifly.daily.conf.JobConf;
import lombok.Data;
import lombok.experimental.Accessors;
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
import org.springframework.util.StringUtils;

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

    private static final String BLANK = "\u3164";
    public static final String SHANBAY_API = "https://apiv3.shanbay.com/weapps/dailyquote/quote/?date=%s";
    public static final String YOUDAO_API = "https://dict.youdao.com/infoline?apiversion=5.0&date=%s";
    public static final String AA1_WENAN_YINGWEN_API = "https://v.api.aa1.cn/api/api-wenan-yingwen/index.php?type=json";
    public static final String AA1_RENJIAN_API = "https://v.api.aa1.cn/api/api-renjian/index.php?type=json";

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
        if (now.isAfter(nextBirthday)) {
            nextBirthday = nextBirthday.plusYears(1);
            age += 1;
        }
        return String.format("宝宝，还差%s天就是你%s岁生日了哦！", nextBirthday.toEpochDay() - now.toEpochDay(), age);
    }

    public static String loveStr(LocalDate loveDay) {
        LocalDate now = LocalDate.now();
        int loveYear = now.getYear() - loveDay.getYear();
        if (now.getMonthValue() == loveDay.getMonthValue() && now.getDayOfMonth() == loveDay.getDayOfMonth()) {
            return String.format("亲爱的，今天是我们相恋%s周年纪念日哦！我们已经相恋%s天了！", loveYear, now.toEpochDay() - loveDay.toEpochDay() + 1);
        }
        LocalDate nextLoveDay = LocalDate.of(now.getYear(), loveDay.getMonthValue(), loveDay.getDayOfMonth());
        if (now.isAfter(nextLoveDay)) {
            nextLoveDay = nextLoveDay.plusYears(1);
            loveYear += 1;
        }
        return String.format("亲爱的，我们已经相恋%s天了！再过%s天就是我们%s周年纪念日了！",
                now.toEpochDay() - loveDay.toEpochDay() + 1,
                nextLoveDay.toEpochDay() - now.toEpochDay(),
                loveYear
        );
    }

    /**
     * 当天天气
     * @param weather
     * @return
     */
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


    public static String dailySaying(LocalDate date) {
        String res = dailySayingForShanBay(date).toString();
        if(StringUtils.hasText(res)){
            return res;
        }
        res = dailySayingForAa1Renjian().toString();
        if(StringUtils.hasText(res)){
            return res;
        }
        res = dailySayingForAa1WenanYingwen().toString();
        if(StringUtils.hasText(res)){
            return res;
        }
        return "开心快乐每一天！";
    }
    /**
     * aa1 我在人间凑数的日子 一言 api
     * @return
     */
    public static Saying dailySayingForAa1Renjian() {
        HttpRequest request = HttpUtil.createGet(AA1_RENJIAN_API);
        HttpResponse response = httpExecute(request);
        JSONObject json = JSON.parseObject(response.body());
        return new Saying().setContent(json.getString("renjian"));
    }

    /**
     * aa1 汉英文案 一言 api
     * @return
     */
    public static Saying dailySayingForAa1WenanYingwen() {
        HttpRequest request = HttpUtil.createGet(AA1_WENAN_YINGWEN_API);
        HttpResponse response = httpExecute(request);
        JSONObject json = JSON.parseObject(response.body());
        return new Saying().setContent(json.getString("text"));
    }
    /**
     * 有道每日一句
     * @param date
     * @return
     */
    public static Saying dailySayingForYouDao(LocalDate date) {
        Saying saying = new Saying();
        String dateStr = LocalDateTimeUtil.formatNormal(date);
        HttpRequest request = HttpUtil.createGet(String.format(YOUDAO_API, dateStr));
        HttpResponse response = httpExecute(request);
        JSONArray jsonArray = JSON.parseArray(JSON.parseObject(response.body()).getString(dateStr));
        if (jsonArray.isEmpty()) {
            return saying;
        }
        JSONObject json = jsonArray.getJSONObject(0);
        return saying
                .setTranslation(json.getString("summary"))
                .setContent(json.getString("title"));
    }

    /**
     * 扇贝每日一句
     * @param date
     * @return
     */
    public static Saying dailySayingForShanBay(LocalDate date) {
        String dateStr = LocalDateTimeUtil.formatNormal(date);
        HttpRequest request = HttpUtil.createGet(String.format(SHANBAY_API, dateStr));
        HttpResponse response = httpExecute(request);
        return JSON.parseObject(response.body(), Saying.class);
    }

    @Data
    @Accessors(chain = true)
    public static class Saying {
        private String content;
        private String translation;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.hasText(content) && !BLANK.equals(translation)) {
                sb.append("დ ").append(content).append("\n");
            }
            if (StringUtils.hasText(translation) && !BLANK.equals(translation)) {
                sb.append("ღ ").append(translation).append("\n");
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        System.out.println(dailySaying(LocalDate.now()));
    }
}
