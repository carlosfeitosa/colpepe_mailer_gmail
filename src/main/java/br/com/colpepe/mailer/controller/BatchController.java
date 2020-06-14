package br.com.colpepe.mailer.controller;

import java.io.FileNotFoundException;
import java.io.IOException;

import br.com.colpepe.mailer.controller.exception.BatchControllerException;
import br.com.colpepe.mailer.service.smtp.exception.SendMessageException;

/**
 * Interface para controlador de envio de mensagens em lote.
 * 
 * @author skull
 *
 */
public interface BatchController {

	/**
	 * Configura as credenciais de acesso.
	 * 
	 * @param username nome do usuário
	 * @param password senha
	 */
	public void setCredentials(String username, String password);

	/**
	 * Configura se usa BCC. Se setado para true, enviara os destinatários em cópia
	 * carbonada oculta.
	 * 
	 * @param useBCC
	 */
	public void setUseBCC(boolean useBCC);

	/**
	 * Configura o limite de destinatários por mensagem.
	 * 
	 * @param maxToPerMessage quantidade máxima de destinatários por mensagem
	 */
	public void setMaxRecipientsPerMessage(int maxToPerMessage);

	/**
	 * Configura o limite de destinatários total.
	 * 
	 * @param sendLimit limite de envios
	 */
	public void setMaxRecipients(int sendLimit);

	/**
	 * Configura o campo de remetente da mensagem.
	 * 
	 * @param from remetente da mensagem
	 */
	public void setFrom(String from);

	/**
	 * Configura o assunto da mensagem.
	 * 
	 * @param subject assunto da mensagem
	 */
	public void setMessageSubject(String subject);

	/**
	 * Configura o arquivo que será utilizado como fonte para o corpo do e-mail.
	 * 
	 * @param filename arquivo contendo o corpo do e-kail
	 */
	public void setMessageBodyFromFilename(String filename) throws IOException;

	/**
	 * Configura o arquivo que servirá de fonte de dados de destinatários.
	 * 
	 * @param filename arquivo contendo lista de destinatários
	 */
	public void setToListFilename(String filename);

	/**
	 * Processa o batch.
	 * 
	 * @return quantidade de e-mails enviados
	 * 
	 * @throws BatchControllerException se não conseguir processar o batch
	 * @throws SendMessageException     se não conseguir enviar a mensagem
	 * @throws FileNotFoundException    se não conseguir abrir o arquivo de
	 *                                  destinatários
	 */
	public int process() throws BatchControllerException, SendMessageException, IOException;
}
