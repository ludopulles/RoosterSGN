package eu.ludiq.roostersgn.exception;

public class ServerException extends Exception {

	private boolean known;
	private int code;
	private String message;
	
	public ServerException() {
		this.known = false;
		this.code = 0;
		this.message = "";
	}

	public ServerException(int code, String message) {
		this.known = true;
		this.code = code;
		this.message = message;
	}
	
	public boolean isErrorKnown() {
		return known;
	}

	public int getErrorCode() {
		return code;
	}

	public String getErrorMessage() {
		return message;
	}
}
