package com.xxl.job.core.enums;

/**
 * 阻塞处理策略
 *
 * Created by xuxueli on 17/5/9.
 */
public enum ExecutorBlockStrategyEnum {

    SERIAL_EXECUTION("Serial execution"), // 单机串行
    /*CONCURRENT_EXECUTION("并行"),*/
    DISCARD_LATER("Discard Later"), // 丢弃后续调度
    COVER_EARLY("Cover Early"); // 覆盖之前调度

    private String title;
    private ExecutorBlockStrategyEnum (String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public static ExecutorBlockStrategyEnum match(String name, ExecutorBlockStrategyEnum defaultItem) {
        if (name != null) {
            for (ExecutorBlockStrategyEnum item:ExecutorBlockStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}
