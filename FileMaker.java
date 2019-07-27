import java.util.Scanner;
import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.text.*;


public class FileMaker{

    public static ArrayList<String> make_file(String fileName) throws Exception{
    		    	File FILE = new File(fileName);      
	    try {   
	    	FileReader fileReader = new FileReader(FILE);
	    }
	    catch (Exception e) {
	    	System.err.println("Could not find file!");
	    	System.exit(1);
	    } 
		ArrayList<String> everything = new ArrayList<String>();

		Scanner sc = new Scanner(FILE);
		String line = "";
		Boolean inComment = false;

		while(sc.hasNextLine()){
		    line = sc.nextLine();
		    String[] ln = line.split(" ");
		    for(String t: ln){
		    	if (t.contains("*/") && t.contains(";"))
		    	{
		    		everything.add(";");
		    		inComment = false;
		    		continue;
		    	}

		    	if (t.contains("/*")) // eliminates comments from our file
		    		inComment = true;
		    	else if (t.contains("*/"))
		    		inComment = false;
		    	else if (inComment)
		    	{
		    		continue;
		    	}
				else if (t.matches("\\S+"))
			   		everything.add(t);
			   	else
			   		continue;
		    }

		}
		return everything;
	}
   
}


