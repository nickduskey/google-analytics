package com.pg.google.api.anaytics.profilelist.node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.pg.google.api.analytics.connector.data.GoogleAnalyticsConnection;
import com.pg.google.api.connector.data.GoogleApiConnectionPortObject;
import com.pg.google.api.connector.data.GoogleApiConnectionPortObjectSpec;
import com.google.api.client.util.DateTime;
import com.google.api.services.analytics.model.Profile;

/**
 * This is the model implementation of ProfileList.
 * 
 *
 * @author Procter & Gamble, eBusiness
 */
public class ProfileListNodeModel extends NodeModel {
    
	/**
     * Constructor for the node model.
     */
    protected ProfileListNodeModel() {
    
    	super(new PortType[]{GoogleApiConnectionPortObject.TYPE},
                new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
        
    	GoogleApiConnectionPortObjectSpec inSpec = (GoogleApiConnectionPortObjectSpec)inObjects[0].getSpec();
        List<Profile> profiles = GoogleAnalyticsConnection.getAllProfiles(inSpec.getGoogleApiConnection());
    	
        DataTableSpec outSpec = createSpec();
        BufferedDataContainer outContainer = exec.createDataContainer(outSpec);
        
        int count = 0;
        for ( Profile profile : profiles ) {
        	
        	List<DataCell> cells = new ArrayList<DataCell>(outSpec.getNumColumns());
 	    	
        	String accountId = profile.getAccountId();
        	String propertyId = profile.getWebPropertyId();
        	String profileId = profile.getId();
        	String name = profile.getName();
        	String siteUrl = profile.getWebsiteUrl();
        	String timezone = profile.getTimezone();
        	DateTime createdDate = profile.getCreated();
        	DateTime updatedDate = profile.getUpdated();
        	String currency = profile.getCurrency();
        	Boolean eCommerceTracking = profile.getECommerceTracking();
        	String excludedParams = profile.getExcludeQueryParameters();
        	
        	cells.add(new StringCell(def(accountId)));
        	cells.add(new StringCell(def(propertyId)));
        	cells.add(new StringCell(def(profileId)));
        	cells.add(new StringCell(def(name)));
        	cells.add(new StringCell(def(siteUrl)));
        	cells.add(new StringCell(def(timezone)));
        	cells.add(new DateAndTimeCell(createdDate.getValue(), true, true, false));
        	cells.add(new DateAndTimeCell(updatedDate.getValue(), true, true, false));
        	cells.add(new StringCell(def(currency)));
        	cells.add(eCommerceTracking ? BooleanCell.TRUE : BooleanCell.FALSE);
        	cells.add(new StringCell(def(excludedParams)));
        	
        	outContainer.addRowToTable(new DefaultRow("Row" + count++, cells));
        }
    	outContainer.close();
        
        return new BufferedDataTable[]{outContainer.getTable()};
    }
    
    private String def( String str  ) {
    	if ( str == null ) return "";
    	return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
    		throws InvalidSettingsException {
    	return new PortObjectSpec[] { createSpec() }; 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

    private DataTableSpec createSpec() {
    	
    	List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>();
    	colSpecs.add(new DataColumnSpecCreator("Account ID", StringCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("WebProperty ID", StringCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Profile ID", StringCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Name", StringCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Site Url", StringCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Timezone", StringCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Created", DateAndTimeCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Updated", DateAndTimeCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Currency", StringCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("eCommerce Tracking ID", BooleanCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Excluded Params", StringCell.TYPE).createSpec());
    	
    	return new DataTableSpec(colSpecs.toArray(new DataColumnSpec[colSpecs.size()]));
    	
    }
    
    
}

