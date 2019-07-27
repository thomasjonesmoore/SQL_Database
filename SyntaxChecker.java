import java.util.Scanner;
import java.io.File;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.text. * ;

public class SyntaxChecker {

	/* Method that passes the checking onto other functions depending
       on what their keyword is */
	public static void checkSyntax(ArrayList < String > file, int index_location) {
		if (file.get(index_location).contains("RELATION")) {
			relationSyntax(file, index_location);
		}
		else if (file.get(index_location).contains("INSERT")) {
			insertSyntax(file, index_location);
		}
		else if (file.get(index_location).contains("PRINT")) {
			printSyntax(file, index_location);
		}
	}

	public static boolean relationSyntax(ArrayList < String > file, int index_location) {
		boolean b = false;
		boolean end = false;
		boolean triple = false;
		boolean parenthesis = false;
		boolean comments = true;
		int i = index_location;
		int c = index_location;
		int j = index_location;

		// Check parenthesis, ;
		while (!file.get(i).contains("INSERT") && !file.get(i).contains("PRINT")) {
			if (file.get(i).contains("/*")) {
				comments = false;
			}
			if (file.get(i).contains("*/")) {
				comments = true;
			}
			if (file.get(i).contains("(")) {
				while (!file.get(i).contains(")") && !file.get(i).contains("INSERT") && !file.get(i).contains("PRINT")) {
					i++;
				}
				if (file.get(i).contains(")")) {
					parenthesis = true;

				}
				else {
					i--;
				}
			}

			if (file.get(i).contains(";")) {
				end = true;
			}
			i++;
		}

		// check format
		if (parenthesis) {
			while (!file.get(j).contains("INSERT") && !file.get(j).contains("PRINT")) {
				if (file.get(j).contains(",") && file.get(j - 2).contains("(")) {
					if (file.get(j + 3).contains(")") || file.get(j + 3).contains(",")) {
						triple = true;
					}
				}
				j++;
			}
		}

		if (comments && triple && parenthesis && end) {
			b = true;
		}

		return b;
	}

	// returns true for correct, false for incorrect syntax
	public static boolean insertSyntax(ArrayList<String> file, int index_location) {
		boolean b = false;
		int i = index_location;
		if (file.get(i).contains("'")) {
			while (!file.get(i).contains(";")) { //parse until the last entry
				i++;
				if (file.get(i).contains("'")) {
					b = true;
				}
				else {
					b = false;
				}
			}
		}
		return b;
	}

	// returns true for correct, false for incorrect syntax
	public static boolean printSyntax(ArrayList<String> file, int index_location) {
		boolean b = false;
		int i = index_location +1 ;
		while (i < file.size() && !file.get(i).contains("INSERT") && !file.get(i).contains("PRINT") && !file.get(i).contains("DESTROY") && !file.get(i).contains("RELATION")) {
			if (file.get(i).contains(";")) {
				b = true;
			}
			i++;
		}
		int j = i - index_location;
		if (j < 1) {	// valid num arguements
			b = false;
		}
		return b;
	}
}
