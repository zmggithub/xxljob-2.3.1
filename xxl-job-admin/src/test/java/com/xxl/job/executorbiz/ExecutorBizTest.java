package com.xxl.job.executorbiz;

import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.client.ExecutorBizClient;
import com.xxl.job.core.biz.model.*;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import freemarker.template.SimpleDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * executor api test
 *
 * Created by xuxueli on 17/5/12.
 */
public class ExecutorBizTest {

    // admin-client
    private static String addressUrl = "http://127.0.0.1:9999/";
    private static String accessToken = null;

    @Test
    public void beat() throws Exception {
        ExecutorBiz executorBiz = new ExecutorBizClient(addressUrl, accessToken);
        // Act
        final ReturnT<String> retval = executorBiz.beat();

        // Assert result
        Assertions.assertNotNull(retval);
        Assertions.assertNull(((ReturnT<String>) retval).getContent());
        Assertions.assertEquals(200, retval.getCode());
        Assertions.assertNull(retval.getMsg());
    }

    @Test
    public void idleBeat(){
        ExecutorBiz executorBiz = new ExecutorBizClient(addressUrl, accessToken);

        final int jobId = 0;

        // Act
        final ReturnT<String> retval = executorBiz.idleBeat(new IdleBeatParam(jobId));

        // Assert result
        Assertions.assertNotNull(retval);
        Assertions.assertNull(((ReturnT<String>) retval).getContent());
        Assertions.assertEquals(500, retval.getCode());
        Assertions.assertEquals("job thread is running or has trigger queue.", retval.getMsg());
    }

    @Test
    public void run(){
        ExecutorBiz executorBiz = new ExecutorBizClient(addressUrl, accessToken);

        // trigger data
        final TriggerParam triggerParam = new TriggerParam();
        triggerParam.setJobId(1);
        triggerParam.setExecutorHandler("demoJobHandler");
        triggerParam.setExecutorParams(null);
        triggerParam.setExecutorBlockStrategy(ExecutorBlockStrategyEnum.COVER_EARLY.name());
        triggerParam.setGlueType(GlueTypeEnum.BEAN.name());
        triggerParam.setGlueSource(null);
        triggerParam.setGlueUpdatetime(System.currentTimeMillis());
        triggerParam.setLogId(1);
        triggerParam.setLogDateTime(System.currentTimeMillis());

        // Act
        final ReturnT<String> retval = executorBiz.run(triggerParam);

        // Assert result
        Assertions.assertNotNull(retval);
    }

    @Test
    public void kill(){
        ExecutorBiz executorBiz = new ExecutorBizClient(addressUrl, accessToken);

        final int jobId = 0;

        // Act
        final ReturnT<String> retval = executorBiz.kill(new KillParam(jobId));

        // Assert result
        Assertions.assertNotNull(retval);
        Assertions.assertNull(((ReturnT<String>) retval).getContent());
        Assertions.assertEquals(200, retval.getCode());
        Assertions.assertNull(retval.getMsg());
    }

    @Test
    public void log(){
        ExecutorBiz executorBiz = new ExecutorBizClient(addressUrl, accessToken);

        final long logDateTim = 0L;
        final long logId = 0;
        final int fromLineNum = 0;

        // Act
        final ReturnT<LogResult> retval = executorBiz.log(new LogParam(logDateTim, logId, fromLineNum));

        // Assert result
        Assertions.assertNotNull(retval);
    }

    @Test
    public void currentTimeMillisTest(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long l1 = System.currentTimeMillis();
        Date date = new Date(l1);
        System.out.println(dateFormat.format(date));

        l1 = System.currentTimeMillis() / 60000;
        System.out.println(l1);

        // 我的本意是用三分之一乘以3，结果为1，但是这样是错
        // 计算过程：1对3取整，结果为0，再乘以3 还是0 ， 所以正确结果为0
        int i1 = (1/3)*3; // 0
        System.out.println(i1);

    }

}
