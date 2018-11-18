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
public class HitCounterInterceptor {

    private static final com.gracenote.sample.project.utility.Logger logger =
            new com.gracenote.sample.project.utility.Logger(HitCounterInterceptor.class.getName());

    public HitCounterInterceptor() {

    }

    @AroundTimeout
    @AroundInvoke
    public Object incrementCounter(InvocationContext context) throws Exception {

        logger.info("HitCounterInterceptor - Starting");

        String methodName = context.getMethod().getName();
        ApplicationStatistics applicationStatistics = ApplicationStatistics.getInstance();

        applicationStatistics.incrementInvocationCount(methodName);

        try {
            Object result = context.proceed();

            logger.info("HitCounterInterceptor - Terminating");
            return result;
        } catch (Exception e) {
            logger.error( "!!!During invocation of: {0} exception occured: {1}", new Object[]{methodName, e});
            throw e;
        } finally {
            logger.info("Method-'{0}' has been invoked: {1} times within the last session measured",
                    new Object[]{methodName, applicationStatistics.getInvocationCount(methodName)});
        }
    }
}
