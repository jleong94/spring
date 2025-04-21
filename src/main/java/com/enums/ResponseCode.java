package com.enums;


public enum ResponseCode {
	
	
//	EMail related case.
	SUCCESS(0, "Success", "EMail successful sent."),
	EMAIL_FAILED_SENT(1, "Failed", "EMail failed to sent. Kindly contact helpdesk support if issue still persist after mulitple times retry."),
	SUCCESS_RETRIEVE_EMAIL_DETAIL(2, "Success", "Success retrieve email details."),
//	Authorization related case.
    UNAUTHORIZED_ACCESS(3, "Failed", "Unauthorized access."),
    JWT_TOKEN_EXPIRED(4, "Failed", "Token expired."),
//	Geeneral status for all error case.
    ERROR_OCCURED(-1, "Failed", null),
	RATE_LIMIT_EXCEEDED(-3, "Failed", "Rate limit exceeded.");

	private final int response_code;
	private final String response_status;
    private final String response_desc;
	
	private ResponseCode(int response_code, String response_status, String response_desc) {
		this.response_code = response_code;
		this.response_status = response_status;
		this.response_desc = response_desc;
	}

	public int getResponse_code() {
		return response_code;
	}

	public String getResponse_status() {
		return response_status;
	}

	public String getResponse_desc() {
		return response_desc;
	}
}
