import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.*;

public class Select
{
  Relation tempRelation;
  // Constructor
  Select() { }
  Select(Relation tempRelation) {
    this.tempRelation = tempRelation;
  }

  /* Select handler called in parser */
  public int selectHandler (ArrayList<String> file, int index_location, Parser p, Select select) {
      String selectTemp = "";
      if(index_location == 0){
	  selectTemp = "Query";
      }
      else{
	  selectTemp = file.get(index_location-2);
      }
    Relation selectRel = new Relation(selectTemp);
    ArrayList<String> tempAtrrList = new ArrayList<String>();
    ArrayList<String> attrTitleList = new ArrayList<String>();
    LinkedList<Tuple> tupleList = new LinkedList<Tuple>();

    // Get selected relation name
    String relName;
    if(file.get(index_location+1).contains(";")) {
      relName = file.get(index_location+1).replace(";","");
    } else {
      relName = file.get(index_location+1);
    }

    // check that the relation we're selecting exists
    Relation relation = p.findRelation(relName);
    if (relation == null) {
	
	relation = p.findTempRelation(relName);
	if(relation == null){
	    System.out.println("Select error: This relation does not exist.");
	    return index_location;
	}
    }

    tempAtrrList = AttributeToString(attrTitleList, relation);

    p.createNewRelation(selectTemp, tempAtrrList, selectRel);

    if (hasWhere(file, index_location) > 0) {
      selectWhereHandler(file, relation, selectRel, hasWhere(file, index_location), attrTitleList, select);
    } else {
      tupleList = relation.gettupeList();
      selectRel.settupeList(tupleList);
    }
    return index_location;
  }


  /* Handles where. Takes in the location index of 'WHERE' as the index, and attribute title list */
  private void selectWhereHandler (ArrayList<String> file, Relation relation, Relation selectRel, int index, ArrayList<String> attrTitleList, Select select) {
    String title;
    LinkedList<Tuple> tempTupleList = new LinkedList<Tuple>();
    LinkedList<Tuple> tempList = new LinkedList<Tuple>();
    LinkedList<Tuple> andList = new LinkedList<Tuple>();

    boolean condFlag = false;

    while (index < file.size() && !file.get(index).contains(";")) {
      if (file.get(index).matches("and")) {
        condFlag = true;
        andList = operationHandler(file, relation, selectRel, index+2, attrTitleList, andList);
        tempTupleList = andHandler(tempTupleList, andList);
        index++;
      }
      if (file.get(index).matches("or")) {
        condFlag = true;
        tempList = operationHandler(file, relation, selectRel, index+2, attrTitleList, tempList);
        tempTupleList = orHandler(tempTupleList, tempList);
        index++;
      }
      if (file.get(index).matches("=|!=|<|>|<=|>=") && condFlag == false) {

        tempTupleList = operationHandler(file, relation, selectRel, index, attrTitleList, tempTupleList);

        index++;
      }

      index++;
    }
    selectRel.settupeList(tempTupleList);
    select.setTempRelation(selectRel);
    if (select.getTempRelation().gettupeList() != null) {
	//System.out.println(select.getTempRelation().gettupeList());
    }
  }

  /* Returns new tuple list where and condition is met */
  private LinkedList<Tuple> andHandler (LinkedList<Tuple> tempTupleList, LinkedList<Tuple> tempList) {
    LinkedList<Tuple> newList = new LinkedList<Tuple>();

    for (Tuple i : tempTupleList) {
      if (tempList.contains(i)) {
        newList.add(i);
      }
    }
    return newList;
  }
  /* Removes duplicate tuples when joining tuple lists. Returns new list of tuples */
  private LinkedList<Tuple> orHandler (LinkedList<Tuple> tempTupleList, LinkedList<Tuple> tempList) {
    LinkedList<Tuple> newList = new LinkedList<Tuple>();
    LinkedList<Tuple> longer = tempTupleList;
    LinkedList<Tuple> shorter = tempList;
    if (shorter.size() > longer.size()) {
      longer = tempList;
      shorter = tempTupleList;
    }
    for (Tuple i : shorter) {
      newList.add(i);
    }
    for (Tuple t : longer) {
      if (!newList.contains(t)) {
        newList.add(t);
      }
    }
    return newList;
  }

  /* Handles {=,!=,>,<,<=, >=} and adds appropriate tuples into tuple list. Called in selectWhereHandler */
  private LinkedList<Tuple> operationHandler (ArrayList<String> file, Relation relation, Relation selectRel, int index, ArrayList<String> attrTitleList, LinkedList<Tuple> tempTupleList) {
    LinkedList<Tuple> tupleList = new LinkedList<Tuple>();
    LinkedList<Value> valueList = new LinkedList<Value>();
    tupleList = relation.gettupeList();
    int optNum;

    String title = file.get(index-1);
    String optVal = operationValue(file, index);
    // Get index of attribute title
    int title_idx = 0;
    for (int i = 0; i < attrTitleList.size(); i++) {
      if (attrTitleList.get(i).contains(title)) {
        title_idx = i;
      }
    }
    for (int i = 0; i < tupleList.size(); i++) {
      if (file.get(index).matches("=")) {
        if (tupleList.get(i).getvalueList().get(title_idx).getvalueString() != null) {  // String
          if (tupleList.get(i).getvalueList().get(title_idx).getvalueString().contains(optVal)) {
            tempTupleList.add(tupleList.get(i));
          }
        }
        else {  // Integer
            if (tupleList.get(i).getvalueList().get(title_idx).getvalueInt().toString().contains(optVal)) {
              tempTupleList.add(tupleList.get(i));
            }
        }
      }
      if (file.get(index).matches("!=")) {
        if (tupleList.get(i).getvalueList().get(title_idx).getvalueString() != null) {
          if (!tupleList.get(i).getvalueList().get(title_idx).getvalueString().contains(optVal)) {
            tempTupleList.add(tupleList.get(i));
          }
        } else {
            if (!tupleList.get(i).getvalueList().get(title_idx).getvalueInt().toString().contains(optVal)) {
              tempTupleList.add(tupleList.get(i));
            }
        }
      }
      if (file.get(index).matches("<")) {
        optNum = Integer.parseInt(optVal);
        if (tupleList.get(i).getvalueList().get(title_idx).getvalueInt() < optNum) {
          tempTupleList.add(tupleList.get(i));
        }
      }
      if (file.get(index).matches(">")) {
        optNum = Integer.parseInt(optVal);
        if (tupleList.get(i).getvalueList().get(title_idx).getvalueInt() > optNum) {
          tempTupleList.add(tupleList.get(i));
        }
      }
      if (file.get(index).matches("<=")) {
        optNum = Integer.parseInt(optVal);
        if (tupleList.get(i).getvalueList().get(title_idx).getvalueInt() <= optNum) {
          tempTupleList.add(tupleList.get(i));
        }
      }
      if (file.get(index).matches(">=")) {
        optNum = Integer.parseInt(optVal);
        if (tupleList.get(i).getvalueList().get(title_idx).getvalueInt() >= optNum) {
          tempTupleList.add(tupleList.get(i));
        }
      }
    }
    return tempTupleList;
    // selectRel.settupeList(tempTupleList);
  }

  /* Takes Attribute list -> Returns String attribute list. Called in selectHandler */
  private ArrayList<String> AttributeToString (ArrayList<String> attrTitleList, Relation relation) {
    LinkedList<Attribute> attrLinkedList = new LinkedList<Attribute>();
    ArrayList<String> tempAtrrList = new ArrayList<String>();
    attrLinkedList = relation.getattributeList();
    String temp = "";
    for (int i = 0; i < attrLinkedList.size(); i++) {
      temp = attrLinkedList.get(i).gettitle();
      attrTitleList.add(temp);
      tempAtrrList.add(temp);
      temp = attrLinkedList.get(i).getValue();
      tempAtrrList.add(temp);
      temp = Integer.toString(attrLinkedList.get(i).getlength());
      tempAtrrList.add(temp);
    }
    return tempAtrrList;
  }
  /* Checks for AND/OR conditions. Returns number of conditions */

  /* Stores the value right of the operation */
  private String operationValue (ArrayList<String> file, int index) {
    String value = file.get(index+1);
    if (value.contains("'")) {
      int cond_idx = index+2;
      while (!file.get(cond_idx).contains("'")) {
        value = value + " " + file.get(cond_idx);
        cond_idx++;
      }
      value = value + " " + file.get(cond_idx);
    }
    if (value.contains(";")) { // Remove ;
      value = value.replace(";","");
    }
    if (value.contains("'")) {
      value = value.replace("'","");
    }
    return value;
  }

  /* Checks for a where clause. Returns the index where the WHERE was found */
  private int hasWhere (ArrayList<String> file, int index) {
    while (!file.get(index).contains(";")) {
      if (file.get(index).contains("WHERE")) {
        return index;
      }
      index++;
    }
    return 0;
  }
  /* Getter for temporary select relation */
  public Relation getTempRelation () {
    return this.tempRelation;
  }
  /* Setter for temporary select relation */
  private void setTempRelation (Relation selectRel) {
    this.tempRelation = selectRel;
  }
}
