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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.feature.simple.SimpleFeatureBuilder;
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
			} else {
				throw new IllegalArgumentException("Only version 1.0.0 implemented!");
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