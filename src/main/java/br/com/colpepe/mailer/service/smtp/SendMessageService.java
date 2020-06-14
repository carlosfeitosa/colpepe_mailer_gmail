package br.com.colpepe.mailer.service.smtp;

import br.com.colpepe.mailer.service.smtp.exception.SendMessageException;
import br.com.colpepe.mailer.service.smtp.vo.MessageVO;

/**
 * Interface para serviço de envio de mensagens.
 * 
 * @author skull
 *
 */
public interface SendMessageService {

	/**
	 * Configura o credenciamento no servidor de mensagens.
	 * 
	 * @param username nome de usuário
	 * @param password senha
	 */
	public void setAccessCredentials(String username, String password);

	/**
	 * Configura a mensagem.
	 * 
	 * @param mensagem mensagem a ser enviada
	 */
	public void setMessage(MessageVO mensagem);

	/**
	 * Envia a mensagem;
	 * 
	 * @return resultado do envio
	 * 
	 * @throws SendMessageException caso não consiga enviar a mensagem.
	 */
	public boolean sendMessage() throws SendMessageException;
}
