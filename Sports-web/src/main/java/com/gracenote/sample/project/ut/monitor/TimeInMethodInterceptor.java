/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.ut.monitor;

import com.gracenote.sample.project.utility.ApplicationStatistics;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;

/**
 *
 * @author Gbenga
 */
public class TimeInMethodInterceptor {

    private static final com.gracenote.sample.project.utility.Logger logger =
            new com.gracenote.sample.project.utility.Logger(TimeInMethodInterceptor.class.getName());

    ApplicationStatistics applicationStatistics;

    @AroundTimeout
    @AroundInvoke
    public Object recordTime(InvocationContext context) throws Exception {
        logger.info("TimeInMethodInterceptor - Starting");

        String methodName = context.getMethod().getName();
        applicationStatistics = ApplicationStatistics.getInstance();

        long endTime = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        try {
            Object result = context.proceed();
            endTime = System.currentTimeMillis();

            applicationStatistics.increaseTotalTime(methodName, endTime - startTime);
            logger.info("TimeInMethodInterceptor - Terminating");
            return result;
        } catch (Exception e) {
            logger.error("!!!During invocation of: {0} exception occured: {1}", new Object[]{methodName, e});
            throw e;
        } finally {
            logger.info("{0} performed in: {1} milliseconds", new Object[]{methodName, (endTime - startTime)});
        }
    }
}
