package org.socraticgrid.properties;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socraticgrid.properties.util.XMLPropertyParser;

public class SGProperties {
	/**
	 * A way to enumerate properties... could be (has been) expanded to keep a default value ¿ 
	 * 
	 * NOTE: Keep it current
	 * 
	 */
	public enum PROPS {
		ENVIRONMENT("environment","local-dev")
		, PROPERTIES_FILE("external.properties","External.properties")
		, PROPERTIESFILE_SCHEDULE("propertiesfile_cron","0 * * * * ?") //default is every minute
		, AGENT_ENDPOINT_IP("agent.endpoint.ip","localhost")
		, AGENT_ENDPOINT_PORT("agent.endpoint.port","8081")
		, APP_NAME("CDSS","CDSS")
		, UCS_ENDPOINT("ucs.endpoint","http://172.31.5.68:8080/UCSClient/ucsclient")
		, WS_SERVER("websocket_server","127.0.0.1:8080")
		;
		
		private String prop;
		public String getProp(){ return prop; }
		private String defaultValue;
		public String getDefaultValue(){ return defaultValue; }
		
		PROPS(String prop, String defaultValue){
			this.prop = prop;
			this.defaultValue = defaultValue;
		}
	}

	
	private static final String CONFIG_FILE = "/config.xml";
	private static final String SHAREDCONFIG_FILE = "/External.properties";
	
	private static Logger log = LoggerFactory.getLogger(SGProperties.class);

	private SGProperties() {};

	private static final class PropertiesSingleton {
		private static volatile Properties properties = new Properties();
		static {
			try( InputStream inputStream = SGProperties.class.getResourceAsStream(SHAREDCONFIG_FILE) ) {
				properties.load(inputStream);
				
				//Get environment properties (e.g. environment) can be null
				String env = properties.getProperty(PROPS.ENVIRONMENT.getProp(), PROPS.ENVIRONMENT.getDefaultValue());
				XMLPropertyParser parser = new XMLPropertyParser(CONFIG_FILE, env);
				parser.parseToProperties(properties);
				
			} catch (Exception e) {
				log.error("************************** Problem configuring Properties !!!",e);
			}
		}
	}
	
	
	public static String getProperty(PROPS props){
		return PropertiesSingleton.properties.getProperty(props.getProp(),props.getDefaultValue());
	}
	
	public static void refreshProperties() {
		//Stubbed - implement property refresh
	}

}
