/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.utility;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Gbenga
 */
public class MonitorableThreadFactory implements ThreadFactory {

    final AtomicInteger threadNumber = new AtomicInteger(1);
    private String namePrefix;

    public MonitorableThreadFactory() {
        this("xray-rest-pool");
    }

    public MonitorableThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(createName());
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

    String createName() {
        return namePrefix + "-" + threadNumber.incrementAndGet();
    }

    public int getNumberOfCreatedThreads() {
        return threadNumber.get();
    }
}
