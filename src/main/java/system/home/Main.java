package system.home;

import java.util.List;

import javax.mail.Message;

import system.home.emailbussines.EmailBusinessImpl;
import system.home.emailbussines.IEmailBusiness;

public class Main {

	public static void main(String[] args) {
	
		IEmailBusiness IEmailBusiness = new EmailBusinessImpl();
		List<Message>  listMens = IEmailBusiness.getMensagesEmail("2021-08-26");
		
		

	}

}
