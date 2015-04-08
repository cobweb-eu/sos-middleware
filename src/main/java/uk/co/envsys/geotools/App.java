package uk.co.envsys.geotools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

import uk.co.envsys.geotools.sosparser.SimpleSOSParser;

/**
 * Simple bootstrapping to test with some sample, file based, XML
 *
 */
public class App 
{
	private static String UCD_XML_100 = "./src/test/test_get_observation_100_ucd_twitter.xml";
	private static String DEMO_52N_XML_100 = "./src/test/test_get_obsevation_100_demo_52n.xml";
	private static String UCD_XML_200 = "./src/test/test_get_observation_200_ucd_twitter.xml";
	
    public static void main( String[] args )
    {
        System.out.println( "Testing SOS XML Parser!" );
        
        // instantiate the parser, for version 1!
        SimpleSOSParser p = SimpleSOSParser.Factory.getParser("1.0.0");
        
        // load some XML from storage
        FileInputStream inXml = null;
        
        // Try on UCD Data
		try {
			inXml = new FileInputStream(UCD_XML_100);
			System.out.println("Parseing - " + UCD_XML_100);
			GTVectorDataBinding b = (GTVectorDataBinding) p.parse(inXml, "", "");
			FeatureCollection<?, ?> f = b.getPayload();
			System.out.println(f.features().next());
		} catch (FileNotFoundException e) {
			System.out.println("Could not find test xml file: " + UCD_XML_100);
			System.out.println("PWD: " + System.getProperty("user.dir"));
			System.exit(-1);
		}
        
		// Try on Sample 52N SOS
		try {
			inXml = new FileInputStream(DEMO_52N_XML_100);
			System.out.println("Parseing - " + DEMO_52N_XML_100);
			GTVectorDataBinding b = (GTVectorDataBinding) p.parse(inXml, "", "");
			FeatureCollection<?, ?> f = b.getPayload();
			System.out.println(f);
		} catch (FileNotFoundException e) {
			System.out.println("Could not find test xml file: " + DEMO_52N_XML_100);
			System.out.println("PWD: " + System.getProperty("user.dir"));
			System.exit(-1);
		}
		
		// try using SOS 2.0.0
		p = SimpleSOSParser.Factory.getParser("2.0.0");
		try {
			inXml = new FileInputStream(UCD_XML_200);
			System.out.println("Parseing - " + UCD_XML_200);
			GTVectorDataBinding b = (GTVectorDataBinding) p.parse(inXml, "", "");
			FeatureCollection<?, ?> f = b.getPayload();
			System.out.println(f.features().next());
		} catch (FileNotFoundException e) {
			System.out.println("Could not find test xml file: " + UCD_XML_200);
			System.out.println("PWD: " + System.getProperty("user.dir"));
			System.exit(-1);
		}
    }
}
