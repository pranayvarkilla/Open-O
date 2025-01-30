//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package org.oscarehr.hospitalReportManager;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.oscarehr.hospitalReportManager.dao.*;
import org.oscarehr.hospitalReportManager.model.*;
import org.oscarehr.managers.SecurityInfoManager;
import org.oscarehr.util.LoggedInInfo;

import org.oscarehr.util.SpringUtils;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.ActionContext;

public class HRMDisplayReport2Action extends ActionSupport {
    ActionContext context = ActionContext.getContext();
    HttpServletRequest request = (HttpServletRequest) context.get(ServletActionContext.HTTP_REQUEST);
    HttpServletResponse response = (HttpServletResponse) context.get(ServletActionContext.HTTP_RESPONSE);


    private static HRMDocumentToProviderDao hrmDocumentToProviderDao = (HRMDocumentToProviderDao) SpringUtils.getBean(HRMDocumentToProviderDao.class);
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public String execute() {

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_hrm", "r", null)) {
            throw new SecurityException("missing required security object (_hrm)");
        }

        return "display";
    }

    public static HRMDocumentToProvider getHRMDocumentFromProvider(String providerNo, Integer hrmDocumentId) {
        return (hrmDocumentToProviderDao.findByHrmDocumentIdAndProviderNo(hrmDocumentId, providerNo));
    }
}
