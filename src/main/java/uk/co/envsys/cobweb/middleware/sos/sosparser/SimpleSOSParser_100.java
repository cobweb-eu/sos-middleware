package uk.co.envsys.cobweb.middleware.sos.sosparser;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import net.opengis.gml.BoundingShapeType;
import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.LocationPropertyType;
import net.opengis.gml.MetaDataPropertyType;
import net.opengis.gml.StringOrRefType;
import net.opengis.om.x10.AnyOrReferenceType;
import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationCollectionType;
import net.opengis.om.x10.ObservationType;
import net.opengis.om.x10.ProcessPropertyType;
import net.opengis.swe.x101.PhenomenonPropertyType;
import net.opengis.swe.x101.TimeObjectPropertyType;

public class SimpleSOSParser_100 extends SimpleSOSParser {

	@Override
	protected GTVectorDataBinding parseXML(XmlObject document) {
		// try and parse as SOS and O&M V1
		if(!document.schemaType().isAssignableFrom(ObservationCollectionDocument.type)) {
			IllegalArgumentException e = new IllegalArgumentException("Expected o&m 1.0 ObservationCollection"); 
			LOGGER.error(e.getMessage());
			throw e;
		} else {
			// Convert to O&M 1.0 ObservationCollection XmlObject
			ObservationCollectionDocument observations = (ObservationCollectionDocument) 
					document.changeType(ObservationCollectionDocument.type);
			
			// Try and parse the document to a FeatureCollection
			GTVectorDataBinding parsedObservations;
			try {
				parsedObservations = parseObservations(observations);
			} catch (XmlException e) {
				IllegalArgumentException ex = new IllegalArgumentException("Error parseing SOS XML:", e); 
				LOGGER.error(ex.getMessage());
				throw ex;
			}
			return parsedObservations;
		}
	}
	
	private GTVectorDataBinding parseObservations(ObservationCollectionDocument observationsDoc) throws XmlException {
		// get the observations
		ObservationCollectionType observations = observationsDoc.getObservationCollection();
	
		int numMembers = observations.sizeOfMemberArray();
		LOGGER.debug("Parseing " + numMembers + "observations");
		
		// make a list to store the features
		List<SimpleFeature> simpleFeatureList = new ArrayList<SimpleFeature>();
		
		for(int i = 0; i < numMembers; i++) {
			ObservationType observation = observations.getMemberArray(i).getObservation();
			if(i == 0) {
				// create the feature type (schema) based on first observation
				type = createFeatureType(observation);
				featureBuilder = new SimpleFeatureBuilder(type);
			}
			// build the feature from the type and add it to the list
			SimpleFeature feature = convertToFeature(observation);
			simpleFeatureList.add(feature);
		}
		
		SimpleFeatureCollection collection = new ListFeatureCollection(type, simpleFeatureList);
		return new GTVectorDataBinding(collection);
	}
	
	/**
	 * Function to create a FeatureType from an observation
	 * 
	 * The FeatureType acts like a "schema" and goes with Features
	 * into the FeatureCollection
	 * 
	 * @param observation The observation to base the FeatureType on
	 * @return {@code SimpleFeatureType} The created FeatureType
	 * @throws XmlException If any critical problems are encountered during parsing, 
	 * e.g., due to missing elements. 
	 */
	private SimpleFeatureType createFeatureType(ObservationType observation) throws XmlException {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("om-1.0-observation");
		
		// do required fields first, allows us to bail early in case of null fields
		builder.add(testNullReturnName(observation.getSamplingTime(), "samplingTime"), 
				TimeObjectPropertyType.class);
		builder.add(testNullReturnName(observation.getProcedure(), "procedure"),
				ProcessPropertyType.class);
		builder.add(testNullReturnName(observation.getObservedProperty(), "observedProperty"),
				PhenomenonPropertyType.class);
		builder.add(testNullReturnName(observation.getFeatureOfInterest(), "featureOfInterest"),
				FeaturePropertyType.class);
		builder.add(testNullReturnName(observation.getResult(), "result"),
				XmlObject.class);
		
		if(observation.isSetResultTime()) {
			builder.add("resultTime", TimeObjectPropertyType.class);
		}
		if(observation.isSetResultQuality()) {
			builder.add("resultQuality", AnyOrReferenceType.class);
		}
		if(observation.isSetBoundedBy()) {
			builder.add("boundedBy", BoundingShapeType.class);
		}
		if(observation.isSetLocation()) {
			builder.add("location", LocationPropertyType.class);
		}
		if(observation.isSetDescription()) {
			builder.add("description", StringOrRefType.class);
		}
		if(observation.isSetId()) {
			builder.add("id", String.class);
		}
		if(observation.isSetMetadata()) {
			builder.add("metadata", AnyOrReferenceType.class);
			builder.add("metadataPropertyArray", MetaDataPropertyType[].class);
		}
		if(observation.sizeOfParameterArray() > 0) {
			// TODO: Parameters!
			LOGGER.warn("Ignoring parameters in observation: unimplemented.");
		}
		return builder.buildFeatureType();
	}

	/**
	 * Function to convert an observation to a SimpleFeature to be stored in a FeatureCollection
	 * This function uses the featureBuilder class member to construct the feature according
	 * to the previously generated SimpleFeatureType
	 * 
	 * @param observation {@code ObservationType} The observation as represented by O&amp;M v1.0
	 * @return {@code SimpleFeature} a feature representing this observation
	 * @throws XmlException if any required elements were not found during parsing
	 */
	private SimpleFeature convertToFeature(ObservationType observation) throws XmlException {
		// featureBuilder already instantiated by outer loop!
		featureBuilder.add(ifNullThrowParseException(observation.getSamplingTime(), "samplingTime"));
		featureBuilder.add(ifNullThrowParseException(observation.getProcedure(), "procedure"));
		featureBuilder.add(ifNullThrowParseException(observation.getObservedProperty(), "observedProperty"));
		featureBuilder.add(ifNullThrowParseException(observation.getFeatureOfInterest(), "featureOfInterest"));
		featureBuilder.add(ifNullThrowParseException(observation.getResult(), "result"));
		if(observation.isSetResultTime()) {
			featureBuilder.add(observation.getResultTime());
		}
		if(observation.isSetResultQuality()) {
			featureBuilder.add(observation.getResultQuality());
		}
		if(observation.isSetBoundedBy()) {
			featureBuilder.add(observation.getBoundedBy());
		}
		if(observation.isSetLocation()) {
			featureBuilder.add(observation.getLocation());
		}
		if(observation.isSetDescription()) {
			featureBuilder.add(observation.getDescription());
		}
		if(observation.isSetId()) {
			featureBuilder.add(observation.getId());
		}
		if(observation.isSetMetadata()) {
			featureBuilder.add(observation.getMetadata());
			featureBuilder.add(observation.getMetaDataPropertyArray());
		}
		return featureBuilder.buildFeature(null);
	}
}
