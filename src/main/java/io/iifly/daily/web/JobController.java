package io.iifly.daily.web;

import io.iifly.daily.job.JueJinJob;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zh-hq
 * @Description
 * @date 2022/8/13
 */
@RestController
@RequestMapping("job")
@RequiredArgsConstructor
public class JobController {

    private final JueJinJob jueJinJob;

    @RequestMapping("juejin")
    public Object juejin() {
        return jueJinJob.task();
    }
}
