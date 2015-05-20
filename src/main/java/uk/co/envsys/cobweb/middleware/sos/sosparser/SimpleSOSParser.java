/**
 * This is an abstract Base class for parsing SOS XML Responses
 * 
 * Implementations will be provided for SOS 1.0.0 and 2.0.0
 * A Factory is available to provide implementations based on SOS versions
 * 
 * The implementations will be very simple parsers, storing the 
 * returning the observations as a GeoCollection of XmlObjects 
 * typed by appropriate schemas with no drilling down or 
 * instantiating of objects round data values
 * 
 */
package uk.co.envsys.cobweb.middleware.sos.sosparser;

import java.io.IOException;
import java.io.InputStream;

import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.sos.x20.GetObservationResponseDocument;
import net.opengis.sos.x20.GetObservationResponseType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.n52.oxf.xmlbeans.tools.SoapUtil;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.AbstractParser;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to provide the facility to parse SOS responses 
 * to GeoTools FeatureCollection objects. It does not provide
 * the implementation directly, but is an abstract base class
 * defining the behaviour. 
 * 
 * It also provides an inner static factory class to return
 * concrete implementations of the base class according to the
 * SOS version etc.
 * 
 * @author Sebastian Clarke - sebastian.clarke@envsys.co.uk
 * Copyright (c) 2015 - Environment Systems
 *
 */
public abstract class SimpleSOSParser extends AbstractParser {
	
	// private members, inherited by implementing subclasses
	protected SimpleFeatureType type;
	protected SimpleFeatureBuilder featureBuilder;
	protected static Logger LOGGER = LoggerFactory.getLogger(SimpleSOSParser.class);
	protected boolean strictMode = true;
	
	protected static final String SOS_V1 = "1.0.0";
	protected static final String SOS_V2 = "2.0.0";
	protected static final String SOCIAL_SENSOR = "UCD_SOCIAL";
	
	
	protected SimpleSOSParser() { // protected constructor, will use factory  
		super(); 
		supportedIDataTypes.add(GTVectorDataBinding.class); 
	} 
	
	protected void SetStrictMode(boolean strict) {
		strictMode = strict;
	}
	
	// must be implemented by subclasses
	protected abstract GTVectorDataBinding parseXML(XmlObject document);

	// implement the IParser interface, for integration with WPS stack
	public IData parse(InputStream input, String mimeType, String schema) {
		XmlObject doc;
		try {
			doc = XmlObject.Factory.parse(input);
		} catch (XmlException e) {
			throw new IllegalArgumentException("Error parseing XML", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error transferring XML", e);
		}
		return parseXML(doc);
	}
	
	/**
	 * Static factory class to return concrete implementations of the SOS parser
	 * based on the desired version or input stream properties
	 * 
	 * @author Sebastian Clarke - sebastian.clarke@envsys.co.uk
	 * Copyright (c) 2015 - Environment Systems
	 *
	 */
	public static class Factory {
		/**
		 * Returns a concrete parser implementation based on the
		 * SOS version number supplied as a string
		 * 
		 * @param version - A String containing the version number
		 * "1.0.0" and "2.0.0" are currently allowed
		 * @return an instantiated SimpleSOSParser for the requested
		 * version of SOS
		 * 
		 */
		public static SimpleSOSParser getParser(String version) {
			if(version == SOS_V1) {
				return new SimpleSOSParser_100();
			} else if(version == SOS_V2) {
				return new SimpleSOSParser_200();
			} else if (version == SOCIAL_SENSOR) {
				return new SocialSOSParser();
			} else {
				throw new IllegalArgumentException("Only " + SOS_V1 + ", " + SOS_V2 + " and " + SOCIAL_SENSOR + " supported.");
			}
		}
		
		/**
		 * Returns a concrete parser implementation based on the
		 * InputStream provided. The input stream is analysed and
		 * the most specific parser available is returned.
		 * 
		 * @param is The InputStream to return a parser for
		 * @return An instantiated SimpleSOSParser for the input stream
		 */
		public static SimpleSOSParser getParser(InputStream is) {
			// try and parse input stream as XML
			XmlObject doc;
			try {
				doc = XmlObject.Factory.parse(is);
			} catch (XmlException e) {
				throw new IllegalArgumentException("Error parseing XML", e);
			} catch (IOException e) {
				throw new IllegalArgumentException("Error transferring XML", e);
			}
			
			// Check if it can be transformed into O&M 1 or O&M 2
			if(!doc.schemaType().isAssignableFrom(ObservationCollectionDocument.type)) {
				// O&M 1
				return new SimpleSOSParser_100();
			}
			
			if(SoapUtil.isSoapEnvelope(doc) &&
					GetObservationResponseType.type.equals(SoapUtil.getSchemaTypeOfXmlPayload(doc))) {
				return new SimpleSOSParser_200();
			} else if(doc.schemaType().isAssignableFrom(GetObservationResponseDocument.type)) {
				return new SimpleSOSParser_200();
			} else {
				throw new IllegalArgumentException("Suitable parser not found");
			}
			
			// TODO: Return the SocialSOSParser sometimes
		}
	}
	
	/**
	 * Utility function to test if an object is null, and if it is throw an XmlException
	 * 
	 * @param toTest The object to test
	 * @param elementName What to call the element in the XmlException
	 * @return Object The same object is returned
	 * @throws XmlException if toTest == null
	 */
	protected Object ifNullThrowParseException(Object toTest, String elementName) throws XmlException {
		if(toTest == null) {
			XmlException e = new XmlException("Could not parse required element: " + elementName);
			LOGGER.error(e.getMessage());
			throw e;
		}
		return toTest;
	}
	
	/**
	 * Utility function to test if an object is null, and if it is throw an XmlException
	 * 
	 * @param toTest The object to test
	 * @param elementName What to call the element in the XmlException
	 * @return String the elementName as it was passed
	 * @throws XmlException if toTest == null
	 */
	protected String testNullReturnName(Object toTest, String elementName) throws XmlException {
		if(toTest == null) {
			XmlException e = new XmlException("Could not parse required element: " + elementName);
			LOGGER.error(e.getMessage());
			throw e;
		}
		return elementName;
	}
}