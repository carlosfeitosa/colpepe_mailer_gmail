package br.com.colpepe.mailer.exception;

public class MailerException extends Exception {

	private static final long serialVersionUID = -7986866283804410154L;

	/**
	 * Construtor padrão.
	 */
	public MailerException() {
		super();
	}

	/**
	 * Construtor alternativo.
	 * 
	 * @param message mensagem de exceção
	 */
	public MailerException(String message) {
		super(message);
	}
}
