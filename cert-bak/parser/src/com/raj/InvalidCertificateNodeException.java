package com.raj;


/**
 * @author rgauttam
 *
 */
public class InvalidCertificateNodeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidCertificateNodeException(String msg) {
		super(msg);
	}
	
	public InvalidCertificateNodeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
