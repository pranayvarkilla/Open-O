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

import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.oscarehr.util.MiscUtils;
import org.owasp.csrfguard.CsrfGuard;
import org.owasp.csrfguard.CsrfGuardException;
import org.owasp.csrfguard.CsrfGuardFilter;
import org.owasp.csrfguard.CsrfValidator;
import org.owasp.csrfguard.ProtectionResult;
import org.owasp.csrfguard.action.IAction;
import org.owasp.csrfguard.http.InterceptRedirectResponse;
import org.owasp.csrfguard.util.RandomGenerator;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import oscar.OscarProperties;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oscar OscarCsrfGuardFilter
 * A CsrfGuardFilter implementation that supports detecting and paring multipart/form-data requests in addition to
 * the existing support
 */
public class OscarCsrfGuardFilter implements Filter {

    private FilterConfig filterConfig = null;

    @Override
    public void destroy() {
        filterConfig = null;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        CsrfGuard csrfGuard = CsrfGuard.getInstance();
        CsrfValidator csrfValidator = new CsrfValidator();

        //maybe the short circuit to disable is set
        if (!csrfGuard.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        /* only work with HttpServletRequest objects */
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpSession session = httpRequest.getSession(false);

            /*
             * if there is no session and we aren't validating when no session exists.
             * OR if this page request is indicated as unprotected in the CsrfGuard properties.
             * This mainly applies to uploads coming through the authenticated SOAP or REST API.
             * If true: short circuit the process.
             */
            if ((session == null && !csrfGuard.isValidateWhenNoSessionExists())
                    || !csrfValidator.isProtectedPage(httpRequest.getRequestURI()).isProtected()) {
                filterChain.doFilter(httpRequest, response);
                return;
            }

            MiscUtils.getLogger().debug(String.format("CsrfGuard Filter analyzing request %s", httpRequest.getRequestURI()));

            // Default to not redirect unless csrf_do_redirect is set
            // TODO: reverse this when there are more pages by csrf covered 
            boolean doRedirect = false;
            if (OscarProperties.getInstance().getProperty("csrf_do_redirect") != null) {
                doRedirect = OscarProperties.getInstance().isPropertyActive("csrf_do_redirect");
            }
            if (!doRedirect) {
                IAction redirectActionToRemove = null;
                for (IAction action : csrfGuard.getActions()) {
                    if ("Redirect".equals(action.getName())) {
                        redirectActionToRemove = action;
                        break;
                    }
                }
                csrfGuard.getActions().remove(redirectActionToRemove);
            }

            InterceptRedirectResponse httpResponse = new InterceptRedirectResponse((HttpServletResponse) response, httpRequest, csrfGuard);

            if ((session != null && session.isNew()) && csrfGuard.isUseNewTokenLandingPage()) {
                String logicalSessionKey = session.getId();
                csrfGuard.writeLandingPage(httpResponse, logicalSessionKey);
            } else if (JakartaServletFileUpload.isMultipartContent(httpRequest)) {
                MultiReadHttpServletRequest multiReadHttpRequest = new MultiReadHttpServletRequest(httpRequest);
                if (isValidMultipartRequest(multiReadHttpRequest, httpResponse)) {
                    filterChain.doFilter(multiReadHttpRequest, httpResponse);
                } else if (!doRedirect) {
                    filterChain.doFilter(multiReadHttpRequest, httpResponse);
                }
            } else if (csrfValidator.isValid(httpRequest, httpResponse)) {
                filterChain.doFilter(httpRequest, httpResponse);
            } else if (!doRedirect) {
                filterChain.doFilter(httpRequest, httpResponse);
            }

            /* update tokens */
            csrfGuard.updateTokens(httpRequest);

        } else {
            filterConfig.getServletContext().log(String.format("[WARNING] CsrfGuard does not know how to work with requests of class %s ", request.getClass().getName()));

            filterChain.doFilter(request, response);
        }
    }

    @Override
    public void init(@SuppressWarnings("hiding") FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    private boolean isValidMultipartRequest(MultiReadHttpServletRequest request, HttpServletResponse response) {
        CsrfGuard csrfGuard = CsrfGuard.getInstance();
        CsrfValidator csrfValidator = new CsrfValidator();
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        ProtectionResult protectionResult = csrfValidator.isProtectedPageAndMethod(requestURI, requestMethod);
        boolean valid = !protectionResult.isProtected();
        HttpSession session = request.getSession(true);
        String tokenFromSession = (String) session.getAttribute(csrfGuard.getLogicalSessionExtractor().extract(request).getKey());

        /** sending request to protected resource - verify token **/
        if (tokenFromSession != null && !valid) {
            try {
                if (csrfGuard.isAjaxEnabled() && isAjaxRequest(request)) {
                    String tokenFromRequest = request.getHeader(csrfGuard.getTokenName());

                    if (tokenFromRequest == null) {
                        /** FAIL: token is missing from the request **/
                        throw new CsrfGuardException("required token is missing from the request");
                    } else {
                        //if there are two headers, then the result is comma separated
                        if (!tokenFromSession.equals(tokenFromRequest)) {
                            if (tokenFromRequest.contains(",")) {
                                tokenFromRequest = tokenFromRequest.substring(0, tokenFromRequest.indexOf(',')).trim();
                            }
                            if (!tokenFromSession.equals(tokenFromRequest)) {
                                /** FAIL: the request token does not match the session token **/
                                throw new CsrfGuardException("request token does not match session token");
                            }
                        }
                    }
                } else if (csrfGuard.isTokenPerPageEnabled()) {
                    verifyPageToken(request);
                } else {
                    verifySessionToken(request);
                }
                valid = true;
            } catch (CsrfGuardException csrfe) {
                callActionsOnError(request, response, csrfe);
            }

            /** rotate session and page tokens **/
            if (!isAjaxRequest(request) && csrfGuard.isRotateEnabled()) {
                rotateTokens(request);
            }
            /** expected token in session - bad state and not valid **/
        } else if (tokenFromSession == null && !valid) {
            try {
                throw new CsrfGuardException("CsrfGuard expects the token to exist in session at this point");
            } catch (CsrfGuardException csrfe) {
                callActionsOnError(request, response, csrfe);

            }
        } else {
            /** unprotected page - nothing to do **/
        }

        return valid;
    }

    private boolean isAjaxRequest(MultiReadHttpServletRequest request) {
        return request.getHeader("X-Requested-With") != null;
    }

    private void verifyPageToken(MultiReadHttpServletRequest request) throws CsrfGuardException {
        CsrfGuard csrfGuard = CsrfGuard.getInstance();
        HttpSession session = request.getSession(true);
        @SuppressWarnings("unchecked")
        Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(CsrfGuard.PAGE_TOKENS_KEY);

        String tokenFromPages = (pageTokens != null ? pageTokens.get(request.getRequestURI()) : null);
        String tokenFromSession = (String) session.getAttribute(csrfGuard.getLogicalSessionExtractor().extract(request).getKey());
        MultipartHttpServletRequest multipartRequest = new StandardServletMultipartResolver().resolveMultipart(request);
        String tokenFromRequest = multipartRequest.getParameter(csrfGuard.getTokenName());

        if (tokenFromRequest == null) {
            /** FAIL: token is missing from the request **/
            throw new CsrfGuardException("required token is missing from the request");
        } else if (tokenFromPages != null) {
            if (!tokenFromPages.equals(tokenFromRequest)) {
                /** FAIL: request does not match page token **/
                throw new CsrfGuardException("request token does not match page token");
            }
        } else if (!tokenFromSession.equals(tokenFromRequest)) {
            /** FAIL: the request token does not match the session token **/
            throw new CsrfGuardException("request token does not match session token");
        }
    }

    private void verifySessionToken(MultiReadHttpServletRequest request) throws CsrfGuardException {
        CsrfGuard csrfGuard = CsrfGuard.getInstance();
        HttpSession session = request.getSession(true);
        String tokenFromSession = (String) session.getAttribute(csrfGuard.getLogicalSessionExtractor().extract(request).getKey());
        MultipartHttpServletRequest multipartRequest = new StandardServletMultipartResolver().resolveMultipart(request);
        String tokenFromRequest = multipartRequest.getParameter(csrfGuard.getTokenName());

        if (tokenFromRequest == null) {
            /** FAIL: token is missing from the request **/
            throw new CsrfGuardException("required token is missing from the request");
        } else if (!tokenFromSession.equals(tokenFromRequest)) {
            /** FAIL: the request token does not match the session token **/
            throw new CsrfGuardException("request token does not match session token");
        }
    }

    private void callActionsOnError(MultiReadHttpServletRequest request,
                                    HttpServletResponse response, CsrfGuardException csrfe) {
        CsrfGuard csrfGuard = CsrfGuard.getInstance();
        for (IAction action : csrfGuard.getActions()) {
            try {
                action.execute(request, response, csrfe, csrfGuard);
            } catch (CsrfGuardException exception) {
                Logger logger = LoggerFactory.getLogger(CsrfGuardFilter.class);
                logger.error("CSRFGuard action execution failed", exception);
            }
        }
    }

    private void rotateTokens(MultiReadHttpServletRequest request) {
        CsrfGuard csrfGuard = CsrfGuard.getInstance();
        HttpSession session = request.getSession(true);

        /** rotate master token **/
        String tokenFromSession = null;

        try {
            tokenFromSession = RandomGenerator.generateRandomId(csrfGuard.getPrng(), csrfGuard.getTokenLength());
        } catch (Exception e) {
            throw new RuntimeException(String.format("unable to generate the random token - %s", e.getLocalizedMessage()), e);
        }

        session.setAttribute(csrfGuard.getLogicalSessionExtractor().extract(request).getKey(), tokenFromSession);

        /** rotate page token **/
        if (csrfGuard.isTokenPerPageEnabled()) {
            @SuppressWarnings("unchecked")
            Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(CsrfGuard.PAGE_TOKENS_KEY);

            try {
                pageTokens.put(request.getRequestURI(), RandomGenerator.generateRandomId(csrfGuard.getPrng(), csrfGuard.getTokenLength()));
            } catch (Exception e) {
                throw new RuntimeException(String.format("unable to generate the random token - %s", e.getLocalizedMessage()), e);
            }
        }
    }
}
