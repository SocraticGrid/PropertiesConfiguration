/**
 * 
 */
package org.socraticgrid.properties.util;

import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author steven
 *
 */
public class XMLPropertyParser {

	private static Logger log = LoggerFactory.getLogger(XMLPropertyParser.class);
	
	private String environmentId;
	private String fileResource;
	private Document rootNode;
	boolean parsed;

	public XMLPropertyParser(String fileResource, String environmentId) {
		this.fileResource = fileResource;
		this.environmentId = environmentId;
	}


	/**
	 * 
	 * @param existingProps
	 */
	public void parseToProperties(Properties existingProps) {
		if(parsed){
			throw new RuntimeException("XMLPropertyParser can only be used once!");
		}
		parsed = true;
		
		//Get a default DocumentBuilderFactory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();

			rootNode = builder.parse(this.getClass().getResourceAsStream(fileResource));
			
			XPathFactory xFactory = XPathFactory.newInstance();
		    XPath xpath = xFactory.newXPath();
		    
		    //Get the properties
		    String xpr = "/configuration/properties/property";
		    NodeList props = (NodeList)xpath.compile(xpr).evaluate(rootNode, XPathConstants.NODESET);
		    for(int i=0; i<props.getLength(); i++) {
		    	NamedNodeMap attrs = props.item(i).getAttributes();
		    	String attrName = attrs.getNamedItem("name").getNodeValue();

		    	//Don't override properties already in existingProps
		    	if( ! existingProps.containsKey(attrName) ){
		    		existingProps.put(attrs.getNamedItem("name").getNodeValue().toString(), attrs.getNamedItem("value").getNodeValue().toString());
		    	}
		    }
		    
		    //Get environmentId
		    xpr = "";
		    if(environmentId != null && ! environmentId.isEmpty()){
		    	xpr = "/configuration/environments/environment[@id='"+environmentId+"']/property";
		    }
		    else{
		    	//If the enironmentId is NULL then find the default in the file
		    	xpr = "/configuration/environments";
		    	Node environments = (Node)xpath.compile(xpr).evaluate(rootNode, XPathConstants.NODE);
		    	NamedNodeMap envAttrs = environments.getAttributes();
		    	Node attrName = envAttrs.getNamedItem("default");
		    	if(attrName != null){
			    	xpr = "/configuration/environments/environment[@id='"+attrName.getNodeValue()+"']/property";
		    	}
		    }
		    //If the environmentId has been specified get the properties for that environment
		    if(! xpr.isEmpty()){
			    props = (NodeList)xpath.compile(xpr).evaluate(rootNode, XPathConstants.NODESET);
			    for(int i=0; i<props.getLength(); i++) {
			    	NamedNodeMap attrs = props.item(i).getAttributes();
			    	String attrName = attrs.getNamedItem("name").getNodeValue();

			    	//Don't override properties already in existingProps
			    	if( ! existingProps.containsKey(attrName) ){
			    		existingProps.put(attrs.getNamedItem("name").getNodeValue().toString(), attrs.getNamedItem("value").getNodeValue().toString());
			    	}
			    }
		    }
			
		    if(log.isDebugEnabled()) {
		    	String ls = System.getProperty("line.separator");
		    	log.debug("\nFinal Properties :\n"+existingProps.toString().replaceAll("\\{|\\}","\t").replaceAll(",\\s+",ls+"\t"));
		    }
			
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String...strings) {
		XMLPropertyParser parser = new XMLPropertyParser("/config.xml", "172.31.5.68");
		Properties props = new Properties();
		parser.parseToProperties(props);
		
		props.list(System.out);
	}
}
