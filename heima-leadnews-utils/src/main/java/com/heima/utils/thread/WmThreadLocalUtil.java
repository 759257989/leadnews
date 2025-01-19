package com.heima.utils.thread;

import com.heima.model.wemedia.pojos.WmUser;

public class WmThreadLocalUtil {
    private final static ThreadLocal<WmUser> threadLocal = new ThreadLocal<>();

    //存入线程中
    public static void setUser(WmUser user) {
        threadLocal.set(user);
    }

    // 从线程中获取
    public static WmUser getUser() {
        return threadLocal.get();
    }

    // 清理
    public static void clear(){
        threadLocal.remove();
    }
}
