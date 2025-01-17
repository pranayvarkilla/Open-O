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
package oscar;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletDiskFileUpload;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.io.FileUtils;
import org.oscarehr.util.MiscUtils;

public class DocumentUploadServlet extends HttpServlet {

    final static int BUFFER = 4096;

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String foldername = "", fileheader = "", forwardTo = "";
        forwardTo = OscarProperties.getInstance().getProperty("RA_FORWORD");
        foldername = OscarProperties.getInstance().getProperty("DOCUMENT_DIR");

        String inboxFolder = OscarProperties.getInstance().getProperty("ONEDT_INBOX");
        String archiveFolder = OscarProperties.getInstance().getProperty("ONEDT_ARCHIVE");

        if (forwardTo == null || forwardTo.length() < 1)
            return;

        String providedFilename = request.getParameter("filename");
        if (providedFilename != null) {
            providedFilename = URLDecoder.decode(providedFilename, "UTF-8");
            File documentDirectory = new File(foldername);
            File providedFile = new File(inboxFolder, providedFilename);
            if (!providedFile.exists()) {
                providedFile = new File(archiveFolder, providedFilename);
            }

            FileUtils.copyFileToDirectory(providedFile, documentDirectory);

            fileheader = providedFilename;
        } else {

            JakartaServletDiskFileUpload upload = new JakartaServletDiskFileUpload();

            try {
                // Parse the request
                @SuppressWarnings("unchecked")
                List<DiskFileItem> /* FileItem */items = upload.parseRequest(request);
                // Process the uploaded items
                Iterator<DiskFileItem> iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = iter.next();

                    if (item.isFormField()) { //
                    } else {
                        String pathName = item.getName();
                        String[] fullFile = pathName.split("[/|\\\\]");
                        File savedFile = new File(foldername, fullFile[fullFile.length - 1]);
                        fileheader = fullFile[fullFile.length - 1];
                        item.write(savedFile.toPath());
                        if (OscarProperties.getInstance().isPropertyActive("moh_file_management_enabled")) {
                            FileUtils.copyFileToDirectory(savedFile, new File(inboxFolder));
                        }
                    }
                }
            } catch (FileUploadException e) {
                MiscUtils.getLogger().error("Error", e);
            } catch (Exception e) {
                MiscUtils.getLogger().error("Error", e);
            }
        }

        DocumentBean documentBean = new DocumentBean();
        request.setAttribute("documentBean", documentBean);
        documentBean.setFilename(fileheader);
        RequestDispatcher dispatch = getServletContext().getRequestDispatcher(forwardTo);
        dispatch.forward(request, response);
    }
}
