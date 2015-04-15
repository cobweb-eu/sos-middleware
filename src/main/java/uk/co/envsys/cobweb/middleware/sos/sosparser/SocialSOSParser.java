package uk.co.envsys.cobweb.middleware.sos.sosparser;

import java.util.Date;

import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantPropertyType;
import net.opengis.gml.x32.TimePeriodPropertyType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.OMProcessPropertyType;
import net.opengis.om.x20.ObservationContextPropertyType;
import net.opengis.om.x20.TimeObjectPropertyType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.isotc211.x2005.gmd.DQElementPropertyType;
import org.isotc211.x2005.gmd.MDMetadataPropertyType;
import org.opengis.feature.simple.SimpleFeatureType;

public class SocialSOSParser extends SimpleSOSParser_200 {
	
	private SimpleFeatureType createType(OMObservationType observation) throws XmlException {
		// type - optional - http://www.opengis.net/def/observationType/OGC-OM/2.0/	
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("UCD Social Sensor Observation");
		
		builder.add(testNullReturnName(observation.getPhenomenonTime(), "phenomenonTime"), 
				Date.class);

		builder.add(testNullReturnName(observation.getObservedProperty(), "observedProperty"), 
				String.class);
		
		builder.add(testNullReturnName(observation.getResultTime(), "resultTime"), 
				String.class);
		
		builder.add(testNullReturnName(observation.getProcedure(), "procedure"), 
				String.class);
		
		if(observation.getFeatureOfInterest() != null) {
			builder.add("foiLink", String.class);
			builder.add("foiTitle", String.class);
		} else {
			XmlException e = new XmlException("Could not parse required element: feature of interest");
			LOGGER.error(e.getMessage());
			throw e;
		}
		
		if(observation.getResult() != null) {
			builder.add("resultSentiment", Double.class);
			builder.add("resultUserID", Integer.class);
			builder.add("resultTweet", String.class);
		} else {
			XmlException e = new XmlException("Could not parse required element: result");
			LOGGER.error(e.getMessage());
			throw e;
		}
		
		if(observation.isSetType()) {
			builder.add("type", ReferenceType.class);
		}
		if(observation.isSetMetadata()) {
			builder.add("metadata", MDMetadataPropertyType.class);
		}
		if(observation.isSetValidTime()) {
			builder.add("validTime", TimePeriodPropertyType.class);
		}
		if(observation.sizeOfRelatedObservationArray() > 0) {
			builder.add("relatedObservations", ObservationContextPropertyType[].class);
		}
		if(observation.sizeOfResultQualityArray() > 0) {
			builder.add("resultQuality", DQElementPropertyType[].class);
		}
		
		return builder.buildFeatureType();
	}
	

}
