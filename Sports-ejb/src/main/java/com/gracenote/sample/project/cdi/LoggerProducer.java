package com.gracenote.sample.project.cdi;

import com.gracenote.sample.project.qualifiers.JBossLogger;
import com.gracenote.sample.project.qualifiers.JavaUtilLogger;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class LoggerProducer {

    @Produces
    @JavaUtilLogger
    public java.util.logging.Logger produceUtilLogger(InjectionPoint injectionPoint) {
        return java.util.logging.Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }

    @Produces
    @JBossLogger
    public org.jboss.logging.Logger produceJbossLogger(InjectionPoint injectionPoint) {
        return org.jboss.logging.Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }

}
