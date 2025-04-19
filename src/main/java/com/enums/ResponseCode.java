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

	public static String getDescriptionByCode(int response_code) {
        for (ResponseCode responseCode : ResponseCode.values()) {
            if (responseCode.getResponse_code() == response_code) {
                return responseCode.getResponse_desc();
            }
        }
        return "";
    }

	public static String getStatusByCode(int response_code) {
        for (ResponseCode responseCode : ResponseCode.values()) {
            if (responseCode.getResponse_code() == response_code) {
                return responseCode.getResponse_status();
            }
        }
        return "";
    }
}
