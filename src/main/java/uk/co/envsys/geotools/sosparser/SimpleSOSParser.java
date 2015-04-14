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
package uk.co.envsys.geotools.sosparser;

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
 * @author Sebastian Clarke - Environment Systems
 * Copyright (c) 2015 - Environment Systems
 *
 */
public abstract class SimpleSOSParser extends AbstractParser {
	
	protected SimpleFeatureType type;
	protected SimpleFeatureBuilder featureBuilder;
	protected static Logger LOGGER = LoggerFactory.getLogger(SimpleSOSParser.class);
	
	protected SimpleSOSParser(){ super(); } // protected constructor, will use factory
	
	// must be implemented by subclasses
	protected abstract GTVectorDataBinding parseXML(XmlObject document);

	// implement the IParser interface
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
	
	public static class Factory {
		public static SimpleSOSParser getParser(String version) {
			if(version == "1.0.0") {
				return new SimpleSOSParser_100();
			} else if(version == "2.0.0") {
				return new SimpleSOSParser_200();
			} else {
				throw new IllegalArgumentException("Only versions 1.0.0 and 2.0.0 are implemented!");
			}
		}
		
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