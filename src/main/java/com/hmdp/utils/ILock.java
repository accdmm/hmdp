package com.hmdp.utils;

public interface ILock {
    /**
     * 尝试获取锁
     *
     * @param timeoutSec 锁持有的超时时间，过期自动释放
     * @return true表示获取锁成功，false表示获取锁失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
    
    /**
     * 尝试获取锁，支持重试
     * @param timeoutSec 锁超时时间
     * @param waitTime 等待获取锁的最大时间
     * @return 是否获取成功
     */
//    boolean tryLock(long timeoutSec, long waitTime) throws InterruptedException;
}