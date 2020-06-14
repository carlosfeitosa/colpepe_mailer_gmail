package br.com.colpepe.mailer.controller.exception;

public class BatchControllerException extends Exception {

	private static final long serialVersionUID = 3226844638810651888L;

	/**
	 * Construtor padrão.
	 */
	public BatchControllerException() {
		super();
	}

	/**
	 * Construtor alternativo.
	 * 
	 * @param message mensagem de exceção
	 */
	public BatchControllerException(String message) {
		super(message);
	}
}
