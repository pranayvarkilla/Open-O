/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


package oscar.oscarReport.pageUtil;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.oscarehr.managers.SecurityInfoManager;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import oscar.OscarProperties;
import oscar.oscarEncounter.oscarMeasurements.pageUtil.EctValidation;
import oscar.oscarReport.data.ObecData;
import oscar.util.DateUtils;

public class ObecAction extends Action {
	
	private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
   
   public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
	   
	   if(!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_report", "r", null)) {
	  		  throw new SecurityException("missing required security object (_report)");
	  	  	}
	   
      Properties proppies = OscarProperties.getInstance();
      
      ObecForm frm = (ObecForm)form;
      ObecData obecData1 = new ObecData();
      DateUtils dateUtils = new DateUtils();
      EctValidation validation = new EctValidation();
      ActionMessages errors = new ActionMessages();
      
      String startDate = frm.getXml_vdate()==null?"":frm.getXml_vdate();
      if(!validation.isDate(startDate)){
         MiscUtils.getLogger().debug("Invalid date format!");
         errors.add(startDate,
         new ActionMessage("errors.invalid", "StartDate"));
         saveErrors(request, errors);
         return (new ActionForward(mapping.getInput()));
      }
      
      int numDays = frm.getNumDays();
      int startYear = 0;
      int startMonth = 0;
      int startDay = 0;
      
      int slashIndex1 = startDate.indexOf("-");
      if(slashIndex1>=0){
         startYear = Integer.parseInt(startDate.substring(0,slashIndex1));
         int slashIndex2 = startDate.indexOf("-", slashIndex1+1);
         if (slashIndex2>slashIndex1){
            startMonth = Integer.parseInt(startDate.substring(slashIndex1+1, slashIndex2));
            int length = startDate.length();
            startDay = Integer.parseInt(startDate.substring(slashIndex2+1, length));
         }
      }
      
      
      
      String endDate = dateUtils.NextDay(startDay, startMonth, startYear, numDays);
      
      String obectxt = obecData1.generateOBEC(startDate, endDate, proppies);
      request.setAttribute("obectxt", obectxt);
      
      return mapping.findForward("success");
   }
}
