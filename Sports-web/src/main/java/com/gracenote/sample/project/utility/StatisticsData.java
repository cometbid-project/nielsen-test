/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.utility;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Gbenga
 */
@Entity
@Table(name = "METHOD_CALL_STATS")
@XmlRootElement(name = "Statictics")
@Cacheable(false)
public class StatisticsData implements Serializable {

    private static final long serialVersionUID = 1L;

    /*
    @GenericGenerator(
    name = "StatisticsData_gen",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
    @Parameter(name = "sequence_name", value = "StatisticsData_seq")
    ,
    @Parameter(name = "initial_value", value = "1")
    ,
    @Parameter(name = "increment_size", value = "1")
    }
    )*/
    @Id
    @TableGenerator(
            name = "StatisticsData_gen",
            table = "DB_PK_table",
            pkColumnValue = "StatisticsData_seq",
            valueColumnName = "SEQ_TYPE"
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "StatisticsData_gen")
    @Column(name = "METHOD_ID", nullable = false)
    private Long methodName;

    @Column(name = "HIT_COUNT", nullable = false)
    private Integer count;

    @Column(name = "TIME_TAKEN", nullable = false)
    private Long totalTime;

    @Column(name = "AVERAGE_TIME", nullable = false)
    private Long avgTime;

    public StatisticsData() {
        this(0, 0L);
    }

    public StatisticsData(Integer count) {
        this(count, 0L);
    }

    public StatisticsData(Long totalTime) {
        this(0, totalTime);
    }

    public StatisticsData(Integer count, Long totalTime) {
        this.count = count;
        this.totalTime = totalTime;
    }

    public Long getMethodName() {
        return methodName;
    }

    public void setMethodName(Long methodName) {
        this.methodName = methodName;
    }

    public Integer getCount() {
        return count;
    }

    public void increment() {
        this.count++;
    }

    public void increaseTotalTime(Long time) {
        totalTime += time;
    }

    public Long getTotalTime() {
        return this.totalTime;
    }

    public Long getAverageTime() {
        avgTime = (this.totalTime / count) / 1000;
        return avgTime;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.methodName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StatisticsData other = (StatisticsData) obj;
        if (!Objects.equals(this.methodName, other.methodName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StatisticsData{"
                + "methodName=" + this.getMethodName()
                + ", count=" + this.getCount()
                + ", totalTime=" + this.getTotalTime()
                + '}';
    }

}
