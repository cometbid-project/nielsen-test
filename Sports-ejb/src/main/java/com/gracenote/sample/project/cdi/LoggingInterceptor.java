package com.gracenote.sample.project.cdi;

import com.gracenote.sample.project.qualifiers.Logged;
import java.io.Serializable;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

@Interceptor
@Logged
public class LoggingInterceptor implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 247827135469339458L;

    @AroundInvoke
    public Object log(InvocationContext context) throws Exception {
        final Logger logger = Logger.getLogger(context.getTarget().getClass());
        logger.infov("Executing method {0}", context.getMethod().toString());
        return context.proceed();
    }
}
