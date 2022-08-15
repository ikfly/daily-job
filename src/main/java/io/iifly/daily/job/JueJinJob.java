package io.iifly.daily.job;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.iifly.daily.conf.JobConf;
import io.iifly.daily.util.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;


/**
 * @author zh-hq
 * @Description
 * @date 2022/8/11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JueJinJob implements Job {

    private final JobConf conf;

    private final JavaMailSender javaMailSender;

    @Override
    @Scheduled(cron = "0 0 9 * * ?")
    public Object task() {
        String result = "任务执行结果:".concat(System.lineSeparator());
        try {
            Utils.isTrue(!getTodayStatus(),
                    () -> result.concat(String.format("签到结果【%s】", checkIn())).concat(System.lineSeparator()));
            Utils.isTrue(freeDrawLotteryCount() > 0,
                    () -> result.concat(String.format("抽奖结果【%s】", drawLottery())).concat(System.lineSeparator()));
            Optional.ofNullable(notCollectBugs())
                    .ifPresent(bugs -> {
                        bugs.forEach(this::collectBug);
                        result.concat(String.format("收集BUG:【%s】", String.valueOf(bugs.size()).concat(System.lineSeparator())));
                    });
            Optional.ofNullable(firstBigLotteryHistoryId())
                    .ifPresent(historyId ->
                            result.concat(String.format("粘幸运结果【%s】", dipLucky(historyId))).concat(System.lineSeparator()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.concat(e.getMessage());
        }
        log.info(result);
        sendMail(result);
        return result;
    }

    public void sendMail(String content) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(((JavaMailSenderImpl) javaMailSender).getUsername());
        mail.setTo(((JavaMailSenderImpl) javaMailSender).getUsername());
        mail.setSubject(conf.getJueJin().getTaskName());
        mail.setText(content);
        Utils.sendMail(mail, javaMailSender);
    }

    public String getCookie() {
        return conf.getJueJin().getCookie();
    }

    /**
     * 查询今日是否已经签到
     *
     * @return true 或 false
     */
    public Boolean getTodayStatus() {
        R<Boolean> r = toR(baseRequest(Api.GET_TODAY_STATUS));
        return r.getData();
    }

    /**
     * 签到
     *
     * @return 签到后总积分
     */
    public String checkIn() {
        R<JSONObject> r = toR(baseRequest(Api.CHECK_IN));
        JSONObject data = r.getData();
        String result = "签到增长矿石：".concat(data.getString("incr_point")).concat(", 总矿石数：").concat(data.getString("sum_point"));
        return result;
    }

    /**
     * 查询免费抽奖次数
     *
     * @return 免费抽奖次数
     */
    public Integer freeDrawLotteryCount() {
        R<JSONObject> r = toR(baseRequest(Api.GET_LOTTERY_CONFIG));
        return r.getData().getInteger("free_count");
    }

    /**
     * 抽奖
     *
     * @return 奖品
     */
    public String drawLottery() {
        R<JSONObject> r = toR(baseRequest(Api.DRAW_LOTTERY));
        return r.getData().getString("lottery_name");
    }

    /**
     * 第一个大奖
     *
     * @return lotteryHistoryId
     */
    public String firstBigLotteryHistoryId() {
        HttpRequest request = baseRequest(Api.BIG_LOTTERY_HISTORY);
        JSONObject body = new JSONObject();
        body.put("page_no", 1);
        body.put("page_size", 1);
        request.body(body.toJSONString());
        R<LotteryHistory> r = toR(request, LotteryHistory.class);
        return Optional.ofNullable(r.getData())
                .map(LotteryHistory::getLotteries)
                .map(lotteries -> lotteries.get(0))
                .map(Lottery::getHistory_id)
                .orElse(null);
    }

    /**
     * 粘幸运值
     *
     * @param lotteryHistoryId
     * @return 结果
     */
    public String dipLucky(String lotteryHistoryId) {
        HttpRequest request = baseRequest(Api.DIP_LUCKY);
        JSONObject body = new JSONObject();
        body.put("lottery_history_id", lotteryHistoryId);
        request.body(body.toJSONString());
        R<JSONObject> r = toR(request);
        JSONObject data = r.getData();
        String result = "粘到幸运值：".concat(data.getString("dip_value")).concat(", 总幸运值：").concat(data.getString("total_value"));
        return result;
    }

    /**
     * 未收集bug查询
     *
     * @return bugs
     */
    public List<Bug> notCollectBugs() {
        R<List<Bug>> r = toR(baseRequest(Api.NOT_COLLECT_BUG), Utils.buildNestedType(List.class, Bug.class));
        return r.getData();
    }

    /**
     * 收集bug
     *
     * @param bug
     * @return 结果
     */
    public Boolean collectBug(Bug bug) {
        R<JSONObject> r = toR(baseRequest(Api.COLLECT_BUG).body(JSON.toJSONString(bug)));
        return r.isSuccess();
    }

    private HttpRequest baseRequest(Api api) {
        return HttpUtil
                .createRequest(api.method, conf.getJueJin().getBaseApi().concat(api.path))
                .cookie(Optional
                        .ofNullable(getCookie())
                        .orElseThrow(NullPointerException::new));
    }

    private static <T> R<T> toR(HttpRequest request, Type... types) {
        R<T> r = JSON.parseObject(Utils.httpExecute(request).body(), new TypeReference<R<T>>(types) {
        });
        Assert.isTrue(r.isSuccess(), r.getErr_msg());
        return r;
    }

    enum Api {
        // 查询今日是否已经签到
        GET_TODAY_STATUS(Method.GET, "/growth_api/v1/get_today_status"),
        // 签到
        CHECK_IN(Method.POST, "/growth_api/v1/check_in"),
        // 获取今天免费抽奖的次数
        GET_LOTTERY_CONFIG(Method.GET, "/growth_api/v1/lottery_config/get"),
        // 抽奖
        DRAW_LOTTERY(Method.POST, "/growth_api/v1/lottery/draw"),
        // 围观大奖
        BIG_LOTTERY_HISTORY(Method.POST, "/growth_api/v1/lottery_history/global_big"),
        // 粘幸运
        DIP_LUCKY(Method.POST, "/growth_api/v1/lottery_lucky/dip_lucky"),
        // 未收集BUG查询
        NOT_COLLECT_BUG(Method.POST, "/user_api/v1/bugfix/not_collect"),
        // 收集BUG
        COLLECT_BUG(Method.POST, "/user_api/v1/bugfix/collect"),
        ;
        @Getter
        private final String path;

        @Getter
        private final Method method;

        Api(Method method, String path) {
            this.method = method;
            this.path = path;
        }
    }

    @Data
    private static class R<T> {
        private String err_msg;
        private T data;

        public boolean isSuccess() {
            return "success".equals(err_msg);
        }

        public boolean isFail() {
            return !isSuccess();
        }


    }

    @Data
    private static class Bug {
        private Integer bug_type;
        private Long bug_time;
    }

    @Data
    private static class LotteryHistory {
        private List<Lottery> lotteries;
    }

    @Data
    private static class Lottery {
        private String history_id;
        private String lottery_name;
        private String user_name;
        private String user_id;
    }
}
