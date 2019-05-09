package openkitsandbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.dynatrace.openkit.DynatraceOpenKitBuilder;
import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.api.WebRequestTracer;

public class OpenKitSandbox {

	private OpenKit openKit;
	private RootAction rootAction;
	private Action childAction;

	public void applicationBeingInstrumented(String testUrl, String clientIP) {

		// Has a timestamp to make unique user tags
		String userTag = "[user tag " + LocalDateTime.now() + "]";

		// Start the user session
		Session session = openKit.createSession(clientIP);
		session.identifyUser(userTag);

		// Start a root action
		rootAction = session.enterAction("[root action name " + DemonstrationHelper.uniqueID() + "]");
		rootAction.reportEvent("[custom event name]");
		applicationAction1(testUrl);
		rootAction.leaveAction();

		// Start a root action
		rootAction = session.enterAction("[root action name " + DemonstrationHelper.uniqueID() + "]");
		applicationAction2();
		rootAction.leaveAction();

		// Reporting a crash
		if (DemonstrationHelper.reportCrash) {
			try {
				System.out.println("Divide by zero: " + 42 / 0);
			} catch (Exception e) {
				String errorName = e.getClass().getName();
				String reason = e.getMessage();
				String stacktrace = ExceptionUtils.getStackTrace(e);

				session.reportCrash(errorName, reason, stacktrace);
			}
		}

		// End the user session
		session.end();
	}

	private void applicationAction1(String testUrl) {
		DemonstrationHelper.sleep();

		childAction = rootAction.enterAction("[child action " + DemonstrationHelper.uniqueID() + "]");
		childAction.reportValue("Custom value (integer)", 42);
		childAction.reportValue("Custom value (double)", 3.141592653589793);
		childAction.reportValue("Custom value (string)", "The quick brown OneAgent jumps over the lazy dog");
		applicationSubAction1(testUrl);
		childAction.leaveAction();

		childAction = rootAction.enterAction("[child action " + DemonstrationHelper.uniqueID() + "]");
		applicationSubAction2();
		childAction.leaveAction();

		childAction = rootAction.enterAction("[child action " + DemonstrationHelper.uniqueID() + "]");
		applicationSubAction3(testUrl);
		childAction.leaveAction();
	}

	private void applicationSubAction1(String testUrl) {
		DemonstrationHelper.sleep();
		try {
			executeWebRequest(testUrl, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void applicationSubAction2() {
		DemonstrationHelper.sleep();

		childAction.reportError("[Error message]", 999, "[Reason]");
	}

	private void applicationSubAction3(String testUrl) {
		try {
			executeWebRequest(testUrl, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void applicationAction2() {
		childAction = rootAction.enterAction("[child action " + DemonstrationHelper.uniqueID() + "]");
		childAction.reportValue("Value1", 1);
		childAction.reportValue("Value2", 2);
		childAction.reportValue("Value3", 3);
		childAction.reportValue("Value4", 1.0);
		childAction.reportValue("Value5", 2.0);
		childAction.reportValue("Value6", 3.0);
		childAction.reportValue("Value7", "One");
		childAction.reportValue("Value8", "Two");
		childAction.reportValue("Value9", "Three");
		childAction.leaveAction();

	}

	private void executeWebRequest(String testUrl, String payload) throws Exception {
		try {

			URL url = new URL(testUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setDoInput(true);

			conn.setRequestMethod("GET");

			if (payload != null && !payload.isEmpty()) {
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(payload);
				wr.flush();
			}

			WebRequestTracer tracer = rootAction.traceWebRequest(conn);
			tracer.start();

			int respCode = conn.getResponseCode();

			int res = -1;
			if (respCode != 200) {
				res = readData(conn.getErrorStream());
				// throw new Exception(new String(res));
			} else {
				InputStream is = conn.getInputStream();
				res = readData(is);

			}

			// set bytesSent, bytesReceived and response code
			tracer.setBytesSent(payload != null ? payload.getBytes().length : 0) // we assume default encoding here
					.setBytesReceived(res) // bytes processed
					.setResponseCode(conn.getResponseCode());

			tracer.stop();

		} catch (Exception ex) {
			throw ex;
		}
	}

	private int readData(InputStream is) throws IOException {
		byte[] buffer = new byte[4096];
		int numBytes, totalBytes = 0;
		while ((numBytes = is.read(buffer)) > 0) {
			totalBytes += numBytes;
		}

//		// Print the contents of the response
//		BufferedReader in = new BufferedReader(new InputStreamReader(is));
//		String inputLine;
//		while ((inputLine = in.readLine()) != null)
//			System.out.println(inputLine);

		return totalBytes;
	}

	void prepareOpenKit(String endpointURL, String applicationID, String applicationName, String applicationVersion,
			String operatingSystem, String manufacturer, String modelID) {

		// Unique identifier of the device, sessions with the same deviceID are grouped
		long deviceID = System.currentTimeMillis();

		openKit = new DynatraceOpenKitBuilder(endpointURL, applicationID, deviceID).withApplicationName(applicationName)
				.withApplicationVersion(applicationVersion)
				.withOperatingSystem(operatingSystem)
				.withManufacturer(manufacturer)
				.withModelID(modelID)
				.enableVerbose()
				.build();

		// Define OpenKit startup timeout
		long timeoutInMilliseconds = 10 * 1000;
		boolean result = openKit.waitForInitCompletion(timeoutInMilliseconds);
		System.out.println("waitForInitCompletion: " + result);

		// Check if OpenKit has started
		boolean isInitialized = openKit.isInitialized();
		if (isInitialized) {
			System.out.println("OpenKit is initialized");
		} else {
			System.out.println("OpenKit is not yet initialized");
		}

	}

	void finishOpenKit() {

		openKit.shutdown();

	}

}
