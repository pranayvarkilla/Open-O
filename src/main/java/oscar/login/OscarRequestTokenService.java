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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import org.oscarehr.ws.oauth.OAuth1Client;
import com.github.scribejava.core.model.OAuth1RequestToken;

/**
 * This resource issues a temporarily request token to the Client
 * which will be later authorised and exchanged for the access token
 */
@Path("/initiate")
public class OscarRequestTokenService {

    private final OAuth1Client oauth1Client;

    public OscarRequestTokenService() {
        // Initialize OAuth1Client with your API key, secret, and callback URL
        String apiKey = System.getenv("OAUTH_API_KEY");
        String apiSecret = System.getenv("OAUTH_API_SECRET");
        String callbackUrl = System.getenv("OAUTH_CALLBACK_URL");
        this.oauth1Client = new OAuth1Client(apiKey, apiSecret, callbackUrl);
    }

    @GET
    @Produces("application/x-www-form-urlencoded")
    public Response getRequestTokenWithGET() {
        return getRequestToken();
    }

    @POST
    @Produces("application/x-www-form-urlencoded")
    public Response getRequestToken() {
        try {
            // Generate a request token
            OAuth1RequestToken requestToken = oauth1Client.getService().getRequestToken();

            // Generate the authorization URL using the request token
            String authorizationUrl = oauth1Client.getAuthorizationUrl(requestToken);

            // Return the authorization URL as part of the response
            return Response.ok("Authorization URL: " + authorizationUrl).build();
        } catch (Exception e) {
            // Handle exceptions and return an error response
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error generating request token: " + e.getMessage())
                    .build();
        }
    }
}