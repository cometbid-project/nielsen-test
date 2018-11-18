/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.utility;

import java.util.logging.Level;

/**
 *
 * @author Gbenga
 */
public class Logger {

    private static String loggerName;

    private static java.util.logging.Logger usedLogger;

    public Logger(String loggerName) {
        Logger.loggerName = loggerName;
        initLogger();
    }

    private void initLogger() {
        usedLogger = java.util.logging.Logger.getLogger(Logger.loggerName);
    }

    private java.util.logging.Logger getLogger() {
        return usedLogger;
    }

    public void error(String messageFormat, Object... argArray) {
        getLogger().log(Level.SEVERE, messageFormat, argArray);
    }

    public void info(String messageFormat, Object... argArray) {
        getLogger().log(Level.INFO, messageFormat, argArray);
    }

    public void warn(String messageFormat, Object... argArray) {
        getLogger().log(Level.WARNING, messageFormat, argArray);
    }

    public void debug(String messageFormat, Object... argArray) {
        getLogger().log(Level.FINEST, messageFormat, argArray);
    }
}
