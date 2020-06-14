package br.com.colpepe.mailer.service.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.colpepe.mailer.service.smtp.SendMessageService;
import br.com.colpepe.mailer.service.smtp.exception.SendMessageException;
import br.com.colpepe.mailer.service.smtp.impl.SendMessageServiceImpl;
import br.com.colpepe.mailer.service.smtp.vo.MessageVO;

public class SendMessageServiceImplTest {

	@Test
	public void testSendMessage() throws SendMessageException {
		String username = "fumato@gmail.com";
		String password = "xxxx";

		MessageVO mensagem = new MessageVO();
		mensagem.setFrom("fumato@gmail.com");
		mensagem.setTo("carlos.feitosa.nt@gmail.com");
		mensagem.setCc("saviouchoa@gmail.com, sabrinasabino@gmail.com");
		mensagem.setSubject("teste do robô");
		mensagem.setBody("testando o robo, conexão com o gmail... levante e ande!!!! auuuuuu");

		SendMessageService sender = new SendMessageServiceImpl();
		sender.setMessage(mensagem);
		sender.setAccessCredentials(username, password);

		assertTrue("Mensagem não enviada", sender.sendMessage());
	}

}
