package openkitsandbox;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LocalTesting {
	public static void main(String[] args) {
		String endpointURL, applicationName, applicationID, testURL;

		OpenKitSandbox oks = new OpenKitSandbox();

		// Define properties in a config file in the format
		// key=value

		try (InputStream input = new FileInputStream("src/config.properties")) {

			Properties prop = new Properties();
			prop.load(input);

			// Dynatrace instrumentation
			endpointURL = prop.getProperty("endpointURL");
			applicationName = prop.getProperty("applicationName");
			applicationID = prop.getProperty("applicationID");

			// Example URL for the application to make an HTTP Request
			testURL = prop.getProperty("testURL");

			// User Session data
			String operatingSystem = "[EclipseIDE]";
			String applicationVersion = "[1.0.0.0]";
			String manufacturer = "[Manufacturer]";
			String modelID = "[Device]";
			String clientIP = "192.168.0.1";
			// Passes the necessary parameter for instrumentation
			oks.prepareOpenKit(endpointURL, applicationID, applicationName, applicationVersion, operatingSystem,
					manufacturer, modelID);

			// Executes the application
			oks.applicationBeingInstrumented(testURL, clientIP);

			oks.finishOpenKit();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
