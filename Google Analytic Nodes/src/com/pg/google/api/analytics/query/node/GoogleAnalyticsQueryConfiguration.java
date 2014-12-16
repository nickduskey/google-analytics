package com.pg.google.api.analytics.query.node;



import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class GoogleAnalyticsQueryConfiguration {

    private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String CFG_DIMENSIONS = "dimensions";

    private static final String CFG_METRICS = "metrics";

    private static final String CFG_SEGMENT = "segment";

    private static final String CFG_FILTERS = "filters";

    private static final String CFG_SORT = "sort";

    private static final String CFG_START_DATE = "start-date";

    private static final String CFG_END_DATE = "end-date";

    private String[] m_dimensions = new String[0];

    private String[] m_metrics = new String[0];

    private String m_segment = "";

    private String m_filters = "";

    private String m_sort = "";

    private String m_startDate = "";

    private String m_endDate = "";

    

    /**
     * Create GoogleAnalyticsQueryConfiguration.
     */
    public GoogleAnalyticsQueryConfiguration() {
        Calendar calendar = new GregorianCalendar();
        m_endDate = FORMAT.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        m_startDate = FORMAT.format(calendar.getTime());
    }

    /**
     * @return the dimensions
     */
    public String[] getDimensions() {
        return m_dimensions;
    }

    /**
     * @param dimensions the dimensions to set
     * @throws InvalidSettingsException If the dimensions exceed the maximum of 7
     */
    public void setDimensions(final String[] dimensions) throws InvalidSettingsException {
        if (dimensions.length > 7) {
            throw new InvalidSettingsException("A single request is limited to a maximum of 7 dimensions");
        }
        m_dimensions = dimensions;
    }

    /**
     * @return the metrics
     */
    public String[] getMetrics() {
        return m_metrics;
    }
    
    public String[] getMetricsWithPrefix() {
    	return addGAPrefix(getMetrics());
    }
    
    public String[] getDimensionsWithPrefix() {
    	return addGAPrefix(getDimensions());
    }
    
    // Pre-pends GA to all metric names
    private String[] addGAPrefix(String[] items) {
    	if ( items == null || items.length <= 0) return new String[] {};
    	
    	String[] gaItems = new String[items.length];
    	for ( int i =0; i<items.length;i++) {
    		gaItems[i] = "ga:" + items[i];
    	}
    	return gaItems;
    }

    /**
     * @param metrics the metrics to set
     * @throws InvalidSettingsException If the metrics exceed the maximum of 10 or no metric is given
     */
    public void setMetrics(final String[] metrics) throws InvalidSettingsException {
        if (metrics.length < 1) {
            throw new InvalidSettingsException("At least one metric is required");
        }
        if (metrics.length > 10) {
            throw new InvalidSettingsException("A single request is limited to a maximum of 10 metrics");
        }
        m_metrics = metrics;
    }

    /**
     * @return the segment
     */
    public String getSegment() {
        return m_segment;
    }

    /**
     * @param segment the segment to set
     */
    public void setSegment(final String segment) {
        m_segment = segment;
    }

    /**
     * @return the filters
     */
    public String getFilters() {
        return m_filters;
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(final String filters) {
        m_filters = filters;
    }

    /**
     * @return the sort
     */
    public String getSort() {
        return m_sort;
    }

    /**
     * @param sort the sort to set
     */
    public void setSort(final String sort) {
        m_sort = sort;
    }

    /**
     * @return the startDate
     */
    public String getStartDate() {
        return m_startDate;
    }

    /**
     * @param startDate the startDate to set
     * @throws InvalidSettingsException If the start date is missing or invalid
     */
    public void setStartDate(final String startDate) throws InvalidSettingsException {
        if (!isDateValid(startDate)) {
            throw new InvalidSettingsException("A valid start date in the format YYYY-MM-DD is required");
        }
        m_startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public String getEndDate() {
        return m_endDate;
    }

    /**
     * @param endDate the endDate to set
     * @throws InvalidSettingsException If the end date is missing or invalid
     */
    public void setEndDate(final String endDate) throws InvalidSettingsException {
        if (!isDateValid(endDate)) {
            throw new InvalidSettingsException("A valid end date in the format YYYY-MM-DD is required");
        }
        m_endDate = endDate;
    }



    /**
     * @param settings The settings object to save in
     */
    public void save(final NodeSettingsWO settings) {
        settings.addStringArray(CFG_DIMENSIONS, m_dimensions);
        settings.addStringArray(CFG_METRICS, m_metrics);
        settings.addString(CFG_SEGMENT, m_segment);
        settings.addString(CFG_FILTERS, m_filters);
        settings.addString(CFG_SORT, m_sort);
        settings.addString(CFG_START_DATE, m_startDate);
        settings.addString(CFG_END_DATE, m_endDate);
        
    }

    /**
     * @param settings The settings object to load from
     * @throws InvalidSettingsException If the settings are invalid
     */
    public void loadInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_dimensions = settings.getStringArray(CFG_DIMENSIONS);
        m_metrics = settings.getStringArray(CFG_METRICS);
        m_segment = settings.getString(CFG_SEGMENT);
        m_filters = settings.getString(CFG_FILTERS);
        m_sort = settings.getString(CFG_SORT);
        m_startDate = settings.getString(CFG_START_DATE);
        m_endDate = settings.getString(CFG_END_DATE);
        
    }

    /**
     * @param settings The settings object to load from
     */
    public void loadInDialog(final NodeSettingsRO settings) {
        Calendar calendar = new GregorianCalendar();
        m_dimensions = settings.getStringArray(CFG_DIMENSIONS, new String[0]);
        m_metrics = settings.getStringArray(CFG_METRICS, new String[0]);
        m_segment = settings.getString(CFG_SEGMENT, "");
        m_filters = settings.getString(CFG_FILTERS, "");
        m_sort = settings.getString(CFG_SORT, "");
        m_endDate = settings.getString(CFG_END_DATE, "");
        if (!isDateValid(m_endDate)) {
            m_endDate = FORMAT.format(calendar.getTime());
        }
        m_startDate = settings.getString(CFG_START_DATE, "");
        if (!isDateValid(m_startDate)) {
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            m_startDate = FORMAT.format(calendar.getTime());
        }
        
    }

    /*
    public Get createGetRequest(final GoogleAnalyticsConnection connection) throws IOException {
        String[] metrics = new String[m_metrics.length];
        for (int i = 0; i < metrics.length; i++) {
            metrics[i] = prependPrefix(m_metrics[i]);
        }
        // Create request with metrics, start and end date
        Get get =
                connection.getAnalytics().data().ga()
                        .get("ga:" + connection.getProfileId(), m_startDate, m_endDate, StringUtils.join(metrics, ","));
        // Add additional parameters if available
        if (m_dimensions.length > 0) {
            String[] dimensions = new String[m_dimensions.length];
            for (int i = 0; i < dimensions.length; i++) {
                dimensions[i] = prependPrefix(m_dimensions[i]);
            }
            get.setDimensions(StringUtils.join(dimensions, ","));
        }
        if (!m_segment.isEmpty()) {
            get.setSegment(getSegmentWithPrefix());
        }
        if (!m_filters.isEmpty()) {
            get.setFilters(prependPrefixToFilters(m_filters));
        }
        if (!m_sort.isEmpty()) {
            String[] noPrefix = m_sort.split(",");
            String[] withPrefix = new String[noPrefix.length];
            for (int i = 0; i < withPrefix.length; i++) {
                withPrefix[i] = prependPrefix(noPrefix[i]);
            }
            get.setSort(StringUtils.join(withPrefix, ","));
        }
        get.setStartIndex(m_startIndex);
        get.setMaxResults(m_maxResults);
        return get;
    } 
	*/
    
    /**
     * @param dateToValidate The date to check
     * @return true if the date is in a valid format, false otherwise
     */
    private boolean isDateValid(final String dateToValidate) {
        if (dateToValidate == null) {
            // Date is missing
            return false;
        }
        // Strict parsing
        FORMAT.setLenient(false);
        try {
            FORMAT.parse(dateToValidate);
        } catch (ParseException e) {
            // Date could not be parsed
            return false;
        }
        // Everything ok
        return true;
    }

    /**
     * Returns the segment with prefix.
     * 
     * The prefix is 'gaid::' for default segments or 'dynamic::' for custom segments.
     * 
     * @return The segment with prefix
     */
    public String getSegmentWithPrefix() {
    	if ( StringUtils.isEmpty(m_segment)) return "";
    	
        if (m_segment.matches("-[0-9]+")) {
            return "gaid::" + m_segment;
        } else {
            return "dynamic::" + prependPrefixToFilters(m_segment);
        }
    }

    /**
     * Will prepend the prefix 'ga:' to the string while keeping the '-' in front (for sort strings)
     * 
     * @param string The string to prepend to
     * @return The string with the prepended prefix 'ga:'
     */
    /*
    private String prependPrefix(final String string) {
        if (string.startsWith("-")) {
            return string.replaceFirst("-", "-ga:");
        } else {
            return "ga:" + string;
        }
    }
    */

    /**
     * Will prepend the prefix 'ga:' to all column names in the given filters string while respecting escaped characters.
     * 
     * @param string The string to prepend to
     * @return The string with the prepended prefix 'ga:' to the columns
     */
    public String getFiltersWithPrefix() {
    	if ( StringUtils.isEmpty(getFilters())) return "";
    	
    	return prependPrefixToFilters(getFilters());
    }
    
    private String prependPrefixToFilters(final String string) {
        StringBuilder sb = new StringBuilder();
        sb.append("ga:");
        for (int i = 0; i < string.length(); i++) {
            boolean hasNext = i + 1 < string.length();
            char c = string.charAt(i);
            if (c == '\\') {
                sb.append(c);
                if (hasNext) {
                    // next character is escaped
                    sb.append(string.charAt(++i));
                }
            } else if (c == ',' || c == ';') {
                // new column starts after c
                sb.append(c + "ga:");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
