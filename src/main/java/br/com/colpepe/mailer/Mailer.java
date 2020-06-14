package br.com.colpepe.mailer;

import java.io.IOException;
import java.util.logging.Logger;

import br.com.colpepe.mailer.controller.BatchController;
import br.com.colpepe.mailer.controller.exception.BatchControllerException;
import br.com.colpepe.mailer.controller.impl.BatchControllerImpl;
import br.com.colpepe.mailer.exception.MailerNoCredentialsException;
import br.com.colpepe.mailer.exception.MailerNoEmailFileNameException;
import br.com.colpepe.mailer.exception.MailerNoMaxPerMessageException;
import br.com.colpepe.mailer.exception.MailerNoSendLimitException;
import br.com.colpepe.mailer.exception.MailerNoToFileNameException;
import br.com.colpepe.mailer.service.smtp.exception.SendMessageException;

/**
 * Classe main, interface com o usuário.
 * 
 * @author skull
 *
 */
public class Mailer {
	private static final String ARG_USERNAME = "username=";
	private static final String ARG_PASSWORD = "password=";
	private static final String ARG_TO_MESSAGE_LIMIT = "maxDestinatarios=";
	private static final String ARG_MESSAGE_LIMIT = "maxMensagens=";
	private static final String ARG_USE_BCC = "useBBC=";
	private static final String ARG_EMAIL_FILENAME = "emailFilename=";
	private static final String ARG_TO_FILENAME = "toFilename=";
	private static final String ARG_SUBJECT = "subject=";
	private static final String ARG_FROM = "from=";

	private static String username;
	private static String password;
	private static int maxDestinationPerMessage;
	private static int sendLimit;
	private static boolean useBCC;
	private static String emailBodyFileName;
	private static String toFileName;
	private static String subject;
	private static String from;

	private static final String HELP_TEXT = "\n\n### ATENÇÃO AO PARÂMETROS NECESSÁRIOS ###\n\n"
			+ "Nome do usuario 					->	" + ARG_USERNAME + "\n" + "Senha							->	"
			+ ARG_PASSWORD + "\n" + "Limite de destinatários por mensagem			->	" + ARG_TO_MESSAGE_LIMIT + "\n"
			+ "Limite de mensagens para enviar				->	" + ARG_MESSAGE_LIMIT + "\n"
			+ "Ocultar destinatários (BCC)				->	" + ARG_USE_BCC + "\n"
			+ "Arquivo contendo o corpo do e-mail			->	" + ARG_EMAIL_FILENAME + "\n"
			+ "Arquivo contendo a lista de destinatários 		->	" + ARG_TO_FILENAME + "\n"
			+ "Assunto							->	" + ARG_SUBJECT + "\n" + "Remetente						-> 	"
			+ ARG_FROM;

	static final Logger logger = Logger.getLogger(Mailer.class.getClass().getName());

	public static void main(String[] args) throws IOException, BatchControllerException, SendMessageException {
		BatchController controller = new BatchControllerImpl();

		logger.info("Lendo parametros informados");

		try {
			updateParams(args);

			checkParams();

			controller.setCredentials(username, password);
			controller.setMaxToPerMessage(maxDestinationPerMessage);
			controller.setSendLimit(sendLimit);
			controller.setUseBCC(useBCC);

			controller.setMessageSubject(subject);
			controller.setFrom(from);
			controller.setMessageBodyFromFilename(emailBodyFileName);
			controller.setToListFilename(toFileName);

			logger.info("Iniciando processamento");

			int messagesSent = controller.process();

			String msg = String.format("Mensagens enviadas: %d", messagesSent);

			logger.info(msg);
		} catch (Exception e) {
			logger.info(HELP_TEXT);
			logger.info("Problemas ao iniciar processamento: ".concat(e.getMessage()));
		}

		logger.info("Processamento finalizado");

	}

	/**
	 * Verifica se os parâmetros foram passados adequadamente.
	 * 
	 * @throws MailerNoCredentialsException   caso não sejam passadas as
	 *                                               credenciais de acesso ao
	 *                                               servidor
	 * @throws MailerNoMaxPerMessageException caso nnao seja passado o número
	 *                                               máximo de destinatários por
	 *                                               mensagem
	 * @throws MailerNoSendLimitException     caso não seja informado o
	 *                                               limite de mensagens a enviar
	 * @throws MailerNoEmailFileNameException caso não seja informado o nome
	 *                                               do arquivo contendo o corpo do
	 *                                               e-mail
	 * @throws MailerNoToFileNameException             caso não seja informado o nome
	 *                                               do arquivo contendo a lista de
	 *                                               destinatários
	 */
	private static void checkParams() throws MailerNoCredentialsException, MailerNoMaxPerMessageException,
			MailerNoSendLimitException, MailerNoEmailFileNameException, MailerNoToFileNameException {
		if (null == username) {
			throw new MailerNoCredentialsException("Nenhum usuário foi informado");
		}

		if (null == password) {
			throw new MailerNoCredentialsException("Nenhuma senha foi informada");
		}

		if (0 == maxDestinationPerMessage) {
			throw new MailerNoMaxPerMessageException();
		}

		if (0 == sendLimit) {
			throw new MailerNoSendLimitException();
		}

		if (null == emailBodyFileName) {
			throw new MailerNoEmailFileNameException();
		}

		if (null == toFileName) {
			throw new MailerNoToFileNameException();
		}


//		private static String subject;
//		private static String from;

	}

	/**
	 * Atualiza parametros para execução.
	 * 
	 * @param args parâmetros enviados pelo usuário
	 */
	private static void updateParams(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String param = args[i];

			if (param.indexOf('=') >= 0) {
				setParam(param, param.substring(param.indexOf('=') + 1));
			}
		}
	}

	/**
	 * Configura um parametro da aplicação
	 * 
	 * @param param parâmetro
	 */
	private static void setParam(String param, String value) {
		if (param.startsWith(ARG_USERNAME)) {
			username = value;
		}

		if (param.startsWith(ARG_PASSWORD)) {
			password = value;
		}

		if (param.startsWith(ARG_TO_MESSAGE_LIMIT)) {
			maxDestinationPerMessage = Integer.valueOf(value);
		}

		if (param.startsWith(ARG_MESSAGE_LIMIT)) {
			sendLimit = Integer.valueOf(value);
		}

		if (param.startsWith(ARG_USE_BCC)) {
			useBCC = Boolean.valueOf(value);
		}

		if (param.startsWith(ARG_EMAIL_FILENAME)) {
			emailBodyFileName = value;
		}

		if (param.startsWith(ARG_TO_FILENAME)) {
			toFileName = value;
		}

		if (param.startsWith(ARG_SUBJECT)) {
			subject = value;
		}

		if (param.startsWith(ARG_FROM)) {
			from = value;
		}
	}

}
