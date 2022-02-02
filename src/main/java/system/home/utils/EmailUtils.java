package system.home.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;


public class EmailUtils {

	/**
	 * Correo Parse
	 * @param messages La lista de correo a analizar
	 */
	public static void parseMessage(Message ...messages) throws MessagingException, IOException {
		if (messages == null || messages.length < 1) 
			throw new MessagingException("ERROR NO HAY MENSAJES NUEVOS");
		int length=messages.length;
		
       
		// Analiza todos los mensajes
		for (int i = 0;  i < messages.length  ; i++) {
			MimeMessage msg = (MimeMessage) messages[i];
			System.out.println ("------------------ sección de análisis" + msg.getMessageNumber () + "mail ---------------- -------- ");
			System.out.println ("Asunto:" + getSubject (msg));
			System.out.println ("De:" + getFrom (msg));
			System.out.println ("Destinatario:" + getReceiveAddress (msg, null));
			System.out.println ("Tiempo de envío:" + getSentDate (msg, null));
			System.out.println ("¿Has leído:" + isSeen (msg));
			System.out.println ("Prioridad de correo:" + getPriority (msg));
			System.out.println ("¿Necesita un acuse de recibo:" + isReplySign (msg));
			System.out.println ("Tamaño del correo:" + msg.getSize () * 1024 + "kb");
			boolean isContainerAttachment = isContainAttachment(msg);
			System.out.println ("¿Contiene archivos adjuntos:" + isContainerAttachment);
			if (isContainerAttachment) {
				saveAttachment (msg, "c: \\ mailtmp \\" + msg.getSubject () + "_"); // Guardar adjunto
			} 
			StringBuffer content = new StringBuffer(30);
			getMailTextContent(msg, content);
			System.out.println ("cuerpo del correo:" + (content.length ()> 100? content.substring(0,100) + "...": content));
			System.out.println ("------------------  " + msg.getMessageNumber () + "Fin del análisis del mensaje ------------- --------- ");
			System.out.println();
		}
	}
	
	/**
	 * Obtener asunto del correo electrónico
	 * @param msg Contenido de correo electrónico
	 * Asunto del mensaje decodificado de retorno
	 */
	private static  String getSubject(MimeMessage msg) throws UnsupportedEncodingException, MessagingException {
		return MimeUtility.decodeText(msg.getSubject());
	}

	/**
	 * Obtener remitente de correo electrónico
	 * @param msg Contenido de correo electrónico
	 * @ nombre de devolución <dirección de correo electrónico>
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException 
	 */
	private static String getFrom(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
		String from = "";  
        Address[] froms = msg.getFrom();  
        if (froms.length < 1)  
            throw new MessagingException("没有发件人!");  
          
        InternetAddress address = (InternetAddress) froms[0];  
        String person = address.getPersonal();  
        if (person != null) {  
            person = MimeUtility.decodeText(person) + " ";  
        } else {  
            person = "";  
        }  
        from = person + "<" + address.getAddress() + ">";  
          
        return from;  
	}
	
	/**
	 * Según el tipo de destinatario, obtenga el destinatario del correo, las direcciones CC y BCC. Si el tipo de destinatario está vacío, obtenga todos los destinatarios
	 * <p> Message.RecipientType.TO destinatario </ p>
	 * <p> Message.RecipientType.CC CC </ p>
	 * <p> Message.RecipientType.BCC Bcc </ p>
	 * @param msg Contenido de correo electrónico
	 * @param tipo de destinatario
	 * @return destinatario 1 <dirección de correo electrónico 1>, destinatario 2 <dirección de correo electrónico 2>, ...
	 * @throws MessagingException
	 */
	public static String getReceiveAddress(MimeMessage msg, Message.RecipientType type) throws MessagingException {
		StringBuffer receiveAddress = new StringBuffer();
		
		Address[] addresss = null;
		if (type == null) {
			addresss = msg.getAllRecipients();
		} else {
			addresss = msg.getRecipients(type);
		}

		if (addresss == null || addresss.length < 1)
			throw new MessagingException("sin destinatario");
			
		for (Address address : addresss) {
			InternetAddress internetAddress = (InternetAddress)address;
			receiveAddress.append(internetAddress.toUnicodeString()).append(",");
		}

		receiveAddress.deleteCharAt (receiveAddress.length () -1); // Eliminar la última coma

		return receiveAddress.toString();
	}

	/**
	 * Obtenga tiempo de envío de correo electrónico
	 * @param msg Contenido de correo electrónico
	 * @return aaaa mm mm dd día semana X HH: mm
	 * @throws MessagingException
	 */
	public static String getSentDate(MimeMessage msg, String pattern) throws MessagingException {
		Date receivedDate = msg.getSentDate();
		if (receivedDate == null)
			return "";

		if (pattern == null || "".equals(pattern))
			pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
		return new SimpleDateFormat(pattern).format(receivedDate);
	}
	
	/**
	 * Obtenga tiempo de envío de correo electrónico
	 * @param msg Contenido de correo electrónico
	 * @return aaaa mm mm dd día semana X HH: mm
	 * @throws MessagingException
	 */
	public static String getSentDateYYYYMMDD(MimeMessage msg, String pattern) throws MessagingException {
		Date receivedDate = msg.getSentDate();
		if (receivedDate == null)
			return "";

		if (pattern == null || "".equals(pattern))
			pattern = "yyyy-MM-dd";
		return new SimpleDateFormat(pattern).format(receivedDate);
	}

	/**
	 * Determine si el correo electrónico contiene archivos adjuntos
	 * @param msg Contenido de correo electrónico
	 * @return return true si hay un archivo adjunto en el correo electrónico, false si no existe
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static boolean isContainAttachment(Part part) throws MessagingException, IOException {
		boolean flag = false;
		if (part.isMimeType("multipart/*")) {
			MimeMultipart multipart = (MimeMultipart) part.getContent();
			int partCount = multipart.getCount();
			for (int i = 0; i < partCount; i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				String disp = bodyPart.getDisposition();
				if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
					flag = true;
				} else if (bodyPart.isMimeType("multipart/*")) {
					flag = isContainAttachment(bodyPart);
				} else {
					String contentType = bodyPart.getContentType();
					if (contentType.indexOf("application") != -1) {
						flag = true;
					}  

					if (contentType.indexOf("name") != -1) {
						flag = true;
					} 
				}

				if (flag) break;
			}
		} else if (part.isMimeType("message/rfc822")) {
			flag = isContainAttachment((Part)part.getContent());
		}
		return flag;
	}

	/**
	 * Determine si el correo ha sido leído
	 * @param msg Contenido de correo electrónico
	 * @return devuelve verdadero si el mensaje ha sido leído, de lo contrario devuelve falso
	 * @throws MessagingException 
	 */
	public static boolean isSeen(MimeMessage msg) throws MessagingException {
		 return msg.getFlags().contains(Flags.Flag.SEEN);  
	}

	/**
	 * Determine si el correo necesita leer el recibo
	 * @param msg Contenido de correo electrónico
	 * @return requiere acuse de recibo para devolver verdadero, de lo contrario devuelve falso
	 * @throws MessagingException
	 */
	public static boolean isReplySign(MimeMessage msg) throws MessagingException {
		boolean replySign = false;
		String[] headers = msg.getHeader("Disposition-Notification-To");
		if (headers != null)
			replySign = true;
		return replySign;
	}

	/**
	 * Obtener prioridad de correo
	 * @param msg Contenido de correo electrónico
	 * @return 1 (High): Emergency 3: Normal (Normal) 5: Low (Low)
	 * @throws MessagingException 
	 */
	public static String getPriority(MimeMessage msg) throws MessagingException {
		String priority = "Normal";
		String[] headers = msg.getHeader("X-Priority");
		if (headers != null) {
			String headerPriority = headers[0];
			if (headerPriority.indexOf("1") != -1 || headerPriority.indexOf("High") != -1)
				priority = "Urgente";
			else if (headerPriority.indexOf("5") != -1 || headerPriority.indexOf("Low") != -1)
				priority = "Bajo";
			else
				priority = "Normal";
		}
		return priority;
	} 

	/**
	 * Obtener contenido de texto por correo electrónico
	 * @param parte del cuerpo del mensaje
	 * @param content La cadena que almacena el contenido de texto del correo electrónico
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static void getMailTextContent(Part part, StringBuffer content) throws MessagingException, IOException {
		// Si se trata de un archivo adjunto de tipo texto, puede obtener el contenido del texto a través del método getContent, pero este no es el resultado que necesitamos, así que haga un juicio aquí
		boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;	
		if (part.isMimeType("text/*") && !isContainTextAttach) {
			content.append(part.getContent().toString());
		} else if (part.isMimeType("message/rfc822")) {	
			getMailTextContent((Part)part.getContent(),content);
		} else if (part.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) part.getContent();
			int partCount = multipart.getCount();
			for (int i = 0; i < partCount; i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				getMailTextContent(bodyPart,content);
			}
		}
	}

	/**
	 * Guardar adjunto
	 * @param parte Uno de los múltiples ensamblados en el correo electrónico
	 * @param directorio de almacenamiento de archivos adjuntos destDir
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void saveAttachment(Part part, String destDir) throws UnsupportedEncodingException, MessagingException,
			FileNotFoundException, IOException {
		if (part.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) part.getContent (); // correo complejo
			// El correo complejo contiene varios cuerpos de correo
			int partCount = multipart.getCount();
			for (int i = 0; i < partCount; i++) {
				// Obtenga uno de los correos electrónicos en el correo electrónico complejo
				BodyPart bodyPart = multipart.getBodyPart(i);
				// Un determinado cuerpo de correo también puede ser un cuerpo complejo compuesto de múltiples cuerpos de correo
				String disp = bodyPart.getDisposition();
				if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
					InputStream is = bodyPart.getInputStream();
					saveFile(is, destDir, decodeText(bodyPart.getFileName()));
				} else if (bodyPart.isMimeType("multipart/*")) {
					saveAttachment(bodyPart,destDir);
				} else {
					String contentType = bodyPart.getContentType();
					if (contentType.indexOf("name") != -1 || contentType.indexOf("application") != -1) {
						saveFile(bodyPart.getInputStream(), destDir, decodeText(bodyPart.getFileName()));
					}
				}
			}
		} else if (part.isMimeType("message/rfc822")) {
			saveAttachment((Part) part.getContent(),destDir);
		}
	}

	/**
	 * Lea los datos en la secuencia de entrada y guárdelos en el directorio especificado
	 * @param es flujo de entrada
	 * @param fileName nombre de archivo
	 * @param directorio de almacenamiento de archivos destDir
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void saveFile(InputStream is, String destDir, String fileName)
			throws FileNotFoundException, IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(new File(destDir + fileName)));
		int len = -1;
		while ((len = bis.read()) != -1) {
			bos.write(len);
			bos.flush();
		}
		bos.close();
		bis.close();
	}

	/**
	 * Decodificación de texto
	 * @param encodeText Decodifica el texto codificado por el método MimeUtility.encodeText (texto de cadena)
	 * @ devolver texto decodificado
	 * @throws UnsupportedEncodingException
	 */
	public static String decodeText(String encodeText) throws UnsupportedEncodingException {
		if (encodeText == null || "".equals(encodeText)) {
			return "";
		} else {
			return MimeUtility.decodeText(encodeText);
		}
	}
	
}
