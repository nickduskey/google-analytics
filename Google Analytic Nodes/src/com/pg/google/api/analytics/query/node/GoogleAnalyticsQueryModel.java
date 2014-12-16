package com.pg.google.api.analytics.query.node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.google.api.services.analytics.model.GaData;
import com.pg.google.api.analytics.connector.data.GoogleAnalyticsConnection;
import com.pg.google.api.analytics.connector.data.GoogleAnalyticsConnectionPortObject;


public class GoogleAnalyticsQueryModel extends NodeModel {

    private GoogleAnalyticsQueryConfiguration configuration = new GoogleAnalyticsQueryConfiguration();

    protected static final NodeLogger LOGGER = NodeLogger.getLogger(GoogleAnalyticsQueryModel.class);
    
    /**
     * Constructor of the node model.
     */
    protected GoogleAnalyticsQueryModel() {
        super(new PortType[]{GoogleAnalyticsConnectionPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
        
    	GoogleAnalyticsConnection connection =
                ((GoogleAnalyticsConnectionPortObject)inObjects[0]).getGoogleAnalyticsConnection();
        
    	List<List<String>> rows = connection.query(
        		configuration.getStartDate(), 
        		configuration.getEndDate(), 
        		configuration.getMetricsWithPrefix(),
        		configuration.getDimensionsWithPrefix(),
        		configuration.getFiltersWithPrefix(),
        		configuration.getSegmentWithPrefix(),
        		exec);
        
    	GaData dataModel = connection.getDataModel();
    	
    	if ( dataModel == null ) {
    		throw new Exception("Unable to retrieve result data model");
    	}
    	
        DataTableSpec outSpec = createSpec(dataModel);
        BufferedDataContainer outContainer = exec.createDataContainer(outSpec);
        
        if (rows != null) {
            for (int i = 0; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                List<DataCell> cells = new ArrayList<DataCell>(outSpec.getNumColumns());
                
                // Add additional columns to DataSpec
                cells.add(new StringCell(dataModel.getProfileInfo().getAccountId()));
                cells.add(new StringCell(dataModel.getProfileInfo().getWebPropertyId()));
                cells.add(new StringCell(dataModel.getProfileInfo().getProfileId()));
                cells.add(new StringCell(dataModel.getProfileInfo().getProfileName()));
                cells.add(new StringCell(String.valueOf(dataModel.getContainsSampledData())));
                
                for (int j = 0; j < row.size(); j++) {
                    // Use already determined type
                    DataType type = outSpec.getColumnSpec((outSpec.getNumColumns() - row.size()) + j).getType();
                    if (type.equals(IntCell.TYPE)) {
                        cells.add(new IntCell(Integer.parseInt(row.get(j))));
                    } else if (type.equals(DoubleCell.TYPE)) {
                        cells.add(new DoubleCell(Double.parseDouble(row.get(j))));
                    } else {
                        String value = row.get(j);
                        if (value.equals("(not set)")) {
                            // '(not set)' is Googles version of missing value
                            cells.add(new MissingCell("(not set)"));
                        } else {
                            cells.add(new StringCell(row.get(j)));
                        }
                    }
                }
                outContainer.addRowToTable(new DefaultRow("Row" + i, cells));
            }
        }
        outContainer.close();
        return new PortObject[]{outContainer.getTable()};
    }

    /**
     * @param data Google Analytics data object
     * @return The KNIME table spec for the given data object
     */
    private DataTableSpec createSpec(final GaData data) {
        List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>(data.getColumnHeaders().size()+5);
        
        // Add additional columns to DataSpec
        colSpecs.add(new DataColumnSpecCreator("Account Id", StringCell.TYPE).createSpec());
        colSpecs.add(new DataColumnSpecCreator("Property Id", StringCell.TYPE).createSpec());
        colSpecs.add(new DataColumnSpecCreator("Profile Id", StringCell.TYPE).createSpec());
        colSpecs.add(new DataColumnSpecCreator("Profile Name", StringCell.TYPE).createSpec());
        colSpecs.add(new DataColumnSpecCreator("Is Sampled", StringCell.TYPE).createSpec());
         
        for (GaData.ColumnHeaders colHeaders : data.getColumnHeaders()) {
            String type = colHeaders.getDataType();
            String name = colHeaders.getName().replaceFirst("ga:", "");
            if (type.equals("INTEGER")) {
                colSpecs.add(new DataColumnSpecCreator(name, IntCell.TYPE).createSpec());
            } else if (type.equals("DOUBLE") || type.equals("PERCENT") || type.equals("TIME") || type.equals("FLOAT")
                    || type.equals("CURRENCY")) {
                // All of these are simple floating point numbers
                colSpecs.add(new DataColumnSpecCreator(name, DoubleCell.TYPE).createSpec());
            } else {
                colSpecs.add(new DataColumnSpecCreator(name, StringCell.TYPE).createSpec());
            }
        }
        return new DataTableSpec(colSpecs.toArray(new DataColumnSpec[colSpecs.size()]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return new PortObjectSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // not used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // not used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) {
    	configuration.save(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
        GoogleAnalyticsQueryConfiguration config = new GoogleAnalyticsQueryConfiguration();
        config.loadInModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
        GoogleAnalyticsQueryConfiguration config = new GoogleAnalyticsQueryConfiguration();
        config.loadInModel(settings);
        configuration = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // not used
    }

}
