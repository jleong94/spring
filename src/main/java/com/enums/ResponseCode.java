package com.enums;


public enum ResponseCode {
	
	SUCCESS(0, "Success", "Successful."),
    UNAUTHORIZED_ACCESS(1, "Failed", null),
    ERROR(2, "Failed", null),
    FAILED(3, "Failed", null),
    RATE_LIMIT(4, "Failed", "Exceed rate limit. Please retry later.");

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
