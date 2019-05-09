package openkitsandbox;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaHandler implements RequestHandler<String, String> {
	@Override
	public String handleRequest(String input, Context context) {

		OpenKitSandbox oks = new OpenKitSandbox();

		// Define Environment Variables in the AWS Lambda console

		// Dynatrace instrumentation
		String endpointURL = System.getenv("endpointURL");
		String applicationName = System.getenv("applicationName");
		String applicationID = System.getenv("applicationID");

		// User Session data
		String operatingSystem = System.getenv("operatingSystem");
		String applicationVersion = System.getenv("applicationVersion");
		String manufacturer = System.getenv("manufacturer");
		String modelID = System.getenv("modelID");
		String clientIP = System.getenv("clientIP");

		// Example URL for the application to make an HTTP Request
		String testURL = System.getenv("testURL");

		// Passes the necessary parameter for instrumentation
		oks.prepareOpenKit(endpointURL, applicationID, applicationName, applicationVersion, operatingSystem,
				manufacturer, modelID);

		// Executes the application
		oks.applicationBeingInstrumented(testURL, clientIP);

		oks.finishOpenKit();

		// Will print the string used for testing purposes on AWS Lambda console
		return "Test input: " + input;
	}
}
