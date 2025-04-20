package com.enums;


public enum ResponseCode {
	
//	Geeneral status for all success case.
	SUCCESS(0, "Success", "Successful"),
//	Transaction related case.
	TXN_DECLINED(1, "Declined", "Transaction declined."),
	TXN_PENDING(2, "Pending", "Transaction in progress."),
//	Merchant related case.
	MERCHANT_ONBOARD_FAILED(3, "Failed", "Failed to onboard merchant."),
	INVALID_MERCHANT_ID(4, "Failed", "Invalid merchant id."),
//	EMail related case.
	SUCCESS_SENT_EMAIL(5, "Success", "EMail success sent."),
	EMAIL_FAILED_SENT(6, "Failed", "EMail failed to sent. Kindly contact helpdesk support if issue still persist after mulitple times retry."),
	SUCCESS_RETRIEVE_EMAIL_DETAIL(7, "Success", "Success retrieve email details."),
	RETRIEVE_EMAIL_DETAIL_FAILED(8, "Failed", "Failed to retrieve email details. Kindly contact helpdesk support if issue still persist after mulitple times retry."),
//	Geeneral status for all error case.
    ERROR_OCCURED(-1, "Failed", "Error Occured."),
    CATCHED_EXCEPTION(-2, "Failed", "Catched exception error."),
    UNAUTHORIZED_ACCESS(-3, "Failed", "Access is unauthorized."),
	RATE_LIMIT_EXCEEDED(-4, "Failed", "Rate limit exceeded.");

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
