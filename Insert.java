import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.*;


public class Insert
{
    public void display(int numAttributes, String relationName)
    {
	     System.out.println("Inserting " + numAttributes + " attributes into " + relationName);
    }

    // Constructor
    Insert() { }

    public int insert_handler(ArrayList<String> file, int index_location, Parser p) {
    	int i = index_location;
      int count_idx = index_location + 1;
    	int size_idx = index_location + 2;
    	int j = index_location + 1;
    	int size = 1;
    	String rel = file.get(index_location+1);
    	String temp = ""; // Contains the string between single quotes
    	String str;
      boolean validInsert = false;
    	LinkedList<Value> valueList = new LinkedList<Value>();
      Relation relation = p.findRelation(rel);

      // get size of INSERT statement
    	while (!file.get(size_idx).contains(";")) {
    	    size++;
    	    size_idx++;
    	}

    	// Get count
      int count = getCount(file, index_location+1, (j+size));

      // Check if the relation contains NUM attributes
      boolean hasNUM = checkNUM(relation);

      // check correct attributes and types
      validInsert = canInsert(file, count, i+1, j+size, p);

      if (validInsert == false) {
        return count_idx;
      }
      else {
        // Getting Values
      	while (i < (j+size + 1)) {

          if (file.get(i).contains(rel) || file.get(i).contains("INSERT")) { // don't take relation name
        		i++;
          }
          else if (file.get(i).contains("'")) {
        		temp = temp + " " + file.get(i).replace("'",""); // remove first quote
        		i++;
        		while (!file.get(i).contains("'")) {
        		    temp = temp + " " + file.get(i);
        		    i++;
        		}
        		temp = temp + " " + file.get(i).replace("'",""); //remove end quote
        		Value val = new Value(temp);
        		valueList.add(val);
        		i++;
          }
          else {
        		str = file.get(i);
            if (str.contains(";")) {
              str = str.replace(";","");
            }
            if (hasNUM == true) {
              try {
          		    int num = Integer.parseInt(str);
          		    Value val = new Value(num);
          		    valueList.add(val);
          		    i++;
          		}
          		catch (NumberFormatException e) {
          		    Value val = new Value(file.get(i));
          		    if (val.getvalueString().contains(";")) {
                    val.setvalueString(val.getvalueString().replace(";",""));
                  }
          		    valueList.add(val);
          		    i++;
          		}
            } else {
              Value val = new Value(file.get(i));
              if (val.getvalueString().contains(";")) {
                val.setvalueString(val.getvalueString().replace(";",""));
              }
              valueList.add(val);
              i++;
            }

          } // everything not in quotes

      	}

      	if (!file.get(i).contains("'") && file.get(i).contains(";")) {
      	    str = file.get(j+size);
      	    str = str.replace(";","");
      	    try {
          		int num = Integer.parseInt(str);
          		Value val = new Value(num);
          		valueList.add(val);
          		i++;
      	    }
      	    catch (NumberFormatException e) {
          		Value val = new Value(file.get(j+size));
            	if (val.getvalueString().contains(";")) {
                val.setvalueString(val.getvalueString().replace(";",""));
              }
          		valueList.add(val);
          		i++;
      	    }
      	}

      	System.out.println("Inserting " + count + " attributes into " + file.get(j));

      	// Create Tuple and add to Relation
      	Tuple newTuple = new Tuple(valueList);
      	relation.gettupeList().add(newTuple);
      	return count_idx;
      }

    }

    /**/
    private boolean checkNUM (Relation relation) {
      ArrayList<String> typeList = new ArrayList<String>();
      String temp = "";
      for (int i = 0; i < relation.getattributeList().size(); i++) {
        if (relation.getattributeList().get(i).getValue().matches("NUM")) {
          return true;
        }
      }
      return false;
    }

    /* checks for valid inserts. called in insert_handler() */
    private boolean canInsert (ArrayList<String> file, int numAttr, int begin_index, int end_index, Parser p) {
      // check if relation name exists
      Relation name = p.findRelation(file.get(begin_index));

      if (name == null) {
        System.out.println("This relation does not exist.");
        return false;
      } else {
        // check number of attributes
        int tupleAttr = name.getnumAttributes();
        if (numAttr == tupleAttr) {
          // check correct attribute types and length
          LinkedList<String> typeList = name.getAttributeType();
          boolean validType = checkType(file, typeList, begin_index, end_index, file.get(begin_index), name);
          if (validType == false) {
            return false;
          }
          return true;
        }
        else {
          System.out.println("Incorrect number of attributes.");
          return false;
        }
      }
    }

    /* Checks the attribute type. called in canInsert() */
    private boolean checkType (ArrayList<String> file, LinkedList<String> typeList, int begin_index, int end_index, String rel, Relation relation) {
      boolean validType = true;
      boolean validLen = true;
      boolean isNum = false;
      String temp = "";
      String str;
      ArrayList<String> tempList = new ArrayList<String>();
      int i = begin_index -1;
      while (i < end_index+1) {
        if (file.get(i).contains(rel) || file.get(i).contains("INSERT")) { // don't take relation name
          i++;
        }
        else if (file.get(i).contains("'")) {
          temp = temp + " " + file.get(i).replace("'",""); // remove first quote
          i++;
          while (!file.get(i).contains("'")) {
              temp = temp + " " + file.get(i);
              i++;
          }
          temp = temp + " " + file.get(i).replace("'",""); //remove end quote
          tempList.add(temp);
          i++;
        }
        else {
          str = file.get(i);
          if (file.get(i).contains(";")){
            str = str.replace(";","");
          }
          tempList.add(str);
          i++;
        }
      }

      if (!file.get(i).contains("'") && file.get(i).contains(";")) {
          str = file.get(end_index);
          str = str.replace(";","");
          tempList.add(str);
          i++;
      }

      // check types
      for (int t = 0; t < tempList.size(); t++) {
        isNum = isNumeric(tempList.get(t));
        if (typeList.get(t).contains("CHAR") && isNum == true) {
          if(rel.contains("STAFF") && t == tempList.size()-1) { // STAFF last attribute is a numeric char
            validType = true;
          }
          else {
            System.out.println("Incorrect attribute type.");
            return false;
          }
        }
        if (typeList.get(t).contains("NUM") && isNum == false) {
          System.out.println("Incorrect attribute type.");
          return false;
        }
      }
      // check length
      validLen = checkLen(tempList, relation);
      if (validLen == false || validType == false) {
        return false;
      }
      return true;
    }

    private boolean checkLen(ArrayList<String> tempList, Relation relation){
      ArrayList<Integer> intList = new ArrayList<Integer>();
      LinkedList<Integer> lenList = relation.getAttributeLength();

      int len;
      for (int i = 0; i < tempList.size(); i++) {
        intList.add(tempList.get(i).length());
      }
      for (int j = 0; j < intList.size(); j++) {
        if (intList.get(j) > lenList.get(j)) {
          System.out.println("Incorrect attribute length.");
          return false;
        }
      }
      return true;
    }

    /* Count the number of attributes */
    private int getCount (ArrayList<String> file, int count_idx, int size) {
      int count = 0;
      while (count_idx < size) {
          if (file.get(count_idx).contains(";")) { // no attributes following relation name
            break;
          }
    	    if (file.get(count_idx).contains("'")) {
        		count_idx++;
        		while (!file.get(count_idx).contains("'")) {
        		    count_idx++;
        		}
    	    }
    	    if (file.get(count_idx).contains("'") && file.get(count_idx).contains(";")) {

    	    }
    	    else {
        		count++;
        		count_idx++;
    	    }
    	}
      return count;
    }

    /* regular expression to check if a string is an integer. called in checkTypeLen() */
    private boolean isNumeric (String str) {
      return str.matches("-?\\d+(\\.\\d+)?");
    }
}
