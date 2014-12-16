package com.pg.google.api.analytics.connector.data;


import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.NodeLogger;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.Analytics.Data.Ga.Get;
import com.google.api.services.analytics.model.Account;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.GaData.ProfileInfo;
import com.google.api.services.analytics.model.Profile;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;
import com.google.api.services.analytics.model.Webproperty;
import com.pg.google.api.connector.data.GoogleApiConnection;


public class GoogleAnalyticsConnection {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(GoogleAnalyticsConnection.class);
	
    private static final String CFG_PROFILE_ID = "profileId";

    private static final String CFG_APPLICATION_NAME = "applicationName";

    private GoogleApiConnection m_connection;

    private String m_profileId;
    
    private String m_timeOut;

    private String m_applicationName;

    private Analytics m_analytics;
    
    private GaData dataModel;
    
    private static final int MAX_ATTEMPTS = 5;
    
    public static Map<String, String> getAccounts( final GoogleApiConnection connection ) throws IOException {
    	Map<String, String> map = new HashMap<>();
    	
    	Analytics analytics =
                new Analytics.Builder(connection.getHttpTransport(), connection.getJsonFactory(),
                        connection.getCredential()).setApplicationName("KNIME-Profiles-Scan").build();
        
    	Accounts accounts = analytics.management().accounts().list().execute();
    	if (accounts!=null && accounts.getTotalResults() > 0 )
	    	for ( Account account : accounts.getItems() ) {
	    		map.put(account.getName(), account.getId() );
	    	}
    	
    	return map;
    }
    
    public static Map<String, String> getWebProperties( final GoogleApiConnection connection, String accountId ) throws IOException {
    	Map<String, String> map = new HashMap<>();
    	
    	Analytics analytics =
                new Analytics.Builder(connection.getHttpTransport(), connection.getJsonFactory(),
                        connection.getCredential()).setApplicationName("KNIME-Profiles-Scan").build();
        
    	Webproperties properties = analytics.management().webproperties().list(accountId).execute();
    	if (properties!=null && properties.getTotalResults() > 0)
	    	for ( Webproperty property : properties.getItems() ) {
	    		map.put(property.getName(), property.getId() );
	    	}
    	
    	return map;
    }
    
    
    public static Map<String, String> getProfiles( final GoogleApiConnection connection, String accountId, String propertyId ) throws IOException {
    	Map<String, String> map = new HashMap<>();
    	
    	Analytics analytics =
                new Analytics.Builder(connection.getHttpTransport(), connection.getJsonFactory(),
                        connection.getCredential()).setApplicationName("KNIME-Profiles-Scan").build();
        
    	Profiles profiles = analytics.management().profiles().list(accountId, propertyId).execute();
    	if ( profiles != null && profiles.getTotalResults() > 0 )
	    	for ( Profile profile : profiles.getItems() ) {
	    		map.put(profile.getName(), profile.getId() );
	    	}
    	
    	return map;
    }
    
    public static Map<String, String> getProfile( final GoogleApiConnection connection, String profileId ) throws IOException {
    	Map<String, String> map = new HashMap<>();
    	
    	Analytics analytics =
                new Analytics.Builder(connection.getHttpTransport(), connection.getJsonFactory(),
                        connection.getCredential()).setApplicationName("KNIME-Profiles-Scan").build();
        
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	// Try to resolve Profile information via Data query:
    	Get query = analytics.data().ga().get("ga:"+profileId, sdf.format(new Date()), sdf.format(new Date()), "ga:pageviews");
    	GaData response = query.execute();
    	if ( response != null && response.getTotalResults() > 0 ) {
    		ProfileInfo profileInfo = response.getProfileInfo();
        	String name = profileInfo.getProfileName();
        	map.put(name, profileId);	
    	}
    	
    	return map;
    }

    /**
     * @param connection The used GoogleApiConnection
     * @param applicationName Name of this application as it is shown to the Google API
     * @param profileId ID of the profile that will be used
     */
    public GoogleAnalyticsConnection(final GoogleApiConnection connection, final String applicationName,
            final String profileId, String timeOut) {
        
    	m_connection = connection;
        m_profileId = profileId;
        m_applicationName = applicationName;
        m_timeOut = timeOut;
        
        
        final HttpRequestInitializer requestInit = new HttpRequestInitializer() {
	         public void initialize(HttpRequest httpRequest) {
	            try {
					m_connection.getCredential().initialize(httpRequest);
				} catch (IOException e) { 
					e.printStackTrace();
				}
	            
	            if ( m_timeOut == null || StringUtils.isBlank(m_timeOut) ) {
	            	m_timeOut = "5";
	            }
	            
	            Integer iTimeOut = new Integer(5);
	            try { 
	            	iTimeOut = Integer.parseInt(m_timeOut);
	            } catch ( Exception ex ) {
	            	ex.printStackTrace();
	            }
	            
	            httpRequest.setConnectTimeout(iTimeOut * 60000);  	// 15 minutes connect timeout
	            httpRequest.setReadTimeout(iTimeOut * 60000);  	// 15 minutes read timeout
	         }
	    };
        
        m_analytics =
                new Analytics.Builder(m_connection.getHttpTransport(), m_connection.getJsonFactory(),
                        requestInit).setApplicationName(m_applicationName).build();
    }
    
	public List<List<String>> query ( String startDate, String endDate, String[] metrics, String[] dimensions, String filters ) throws Exception, IOException {
		return query(startDate, endDate, metrics, dimensions, filters, null, null);
	}
	
	
	public List<List<String>> query ( String startDate, String endDate, String[] metrics, String[] dimensions, String filters, String segment, ExecutionContext context ) throws Exception,IOException {
		
		List<List<String>> queryResults = new ArrayList<List<String>>();
		
		// Pagination Variables
		int maxResults = 5000;
		int currentIndex = 1;
		
		if (getProfileId() == null ) {
			throw new Exception("No Profile selected");
		}
		
		String profileId = "ga:"+getProfileId();
		
		
		try {
		
			// GUARD STATEMENT:
			
			String metricList = StringUtils.join(metrics, ",");
			String dimensionList = StringUtils.join(dimensions, ",");
			String filterList = filters;
			String sDate = startDate;
			String eDate = endDate;
			
			LOGGER.info ("Query: " + profileId + " " + sDate + " to " + eDate );
			
			// Build Query
			Get apiQuery = this.m_analytics.data().ga().get(profileId, sDate, eDate, metricList );
			apiQuery.setDimensions(dimensionList);
			if ( filterList != null  && !"".equals(filterList) ) apiQuery.setFilters(filterList); // Filter Reference: https://developers.google.com/analytics/devguides/reporting/core/v3/reference#filters
			if ( segment != null  && !"".equals(segment) ) apiQuery.setSegment(segment);
			apiQuery.setMaxResults(maxResults);
			apiQuery.setStartIndex(currentIndex);
			
			// Execute Query
			GaData gaData = qpsQueryProtection(apiQuery);
			setDataModel(gaData);
			
			// GUARD: No results
			if (gaData == null || gaData.getTotalResults() <= 0) {
				LOGGER.info("Results: 0");
				return queryResults;
			}
			
			if ( gaData.getContainsSampledData() ) {
				LOGGER.warn("Contains Sampled Data");
			}
			
			LOGGER.info ("Results: " + gaData.getTotalResults() );
			
			// Iterate through resultset (include pagination)
			while ( gaData.getRows() != null && gaData.getRows().size() > 0 ) {
				
				if ( context !=null ) {
					context.checkCanceled();
				}
				
				if ( context != null ) {
					context.setMessage("Getting Results " + currentIndex + " to " + (currentIndex + maxResults) + " of " + gaData.getTotalResults());
					context.setProgress((double) currentIndex / (double) gaData.getTotalResults() );
				}
				LOGGER.debug("Getting Results " + currentIndex + " to (max)" + (currentIndex + maxResults) );
				
				for (List<String> rowValues : gaData.getRows()) {
					queryResults.add(rowValues);
				}
				
				// Next page:
				currentIndex = currentIndex + maxResults; 
				apiQuery.setStartIndex(currentIndex);
				gaData = qpsQueryProtection(apiQuery);
			}
			
			apiQuery = null;
			gaData = null;
			
		} catch ( IOException exc ) {
			LOGGER.error("Unable to execute query for " + profileId + ": " + exc.toString());
		}
		
		return queryResults;
	}
       
	private GaData qpsQueryProtection ( Get apiQuery ) throws IOException {
		GaData gaData = null;
		int attempts = 1;
		IOException exception = null;
		
		do {
			try {
				
				gaData = apiQuery.execute();
				exception = null;
			
			} catch ( IOException ioexc ) {
				exception = ioexc;
				
				// Guard statement:
				if ( !(ioexc instanceof SocketTimeoutException  || ioexc instanceof GoogleJsonResponseException ) ) {
					return gaData;
				}
				
				if ( ioexc instanceof GoogleJsonResponseException) {
					GoogleJsonResponseException e = (GoogleJsonResponseException)ioexc;
					String reason = e.getDetails().getErrors().get(0).getReason();
					LOGGER.error(reason);
					LOGGER.error(e.getDetails().getMessage());
					
					// Likely security exception so no reason to continue trying:
					if ( !("rateLimitExceeded".equals(reason) || "dailyLimitExceeded".equals(reason) || "userRateLimitExceeded".equals(reason) || "backendError".equals(reason) ))
					{
						throw e;
					}
					
				} else {
					LOGGER.error(ioexc.getMessage());
				}
				
				try { Thread.sleep( attempts * 1000); } catch ( Exception sleepException ){};
				
			}
			
		} while ( gaData == null && attempts++ <= MAX_ATTEMPTS && exception != null );
		
		// Was unable to recover :(
		if (exception != null ) throw exception;
		
		return gaData;
	}

    /**
     * @param model The model containing the connection information
     * @throws InvalidSettingsException If the model was invalid
     */
    public GoogleAnalyticsConnection(final ModelContentRO model) throws InvalidSettingsException {
        try {
            m_connection = new GoogleApiConnection(model);
            m_profileId = model.getString(CFG_PROFILE_ID);
            m_applicationName = model.getString(CFG_APPLICATION_NAME);
            m_analytics =
                    new Analytics.Builder(m_connection.getHttpTransport(), m_connection.getJsonFactory(),
                            m_connection.getCredential()).setApplicationName(m_applicationName).build();
        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidSettingsException(e);
        }
    }

    /**
     * @return the analytics The Google Analytics object
     */
    public Analytics getAnalytics() {
        return m_analytics;
    }

    /**
     * @return the profileId
     */
    public String getProfileId() {
        return m_profileId;
    }

    public String getTimeOut() {
    	return m_timeOut;
    }
    
    
    
    public GaData getDataModel() {
		return dataModel;
	}

	public void setDataModel(GaData dataModel) {
		this.dataModel = dataModel;
	}

	/**
     * @param model The model to save the current configuration in
     */
    public void save(final ModelContentWO model) {
        m_connection.save(model);
        model.addString(CFG_PROFILE_ID, m_profileId);
        model.addString(CFG_APPLICATION_NAME, m_applicationName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GoogleAnalyticsConnection)) {
            return false;
        }
        GoogleAnalyticsConnection con = (GoogleAnalyticsConnection)obj;
        EqualsBuilder eb = new EqualsBuilder();
        eb.append(m_connection, con.m_connection);
        eb.append(m_profileId, con.m_profileId);
        eb.append(m_applicationName, con.m_applicationName);
        return eb.isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(m_connection);
        hcb.append(m_profileId);
        hcb.append(m_applicationName);
        return hcb.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(m_connection.toString() + "\n");
        sb.append("Profile ID:\n" + m_profileId + "\n\n");
        sb.append("Application name:\n" + m_applicationName + "\n");
        return sb.toString();
    }
    
 
}
