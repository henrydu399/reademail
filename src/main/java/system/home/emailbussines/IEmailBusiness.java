package system.home.emailbussines;

import java.util.List;

import javax.mail.Message;

public interface IEmailBusiness {
	
	List<Message>  getMensagesEmail(String date);
	void 		sendEmail();

}
