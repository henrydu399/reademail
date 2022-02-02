package system.home.emailbussines;

import java.io.File;
import java.io.IOException;

public interface IStoreBusiness {

	void createFile(String pathFull, File file , String extension , String body) throws IOException;
	
	void ReadFile(String path, File file);
	
	
}
