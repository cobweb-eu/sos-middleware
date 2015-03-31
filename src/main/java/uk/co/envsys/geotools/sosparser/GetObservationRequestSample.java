package uk.co.envsys.geotools.sosparser;

import static org.n52.oxf.ows.capabilities.Parameter.COMMON_NAME_TIME;
import static org.n52.oxf.sos.adapter.ISOSRequestBuilder.GET_OBSERVATION_EVENT_TIME_PARAMETER;
import static org.n52.oxf.sos.adapter.ISOSRequestBuilder.GET_OBSERVATION_OBSERVED_PROPERTY_PARAMETER;
import static org.n52.oxf.sos.adapter.ISOSRequestBuilder.GET_OBSERVATION_OFFERING_PARAMETER;
import static org.n52.oxf.sos.adapter.ISOSRequestBuilder.GET_OBSERVATION_PROCEDURE_PARAMETER;
import static org.n52.oxf.sos.adapter.ISOSRequestBuilder.GET_OBSERVATION_RESPONSE_FORMAT_PARAMETER;
import static org.n52.oxf.sos.adapter.ISOSRequestBuilder.SERVICE;
import static org.n52.oxf.sos.adapter.ISOSRequestBuilder.VERSION;
import static org.n52.oxf.sos.adapter.SOSAdapter.GET_OBSERVATION;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationCollectionType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.adapter.ParameterShell;
import org.n52.oxf.ows.capabilities.ITime;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.oxf.ows.capabilities.Parameter;
import org.n52.oxf.valueDomains.time.TemporalValueDomain;
import org.n52.oxf.valueDomains.time.TimePeriod;


public class GetObservationRequestSample extends SOSRequestSampleAdapter {

	public GetObservationRequestSample(String version) {
		super(version);
	}
	
	public XmlObject getObservation() {
		return performOperationParseResult(createGetObservationOperation());
	}
	
	public ObservationCollectionType getParsedObservation() {
		OperationResult result = performOperation(createGetObservationOperation());
		try {
			ObservationCollectionDocument ocd = ObservationCollectionDocument.Factory.parse(result.getIncomingResultAsStream());
			return ocd.getObservationCollection();
		} catch (XmlException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Operation createGetObservationOperation() {
        return new Operation(GET_OBSERVATION, getServiceGETUrl(), getServicePOSTUrl());
    }

	@Override
	protected ParameterContainer createParameterContainer() throws OXFException {
		final ParameterContainer parameters = new ParameterContainer();
        parameters.addParameterShell(SERVICE, "SOS");
        parameters.addParameterShell(VERSION, "1.0.0");
        parameters.addParameterShell(GET_OBSERVATION_OFFERING_PARAMETER, "http://smartcoasts.ucd.ie/offering/0");
        parameters.addParameterShell(GET_OBSERVATION_OBSERVED_PROPERTY_PARAMETER, "http://smartcoasts.ucd.ie/observableProperty/0");
        parameters.addParameterShell(GET_OBSERVATION_PROCEDURE_PARAMETER, "http://smartcoasts.ucd.ie/procedure/0");
        parameters.addParameterShell(GET_OBSERVATION_RESPONSE_FORMAT_PARAMETER, "text/xml;subtype=\"om/1.0.0\"");
        parameters.addParameterShell(createTimeConstraintParameter());
        return parameters;
	}
	
    private ParameterShell createTimeConstraintParameter() throws OXFException {
        final ITime last24Hours = createUsefulInterval();
        final Parameter timeConstraint = createTimeParameterFor(last24Hours);
        return new ParameterShell(timeConstraint, last24Hours);
    }
    
    private Parameter createTimeParameterFor(final ITime timeValue) {
        final TemporalValueDomain domain = new TemporalValueDomain(timeValue);
        return new Parameter(GET_OBSERVATION_EVENT_TIME_PARAMETER, true, domain, COMMON_NAME_TIME);
    }

    private ITime createLast24HoursInterval() {
        final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        final long nowInMillis = System.currentTimeMillis();
        final long aDayInMillis = 1 * 24 * 60 * 60 * 1000;
        final long lastWeekInMillis = nowInMillis - aDayInMillis;
        final String now = iso8601.format(new Date(nowInMillis));
        final String lastWeek = iso8601.format(new Date(lastWeekInMillis));
        return new TimePeriod(lastWeek, now);
    }
    
    private ITime createUsefulInterval() {
    	final Calendar myCal = Calendar.getInstance();
    	myCal.set(2015, 1, 13, 23, 15);
    	final Date startTime = myCal.getTime();
    	myCal.set(2015, 1, 13, 23, 16);
    	final Date endTime = myCal.getTime();
   
    	final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    	return new TimePeriod(iso8601.format(startTime), iso8601.format(endTime));
    }
}
