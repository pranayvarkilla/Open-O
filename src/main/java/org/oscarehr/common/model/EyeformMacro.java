//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package org.oscarehr.common.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import org.oscarehr.eyeform.model.EyeformMacroBillingItem;

@Entity
@Table(name = "eyeform_macro_def")
public class EyeformMacro extends AbstractModel<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String macroName;
    private Date lastUpdated;
    private Boolean copyFromLastImpression;

    @Column(columnDefinition = "TEXT")
    private String impressionText;

    @Column(columnDefinition = "TEXT")
    private String planText;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "macroId", referencedColumnName = "id")
    @OrderBy("billingServiceCode ASC")
    private List<EyeformMacroBillingItem> billingItems;

    @Override
    public Integer getId() {
        return id;
    }

    public String getMacroName() {
        return macroName;
    }

    public void setMacroName(String macroName) {
        this.macroName = macroName;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Boolean getCopyFromLastImpression() {
        return copyFromLastImpression;
    }

    public void setCopyFromLastImpression(Boolean copyFromLastImpression) {
        this.copyFromLastImpression = copyFromLastImpression;
    }

    public String getImpressionText() {
        return impressionText;
    }

    public void setImpressionText(String impressionText) {
        this.impressionText = impressionText;
    }

    public String getPlanText() {
        return planText;
    }

    public void setPlanText(String planText) {
        this.planText = planText;
    }

    public List<EyeformMacroBillingItem> getBillingItems() {
        return billingItems;
    }

    public void setBillingItems(List<EyeformMacroBillingItem> billingItems) {
        this.billingItems = billingItems;
    }
}
