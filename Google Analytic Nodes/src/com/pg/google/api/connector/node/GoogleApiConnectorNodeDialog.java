
package com.pg.google.api.connector.node;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.pg.google.api.connector.data.GoogleApiConnection;
import com.pg.knime.node.StandardNodeDialogPane;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class GoogleApiConnectorNodeDialog extends StandardNodeDialogPane {

    private JTextField txtRefreshToken = new JTextField();
    private DefaultComboBoxModel<String> cbmApiSelection = new DefaultComboBoxModel<String>();
    private JComboBox<String> cbxApiSelection;
    
    private GoogleApiConnectorNodeConfiguration configuration = new GoogleApiConnectorNodeConfiguration();
    
    /**
     * Constructor creating the dialogs content.
     * @wbp.parser.entryPoint
     */
    public GoogleApiConnectorNodeDialog() {
    	
    	// Setup user authentication select drop-down:
    	cbxApiSelection = new JComboBox<String>(cbmApiSelection);
    	for (String type: configuration.getAuthTypes() ) {
    		cbmApiSelection.addElement(type);
    	}
    	
    	
    	
    	// Setup token request button
    	JButton tokenButton = new JButton("<HTML><FONT color=\"#000099\"><U>Click to create user token</U></FONT></HTML>");
    	tokenButton.setBorderPainted(false);
    	tokenButton.setOpaque(false);
        tokenButton.addActionListener(new TokenButtonAction());
    	
    	JPanel pnlSettings = buildStandardPanel(
    			new LabelComponentPair("Credentials", cbxApiSelection),
    			new LabelComponentPair("User Token", txtRefreshToken ),
    			new LabelComponentPair("", tokenButton)
    	);
    	
    	addTab("Settings", pnlSettings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    	configuration.setRefreshToken(txtRefreshToken.getText());
    	configuration.save(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
        configuration = new GoogleApiConnectorNodeConfiguration();
        configuration.loadInDialog(settings);
        
        // Default to first item stored as possible Token:
        if ( configuration.getRefreshToken().isEmpty() ) {
        	configuration.setRefreshToken(configuration.getTokens()[0]);
        }
        
        // Set drop-down to match Token:
        cbmApiSelection.setSelectedItem(configuration.getAuthTypeFromToken(configuration.getRefreshToken()));
        
        // Set value to match Token:
        txtRefreshToken.setText(configuration.getRefreshToken());
        
        /*
         * Add Listeners
         */
        
        // Drop-down change listener
        if ( cbxApiSelection.getActionListeners().length == 0 )
        	cbxApiSelection.addActionListener(new TokenChangeAction());
        
        // Setup token refresh field
        if ( txtRefreshToken.getFocusListeners().length == 0 )
        	txtRefreshToken.addFocusListener(new RefreshTokenValidation());
    	
    }
 
    /**
     * RefreshTokenValidation
     */
    class RefreshTokenValidation implements FocusListener {

		@Override
		public void focusGained(FocusEvent e) {
			
		}

		@Override
		public void focusLost(FocusEvent e) {
			String token = txtRefreshToken.getText();
			
			// Guard Statements:
			if (token == null || StringUtils.isBlank(token)) return;
			
			if (token.startsWith("1/") ) return;
			
			// Try and convert to long-lasting refresh token
			try {
				String refreshToken = GoogleApiConnection.Get_Refresh_Token(configuration.getClientId(), configuration.getClientSecret(), token);
				txtRefreshToken.setText(refreshToken);
			} catch  (IOException exc ) {
				exc.printStackTrace();
			}
		}
    	
    }
    
    /**
     * TokenChangeAction
     */
    class TokenChangeAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			String selectedItem = (String)cbmApiSelection.getSelectedItem();
			if ( selectedItem == null ) return;
			
			String token = configuration.getTokenFromAuthType(selectedItem);
			configuration.setRefreshToken(token);
			
			txtRefreshToken.setText(configuration.getRefreshToken());
		}
    	
    }

    /**
     * TokenButtonAction
     */
    class TokenButtonAction implements ActionListener {
    	@Override
    	public void actionPerformed(ActionEvent e) {
    		
    	
    		// Generate the URL to send the user to grant access.
    		Collection<String> requestedScopes = new ArrayList<String>();
    		Collections.addAll(requestedScopes, GoogleApiConnectorNodeConfiguration.SCOPES);
    		
    		String authorizationUrl = new GoogleAuthorizationCodeRequestUrl(
					configuration.getClientId(), 
					"urn:ietf:wg:oauth:2.0:oob", 
					requestedScopes).build();
    		
    		if ( !Desktop.isDesktopSupported() ) {
				JOptionPane.showMessageDialog(null, "Please go to " + authorizationUrl + " in your browser to retrieve token.");
    			return;			
			}
    		
    		try {
    			final URI uri = new URI(authorizationUrl);
    			Desktop.getDesktop().browse(uri);
    		} catch ( Exception exc ) {
    			JOptionPane.showMessageDialog(null, "Please go to " + authorizationUrl + " in your browser to retrieve token.");
    			return;	
    		} 
			
    	}
    }
    
}
