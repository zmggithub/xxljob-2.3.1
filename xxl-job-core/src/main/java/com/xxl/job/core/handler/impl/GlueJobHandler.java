package com.xxl.job.core.handler.impl;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;

/**
 * glue job handler该种作业处理器专门用于处理Glue(Java)类型作业，上节分析过Java类型作业会被GlueFactory编译、初始化成实例，然后封装到GlueJobHandler中进行执行；
 *
 * @author xuxueli 2016-5-19 21:05:45
 */
public class GlueJobHandler extends IJobHandler {

	private long glueUpdatetime;
	private IJobHandler jobHandler;
	public GlueJobHandler(IJobHandler jobHandler, long glueUpdatetime) {
		this.jobHandler = jobHandler;
		this.glueUpdatetime = glueUpdatetime;
	}
	public long getGlueUpdatetime() {
		return glueUpdatetime;
	}

	@Override
	public void execute() throws Exception {
		XxlJobHelper.log("----------- glue.version:"+ glueUpdatetime +" -----------");
		jobHandler.execute();
	}

	@Override
	public void init() throws Exception {
		this.jobHandler.init();
	}

	@Override
	public void destroy() throws Exception {
		this.jobHandler.destroy();
	}
}
