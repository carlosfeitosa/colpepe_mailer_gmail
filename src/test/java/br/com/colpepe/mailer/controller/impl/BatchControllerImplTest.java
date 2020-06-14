package br.com.colpepe.mailer.controller.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import br.com.colpepe.mailer.controller.BatchController;
import br.com.colpepe.mailer.controller.exception.BatchControllerException;
import br.com.colpepe.mailer.service.smtp.exception.SendMessageException;

public class BatchControllerImplTest {

	@Test
	public void testProcess() throws BatchControllerException, SendMessageException, IOException {
		BatchController controller = new BatchControllerImpl();

		String username = "fumato@gmail.com";
		String password = "xxx";
		String emailBodyFileName = "/Users/skull/Desktop/colpepe/email.html";
		String toFileName = "/Users/skull/Desktop/colpepe/lista.txt";

		controller.setCredentials(username, password);
		controller.setMaxToPerMessage(3);
		controller.setSendLimit(11);
		controller.setUseBCC(false);

		controller.setMessageSubject("teste robô COLPEPE");
		controller.setFrom("fumato snaidefight <fumato@gmail.com>");
		controller.setMessageBodyFromFilename(emailBodyFileName);
		controller.setToListFilename(toFileName);

		assertTrue("Quantidade de mensagens enviadas deve ser maior que zero", controller.process() > 0);
	}

}
