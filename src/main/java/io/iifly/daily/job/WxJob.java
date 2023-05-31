package io.iifly.daily.job;

import com.google.common.collect.Lists;
import io.iifly.daily.conf.JobConf;
import io.iifly.daily.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RefreshScope
@RequiredArgsConstructor
public class WxJob implements Job{

    private final JobConf jobConf;

    private final WxMpService wxMpService;
    @Override
    @Scheduled(cron = "#{jobConf.wx.cron}")
    public Object task() {
        push();
        return true;
    }

    public void push() {
        Utils.Weather weather = Utils.todayWeather(jobConf.getWeather());
        List<WxMpTemplateData> data = Lists.newArrayList();
        data.add(new WxMpTemplateData("date", weather.getDate(), "#63B8FF"));
        data.add(new WxMpTemplateData("week", weather.getWeek(), "#63B8FF"));
        data.add(new WxMpTemplateData("tem", weather.getTem(), "#00FFFF"));
        data.add(new WxMpTemplateData("city", weather.getCity(), "#AB82FF"));
        data.add(new WxMpTemplateData("wea", weather.getWea(), "#00FFFF"));
        data.add(new WxMpTemplateData("low", weather.getTemNight(), "#EEA9B8"));
        data.add(new WxMpTemplateData("high", weather.getTemDay(), "#EE9572"));
        data.add(new WxMpTemplateData("birth", Utils.birthStr(jobConf.getWx().getBabyBirthDay()), "#E066FF"));
        data.add(new WxMpTemplateData("love", Utils.loveStr(jobConf.getWx().getLoveDay()), "#FF6EB4"));
        Utils.Saying saying = Utils.dailySaying(LocalDate.now());
        data.add(new WxMpTemplateData("say1", saying.getContent(), "#1A94E6"));
        data.add(new WxMpTemplateData("say2", saying.getTranslation(), "#1A94E6"));
        JobConf.WxProp wx = jobConf.getWx();
        wx.getToUser().forEach(user -> {
            WxMpTemplateMessage message = WxMpTemplateMessage
                    .builder()
                    .toUser(user)
                    .templateId(wx.getTemplateId())
                    .data(data)
                    .build();
            Utils.pushTemplateMessage(wxMpService, message);
        });
    }
}
