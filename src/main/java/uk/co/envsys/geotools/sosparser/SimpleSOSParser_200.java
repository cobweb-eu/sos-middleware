/**
 * 
 */
package uk.co.envsys.geotools.sosparser;

import java.util.ArrayList;
import java.util.List;

import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantPropertyType;
import net.opengis.gml.x32.TimePeriodPropertyType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.OMProcessPropertyType;
import net.opengis.om.x20.ObservationContextPropertyType;
import net.opengis.om.x20.TimeObjectPropertyType;
import net.opengis.sos.x20.GetObservationResponseDocument;
import net.opengis.sos.x20.GetObservationResponseType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.isotc211.x2005.gmd.DQElementPropertyType;
import org.isotc211.x2005.gmd.MDMetadataPropertyType;
import org.n52.oxf.xmlbeans.tools.SoapUtil;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Class to provide concrete implementation of a Simple SOS Parser for
 * Observations returned in O&M v2.0 from SOS 2.0.0
 * @author envsys
 *
 */
public class SimpleSOSParser_200 extends SimpleSOSParser {

	/* (non-Javadoc)
	 * @see uk.co.envsys.geotools.sosparser.SimpleSOSParser#parseXML(org.apache.xmlbeans.XmlObject)
	 */
	@Override
	protected GTVectorDataBinding parseXML(XmlObject document) {
		if(SoapUtil.isSoapEnvelope(document)) {
			LOGGER.debug("Found SOAP Envelope");
			if(GetObservationResponseType.type.equals(SoapUtil.getSchemaTypeOfXmlPayload(document))) {		
				XmlObject payload = SoapUtil.stripSoapEnvelope(document);		
				if(!payload.schemaType().isAssignableFrom(GetObservationResponseType.type)) {
					IllegalArgumentException ex = new IllegalArgumentException("Could not parse GetObservationResponse from SOAP Body"); 
					LOGGER.error(ex.getMessage());
					throw ex;
				} else {
					// DO THE THINGS
					LOGGER.debug("Found GetObservationResponse in SOAP Body!");
					try {
						return parseObservations(payload);
					} catch (XmlException e) {
						IllegalArgumentException ex = new IllegalArgumentException("Error parseing SOS XML:", e); 
						LOGGER.error(ex.getMessage());
						throw ex;
					}
				}
			} else{
				IllegalArgumentException ex = new IllegalArgumentException("SOAP Body does not contain valid GetObservationResponse"); 
				LOGGER.error(ex.getMessage());
				throw ex;
			}
		} else if(document.schemaType().isAssignableFrom(GetObservationResponseDocument.type)) {
			LOGGER.warn("GetObservations not in SOAP Envelope");
			try {
				return parseObservations(document);
			} catch (XmlException e) {
				IllegalArgumentException ex = new IllegalArgumentException("Error parseing SOS XML:", e); 
				LOGGER.error(ex.getMessage());
				throw ex;
			}
		} else {
			IllegalArgumentException e = new IllegalArgumentException("Could not find SOAP Envelope or GetObservationResponse in response"); 
			LOGGER.error(e.getMessage());
			throw e;
		}
	}
	
	private GTVectorDataBinding parseObservations(XmlObject observations) throws XmlException {
		GetObservationResponseType obs = (GetObservationResponseType)
				observations.changeType(GetObservationResponseType.type);	
		
	
		int numMembers = obs.sizeOfObservationDataArray();
		LOGGER.debug("Parseing " + numMembers + "observations");
		
		// make a list to store the features
		List<SimpleFeature> simpleFeatureList = new ArrayList<SimpleFeature>();
		
		for(int i = 0; i < numMembers; i++) {
			OMObservationType observation = obs.getObservationDataArray(i).getOMObservation();
			
			if(i == 0) {
				// create the feature type (schema) based on first observation
				type = createType(observation);
				featureBuilder = new SimpleFeatureBuilder(type);
			}
			// build the feature from the type and add it to the list
			SimpleFeature feature = convertToFeature(observation);
			simpleFeatureList.add(feature);
		}
		
		SimpleFeatureCollection collection = new ListFeatureCollection(type, simpleFeatureList);
		return new GTVectorDataBinding(collection);
	}
	
	private SimpleFeatureType createType(OMObservationType observation) throws XmlException {
		// type - optional - http://www.opengis.net/def/observationType/OGC-OM/2.0/	
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("http://schemas.opengis.net/om/2.0/");
		
		builder.add(testNullReturnName(observation.getObservedProperty(), "observedProperty"), 
				ReferenceType.class);
		builder.add(testNullReturnName(observation.getProcedure(), "procedure"), 
				OMProcessPropertyType.class);
		builder.add(testNullReturnName(observation.getFeatureOfInterest(), "featureOfInterest"), 
				FeaturePropertyType.class);
		builder.add(testNullReturnName(observation.getPhenomenonTime(), "phenomenonTime"), 
				TimeObjectPropertyType.class);
		builder.add(testNullReturnName(observation.getResultTime(), "resultTime"), 
				TimeInstantPropertyType.class);
		builder.add(testNullReturnName(observation.getResult(), "result"), 
				XmlObject.class);
		
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
	
	private SimpleFeature convertToFeature(OMObservationType observation) throws XmlException {
		featureBuilder.add(ifNullThrowParseException(observation.getObservedProperty(), "observedProperty"));
		featureBuilder.add(ifNullThrowParseException(observation.getProcedure(), "procedure"));
		featureBuilder.add(ifNullThrowParseException(observation.getFeatureOfInterest(), "featureOfInterest"));
		featureBuilder.add(ifNullThrowParseException(observation.getPhenomenonTime(), "phenomenonTime"));
		featureBuilder.add(ifNullThrowParseException(observation.getResultTime(), "resultTime"));
		featureBuilder.add(ifNullThrowParseException(observation.getResult(), "result"));
		
		if(observation.isSetType()) {
			featureBuilder.add(observation.getType());
		}
		if(observation.isSetMetadata()) {
			featureBuilder.add(observation.getMetadata());
		}
		if(observation.isSetValidTime()) {
			featureBuilder.add(observation.getValidTime());
		}
		if(observation.sizeOfRelatedObservationArray() > 0) {
			featureBuilder.add(observation.getRelatedObservationArray());
		}
		if(observation.sizeOfResultQualityArray() > 0) {
			featureBuilder.add(observation.getResultQualityArray());
		}
		
		return featureBuilder.buildFeature(null);
	}
}
