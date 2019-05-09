package openkitsandbox;

import java.time.LocalDateTime;
import java.util.Random;

public class DemonstrationHelper {
	static boolean reportCrash = false;

	static String uniqueID() {
		// Generates a timestamp to make it unique
		return LocalDateTime.now().toString();
	}

	static void sleep() {
		// Pauses the thread for an amount of time
		// Thread.sleep is inaccurate, so don't expect to see the exact time on Dynatrace 
		
		try {
			long n = (new Random().nextInt(2) + 1) * 1000;
			Thread.sleep(n);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

}
