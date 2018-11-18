/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.utility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Gbenga
 */
public class ApplicationStatistics {

    private static ApplicationStatistics INSTANCE;
    private static final Logger LOGGER = new Logger(ApplicationStatistics.class.getName());

    private ApplicationStatistics(Map<String, StatisticsData> statisticsMap) {
        statisticsCache = statisticsMap;
    }

    public static ApplicationStatistics getInstance() {

        if (INSTANCE == null) {
            Map<String, StatisticsData> statisticsMap = new HashMap<>();
            INSTANCE = new ApplicationStatistics(statisticsMap);
        }

        return INSTANCE;
    }

    private Map<String, StatisticsData> statisticsCache;

    public Map<String, StatisticsData> getStatisticsCache() {
        return statisticsCache;
    }

    public void setStatisticsCache(Map<String, StatisticsData> statisticsCache) {
        this.statisticsCache = statisticsCache;
    }

    public void incrementInvocationCount(String methodName) {
        if (statisticsCache.get(methodName) == null) {
            statisticsCache.put(methodName, new StatisticsData());
        }

        statisticsCache.get(methodName).increment();
    }

    public void printInvocationCount(String methodName) {
        Set<String> keys = statisticsCache.keySet();
        Iterator<String> iter = keys.iterator();

        while (iter.hasNext()) {
            String name = iter.next();
            StatisticsData data = statisticsCache.get(name);
            LOGGER.info("method Name - {0} and No of Hits: {1}, Total Time: {2}, Average Time: {3}",
                    new Object[]{name, data.getCount(), data.getTotalTime(), data.getAverageTime()});
        }
    }

    public void increaseTotalTime(String methodName, long timeTaken) {
        if (statisticsCache.get(methodName) == null) {
            statisticsCache.put(methodName, new StatisticsData());
        }

        statisticsCache.get(methodName).increaseTotalTime(timeTaken);
    }

    public void addStatistics(String methodName, StatisticsData keeper) {

        if (statisticsCache.get(methodName) == null) {
            statisticsCache.put(methodName, new StatisticsData());
        }

        statisticsCache.put(methodName, keeper);
    }

    public Integer getInvocationCount(String methodName) {
        StatisticsData stats = statisticsCache.get(methodName);
        if (stats != null) {
            return stats.getCount();
        } else {
            return 0;
        }
    }

    public Map<String, StatisticsData> gatherStatistics() {
        return statisticsCache;
    }
}
