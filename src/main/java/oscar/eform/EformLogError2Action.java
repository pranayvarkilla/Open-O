//CHECKSTYLE:OFF
package oscar.eform;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.ActionContext;

public class EformLogError2Action extends ActionSupport {
    ActionContext context = ActionContext.getContext();
    HttpServletRequest request = (HttpServletRequest) context.get(ServletActionContext.HTTP_REQUEST);
    HttpServletResponse response = (HttpServletResponse) context.get(ServletActionContext.HTTP_RESPONSE);

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
