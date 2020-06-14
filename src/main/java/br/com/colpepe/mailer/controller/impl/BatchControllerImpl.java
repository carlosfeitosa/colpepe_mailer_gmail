package br.com.colpepe.mailer.controller.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import br.com.colpepe.mailer.controller.BatchController;
import br.com.colpepe.mailer.controller.exception.BatchControllerException;
import br.com.colpepe.mailer.controller.exception.BatchControllerNoCredentialsException;
import br.com.colpepe.mailer.controller.exception.BatchControllerNoFromException;
import br.com.colpepe.mailer.controller.exception.BatchControllerNoListFilename;
import br.com.colpepe.mailer.controller.exception.BatchControllerNoMaxPerMessageException;
import br.com.colpepe.mailer.controller.exception.BatchControllerNoMessageBodyController;
import br.com.colpepe.mailer.controller.exception.BatchControllerNoSendLimitException;
import br.com.colpepe.mailer.controller.exception.BatchControllerToListFileNotFound;
import br.com.colpepe.mailer.service.smtp.SendMessageService;
import br.com.colpepe.mailer.service.smtp.exception.SendMessageException;
import br.com.colpepe.mailer.service.smtp.impl.SendMessageServiceImpl;
import br.com.colpepe.mailer.service.smtp.vo.MessageVO;

/**
 * Classe concreta que implementa a interface batch controller
 * 
 * @author skull
 *
 */
public class BatchControllerImpl implements BatchController {

	private String username;
	private String password;
	private boolean useBcc = false;
	private int maxToPerMessage;
	private int sendLimit;
	private String from;

	private String messageSubject;
	private String messageBody;
	private String toListFilename;

	private int lastProcessed;

	private SendMessageService service;

	static final Logger logger = Logger.getLogger(BatchControllerImpl.class.getClass().getName());

	public BatchControllerImpl() {
		super();

		service = new SendMessageServiceImpl();

		maxToPerMessage = 0;
		sendLimit = 0;

		lastProcessed = 0;
	}

	@Override
	public void setCredentials(String username, String password) {
		logger.info("Parametros de credenciamento recebidos");
		String msg = String.format("Nome de usuário: %s", username);
		logger.info(msg);

		this.username = username;
		this.password = password;
	}

	@Override
	public void setUseBCC(boolean useBCC) {
		if (useBCC) {
			logger.info("Recebido parâmetro de BCC");
		}

		this.useBcc = useBCC;
	}

	@Override
	public void setMaxToPerMessage(int maxToPerMessage) {
		logger.info("Recebido quantidade máxima de destinatários por mensagem");
		String msg = String.format("Quantidade máxima de destinatários por mensagem: %d", maxToPerMessage);
		logger.info(msg);

		this.maxToPerMessage = maxToPerMessage;
	}

	@Override
	public void setSendLimit(int sendLimit) {
		logger.info("Recebido quantidade máxima de envios");
		String msg = String.format("Quantidade máxima de mensagens enviadas: %d", sendLimit);
		logger.info(msg);

		this.sendLimit = sendLimit;
	}

	@Override
	public void setFrom(String from) {
		logger.info("Recebido remetente");
		String msg = String.format("Remetente: %s", from);
		logger.info(msg);

		this.from = from;
	}

	@Override
	public void setMessageSubject(String subject) {
		this.messageSubject = subject;

	}

	@Override
	public void setMessageBodyFromFilename(String filename) throws IOException {
		logger.info("Carregando conteúdo de arquivo como corpo de mensagem");
		String msg = String.format("Nome do arquivo usado como corpo da mensagem: %s", filename);
		logger.info(msg);

		messageBody = new String(Files.readAllBytes(Paths.get(filename)));
	}

	@Override
	public void setToListFilename(String filename) {
		logger.info("Carregando nome do arquivo contendo a lista dos destinatários");
		String msg = String.format("Nome do arquivo usado como lista: %s", filename);
		logger.info(msg);

		this.toListFilename = filename;
	}

	@Override
	public int process() throws BatchControllerException, SendMessageException, IOException {
		checkPrereq();

		logger.info("Carregando lista de destinatários");

		lastProcessed = 0;

		logger.info("Iniciando o processamento batch");
		logger.info("Verificando quantidade de destinatários");

		String lineSeparator = System.getProperty("line.separator");

		int totalLines = getFileTotalLines(lineSeparator);

		String msg = String.format("Total de linhas no arquivo de destinatários: %s", totalLines);
		logger.info(msg);

		List<String> lista = new ArrayList<>();

		MessageVO mensagem = getPreparedMessage();
		service.setAccessCredentials(username, password);

		String destinatarios;
		String line;

		int lineNumber = 0;
		int messageCount = 0;

		try (BufferedReader reader = new BufferedReader(new FileReader(toListFilename))) {
			while ((line = reader.readLine()) != null) {

				if (maxToPerMessage == lista.size()) {
					destinatarios = String.join(",", lista);

					sendMessage(mensagem, destinatarios);
					messageCount++;

					writeProcessedLog(String.join(lineSeparator, lista), messageCount);

					lista.clear();

				} else if (lineNumber == sendLimit) {

					break;
				}

				lista.add(line);

				lineNumber++;
			}
		}

		if (!lista.isEmpty()) {
			destinatarios = String.join(",", lista);

			sendMessage(mensagem, destinatarios);
			messageCount++;

			writeProcessedLog(String.join(lineSeparator, lista), messageCount);
		}

		lastProcessed = lineNumber;

		msg = String.format("Total de linhas processadas: %d", lastProcessed);
		logger.info(msg);
		msg = String.format("Total de mensagens enviadas: %d", messageCount);
		logger.info(msg);

		return lastProcessed;
	}

	/**
	 * Escreve o log dos destinatários processados.
	 * 
	 * @param log log que será salvo no arquivo
	 * 
	 * @throws IOException
	 */
	private void writeProcessedLog(String log, int messageNumber) throws IOException {
		logger.info("Preparando para salvar log de processamento");
		String msg = String.format("Mensagem #%d", messageNumber);
		logger.info(msg);

		SimpleDateFormat sdff = new SimpleDateFormat("yyyyMMddHHmmss");
		String today = (sdff.format(new Date()));
		String filename = String.format("%s_processed_%s_%d.txt", toListFilename, today, messageNumber);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			writer.write(log);
		}
	}

	/**
	 * Envia a mensagem.
	 * 
	 * @param mensagem      mensagem a ser enviada
	 * @param destinatarios destinatários da mensagem
	 * 
	 * @throws SendMessageException caso tenha problemas ao enviar a mensagem.
	 */
	private void sendMessage(MessageVO mensagem, String destinatarios) throws SendMessageException {
		if (useBcc) {
			mensagem.setBcc(destinatarios);
		} else {
			mensagem.setCc(destinatarios);
		}

		service.setMessage(mensagem);

		service.sendMessage();

	}

	/**
	 * Retorna mensagem montada.
	 * 
	 * @return mensagem montada
	 */
	private MessageVO getPreparedMessage() {
		MessageVO mensagem = new MessageVO();
		mensagem.setFrom(from);
		mensagem.setTo(from);
		mensagem.setSubject(messageSubject);
		mensagem.setBody(messageBody);

		return mensagem;
	}

	/**
	 * Retorna o total de linhas do arquivo de destinatários.
	 * 
	 * @return total de linhas
	 * 
	 * @throws IOException caso hajam problemas na leitua do arquivo
	 */
	private int getFileTotalLines(String lineSeparator) throws IOException {
		File file = new File(toListFilename);

		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] byteArray = new byte[(int) file.length()];

			if (fis.read(byteArray) > 0) {

				String data = new String(byteArray);
				String[] stringArray = data.split(lineSeparator); // ex: "\r\n"

				return stringArray.length;
			}
		}

		return 0;
	}

	/**
	 * Verifica se os pre-requisitos foram atentidos para execução do batch.
	 * 
	 * @throws BatchControllerException caso os pré requisitos não sejam atendidos
	 */
	private void checkPrereq() throws BatchControllerException {
		logger.info("Checando pré requisitos");

		if (null == username || null == password) {
			throw new BatchControllerNoCredentialsException();
		}

		if (0 == maxToPerMessage) {
			throw new BatchControllerNoMaxPerMessageException();
		}

		if (0 == sendLimit) {
			throw new BatchControllerNoSendLimitException();
		}

		if (null == from) {
			throw new BatchControllerNoFromException();
		}

		if (null == messageBody) {
			throw new BatchControllerNoMessageBodyController();
		}

		if (null == toListFilename) {
			throw new BatchControllerNoListFilename();
		} else {
			File arquivo = new File(toListFilename);

			if (!arquivo.exists()) {
				throw new BatchControllerToListFileNotFound();
			}
		}
	}

}
