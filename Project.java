import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.*;

public class Project
{
  Relation tempRelation;
  // Constructor
  Project() { }
  Project(Relation tempRelation){
    this.tempRelation = tempRelation;
  }

  /* Called in parser. Handles PROJECT statements */
  public int projectHandler (ArrayList<String> file, int index_location, Parser p, Select select, Project project) {
      String projectTemp = "";
      String relName = "";

      if(index_location == 0){
    	  projectTemp = "Query";
        relName = "Query";
      }
      else{
        projectTemp = file.get(index_location-2);
        relName = getTargetRelation(file, index_location);
      }

    Relation projectRel = new Relation(projectTemp);
    Relation relation = p.findRelation(relName);

    ArrayList<String> attrTitleList = new ArrayList<String>();
    ArrayList<String> tempAtrrList = new ArrayList<String>();


      relName = getTargetRelation(file, index_location);
      relation = p.findRelation(relName);
      if (relation == null) {
	  relation = p.findTempRelation(relName);
	  if (relation == null) {
	      System.out.println("Project error: This relation does not exist.");
	      return index_location;
      }
    }

    // Create temporary relation
    tempAtrrList = getTempAttrList(file, index_location, relation, attrTitleList);
    p.createNewRelation(projectTemp, tempAtrrList, projectRel);

    selectTempRelation(relation, projectRel, attrTitleList, project);

    return index_location;
  }


  /* Set the temporary relation with the correct attributes being projected */
  private void selectTempRelation (Relation relation, Relation projectRel, ArrayList<String> attrTitleList, Project project) {
    ArrayList<Integer> indexList = new ArrayList<Integer>();
    indexList = getIndexList(relation, attrTitleList, indexList);
    LinkedList<Tuple> tupleList = relation.gettupeList();
    String temp;
    int tempInt;

    // values -> valuelist -> tuple
    for (int i = 0; i < tupleList.size(); i++) {
      LinkedList<Value> valueList = new LinkedList<Value>();
      for (int j = 0; j < indexList.size(); j++) {
        if (tupleList.get(i).getvalueList().get(indexList.get(j)).getvalueString() != null) {
          temp = tupleList.get(i).getvalueList().get(indexList.get(j)).getvalueString();
          Value val = new Value(temp);
          valueList.add(val);
        } else {
          tempInt = tupleList.get(i).getvalueList().get(indexList.get(j)).getvalueInt();
          Value val = new Value(tempInt);
          valueList.add(val);
        }
      }
      // Set tuple list for temp relation
      Tuple tempTup = new Tuple(valueList);
      projectRel.gettupeList().add(tempTup);
    }
    // Set the temp relation
    project.setTempRelation(projectRel);
  }
  /* Getter for temporary project relation */
  public Relation getTempRelation () {
    System.out.println();
    return this.tempRelation;
  }
  /* Setter for temporary project relation */
  private void setTempRelation (Relation projectRel) {
    this.tempRelation = projectRel;
  }
  /* Returns the index of the attributes being projected */
  private ArrayList<Integer> getIndexList (Relation relation, ArrayList<String> attrTitleList, ArrayList<Integer> indexList) {
    LinkedList<Attribute> attributeList = relation.getattributeList();
    int title_idx = 0;
    for (int i = 0; i < attrTitleList.size(); i++) {
      for (int j = 0; j < attributeList.size(); j++) {
        if (attributeList.get(j).gettitle().contains(attrTitleList.get(i))) {
          title_idx = j;
          indexList.add(title_idx);
        }
      }
    }
    return indexList;
  }
  /* Returns a string list of atttributes for the temporary relation */
  private ArrayList<String> getTempAttrList (ArrayList<String> file, int index, Relation relation, ArrayList<String> attrTitleList) {
    LinkedList<Attribute> attrLinkedList = new LinkedList<Attribute>();
    attrLinkedList = relation.getattributeList();
    ArrayList<String> tempAtrrList = new ArrayList<String>();
    String temp = "";
    ArrayList<String> attributeList = new ArrayList<String>();
    String attribute;
    // Get attributes
    while (!file.get(index).matches("FROM")) {
      if (!file.get(index).matches("PROJECT")) {  // skip PROJECT
        attribute = file.get(index);
        if (attribute.contains(",")) {
          attribute = attribute.replace(",","");
        }
        attributeList.add(attribute);
      }
      index++;
    }
    // String -> Attribute
    for (int i = 0; i < attrLinkedList.size(); i++) {
      for (int j = 0; j < attributeList.size(); j++) {
        if (attrLinkedList.get(i).gettitle().matches(attributeList.get(j))) {
          temp = attrLinkedList.get(i).gettitle();
          attrTitleList.add(temp);
          tempAtrrList.add(temp);
          temp = attrLinkedList.get(i).getValue();
          tempAtrrList.add(temp);
          temp = Integer.toString(attrLinkedList.get(i).getlength());
          tempAtrrList.add(temp);
        }
      }
    }
    return tempAtrrList;
  }
  /* Get the name of target relation. Called in projectHandler */
  private String getTargetRelation (ArrayList<String> file, int index) {
      while (index < file.size() && !file.get(index).contains(";")) {
      index++;
      }

      String relName = "";
      if(index == file.size()){
	       relName = file.get(index - 1);
      }
      else{
	       relName = file.get(index);
      }
    if (relName.contains(";")) {
      relName = relName.replace(";","");
    }
    return relName;
  }

}
