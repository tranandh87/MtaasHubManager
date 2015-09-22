package com.mtaas.bean;

public class TestClassExecutionStatus extends ExecutionStatus {

	private int testClassExecutionId;
	private String testClassName;
	public int getTestClassExecutionId() {
		return testClassExecutionId;
	}
	public void setTestClassExecutionId(int testClassExecutionId) {
		this.testClassExecutionId = testClassExecutionId;
	}
	public String getTestClassName() {
		return testClassName;
	}
	public void setTestClassName(String testClassName) {
		this.testClassName = testClassName;
	}
}
