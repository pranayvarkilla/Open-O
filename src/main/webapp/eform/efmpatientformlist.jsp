<%--

    Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
    This software is published under the GPL GNU General Public License.
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

    This software was written for the
    Department of Family Medicine
    McMaster University
    Hamilton
    Ontario, Canada

--%>
<!DOCTYPE html>
<%@page import="org.oscarehr.sharingcenter.SharingCenterUtil" %>
<%@page import="org.oscarehr.sharingcenter.dao.AffinityDomainDao" %>
<%@page import="org.oscarehr.sharingcenter.model.AffinityDomainDataObject" %>
<%@page import="org.oscarehr.util.SpringUtils" %>

<%@page import="java.util.*,oscar.eform.*" %>
<%@page import="org.oscarehr.web.eform.EfmPatientFormList" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%
    String demographic_no = request.getParameter("demographic_no");
    String deepColor = "#CCCCFF", weakColor = "#EEEEFF";

	if (session.getAttribute("userrole") == null) {
		response.sendRedirect("../logout.jsp");
	}
    String roleName$ = (String) session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    String country = request.getLocale().getCountry();
    String orderByRequest = request.getParameter("orderby");
    String orderBy = "";
	if (orderByRequest == null) {
		orderBy = EFormUtil.DATE;
	} else if (orderByRequest.equals("form_subject")) {
		orderBy = EFormUtil.SUBJECT;
	} else if (orderByRequest.equals("form_name")) {
		orderBy = EFormUtil.NAME;
	}

    String groupView = request.getParameter("group_view");
    if (groupView == null) {
        groupView = "";
    }

    String appointment = request.getParameter("appointment");
    String parentAjaxId = request.getParameter("parentAjaxId");
%>
<%@ taglib uri="/WEB-INF/oscar-tag.tld" prefix="oscar" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<html:html>

    <head>
        <title><bean:message key="eform.showmyform.title"/></title>


        <link href="${pageContext.request.contextPath}/css/bootstrap.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/css/DT_bootstrap.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/css/bootstrap-responsive.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/library/DataTables-1.10.12/media/css/jquery.dataTables.min.css" rel="stylesheet">

        <script src="${pageContext.request.contextPath}/js/global.js"></script>

        <script src="${pageContext.request.contextPath}/library/jquery/jquery-3.6.4.min.js"></script>
        <script src="${ pageContext.request.contextPath }/library/jquery/jquery-ui-1.12.1.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
        <script src="${ pageContext.request.contextPath }/library/DataTables/datatables.min.js"></script><!-- 1.13.4 -->

        <script src="${ pageContext.request.contextPath }/js/jquery.fileDownload.js"></script>
        <script src="${ pageContext.request.contextPath }/share/javascript/Oscar.js"></script>

        <script>

			$(document).ready(function () {
				// let shareDocumentsTarget = "../sharingcenter/documents/shareDocumentsAction.jsp";

				// Share button click event
				// $("#SendToAffinityDomain").on("click", function () {
				// 	// change the form's action (share page) then submit (only if forms are selected)
				// 	if ($("input:checkbox[name='sendToPhr']:checked").length > 0) {
				// 		$("#sendToPhrForm").attr('action', shareDocumentsTarget);
				// 		$("#sendToPhrForm").trigger("submit");
				// 	} else {
				// 		alert('No forms selected');
				// 		return false;
				// 	}
				// });
				let table = jQuery('#efmTable').DataTable({
					"lengthMenu": [[15, 30, 60, 120, -1], [15, 30, 60, 120, '<bean:message key="demographic.search.All"/>']],
					"order": [2],
					columnDefs: [{ orderable: false, targets: 3 }],
					"language": {
						"url": "<%=request.getContextPath() %>/library/DataTables/i18n/<bean:message key="global.i18nLanguagecode"/>.json"
					}
				});

			});

			function popupPage(varpage, windowname) {
				let page = "" + varpage;
				windowprops = "height=700,width=800,location=no,"
					+ "scrollbars=yes,menubars=no,status=yes,toolbars=no,resizable=yes,top=10,left=200";
				let popup = window.open(page, windowname, windowprops);
				if (popup != null) {
					if (popup.opener == null) {
						popup.opener = self;
					}
					popup.focus();
				}
			}

			function checkSelectBox() {
				let selectVal = document.forms[0].group_view.value;
				if (selectVal === "default") {
					return false;
				}
			}

			function updateAjax() {
				let parentAjaxId = "<%=parentAjaxId%>";
				if (parentAjaxId !== "null") {
					window.opener.document.forms['encForm'].elements['reloadDiv'].value = parentAjaxId;
					window.opener.updateNeeded = true;
				}

			}
        </script>
        <style>
            :root *:not(h2) {
                font-size: 12px;
                line-height: 1;

            }

            div.menu-columns {
                display: flex;
                gap: 10px;
            }

            div.left-column {
                display: flex;
                flex-direction: column;
                gap: 5px;
                flex-basis: max-content;
                text-wrap: nowrap;
            }
        </style>

    </head>

    <body onunload="updateAjax()">
    <div class="container">
        <h2><bean:message key="eform.showmyform.msgFormLybrary"/></h2>
        <div class="menu-columns">

            <div class="left-column">

                <a href="${ pageContext.request.contextPath }/demographic/demographiccontrol.jsp?demographic_no=<%=demographic_no%>&appointment=<%=appointment%>&displaymode=edit&dboperation=search_detail"><bean:message
                        key="demographic.demographiceditdemographic.btnMasterFile"/></a>
                <a href="efmformslistadd.jsp?demographic_no=<%=demographic_no%>&appointment=<%=appointment%>&parentAjaxId=<%=parentAjaxId%>"
                   class="current"> <bean:message key="eform.showmyform.btnAddEForm"/></a>
                <a href="efmpatientformlist.jsp?demographic_no=<%=demographic_no%>&appointment=<%=appointment%>&parentAjaxId=<%=parentAjaxId%>"><bean:message
                        key="eform.calldeletedformdata.btnGoToForm"/></a>
                <a href="efmpatientformlistdeleted.jsp?demographic_no=<%=demographic_no%>&appointment=<%=appointment%>&parentAjaxId=<%=parentAjaxId%>"><bean:message
                        key="eform.showmyform.btnDeleted"/></a>

                <security:oscarSec roleName="<%=roleName$%>" objectName="_admin,_admin.eform" rights="r"
                                   reverse="<%=false%>">
                    <a href="#"
                       onclick="javascript: return popup(600, 1200, '${ pageContext.request.contextPath }/administration/?show=Forms', 'manageeforms');"
                       style="color: #835921;"><bean:message key="eform.showmyform.msgManageEFrm"/></a>
                </security:oscarSec>

                <jsp:include page="efmviewgroups.jsp">
                    <jsp:param name="url" value="${ pageContext.request.contextPath }/eform/efmpatientformlist.jsp"/>
                    <jsp:param name="groupView" value="<%=groupView%>"/>
                    <jsp:param name="patientGroups" value="1"/>
                    <jsp:param name="parentAjaxId" value="<%=parentAjaxId%>"/>
                </jsp:include>

            </div>
            <div class="right-column">

                <form id="sendToPhrForm" action="efmpatientformlistSendPhrAction.jsp">
                    <input type="hidden" name="clientId" value="<%=demographic_no%>"/>
                    <table id="efmTable" class="display compact nowrap">
                        <thead>
                        <tr>
                            <th>
                                <bean:message key="eform.showmyform.btnFormName"/>
                            </th>
                            <th>
                                <bean:message key="eform.showmyform.btnSubject"/>
                            </th>
                            <th>
                                <bean:message key="eform.showmyform.formDate"/>
                            </th>
                            <th><bean:message key="eform.showmyform.msgAction"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            ArrayList<HashMap<String, ? extends Object>> eForms;
                            if (groupView.equals("")) {
                                eForms = EFormUtil.listPatientEForms(orderBy, EFormUtil.CURRENT, demographic_no, roleName$);
                            } else {
                                eForms = EFormUtil.listPatientEForms(orderBy, EFormUtil.CURRENT, demographic_no, groupView, roleName$);
                            }

                            for (int i = 0; i < eForms.size(); i++) {
                                HashMap<String, ? extends Object> curform = eForms.get(i);
                        %>
                        <tr>

                            <td><a href="#"
                                   ONCLICK="popupPage('efmshowform_data.jsp?fdid=<%=curform.get("fdid")%>&appointment=<%=appointment%>', '<%="FormP" + i%>'); return false;"
                                   TITLE="<bean:message key="eform.showmyform.msgViewFrm"/>"
                                   onmouseover="window.status='<bean:message
                                           key="eform.showmyform.msgViewFrm"/>'; return true"><%=curform.get("formName")%>
                            </a></td>
                            <td><%=curform.get("formSubject")%>
                            </td>
                            <td><%=curform.get("formDate")%>
                            </td>
                            <td><a href="${ pageContext.request.contextPath }/eform/removeEForm.do?fdid=<%=curform.get("fdid")%>&group_view=<%=groupView%>&demographic_no=<%=demographic_no%>&parentAjaxId=<%=parentAjaxId%>"
                                    onClick="javascript: return confirm('Are you sure you want to delete this eform?');"><bean:message
                                    key="eform.uploadimages.btnDelete"/></a></td>
                        </tr>
                        <%
                            }
                        %>
                        </tbody>
                    </table>
                </form>
            </div>

        </div>
    </div>
    </body>
</html:html>