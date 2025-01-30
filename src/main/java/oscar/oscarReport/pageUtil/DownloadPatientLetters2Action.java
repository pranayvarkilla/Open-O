//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


package oscar.oscarReport.pageUtil;

import java.util.Hashtable;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.oscarehr.managers.SecurityInfoManager;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import oscar.oscarReport.data.ManageLetters;

/**
 * locate and create the letter template for the selected template.
 * For a list or patients create the letter and add it to the patient record.
 * mark in patients record that a letter was generated
 * Combine the list and return a full list
 *
 * @author jay
 */
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.ActionContext;

public class DownloadPatientLetters2Action extends ActionSupport {
    ActionContext context = ActionContext.getContext();
    HttpServletRequest request = (HttpServletRequest) context.get(ServletActionContext.HTTP_REQUEST);
    HttpServletResponse response = (HttpServletResponse) context.get(ServletActionContext.HTTP_RESPONSE);


    private static Logger log = MiscUtils.getLogger();
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Creates a new instance of GeneratePatientLetters
     */
    public DownloadPatientLetters2Action() {
    }

    public String execute() {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_report", "r", null)) {
            throw new SecurityException("missing required security object (_report)");
        }

        if (log.isTraceEnabled()) {
            log.trace("Start of DownloadPatientLetters2Action Action");
        }

        String fileId = request.getParameter("reportID");
        try {
            ManageLetters manageLetters = new ManageLetters();
            Hashtable h = manageLetters.getReportData(fileId);
            String filename = (String) h.get("file_name");
            response.addHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
            //response.addHeader("Content-Disposition", "attachment;filename=report.txt" );  
            ServletOutputStream output = response.getOutputStream();
            manageLetters.writeLetterToStream(fileId, output);
            output.flush();
            output.close();

        } catch (Exception ex) {
            log.error("Error", ex);
        }

        if (log.isTraceEnabled()) {
            log.trace("End of DownloadPatientLetters2Action Action");
        }
        return null;
    }
}
