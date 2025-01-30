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
package org.oscarehr.app;

// import org.owasp.csrfguard.log.JavaLogger;
// import org.owasp.csrfguard.log.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oscar.OscarProperties;

import java.io.File;
//import java.io.IOException;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.FileHandler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;

/**
 * Oscar CsrfGuardLogger
 * Extends the standard CsrfGuard JavaLogger to take into account the oscar property "csrf_log_all_messages"
 * If the property is enabled, only
 */
public class CsrfGuardLogger {

    private static final OscarProperties oscarProperties = OscarProperties.getInstance();
    private static final List<String> errorLogLevels = Arrays.asList("WARN", "ERROR");

    // Create logger that logs CSRFGuard messages
    private static final Logger LOGGER = LoggerFactory.getLogger("Owasp.CsrfGuard");

    static {
        try {
            String documentsFolder = oscarProperties.getProperty("BASE_DOCUMENT_DIR") != null
                    ? oscarProperties.getProperty("BASE_DOCUMENT_DIR")
                    : System.getProperty("java.io.tmpdir");

            if (!documentsFolder.endsWith("/")) {
                documentsFolder += "/";
            }

            String logDirectory = documentsFolder + "logs";
            File logFile = new File(logDirectory + "/csrf.log");
            if (!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }

            System.setProperty("org.slf4j.simpleLogger.logFile", logFile.getPath());
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");

        } catch (Exception e) {
            LOGGER.error("CSRFGuard Logger exception", e);
        }
    }

    public void logInfo(String msg) {
        if (isLoggable()) {
            LOGGER.info(msg);
        }
    }

    public void logWarn(String msg) {
        if (isLoggable("WARN")) {
            LOGGER.warn(msg);
        }
    }

    public void logError(String msg, Throwable exception) {
        if (isLoggable("ERROR")) {
            LOGGER.error(msg, exception);
        }
    }

    public void logException(Exception exception) {
        if (isLoggable()) {
            LOGGER.error("Exception occurred: ", exception);
        }
    }

    private boolean isLoggable() {
        return oscarProperties.isPropertyActive("csrf_log_all_messages");
    }

    private boolean isLoggable(String level) {
        return oscarProperties.isPropertyActive("csrf_log_all_messages") || errorLogLevels.contains(level);
    }
}
