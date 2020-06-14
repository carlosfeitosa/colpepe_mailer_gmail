package br.com.colpepe.mailer.service.smtp.exception;

public class SendMessageException extends Exception {

	private static final long serialVersionUID = 3624822312068777752L;

	/**
	 * Construtor padrão.
	 */
	public SendMessageException() {
		super();
	}

	/**
	 * Construtor alternativo.
	 * 
	 * @param message mensagem de exceção
	 */
	public SendMessageException(String message) {
		super(message);
	}
}
