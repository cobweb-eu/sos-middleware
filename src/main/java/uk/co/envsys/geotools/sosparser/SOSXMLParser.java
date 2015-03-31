package uk.co.envsys.geotools.sosparser;

import java.io.IOException;
import java.io.InputStream;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationCollectionType;
import net.opengis.om.x10.ObservationType;
import net.opengis.om.x20.ObservationContextDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.OXFException;
import org.n52.wps.FormatDocument.Format;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.parser.AbstractParser;

public class SOSXMLParser extends AbstractParser {
	
	// members
	private XmlObject result;
	private String version;
	
	// Constructor
	public SOSXMLParser(String version) throws OXFException {
		super();
		
		if(version != "1.0.0" && version != "2.0.0") {
			throw new OXFException("SOS Version must be 1.0.0 or 2.0.0");
		}
		
		this.version = version;
	}

	
	public void printObservationValues() {
		/*
		if(observation.sizeOfMemberArray() == 0) {
			System.out.println("No observations");
			return;
		}
		
		if(observation.sizeOfMemberArray() > 1) {
			System.out.println("More than one observation, using first");
		}
		
		ObservationType obs = observation.getMemberArray()[0].getObservation();
		XmlObject result = obs.getResult();
		
		*/
	}
	

	public IData parse(InputStream input, String mimeType, String schema) {
		if(this.version == "1.0.0") {
			return parse_100(input, mimeType, schema); 
		} else { 
			return parse_200(input, mimeType, schema); 
		}
	}	
	
	private IData parse_100(InputStream input, String mimeType, String schema) {
		ObservationCollectionDocument ocd;
		
		try {
			ocd = ObservationCollectionDocument.Factory.parse(input);
		} catch (XmlException e) {
			throw new IllegalArgumentException("Error parseing XML", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error transferring XML", e);
		}
		return null;
	}
	
	private IData parse_200(InputStream input, String mimeType, String schema) {
		throw new IllegalArgumentException("Parseing sos 2.0.0 is not yet implemented");
	}
	
}
	