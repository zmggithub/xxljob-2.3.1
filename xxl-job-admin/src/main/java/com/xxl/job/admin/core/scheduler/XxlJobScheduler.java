package com.xxl.job.admin.core.scheduler;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.thread.*;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.client.ExecutorBizClient;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * XxlJob调度器
 * @author xuxueli 2018-10-28 00:18:17
 */

public class XxlJobScheduler  {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobScheduler.class);


    public void init() throws Exception {
        // 0、初始化国际化i18n
        initI18n();

        // 1、初始化任务触发器线程池助手,主要职能：快慢两个线程池异步触发远程执行器
        JobTriggerPoolHelper.toStart();

        // 2、初始化任务注册助手,主要职能：两个线程分别用于任务执行器注册或删除和轮训(30s)监控执行器注册状态
        JobRegistryHelper.getInstance().start();

        // 3、初始化任务失败故障监视器助手,主要职能：启动故障任务监视线程查询失败日志进行重试，从而触发告警逻辑和任务重试逻辑。
        JobFailMonitorHelper.getInstance().start();

        // 4、初始化任务完成助手,主要职能：任务回调线程池 处理执行器响应的任务处理回调接口；任务丢失监视器线程 任务结果丢失处理。
        JobCompleteHelper.getInstance().start();

        // 5、初始化任务日志报告助手,主要职能：日志报表线程，处理日志统计和清理
        JobLogReportHelper.getInstance().start();

        // 6、初始化任务调度助手,主要职能：任务调度线程，定时调度任务，任务投递和下次执行时间维护
        JobScheduleHelper.getInstance().start();

        logger.info(">>>>>>>>> init xxl-job admin success.");
    }


    public void destroy() throws Exception {

        // stop-schedule
        JobScheduleHelper.getInstance().toStop();

        // admin log report stop
        JobLogReportHelper.getInstance().toStop();

        // admin lose-monitor stop
        JobCompleteHelper.getInstance().toStop();

        // admin fail-monitor stop
        JobFailMonitorHelper.getInstance().toStop();

        // admin registry stop
        JobRegistryHelper.getInstance().toStop();

        // admin trigger pool stop
        JobTriggerPoolHelper.toStop();

    }

    // ---------------------- I18n ----------------------

    private void initI18n(){
        for (ExecutorBlockStrategyEnum item:ExecutorBlockStrategyEnum.values()) {
            item.setTitle(I18nUtil.getString("jobconf_block_".concat(item.name())));
        }
    }

    // ---------------------- executor-client ----------------------
    private static ConcurrentMap<String, ExecutorBiz> executorBizRepository = new ConcurrentHashMap<String, ExecutorBiz>();
    public static ExecutorBiz getExecutorBiz(String address) throws Exception {
        // valid
        if (address==null || address.trim().length()==0) {
            return null;
        }

        // load-cache
        address = address.trim();
        ExecutorBiz executorBiz = executorBizRepository.get(address);
        if (executorBiz != null) {
            return executorBiz;
        }

        // set-cache
        executorBiz = new ExecutorBizClient(address, XxlJobAdminConfig.getAdminConfig().getAccessToken());

        executorBizRepository.put(address, executorBiz);
        return executorBiz;
    }

}
