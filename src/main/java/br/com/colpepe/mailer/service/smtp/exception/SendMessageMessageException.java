package br.com.colpepe.mailer.service.smtp.exception;

public class SendMessageMessageException extends SendMessageException {

	private static final long serialVersionUID = -4630120113937917671L;

	/**
	 * Construtor alternativo.
	 * 
	 * @param inconformidade
	 */
	public SendMessageMessageException(String inconformidade) {
		super(inconformidade);
	}
}
