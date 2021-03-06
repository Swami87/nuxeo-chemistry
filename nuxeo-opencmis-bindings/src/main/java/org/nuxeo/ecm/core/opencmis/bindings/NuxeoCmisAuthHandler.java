/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.nuxeo.ecm.core.opencmis.bindings;

import java.util.Map;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.impl.webservices.AbstractService;
import org.apache.chemistry.opencmis.server.impl.webservices.CmisWebServicesServlet;
import org.apache.chemistry.opencmis.server.shared.CsrfManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.api.login.Authenticator;

/**
 * SOAP handler that extracts authentication information from the SOAP headers and propagates it to Nuxeo for login.
 */
public class NuxeoCmisAuthHandler extends CXFAuthHandler implements LoginProvider {

    public static final String NUXEO_LOGIN_CONTEXT = "nuxeo.opencmis.LoginContext";

    private static final Log log = LogFactory.getLog(NuxeoCmisAuthHandler.class);

    protected LoginProvider loginProvider;

    protected CsrfManager csrfManager = new CsrfManager(null, null); // TODO configure CSRF token

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        boolean res = super.handleMessage(context);

        HttpServletRequest request = (HttpServletRequest) context.get(MessageContext.SERVLET_REQUEST);
        request.setAttribute(CmisWebServicesServlet.CMIS_VERSION, CmisVersion.CMIS_1_1);
        request.setAttribute(CmisWebServicesServlet.CSRF_MANAGER, csrfManager);

        @SuppressWarnings("unchecked")
        Map<String, String> callContextMap = (Map<String, String>) context.get(AbstractService.CALL_CONTEXT_MAP);
        if (callContextMap != null) {
            // login to Nuxeo
            String username = callContextMap.get(CallContext.USERNAME);
            String password = callContextMap.get(CallContext.PASSWORD);
            try {
                LoginContext loginContext = getLoginProvider().login(username, password);
                // store in message context, for later logout
                context.put(NUXEO_LOGIN_CONTEXT, loginContext);
                context.setScope(NUXEO_LOGIN_CONTEXT, Scope.APPLICATION);
            } catch (LoginException e) {
                throw new RuntimeException("Login failed for user '" + username + "'", e);
            }
        }
        return res;
    }

    @Override
    public void close(MessageContext context) {
        LoginContext loginContext = (LoginContext) context.get(NUXEO_LOGIN_CONTEXT);
        if (loginContext != null) {
            try {
                loginContext.logout();
            } catch (LoginException e) {
                log.error("Cannot logout", e);
            }
        }
        super.close(context);
    }

    protected LoginProvider getLoginProvider() {
        if (loginProvider == null) {
            loginProvider = this;
            String className = Framework.getProperty(LoginProvider.class.getName());
            if (className != null) {
                try {
                    Object instance = Class.forName(className).newInstance();
                    if (instance instanceof LoginProvider) {
                        loginProvider = (LoginProvider) instance;
                    } else {
                        log.error(className + " is not an instance of " + LoginProvider.class.getName());
                    }
                } catch (ReflectiveOperationException e) {
                    log.error(e);
                }
            }
        }
        return loginProvider;
    }

    // LoginProvider
    @Override
    public LoginContext login(String username, String password) {
        try {
            // check identity against UserManager
            if (!getAuthenticator().checkUsernamePassword(username, password)) {
                throw new RuntimeException("Authentication failed for user '" + username + "'");
            }
            // login to Nuxeo framework
            return Framework.login(username, password);
        } catch (LoginException e) {
            throw new RuntimeException("Login failed for user '" + username + "'", e);
        }
    }

    protected static Authenticator getAuthenticator() {
        return Framework.getService(Authenticator.class);
    }

}
