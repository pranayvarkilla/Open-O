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
package oscar.login;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.core.Response;

import org.oscarehr.ws.oauth.OAuth1Client;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;

public class OscarRequestTokenHandler {

    private static final Logger LOG = Logger.getLogger(OscarRequestTokenHandler.class.getName());
    private final OAuth1Client oauth1Client;
    private long tokenLifetime = 3600L;
    private String defaultScope;

    public OscarRequestTokenHandler(OAuth1Client oauth1Client) {
        this.oauth1Client = oauth1Client;
    }

    public Response handle() {
        try {
            // Generate a request token using the OAuth1Client
            OAuth10aService service = oauth1Client.getService();
            OAuth1RequestToken requestToken = service.getRequestToken();

            // Generate the authorization URL using the request token
            String authorizationUrl = oauth1Client.getAuthorizationUrl(requestToken);

            // Generate the response body with the request token and authorization URL
            Map<String, Object> responseParams = new HashMap<>();
            responseParams.put("oauth_token", requestToken.getToken());
            responseParams.put("oauth_token_secret", requestToken.getTokenSecret());
            responseParams.put("oauth_callback_confirmed", Boolean.TRUE);
            responseParams.put("authorization_url", authorizationUrl);

            String responseBody = formEncode(responseParams);

            return Response.ok(responseBody).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error generating request token", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error generating request token: " + e.getMessage())
                    .build();
        }
    }

    private String formEncode(Map<String, Object> responseParams) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : responseParams.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    public void setTokenLifetime(long tokenLifetime) {
        this.tokenLifetime = tokenLifetime;
    }

    public void setDefaultScope(String defaultScope) {
        this.defaultScope = defaultScope;
    }
}
