
package com.pg.google.api.connector.node;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.pg.google.api.connector.data.GoogleApiConnection;
import com.pg.knime.secure.DefaultVault;

/**
 * Configuration of the GoogleApiConnector node.
 * 
 */
public class GoogleApiConnectorNodeConfiguration {

	private String m_refreshToken;
    
	private String[] tokens;
    private String[] authTypes;
    
	public static final String CFG_CLIENT_ID = "client_id";
    public static final String CFG_CLIENT_SECRET = "client_secret";
    public static final String CFG_REFRESH_TOKEN = "refresh_token";
        
    private static final String DEFAULT_AUTH_TYPE = "User Specified";
    
    public static final String[] SCOPES = new String[] {"https://www.googleapis.com/auth/analytics.readonly", "https://www.googleapis.com/auth/analytics"};
    
    private DefaultVault vault;
    
    public GoogleApiConnectorNodeConfiguration() {
    	
    	/* If GoogleVault implementation not present default to custom token */
    	try {
    		vault = (DefaultVault)Class.forName("com.pg.knime.vault.GoogleVault").newInstance();	
    	} catch ( ClassNotFoundException | IllegalAccessException | InstantiationException exc ) {
    		vault = new DefaultVault();
    	}
    	
    	tokens = vault.getKey("USER_TOKENS").split(",");
    	authTypes = vault.getKey("TOKEN_TYPES").split(",");
    }
    
	public String getClientId() {
		return vault.getKey("DEFAULT_CLIENT_ID");
	}

	public String getClientSecret() {
		return vault.getKey("DEFAULT_CLIENT_SECRET");
	}

	public String[] getAuthTypes() {
		ArrayList<String> types = new ArrayList<String>();
		
		for ( String t : authTypes ) {
			types.add(t);
		}
		types.add(DEFAULT_AUTH_TYPE);
		
		return types.toArray(new String[] {} );
	}
	
	public String[] getTokens() {
		ArrayList<String> types = new ArrayList<String>();
		
		for ( String t : tokens ) {
			types.add(t);
		}
		types.add("");
		
		return types.toArray(new String[] {} );
	}
	
	public String getAuthTypeFromToken (String token) {
		
		for ( int i = 0; i < tokens.length; i++ ) {
			if ( token.equals(tokens[i]) ) return authTypes[i];
		}
		
		return DEFAULT_AUTH_TYPE;
	}
	
	public String getTokenFromAuthType( String authType ) {
		
		for ( int i=0; i < authTypes.length; i++ ) {
			if ( authType.equals(authTypes[i])) return tokens[i];
		}
		
		return "";
		
	}
	
	public String getRefreshToken() {
		return m_refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.m_refreshToken = refreshToken;
	}
	

	/**
     * @param settings The settings object to save in
     */
    public void save(final NodeSettingsWO settings) {
        settings.addString(CFG_REFRESH_TOKEN, m_refreshToken);
    }

    /**
     * @param settings The settings object to load from
     * @throws InvalidSettingsException If the settings are invalid
     */
    public void loadInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
    	m_refreshToken = settings.getString(CFG_REFRESH_TOKEN, "");
    }

    /**
     * @param settings The settings object to load from
     */
    public void loadInDialog(final NodeSettingsRO settings) {
        m_refreshToken = settings.getString(CFG_REFRESH_TOKEN, "");
    }

    /**
     * @return GoogleApiConnection based on this configuration
     * @throws InvalidSettingsException If the current configuration is not valid
     */
    public GoogleApiConnection createGoogleApiConnection() throws InvalidSettingsException {
        
    	try {
            return new GoogleApiConnection(getClientId(), getClientSecret(), getRefreshToken());
        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidSettingsException(e);
        }
    }

}
