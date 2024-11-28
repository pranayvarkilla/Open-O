//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package org.oscarehr.olis.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.oscarehr.common.model.AbstractModel;

@Entity
public class OLISSystemPreferences extends AbstractModel<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String startTime;
    private String endTime;
    private Integer pollFrequency;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastRun;

    private boolean filterPatients;


    @Override
    public Integer getId() {
        return id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getPollFrequency() {
        return pollFrequency;
    }

    public void setPollFrequency(Integer pollFrequency) {
        this.pollFrequency = pollFrequency;
    }

    public Date getLastRun() {
        return lastRun;
    }

    public void setLastRun(Date lastRun) {
        this.lastRun = lastRun;
    }


    public boolean isFilterPatients() {
        return filterPatients;
    }

    public void setFilterPatients(boolean filterPatients) {
        this.filterPatients = filterPatients;
    }

    public OLISSystemPreferences() {
        super();
    }
}
