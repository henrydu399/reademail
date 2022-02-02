package system.home.emailbussines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class IStoreBusinessImpl implements IStoreBusiness {


	public void createFile(String pathFull, File file , String extension , String body) throws IOException {
		    		
	    		BufferedWriter bw = new BufferedWriter( new FileWriter(pathFull,true) );	   		
	    		bw.write(body);
	    		bw.flush();
	    		bw.newLine();
	    		bw.close();				
	}

	@Override
	public void ReadFile(String path, File file) {
		// TODO Auto-generated method stub
		
	}

}
