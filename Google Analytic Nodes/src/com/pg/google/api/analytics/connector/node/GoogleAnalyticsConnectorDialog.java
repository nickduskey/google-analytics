package com.pg.google.api.analytics.connector.node;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import com.pg.google.api.analytics.connector.data.GoogleAnalyticsConnection;
import com.pg.google.api.connector.data.GoogleApiConnection;
import com.pg.google.api.connector.data.GoogleApiConnectionPortObjectSpec;
import com.pg.knime.node.SortedComboBoxModel;


@SuppressWarnings("cast")
public class GoogleAnalyticsConnectorDialog extends NodeDialogPane {

	private DefaultComboBoxModel<String> accounts, properties,profiles;
	private Map<String, String> accountMap, propertyMap, profileMap;
	
	private JTextField txtProfileId;
	
	private JButton btnListAccounts, btnListProperties, btnListProfiles;
	
	private JTextField txtTimeout;
	
    /**
     * Constructor creating the dialogs content.
     */
    public GoogleAnalyticsConnectorDialog() {
    	
    	
    	/*
    	 * Settings Tab
    	 */
    	JPanel pnlSettings = new JPanel( new GridBagLayout() );
    	pnlSettings.add(new JLabel("Accounts:", SwingConstants.LEFT), getGBC(0, 0, 0, 0));
    	
    	accounts = new SortedComboBoxModel<String>();
    	pnlSettings.add(new JComboBox<String>(accounts), getGBC(0, 1, 1, 0));
    	
    	btnListAccounts = new JButton("Get");
    	pnlSettings.add(btnListAccounts, getGBC(1, 1, 0, 0));
    	
    	pnlSettings.add(new JLabel("Web Properties:", SwingConstants.LEFT), getGBC(0, 2, 0, 0));
    	
    	properties = new SortedComboBoxModel<String>();
    	pnlSettings.add(new JComboBox<String>(properties), getGBC(0, 3, 1, 0));
    	
    	btnListProperties = new JButton("Get");
    	pnlSettings.add(btnListProperties, getGBC(1, 3, 0, 0));
    	
    	pnlSettings.add(new JLabel("Profiles:", SwingConstants.LEFT), getGBC(0, 4, 0, 0));
    	
    	profiles = new SortedComboBoxModel<String>();
    	JComboBox<String> cbxProfiles = new JComboBox<String>(profiles);
    	pnlSettings.add(cbxProfiles, getGBC(0, 5, 1, 0));
    	cbxProfiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String profileName = (String)profiles.getSelectedItem();
				txtProfileId.setText(profileMap.get(profileName));
			}
		});
    	
    	btnListProfiles = new JButton("Get");
    	pnlSettings.add(btnListProfiles, getGBC(1, 5, 0, 0));
    	
    	pnlSettings.add(new JLabel("Profile Id:", SwingConstants.LEFT), getGBC(0, 6, 0, 0));
    	txtProfileId = new JTextField();
    	pnlSettings.add(txtProfileId, getGBC(0, 7, 1, 0));
    	
    	
    	
    	pnlSettings.add(new JPanel(), getGBC(0, 8, 100, 100));
    	addTab("Settings", pnlSettings);

    	/*
    	 * Advanced Tab
    	 */
    	JPanel pnlAdvanced = new JPanel( new GridBagLayout() );
    	
    	pnlAdvanced.add(new JLabel("Connection Time-Out (minutes)", SwingConstants.LEFT), getGBC(0, 0, 0, 0));
    	
    	txtTimeout = new JTextField();
    	pnlAdvanced.add(txtTimeout, getGBC(0, 2, 0, 0));
    	
    	pnlAdvanced.add(new JPanel(), getGBC(0, 3, 100, 100));
    	addTab("Advanced", pnlAdvanced);
    	
    }
    
    private static GridBagConstraints getGBC( int gridx, int gridy, int weightx, int weighty ) {
    	return new GridBagConstraints(
				gridx, 							// gridx
				gridy, 							// gridy
				1, 								// gridwidth
				1,								// gridheight
				weightx,						// weightx
				weighty, 						// weighty
				GridBagConstraints.NORTHWEST, 	// anchor
				GridBagConstraints.HORIZONTAL, 	// fill
				new Insets(5, 5, 5, 5), 		// insets
				0, 								// ipadx
				0);								// ipady
    	
    	
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        GoogleAnalyticsConnectorConfiguration config = new GoogleAnalyticsConnectorConfiguration();
        config.setProfileId(txtProfileId.getText());
        config.setTimeOut(txtTimeout.getText());
        config.save(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
        
    	if (specs[0] == null) {
            throw new NotConfigurableException("Missing Google API Connection");
        }
        
        GoogleApiConnectionPortObjectSpec connectionSpec = (GoogleApiConnectionPortObjectSpec)specs[0];
        if (connectionSpec.getGoogleApiConnection() == null) {
            throw new NotConfigurableException("Missing Google API Connection");
        }
        
        
        btnListAccounts.addActionListener(
        		new GetAccountList(connectionSpec.getGoogleApiConnection())
        );
        btnListProperties.addActionListener(
        		new GetWebPropertyList(connectionSpec.getGoogleApiConnection())
        );
        btnListProfiles.addActionListener(
        		new GetProfileList(connectionSpec.getGoogleApiConnection())
        );
        txtProfileId.addFocusListener(
        		new GetProfileName(connectionSpec.getGoogleApiConnection())
        );
        
        GoogleAnalyticsConnectorConfiguration config = new GoogleAnalyticsConnectorConfiguration();
        config.loadInDialog(settings);
        
        if (!config.getProfileId().isEmpty()) {
            txtProfileId.setText(config.getProfileId());
        }
        if (!config.getTimeOut().isEmpty() ) {
        	txtTimeout.setText(config.getTimeOut());
        }
        
    }

    class GetAccountList implements ActionListener {

    	private GoogleApiConnection connection;
    	
    	public GetAccountList(GoogleApiConnection connection) {
			this.connection = connection;
		}
    	
		@Override
		public void actionPerformed(ActionEvent e) {
			accounts.removeAllElements();
			try {
				accountMap = GoogleAnalyticsConnection.getAccounts(connection);
				for ( String accountName : accountMap.keySet() ) {
					accounts.addElement(accountName);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    	
    }

    class GetWebPropertyList implements ActionListener {

    	private GoogleApiConnection connection;
    	
    	public GetWebPropertyList(GoogleApiConnection connection) {
			this.connection = connection;
		}
    	
		@Override
		public void actionPerformed(ActionEvent e) {
			String selectedName = (String)accounts.getSelectedItem();
			// Guard statement:
			if ( selectedName == null ) return;
			String selectedId = accountMap.get(selectedName);
			
			properties.removeAllElements();
			
			try {
				propertyMap = GoogleAnalyticsConnection.getWebProperties(connection, selectedId);
				for ( String name : propertyMap.keySet() ) {
					properties.addElement(name);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    	
    }    
    
    class GetProfileList implements ActionListener {

    	private GoogleApiConnection connection;
    	
    	public GetProfileList(GoogleApiConnection connection) {
			this.connection = connection;
		}
    	
		@Override
		public void actionPerformed(ActionEvent e) {
			profiles.removeAllElements();
			
			String selectedAccountName = (String)accounts.getSelectedItem();
			// Guard statement:
			if ( selectedAccountName == null ) return;
			
			String selectedAccountId = accountMap.get(selectedAccountName);
			
			String selectedName = (String)properties.getSelectedItem();
			String selectedId = propertyMap.get(selectedName);
			
			try {
				profileMap = GoogleAnalyticsConnection.getProfiles(connection, selectedAccountId, selectedId);
				for ( String name : profileMap.keySet() ) {
					profiles.addElement(name);
				}
				
				String profileName = (String)profiles.getSelectedItem();
				String profileId = profileMap.get(profileName);
				txtProfileId.setText(profileId);
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    	
    } 
    
    class GetProfileName implements FocusListener {
    	GoogleApiConnection connection;
    	
    	public GetProfileName(GoogleApiConnection connection) {
			this.connection = connection;
		}
    	
		@Override
		public void focusGained(FocusEvent e) {
			
			
		}

		@Override
		public void focusLost(FocusEvent e) {
			
			String profileId = txtProfileId.getText();
			String selectedProfileName = (String)profiles.getSelectedItem();
			String selectedProfileId = null;
			if ( profileMap !=null ) selectedProfileId = profileMap.get(selectedProfileName);
			
			// No profile Id entered
			if (StringUtils.isEmpty(profileId)) return;
			
			// Profile Id was chosen via ComboBox
			if ( profileId !=null && profileId.equals(selectedProfileId)) {
				return;
			}
			
			// Get the profile information
			try {
				profiles.removeAllElements();
				accounts.removeAllElements();
				properties.removeAllElements();
				
				profileMap = GoogleAnalyticsConnection.getProfile(connection, profileId);
				for ( String name : profileMap.keySet() ) {
					profiles.addElement(name);
					profiles.setSelectedItem(name);
				}
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
    	
    }
    
}
