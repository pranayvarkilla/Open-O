//CHECKSTYLE:OFF
package oscar.eform;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class EformLogError2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    public String execute() throws Exception {
        String formId = request.getParameter("formId");
        String error = request.getParameter("error");

        /*
         * silent update to the eform error log.
         */
        if (formId != null && !formId.isEmpty()) {
            EFormUtil.logError(Integer.parseInt(formId), error);
        }
        return null;
    }
}
