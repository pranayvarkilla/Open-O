//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package oscar.oscarLab.ca.all.pageUtil;


import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.oscarehr.managers.SecurityInfoManager;
import org.oscarehr.olis.OLISResults2Action;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import oscar.oscarLab.ca.all.parsers.Factory;
import oscar.oscarLab.ca.all.parsers.MessageHandler;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.ActionContext;

public class PrintOLISLab2Action extends ActionSupport {
    ActionContext context = ActionContext.getContext();
    HttpServletRequest request = (HttpServletRequest) context.get(ServletActionContext.HTTP_REQUEST);
    HttpServletResponse response = (HttpServletResponse) context.get(ServletActionContext.HTTP_RESPONSE);


    private static final Logger logger = MiscUtils.getLogger();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public String execute() {

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_lab", "r", null)) {
            throw new SecurityException("missing required security object (_lab)");
        }

        try {
            String segmentId = request.getParameter("segmentID");
            String resultUuid = request.getParameter("uuid");
            MessageHandler handler = null;
            if (segmentId == null && segmentId.equals("0")) {
                // if viewing in preview from OLIS search, use uuid
                handler = OLISResults2Action.searchResultsMap.get(resultUuid);
            } else {
                handler = Factory.getHandler(segmentId);
            }
            response.setContentType("application/pdf");  //octet-stream
            response.setHeader("Content-Disposition", "attachment; filename=\"" + handler.getPatientName().replaceAll("\\s", "_") + "_OLISLabReport.pdf\"");
            OLISLabPDFCreator pdf = new OLISLabPDFCreator(request, response.getOutputStream());
            pdf.printPdf();

        } catch (IOException ioe) {
            logger.error("IOException occured insided OLISPrintLabsAction", ioe);
            request.setAttribute("printError", new Boolean(true));
            return "error";
        } catch (Exception e) {
            logger.error("Unknown Exception occured insided OLISPrintLabsAction", e);
            request.setAttribute("printError", new Boolean(true));
            return "error";
        }


        return SUCCESS;
    }


}
