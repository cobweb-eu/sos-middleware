package uk.co.envsys.cobweb.middleware.sos.sosparser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.opengis.gml.x32.AbstractTimeObjectType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantPropertyType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.OMProcessPropertyType;
import net.opengis.sos.x20.GetObservationResponseType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Class to extend the SOS 200 parser to specifically parse
 * responses from UCD's sixth social sensor giving information
 * on Tweeted messages about floods with some sentiment analysis
 * 
 * @author Sebastian Clarke - sebastian.clarke@envsys.co.uk
 * Copyright (c) 2015 - Environment Systems 
 * 
 */
public class SocialSOSParser extends SimpleSOSParser_200 {
	

	protected GTVectorDataBinding parseXML(XmlObject document) {
		XmlObject observations = getPayload(document);
		try {
			return parseObservations(observations);
		} catch(XmlException e) {
			IllegalArgumentException ex = new IllegalArgumentException("Problem parseing xml", e);
			LOGGER.error(ex.getMessage());
			throw ex;
		}
	}
	
	private GTVectorDataBinding parseObservations(XmlObject observations) throws XmlException {
		GetObservationResponseType obs = (GetObservationResponseType)
				observations.changeType(GetObservationResponseType.type);	
		int numMembers = obs.sizeOfObservationDataArray();
		
		LOGGER.debug("Parseing " + numMembers + "observations");
		// make a list to store the features
		List<SimpleFeature> simpleFeatureList = new ArrayList<SimpleFeature>();
		// create the feature type (no need to use observation, we know what to expect)
		type = createType();
		featureBuilder = new SimpleFeatureBuilder(type);
		for(int i = 0; i < numMembers; i++) {
			OMObservationType observation = obs.getObservationDataArray(i).getOMObservation();
			SimpleFeature feature = convertToFeature(observation);
			simpleFeatureList.add(feature);
		}
		
		SimpleFeatureCollection collection = new ListFeatureCollection(type, simpleFeatureList);
		return new GTVectorDataBinding(collection);
	} 
	
	/**
	 * Creates the FeatureType to match the expected output
	 * from the UCD SIXTH Social Sensor. This function is called by the superclass
	 * 
	 * @param observation The SOS Observation in SOS 2.0.0
	 * @return SimpleFeatureType 
	 */
	private SimpleFeatureType createType() {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("UCD Social Sensor Observation");
		builder.add("phenomenonTime", Date.class);
		builder.add("observedProperty", String.class);
		builder.add("resultTime", String.class);
		builder.add("procedure", String.class);
		builder.add("foiHref", String.class);
		builder.add("foiTitle", String.class);
		builder.add("result", String.class);
		
		return builder.buildFeatureType();
	}


	@SuppressWarnings("restriction")
	private SimpleFeature convertToFeature(OMObservationType observation) 
														throws XmlException {
		try {
			try {
				AbstractTimeObjectType timeObject = 
						observation.getPhenomenonTime().getAbstractTimeObject();
				if(TimeInstantType.type.isAssignableFrom(timeObject.schemaType())) {
					TimeInstantType timeInstant = (TimeInstantType) timeObject.changeType(TimeInstantType.type);
					Date phenomenonTime = javax.xml.bind.DatatypeConverter.parseDateTime(timeInstant.getTimePosition().getStringValue()).getTime();
					featureBuilder.add(phenomenonTime);
				} else {
					throw new XmlException("Could not parse phenomenonTime as ISO 8601 Time string");	
				}
			} catch(NullPointerException e) {
				throw new XmlException("Could not parse phenomenonTime", e);
			}
			
			try {
				ReferenceType observedProperty = observation.getObservedProperty();
				if(observedProperty.isSetHref()) {	
					featureBuilder.add(observedProperty.getHref());
				} else {
					throw new XmlException("No href tag set on observedProperty element");
				}
			} catch(NullPointerException e) {
				throw new XmlException("Could not parse observedProperty href", e);
			}
			
			try {
				TimeInstantPropertyType resultTime = observation.getResultTime();
				if(resultTime.isSetHref()) {	
					featureBuilder.add(resultTime.getHref());
				} else {
					throw new XmlException("No href tag set on resultTime element");
				}
			} catch(NullPointerException e) {
				throw new XmlException("Could not parse resultTime href", e);
			}
			
			try {
				OMProcessPropertyType procedure = observation.getProcedure();
				if(procedure.isSetHref()) {
					featureBuilder.add(procedure.getHref());
				} else {
					throw new XmlException("No href tag set on procedure element");
				}
			} catch(NullPointerException e) {
				throw new XmlException("Could not parse procedure href", e);
			}
			
			// get foi Link and Title as separate strings
			try {
				FeaturePropertyType foi = observation.getFeatureOfInterest();
				if(foi.isSetHref() && foi.isSetTitle()) {
					featureBuilder.add(foi.getHref());
					featureBuilder.add(foi.getTitle());
				} else {
					throw new XmlException("No href or title set on featureOfInterest element");
				}
			} catch(NullPointerException e) {
				throw new XmlException("Could not parse featureOfInterest", e);
			}
			
			// parse result string for separate sentiment, userid and tweet fields
			String resultString = observation.getResult().getDomNode().getFirstChild().getNodeValue();
			if(resultString != null) {
				featureBuilder.add(resultString);
			} else {
				throw new XmlException("No result string found in result element");
			}
		} catch (XmlException e) {
			LOGGER.error(e.getMessage());
			throw e;
		}
	
		return featureBuilder.buildFeature(null);
	}
}
