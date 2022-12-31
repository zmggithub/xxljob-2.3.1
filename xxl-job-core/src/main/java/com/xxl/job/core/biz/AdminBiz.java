package com.xxl.job.core.biz;

import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;

import java.util.List;

/**
 * 定义了回调，注册，移除注册等方法，定义了执行器和调度中心的交互方式
 *
 * 12.30 这里有两个地方调用，所以要根据不同的调用场景来梳理接下去的逻辑：
 * 一、JobApiController：实现类是 AdminBizImpl，是admin对外提供的接口
 * 二、ExecutorRegistryThread：实现类是 AdminBizClient，包装了注册admin的address与accessToken。是executor中的core包嵌入的EmbedServer触发的 registryThread注册或删除线程
 *
 * @author xuxueli 2017-07-27 21:52:49
 */
public interface AdminBiz {


    // ---------------------- callback ----------------------

    /**
     * callback
     *
     * @param callbackParamList
     * @return
     */
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);


    // ---------------------- registry ----------------------

    /**
     * registry 这地方没理清。。。 12.30号看看
     * @param registryParam
     * @return
     */
    public ReturnT<String> registry(RegistryParam registryParam);

    /**
     * registry remove
     *
     * @param registryParam
     * @return
     */
    public ReturnT<String> registryRemove(RegistryParam registryParam);


    // ---------------------- biz (custome) ----------------------
    // group、job ... manage

}
