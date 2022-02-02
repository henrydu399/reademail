package system.home.emailbussines;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import system.home.enums.PropertiesKeyEnum;
import system.home.utils.EmailUtils;
import system.home.utils.PropertiesUtil;

public class EmailBusinessImpl implements IEmailBusiness {

	Folder folder;
	Store store;
	
	private Message[]  readEmail( ) {

		Message[] messages =null;
		
		try {
		Properties propertiesLocal = PropertiesUtil.read();
		// Crear objeto de instancia de sesión
		Session session = Session.getInstance(this.getPropertiesRecived(propertiesLocal));
		 store = session.getStore("pop3");
		 store.connect(
				 propertiesLocal.get( PropertiesKeyEnum.user.name() ).toString(), 
				 propertiesLocal.get( PropertiesKeyEnum.password.name() ).toString() 
				 );
		// obtener bandeja de entrada
		 folder = store.getFolder("INBOX");
		/* Folder.READ_ONLY: permiso de solo lectura
		 * Carpeta.READ_WRITE: legible y editable (puede modificar el estado del correo)
		 */
		folder.open (Folder.READ_ONLY); // Abra la bandeja de entrada

		// Debido a que el protocolo POP3 no puede conocer el estado del correo, getUnreadMessageCount obtiene la cantidad total de correo en la bandeja de entrada
		//System.out.println ("Número de mensajes no leídos:" + folder.getUnreadMessageCount ());

		// Debido a que el protocolo POP3 no puede conocer el estado del correo electrónico, el resultado obtenido a continuación es siempre 0
		//System.out.println ("Número de mensajes eliminados:" + folder.getDeletedMessageCount ());
		//System.out.println ("Correo nuevo:" + folder.getNewMessageCount ());

		// Obtenga el número total de mensajes en la bandeja de entrada
		//System.out.println ("Número total de mensajes:" + folder.getMessageCount ());

		// Obtenga todos los correos electrónicos en la bandeja de entrada y analícelos
		 messages = folder.getMessages();
		 
			//EmailUtils.parseMessage(messages);
			
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return messages;
		

	}

	@Override
	public void sendEmail() {

		Properties propertiesLocal = PropertiesUtil.read();
		
		Session session = Session.getInstance(this.getPropertiesSend(propertiesLocal),
			      new javax.mail.Authenticator() {
			        @Override
			        protected PasswordAuthentication getPasswordAuthentication() {
			            return new PasswordAuthentication(
			            		(String) propertiesLocal.get(PropertiesKeyEnum.user.name())  ,
			            		(String) propertiesLocal.get(PropertiesKeyEnum.password.name())
			            		);
			        }
			      });
			    session.setDebug(true);

			    try {

			        Message message = new MimeMessage(session);
			        message.setFrom(new InternetAddress((String) propertiesLocal.get(PropertiesKeyEnum.user.name())));
			        message.setRecipients(Message.RecipientType.TO,
			            InternetAddress.parse("jfop_2@hotmail.com"));   // like inzi769@gmail.com
			        message.setSubject("saludo");
			        message.setText("hola joan desde el app");

			        Transport.send(message);

			        System.out.println("Done");

			    } catch (MessagingException e) {
			        throw new RuntimeException(e);
			    }
		
	}

	private Properties getPropertiesSend(Properties propertiesLocal) {
		Properties props = new Properties();
		props.put("mail.smtp.auth", propertiesLocal.get(PropertiesKeyEnum.requiereauth.name())  );
		props.put("mail.smtp.starttls.enable", propertiesLocal.get(PropertiesKeyEnum.useTls.name()));
		props.put("mail.smtp.host", propertiesLocal.get(PropertiesKeyEnum.smtpemail.name()));
		props.put("mail.smtp.port", propertiesLocal.get(PropertiesKeyEnum.port.name()));
		return props;
	}
	
	private Properties getPropertiesRecived(Properties propertiesLocal) {
		Properties props = new Properties();
		props.setProperty ("mail.pop3.starttls.enable", propertiesLocal.get(PropertiesKeyEnum.useTls.name()).toString()  );
		//props.setProperty ("mail.store.protocol", propertiesLocal.get(PropertiesKeyEnum.protocol.name()).toString() ); // protocolo
		props.setProperty ("mail.pop3.port", propertiesLocal.get(PropertiesKeyEnum.portRecived.name()).toString() ); // puerto
		props.setProperty ("mail.pop3.host",  propertiesLocal.get(PropertiesKeyEnum.popemail.name()).toString() ); // servidor pop3
		props.setProperty("mail.pop3.auth", "true"); 
		props.setProperty("mail.debug.auth", "true");
		return props;
	}

	
	public List<Message> getMensagesEmail(String date) {

		List<Message> temp = new ArrayList<Message>();
		try {	
			Message[] mensages = this.readEmail();
			
			if( date == null) {
				temp = Arrays.asList(mensages);
				return temp;
			}
				
		
			if( date != null && !date.isEmpty() ) {
				for (Message mensaje : mensages ) {
					MimeMessage msg = (MimeMessage) mensaje;
					String fechaEnvio;
					try {
						fechaEnvio = EmailUtils.getSentDateYYYYMMDD(msg, null);
						if(  fechaEnvio.equals(mensages)) {
							temp.add(msg);
						}
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				

				}
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			
			try {
				this.folder.close(true);
				this.store.close();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return temp;
	}



}
