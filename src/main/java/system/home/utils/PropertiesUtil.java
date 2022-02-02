package system.home.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

	public static Properties read() {
		 Properties prop = null;
		 try (InputStream input = new FileInputStream("D:\\PROPERTIES\\READEMAIL\\config.properties")) {
	            prop = new Properties();
	            prop.load(input);      
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
		 return prop;
	}
	
}
