package com.mtaas.testRunner;

import java.util.Set;

public interface ITestResponseListener {
	void onInsertSuiteExecution(int suiteId, Set<Integer> suiteExecutionId, int reqId);
	void noGridAvailable(String message);
}
