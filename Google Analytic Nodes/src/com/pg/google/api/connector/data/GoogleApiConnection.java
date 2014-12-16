package com.pg.google.api.connector.data;


import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.pg.google.api.connector.node.GoogleApiConnectorNodeConfiguration;

public class GoogleApiConnection {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    
    private GoogleCredential m_credential;
        
    private String m_clientId;
    
    private String m_clientSecret;
    
    private String m_refreshToken;

    /**
     * 
     */
    public GoogleApiConnection(final String clientId, final String clientSecret, final String refreshToken) 
    		throws GeneralSecurityException, IOException, InvalidSettingsException { 
    	
    	m_clientId = clientId;
    	m_clientSecret = clientSecret;
    	m_refreshToken = refreshToken;
    	
        if ( StringUtils.isNotEmpty(m_clientId) && StringUtils.isNotEmpty(m_clientSecret) && StringUtils.isNotEmpty(m_refreshToken) ) {
        	createFromOAuth(m_clientId, m_clientSecret, m_refreshToken);
        }
        
    }
    
    private void createFromOAuth(final String clientId, final String clientSecret, final String refreshToken) 
    		throws GeneralSecurityException, IOException {

    	m_credential =
    			new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT).setJsonFactory(JSON_FACTORY)
						.setClientSecrets(clientId, clientSecret).build()
						.setRefreshToken(refreshToken);
    }
    
    /**
     * @param model The model containing the connection information
     * @throws GeneralSecurityException If there was a problem with the key file
     * @throws IOException If the key file was not accessible
     * @throws InvalidSettingsException If the model was invalid
     */
    public GoogleApiConnection(final ModelContentRO model) throws GeneralSecurityException, IOException,
            InvalidSettingsException {
    	
    	this(
    		model.getString(GoogleApiConnectorNodeConfiguration.CFG_CLIENT_ID),
    		model.getString(GoogleApiConnectorNodeConfiguration.CFG_CLIENT_SECRET),
    		model.getString(GoogleApiConnectorNodeConfiguration.CFG_REFRESH_TOKEN)
    	);
    }

    /**
     * @return The httpTransport instance
     */
    public HttpTransport getHttpTransport() {
        return HTTP_TRANSPORT;
    }

    /**
     * @return The jsonFactory instance
     */
    public JsonFactory getJsonFactory() {
        return JSON_FACTORY;
    }

    /**
     * @return The Google API credential
     */
    public GoogleCredential getCredential() {
        return m_credential;
    }

    /**
     * @param model The model to save the current configuration in
     */
    public void save(final ModelContentWO model) {
        model.addString(GoogleApiConnectorNodeConfiguration.CFG_CLIENT_ID, m_clientId);
        model.addString(GoogleApiConnectorNodeConfiguration.CFG_CLIENT_SECRET, m_clientSecret);
        model.addString(GoogleApiConnectorNodeConfiguration.CFG_REFRESH_TOKEN, m_refreshToken);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GoogleApiConnection)) {
            return false;
        }
        GoogleApiConnection con = (GoogleApiConnection)obj;
        EqualsBuilder eb = new EqualsBuilder();
        eb.append(m_clientId, con.m_clientId);
        eb.append(m_clientSecret, con.m_clientSecret);
        eb.append(m_refreshToken, con.m_refreshToken);
        return eb.isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(m_clientId);
        hcb.append(m_clientSecret);
        hcb.append(m_refreshToken);
        return hcb.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Client id:\n" + m_clientId + "\n\n");
        sb.append("Client refresh token:\n" + m_refreshToken + "\n\n");
        
        return sb.toString();
    }
    
    public static String Get_Refresh_Token(String clientId, String clientSecret, String accessToken) throws IOException {
    	// Convert accessToken to refreshToken
		GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
				new NetHttpTransport(), 
				new JacksonFactory(),
				clientId, 
				clientSecret, 
				accessToken, 
				"urn:ietf:wg:oauth:2.0:oob"	
		).execute();

		return response.getRefreshToken();
    }

}
