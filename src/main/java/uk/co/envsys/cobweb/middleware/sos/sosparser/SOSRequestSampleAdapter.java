package uk.co.envsys.geotools.sosparser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.OWSException;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.oxf.sos.adapter.SOSAdapter;


/**
 * @author Sebastian Clarke - Environment System Ltd - www.envsys.co.uk
 * (Copyright 2015 - All rights reserved
 *
 *	This class is a sample SOS request adapter, binding to demo sensors. 
 *	This will connect using the OLD-STYLE adapter (may be 
 *	deprecated soon, but currently supported, rather than wrapper)
 *
 *	This is an abstract class, to be extended by specific request types
 *	eg: GetCapabilities, GetObservation etc.
 *
 *	This is a first go at performing SOS requests, probably out of scope...
 */

public abstract class SOSRequestSampleAdapter {
	
	private static final XmlOptions XML_OPTIONS = new XmlOptions().setSavePrettyPrint().setSaveInner().setSaveOuter();
	
    private static final String REPORT_SEPARATOR_LINE = "################################\n";
    
    private static final String SOS_BY_GET_URL = "http://smartcoasts.ucd.ie:8080/52n-sos-webapp/service";
    
    private static final String SOS_BY_POST_URL = "http://smartcoasts.ucd.ie:8080/52n-sos-webapp/service";
    
    protected SOSAdapter adapter;
    
    public SOSRequestSampleAdapter(String version) {
    	adapter = new SOSAdapter(version);
    }
    
    public String getServiceGETUrl() {
        return SOS_BY_GET_URL;
    }
    
    public String getServicePOSTUrl() {
        return SOS_BY_POST_URL;
    }
    
    protected XmlObject performOperationParseResult(Operation operation){
        OperationResult operationResult = performOperation(operation);
        return parseResponse(operationResult.getIncomingResultAsStream());
    }
    
    protected OperationResult performOperation(Operation operation) {
        try {
            ParameterContainer parameters = createParameterContainer();
            return adapter.doOperation(operation, parameters);
        } catch (ExceptionReport e) {
            System.out.println("Remote reported an error:\n" + formatExceptionReport(e));
            return null;
        } catch (OXFException e) {
            System.out.println("SOS operation failed: " + e.getMessage());
            return null;
        }
    }
    
    protected abstract ParameterContainer createParameterContainer() throws OXFException;

    protected XmlObject parseResponse(InputStream responseStream) {
        // alternatively, parse stream with other parsers (e.g. stax)
        XmlObject xml = parseResponseWithXmlBeans(responseStream); 
        System.out.println("Parseing XML: " + xml.xmlText(XML_OPTIONS));
        return xml;
    }

    protected XmlObject parseResponseWithXmlBeans(InputStream responseStream) {
        try {
            return XmlObject.Factory.parse(responseStream, XML_OPTIONS);
        } catch (XmlException e) {
            System.out.println("Could not parse XML: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.out.println("Could not read response stream: " + e.getMessage());
            return null;
        }
    }

    protected String formatExceptionReport(ExceptionReport report) {
        StringBuilder sb = new StringBuilder("\n");
        Iterator<OWSException> iterator = report.getExceptionsIterator();
        while (iterator.hasNext()) {
            sb.append(REPORT_SEPARATOR_LINE);
            sb.append(formatOwsException(iterator.next()));
        }
        sb.append(REPORT_SEPARATOR_LINE);
        return sb.toString();
    }

    protected String formatOwsException(OWSException exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("ExceptionCode: ").append(exception.getExceptionCode()).append("\n");
        sb.append("Message: ").append(exception.getMessage()).append("\n");
        sb.append("Locator: ").append(exception.getLocator()).append("\n");
        sb.append("Caused by: ").append(exception.getCause()).append("\n");
        String[] exceptionDetails = exception.getExceptionTexts();
        if (exceptionDetails != null && exceptionDetails.length > 0) {
            sb.append("\t").append(REPORT_SEPARATOR_LINE);
            for (String exceptionText : exceptionDetails) {
                sb.append("\t[EXC] ").append(exceptionText).append("\n");
            }
            sb.append("\t").append(REPORT_SEPARATOR_LINE);
        }
        sb.append("Sent Request: ").append(exception.getSentRequest());
        return sb.append("\n").toString();
    }
    
}
