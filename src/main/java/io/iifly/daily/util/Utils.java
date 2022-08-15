package io.iifly.daily.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

import javax.mail.internet.MimeMessage;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author zh-hq
 * @Description
 * @date 2022/8/12
 */
@Slf4j
public class Utils {

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
        log.info(request.getUrl() + " ==> " + Optional.ofNullable(request.bodyBytes()).map(String::new).orElse(null));
        stopWatch.start();
        HttpResponse response = request.execute();
        stopWatch.stop();
        log.info(request.getUrl() + " <== (" + stopWatch.getLastTaskTimeMillis() + "ms) " + response.body());
        Assert.isTrue(response.isOk(), response.body());
        return response;
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
                throw new MailParseException("邮件类型错误");
            }
            log.info("sendMail is success");
        } catch (MailException e) {
            log.error("sendMail is exception", e);
        }
    }

}
