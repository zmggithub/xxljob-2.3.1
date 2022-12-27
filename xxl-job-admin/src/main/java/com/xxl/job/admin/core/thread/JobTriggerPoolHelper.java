package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.core.trigger.XxlJobTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * job trigger thread pool helper 作业触发器线程池助手
 * 包含两个线程池，快速触发任务线程池和慢触发任务线程池，会根据每分钟执行的次数决定任务投递到快速触发任务线程池还是慢触发任务线程池中。
 * 部分慢执行的线程，会拖慢整个线程池，因此我们需要将快慢分离，慢线程？ 1min内 == 执行耗时大于500ms && 计数10次
 * @author xuxueli 2018-07-03 21:08:07
 */
public class JobTriggerPoolHelper {
    private static Logger logger = LoggerFactory.getLogger(JobTriggerPoolHelper.class);

    // ---------------------- trigger pool ----------------------

    // fast/slow thread pool
    private ThreadPoolExecutor fastTriggerPool = null;
    private ThreadPoolExecutor slowTriggerPool = null;

    public void start(){

        // 创建了一个最大核心线程数为10的，最多拥有200个线程数的，空闲线程存活时间为60s的线程池
        fastTriggerPool = new ThreadPoolExecutor(
                10,
                XxlJobAdminConfig.getAdminConfig().getTriggerPoolFastMax(), // 最小200
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "xxl-job, admin JobTriggerPoolHelper-fastTriggerPool-" + r.hashCode());
                    }
                });

        // 创建了一个最大核心线程数为10的，最少拥有100个线程数的，空闲线程存活时间为60s的线程池
        slowTriggerPool = new ThreadPoolExecutor(
                10,
                XxlJobAdminConfig.getAdminConfig().getTriggerPoolSlowMax(), // 最小100
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(2000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "xxl-job, admin JobTriggerPoolHelper-slowTriggerPool-" + r.hashCode());
                    }
                });
    }


    public void stop() {
        //triggerPool.shutdown();
        fastTriggerPool.shutdownNow();
        slowTriggerPool.shutdownNow();
        logger.info(">>>>>>>>> xxl-job trigger thread pool shutdown success.");
    }


    // 作业超时计数 job timeout count
    private volatile long minTim = System.currentTimeMillis()/60000;     // ms > min
    private volatile ConcurrentMap<Integer, AtomicInteger> jobTimeoutCountMap = new ConcurrentHashMap<>();


    /**
     * 触发执行器 add trigger
     */
    public void addTrigger(final int jobId,
                           final TriggerTypeEnum triggerType,
                           final int failRetryCount,
                           final String executorShardingParam,
                           final String executorParam,
                           final String addressList) {

        // 获取线程池 choose thread pool
        ThreadPoolExecutor triggerPool_ = fastTriggerPool;

        // 获取超时次数 tips:AtomicInteger用于多线程下线程安全的数据读写操作，避免使用锁同步，底层采用CAS实现，内部的存储值使用volatile修饰，因此多线程之间是修改可见的。
        AtomicInteger jobTimeoutCount = jobTimeoutCountMap.get(jobId);

        // 一分钟内超时10次，则采用慢触发器执行 job-timeout 10 times in 1 min
        if (jobTimeoutCount!=null && jobTimeoutCount.get() > 10) {
            triggerPool_ = slowTriggerPool;
        }

        // trigger
        triggerPool_.execute(new Runnable() {
            @Override
            public void run() {

                long start = System.currentTimeMillis();

                try {
                    // do trigger 执行触发器
                    XxlJobTrigger.trigger(jobId, triggerType, failRetryCount, executorShardingParam, executorParam, addressList);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {

                    // check timeout-count-map job执行后和执行前不是同一个分钟值，则清空jobTimeoutCountMap，意义是为了每分钟清空慢作业累计缓存
                    long minTim_now = System.currentTimeMillis()/60000;
                    if (minTim != minTim_now) {
                        minTim = minTim_now; // 当达到下一分钟则清除超时任务
                        jobTimeoutCountMap.clear();
                    }

                    // incr timeout-count-map
                    long cost = System.currentTimeMillis()-start;
                    if (cost > 500) {       // ob-timeout threshold 500ms  临界点
                        // 执行时间超过500ms,则记录执行次数。无则put，有则返回值
                        AtomicInteger timeoutCount = jobTimeoutCountMap.putIfAbsent(jobId, new AtomicInteger(1));
                        if (timeoutCount != null) {
                            timeoutCount.incrementAndGet();
                        }
                    }

                }

            }
        });
    }



    // ---------------------- helper ----------------------

    private static JobTriggerPoolHelper helper = new JobTriggerPoolHelper();

    public static void toStart() {
        helper.start();
    }
    public static void toStop() {
        helper.stop();
    }

    /**
     * 调度器 触发 远程执行器
     *
     * @param jobId
     * @param triggerType
     * @param failRetryCount
     * 			>=0: use this param
     * 			<0: use param from job info config
     * @param executorShardingParam
     * @param executorParam
     *          null: use job param
     *          not null: cover job param
     */
    public static void trigger(int jobId, TriggerTypeEnum triggerType, int failRetryCount, String executorShardingParam, String executorParam, String addressList) {
        helper.addTrigger(jobId, triggerType, failRetryCount, executorShardingParam, executorParam, addressList);
    }

}
