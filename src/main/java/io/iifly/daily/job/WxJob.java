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
        List<WxMpTemplateData> date = Lists.newArrayList();
        date.add(new WxMpTemplateData("date", weather.getDate(), "#63B8FF"));
        date.add(new WxMpTemplateData("week", weather.getWeek(), "#63B8FF"));
        date.add(new WxMpTemplateData("tem", weather.getTem(), "#00FFFF"));
        date.add(new WxMpTemplateData("city", weather.getCity(), "#AB82FF"));
        date.add(new WxMpTemplateData("wea", weather.getWea(), "#00FFFF"));
        date.add(new WxMpTemplateData("low", weather.getTem2(), "#EEA9B8"));
        date.add(new WxMpTemplateData("high", weather.getTem1(), "#EE9572"));
        date.add(new WxMpTemplateData("birth", Utils.birthStr(jobConf.getWx().getBabyBirthDay()), "#E066FF"));
        date.add(new WxMpTemplateData("love", Utils.loveStr(jobConf.getWx().getLoveDay()), "#FF6EB4"));
        date.add(new WxMpTemplateData("remark", Utils.dailySayingForShanBay(LocalDate.now()).toString() , "#1A94E6"));
        JobConf.WxProp wx = jobConf.getWx();
        wx.getToUser().forEach(user -> {
            WxMpTemplateMessage message = WxMpTemplateMessage
                    .builder()
                    .toUser(user)
                    .templateId(wx.getTemplateId())
                    .data(date)
                    .build();
            Utils.pushTemplateMessage(wxMpService, message);
        });
    }
}
