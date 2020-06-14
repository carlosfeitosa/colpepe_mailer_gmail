package br.com.colpepe.mailer.exception;

public class MailerNoCredentialsException extends MailerException {

	private static final long serialVersionUID = -3259090935857480525L;

	public MailerNoCredentialsException(String message) {
		super(message);
	}
}
