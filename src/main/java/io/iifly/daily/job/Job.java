package io.iifly.daily.job;

import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author zh-hq
 * @Description
 * @date 2022/8/11
 */
public interface Job extends ApplicationListener<RefreshScopeRefreshedEvent> {
    /**
     * 任务
     */
    Object task();

    @Override
    default void onApplicationEvent(RefreshScopeRefreshedEvent refreshScopeRefreshedEvent){
    }
}
