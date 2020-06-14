package br.com.colpepe.mailer.service.smtp.impl;

import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import br.com.colpepe.mailer.service.smtp.SendMessageService;
import br.com.colpepe.mailer.service.smtp.exception.SendMessageException;
import br.com.colpepe.mailer.service.smtp.exception.SendMessageMessageException;
import br.com.colpepe.mailer.service.smtp.exception.SendMessageNoCredentialsException;
import br.com.colpepe.mailer.service.smtp.exception.SendMessageNoMessageException;
import br.com.colpepe.mailer.service.smtp.vo.MessageVO;

/**
 * Classe concreta que implementa o serviço de envio de mensagem.
 * 
 * @author skull
 *
 */
public class SendMessageServiceImpl implements SendMessageService {
	private static final String MSG_SEM_FROM = "Mensagem sem remetente";
	private static final String MSG_SEM_TO = "Mensagem sem destinatário(s)";
	private static final String MSG_SEM_SUBJECT = "Mensagem sem assunto";
	private static final String MSG_SEM_BODY = "Mensagem sem corpo";

	private static final String MAIL_PROP_HOST = "mail.smtp.host";
	private static final String MAIL_PROP_HOST_VALUE = "smtp.gmail.com";

	private static final String MAIL_PROP_PORT = "mail.smtp.port";
	private static final String MAIL_PROP_PORT_VALUE = "465";

	private static final String MAIL_PROP_SMTP_AUTH = "mail.smtp.auth";
	private static final String MAIL_PROP_SMTP_AUTH_VALUE = "true";

	private static final String MAIL_PROP_SMTP_FACTORY_PORT = "mail.smtp.socketFactory.port";

	private static final String MAIL_PROP_FACTORY = "mail.smtp.socketFactory.class";
	private static final String MAIL_PROP_FACTORY_CLASS = "javax.net.ssl.SSLSocketFactory";

	private static final String MAIL_PROP_FACTORY_HOST_VERIFICATION = "mail.smtp.ssl.checkserveridentity";
	private static final boolean MAIL_PROP_FACTORY_HOST_VERIFICATION_VALUE = true;

	private static final String MAIL_CONTENT_TYPE = "text/html";

	static final Logger logger = Logger.getLogger(SendMessageServiceImpl.class.getClass().getName());

	String username;
	String password;
	MessageVO mensagem;
	Session session;

	public SendMessageServiceImpl() {
		super();
	}

	/**
	 * Retorna parametros para conexão com o servidor de envio de mensagens.
	 * 
	 * @return configurações
	 */
	private Properties getProperties() {
		logger.info("Recuperando configurações");

		Properties prop = new Properties();
		prop.put(MAIL_PROP_HOST, MAIL_PROP_HOST_VALUE);
		prop.put(MAIL_PROP_PORT, MAIL_PROP_PORT_VALUE);
		prop.put(MAIL_PROP_SMTP_AUTH, MAIL_PROP_SMTP_AUTH_VALUE);
		prop.put(MAIL_PROP_SMTP_FACTORY_PORT, MAIL_PROP_PORT_VALUE);
		prop.put(MAIL_PROP_FACTORY, MAIL_PROP_FACTORY_CLASS);
		prop.put(MAIL_PROP_FACTORY_HOST_VERIFICATION, MAIL_PROP_FACTORY_HOST_VERIFICATION_VALUE);

		return prop;
	}

	@Override
	public void setMessage(MessageVO mensagem) {
		logger.info("Recebendo mensagem");
		String msg = String.format("Remetente: %s", mensagem.getFrom());
		logger.info(msg);
		msg = String.format("Destinatário(s): %s", mensagem.getTo());
		logger.info(msg);
		msg = String.format("Assunto: %s", mensagem.getSubject());
		logger.info(msg);

		this.mensagem = mensagem;

	}

	@Override
	public void setAccessCredentials(String username, String password) {
		logger.info("Recebendo credenciamento");
		String msg = String.format("Usuário: %s", username);
		logger.info(msg);

		this.username = username;
		this.password = password;
	}

	@Override
	public boolean sendMessage() throws SendMessageException {
		logger.info("Preparando envio de mensagem");

		checkPrereq();

		initSession(getProperties(), false);

		try {
			logger.info("Preparando mensagem para envio");

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(mensagem.getFrom()));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mensagem.getTo()));

			if (null != mensagem.getCc()) {
				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(mensagem.getCc()));
			}
			if (null != mensagem.getBcc()) {
				message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(mensagem.getBcc()));
			}

			message.setSubject(mensagem.getSubject());
			message.setContent(mensagem.getBody(), MAIL_CONTENT_TYPE);

			logger.info("Enviando mensagem");
			Transport.send(message);

			logger.info("Mensagem enviada");

			return true;

		} catch (MessagingException e) {
			throw new SendMessageException(e.getMessage());
		}
	}

	/**
	 * Verifica se os pre-requisitos foram atentidos para o envio da mensagem.
	 * 
	 * @throws SendMessageException caso os pré requisitos não sejam atendidos
	 */
	private void checkPrereq() throws SendMessageException {
		logger.info("Checando pré requisitos");

		if (null == username || null == password) {
			throw new SendMessageNoCredentialsException();
		}

		if (null == mensagem) {
			throw new SendMessageNoMessageException();
		} else {
			if (null == mensagem.getFrom()) {
				throw new SendMessageMessageException(MSG_SEM_FROM);
			} else if (null == mensagem.getTo()) {
				throw new SendMessageMessageException(MSG_SEM_TO);
			} else if (null == mensagem.getSubject()) {
				throw new SendMessageMessageException(MSG_SEM_SUBJECT);
			} else if (null == mensagem.getBody()) {
				throw new SendMessageMessageException(MSG_SEM_BODY);
			}
		}
	}

	/**
	 * Inicializa a sessão do javamail.
	 * 
	 * @param config       properties com configurações
	 * @param factoryClass classe para factory
	 * @param resetState   reseta o estado do javamail (sobrescreve as
	 *                     configurações)
	 */
	private void initSession(Properties config, boolean resetState) {
		logger.info("Preparando conexão");

		if (null == session || resetState) {

			session = Session.getInstance(config, new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		}
	}

}
