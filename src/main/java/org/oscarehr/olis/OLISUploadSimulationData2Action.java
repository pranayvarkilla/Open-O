//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package org.oscarehr.olis;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;

import com.indivica.olis.Driver;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.ActionContext;

public class OLISUploadSimulationData2Action extends ActionSupport {
    ActionContext context = ActionContext.getContext();
    HttpServletRequest request = (HttpServletRequest) context.get(ServletActionContext.HTTP_REQUEST);
    HttpServletResponse response = (HttpServletResponse) context.get(ServletActionContext.HTTP_RESPONSE);


    @Override
    public String execute() {

        Logger logger = MiscUtils.getLogger();

        String simulationData = null;
        boolean simulationError = false;

        try {
            DiskFileItemFactory factory = DiskFileItemFactory.builder().setPath(System.getProperty("java.io.tmpdir")).get();
            JakartaServletFileUpload upload = new JakartaServletFileUpload(factory);
            @SuppressWarnings("unchecked")
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    String name = item.getFieldName();
                    if (name.equals("simulateError")) {
                        simulationError = true;
                    }
                } else {
                    if (item.getFieldName().equals("simulateFile")) {
                        InputStream is = item.getInputStream();
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(is, writer, "UTF-8");
                        simulationData = writer.toString();
                    }
                }
            }

            if (simulationData != null && simulationData.length() > 0) {
                if (simulationError) {
                    Driver.readResponseFromXML(LoggedInInfo.getLoggedInInfoFromSession(request), request, simulationData);
                    simulationData = (String) request.getAttribute("olisResponseContent");
                    request.getSession().setAttribute("errors", request.getAttribute("errors"));
                }
                request.getSession().setAttribute("olisResponseContent", simulationData);
                request.setAttribute("result", "File successfully uploaded");
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        }

        return SUCCESS;
    }
}
