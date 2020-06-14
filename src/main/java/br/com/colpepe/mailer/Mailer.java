package br.com.colpepe.mailer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.colpepe.mailer.controller.BatchController;
import br.com.colpepe.mailer.controller.impl.BatchControllerImpl;
import br.com.colpepe.mailer.exception.MailerNoCredentialsException;
import br.com.colpepe.mailer.exception.MailerNoEmailFileNameException;
import br.com.colpepe.mailer.exception.MailerNoFromException;
import br.com.colpepe.mailer.exception.MailerNoMaxRecipientsMessageException;
import br.com.colpepe.mailer.exception.MailerNoMaxRecipientsTotalException;
import br.com.colpepe.mailer.exception.MailerNoRecipientsFileNameException;
import br.com.colpepe.mailer.exception.MailerNoSubjectException;

/**
 * Classe main, interface com o usuário.
 * 
 * @author skull
 *
 */
public class Mailer {
	private static final String ARG_CONFIG_FILENAME = "config=";
	private static final String ARG_USERNAME = "username=";
	private static final String ARG_PASSWORD = "password=";
	private static final String ARG_TO_MESSAGE_LIMIT = "maxDestinatariosPorMensagem=";
	private static final String ARG_MESSAGE_LIMIT = "maxDestinatariosTotal=";
	private static final String ARG_USE_BCC = "useBBC=";
	private static final String ARG_EMAIL_FILENAME = "emailFilename=";
	private static final String ARG_TO_FILENAME = "recipientListFilename=";
	private static final String ARG_SUBJECT = "subject=";
	private static final String ARG_FROM = "from=";

	private static String username;
	private static String password;
	private static int maxRecipientsPerMessage;
	private static int maxRecipientsTotal;
	private static boolean useBCC;
	private static String emailBodyFileName;
	private static String toFileName;
	private static String subject;
	private static String from;

	private static final String HELP_TEXT = "\n\n### ATENÇÃO AO PARÂMETROS NECESSÁRIOS ###\n\n"
			+ "Nome do usuario 					->	" + ARG_USERNAME + "\n" + "Senha							->	"
			+ ARG_PASSWORD + "\n" + "Limite de destinatários por mensagem			->	" + ARG_TO_MESSAGE_LIMIT + "\n"
			+ "Limite de destinatários para enviar			->	" + ARG_MESSAGE_LIMIT + "\n"
			+ "Ocultar destinatários (BCC)				->	" + ARG_USE_BCC + "\n"
			+ "Arquivo contendo o corpo do e-mail			->	" + ARG_EMAIL_FILENAME + "\n"
			+ "Arquivo contendo a lista de destinatários 		->	" + ARG_TO_FILENAME + "\n"
			+ "Assunto							->	" + ARG_SUBJECT + "\n" + "Remetente						-> 	"
			+ ARG_FROM + "\n\n---> Para carregar as configurações através de arquivo  -> 	" + ARG_CONFIG_FILENAME + "\n";

	static final Logger logger = Logger.getLogger(Mailer.class.getClass().getName());

	public static void main(String[] args) throws Exception {
		BatchController controller = new BatchControllerImpl();

		logger.info("Lendo parametros informados");

		try {
			updateParams(args);

			checkParams();

			controller.setCredentials(username, password);
			controller.setMaxRecipientsPerMessage(maxRecipientsPerMessage);
			controller.setMaxRecipients(maxRecipientsTotal);
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
			logger.log(Level.SEVERE, "Problemas ao processar mensagens", e);
		}

		logger.info("Processamento finalizado");

	}

	/**
	 * Verifica se os parâmetros foram passados adequadamente.
	 * 
	 * @throws MailerNoCredentialsException          caso não sejam passadas as
	 *                                               credenciais de acesso ao
	 *                                               servidor
	 * @throws MailerNoMaxRecipientsMessageException caso não seja passado o número
	 *                                               máximo de destinatários por
	 *                                               mensagem
	 * @throws MailerNoMaxRecipientsTotalException   caso não seja informado o
	 *                                               limite de destinatários total a
	 *                                               enviar
	 * @throws MailerNoEmailFileNameException        caso não seja informado o nome
	 *                                               do arquivo contendo o corpo do
	 *                                               e-mail
	 * @throws MailerNoRecipientsFileNameException   caso não seja informado o nome
	 *                                               do arquivo contendo a lista de
	 *                                               destinatários
	 * @throws MailerNoSubjectException              caso não seja informado o
	 *                                               assunto da mensagem
	 * @throws MailerNoFromException                 caso não seja informado um
	 *                                               remetente
	 */
	private static void checkParams() throws MailerNoCredentialsException, MailerNoMaxRecipientsMessageException,
			MailerNoMaxRecipientsTotalException, MailerNoEmailFileNameException, MailerNoRecipientsFileNameException,
			MailerNoSubjectException, MailerNoFromException {

		if (null == username) {
			throw new MailerNoCredentialsException("Nenhum usuário foi informado");
		}

		if (null == password) {
			throw new MailerNoCredentialsException("Nenhuma senha foi informada");
		}

		if (0 == maxRecipientsPerMessage) {
			throw new MailerNoMaxRecipientsMessageException();
		}

		if (0 == maxRecipientsTotal) {
			throw new MailerNoMaxRecipientsTotalException();
		}

		if (null == emailBodyFileName) {
			throw new MailerNoEmailFileNameException();
		}

		if (null == toFileName) {
			throw new MailerNoRecipientsFileNameException();
		}

		if (null == subject) {
			throw new MailerNoSubjectException();
		}

		if (null == from) {
			throw new MailerNoFromException();
		}
	}

	/**
	 * Atualiza parametros para execução.
	 * 
	 * @param args parâmetros enviados pelo usuário
	 * @throws IOException
	 */
	private static void updateParams(String[] args) throws IOException {
		for (int i = 0; i < args.length; i++) {
			String param = args[i];

			if (param.indexOf('=') >= 0) {
				setParam(param, param.substring(param.indexOf('=') + 1));
			}
		}
	}

	/**
	 * Lê a configuração dos parâmetros a partir de um arquivo.
	 * 
	 * @throws IOException
	 */
	private static void readConfigurationFromFile(String fileName) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			String param;

			while ((param = reader.readLine()) != null) {
				setParam(param, param.substring(param.indexOf('=') + 1));
			}
		}
	}

	/**
	 * Configura um parametro da aplicação
	 * 
	 * @param param parâmetro
	 * 
	 * @throws IOException caso informado um parâmetro para configuração via arquivo
	 *                     e o arquivo não for encontrado
	 */
	private static void setParam(String param, String value) throws IOException {
		if (param.startsWith(ARG_CONFIG_FILENAME)) {

			readConfigurationFromFile(value);

			// se informado um arquivo de configuração, as demais configurações passada via
			// parâmetro pelo usuário são sobrescritas
			return;
		}

		if (param.startsWith(ARG_USERNAME)) {
			username = value;
		}

		if (param.startsWith(ARG_PASSWORD)) {
			password = value;
		}

		if (param.startsWith(ARG_TO_MESSAGE_LIMIT)) {
			maxRecipientsPerMessage = Integer.valueOf(value);
		}

		if (param.startsWith(ARG_MESSAGE_LIMIT)) {
			maxRecipientsTotal = Integer.valueOf(value);
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
