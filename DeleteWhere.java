import java.util.Scanner;
import java.io.File;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.text.*;
import java.util.*;

public class DeleteWhere {

    // Holds all the relations
    LinkedList<Relation> rList;

    public DeleteWhere() {}

    // Called once, returns nothing
    public void start(LinkedList<String> relNames, LinkedList<Relation> rList){
    	this.rList = rList;
    	deleteWhereHandler(relNames);
    }

    // Handles when the WHERE clause is in a DELETE statement. Does initial setup
    private void deleteWhereHandler(LinkedList<String> relNames){
    	boolean containsAnd = relNames.contains("and");
    	boolean containsOr = relNames.contains("or");
    	String relName = relNames.get(0);
    	int indexStartOfOperations = relNames.indexOf("WHERE") + 1;
    	ArrayList<String> statement = new ArrayList<String>();
    	int i = indexStartOfOperations;
    	int j = 0;

    	if(containsAnd == true) { // Handles 'and' statement first
  	    deleteWhereContainsAndHandler(relNames);
    	}
    	if(containsOr == true) { // If statement has an 'or' in it
  	    i = getIndexOfFirstStatement(relNames, "or");
  	    int numOfOperations = getNumOfOperations(relNames, i);
  	    while(j < numOfOperations) {
      		while(i < relNames.size() && !relNames.get(i).equals("and") && !relNames.get(i).equals("or")){
    		    if(relNames.get(i).contains("'")){
        			statement.add(getQuotedWords(relNames, i));
        			i += getNumQuotedWords(relNames, i);
    		    }
    		    else if((i+1) == relNames.size()){
        			String endOfLineStatement = relNames.get(i).replace(";", "");
        			statement.add(endOfLineStatement);
    		    }
    		    else{
              statement.add(relNames.get(i));
    		    }
    		    i++;
      		}
      		deleteWhereNoAnd(relName, statement);
      		j++;
      		i++;
      		statement.clear();
  	    }
    	}
    }

    // Returns index of the first statement right before the 'or'/'and'
    private int getIndexOfFirstStatement(LinkedList<String> relNames, String and_or){
    	int i = 0;

    	while(i < relNames.size() && !relNames.get(i).equals(and_or)){
  	    i++;
    	}
    	if(relNames.indexOf("WHERE") == (i - 4)){ //if 'or' statement is at front of line
  	    i = i - 3;
    	}
    	else{ //if 'or' statement is at back of line
  	    i = i + 1;
    	}
    	return i;
    }

    // Handles situation when there is an 'and' in the WHERE clause
    private void deleteWhereContainsAndHandler(LinkedList<String> relNames) {
    	String relName = relNames.get(0);
    	int indexStartOfOperations = relNames.indexOf("WHERE") + 1;
    	ArrayList<String> statement = new ArrayList<String>();
    	int i = indexStartOfOperations;
    	int numOfOperations = 1 + getNumOfOperations(relNames, getIndexOfFirstStatement(relNames, "and"));
    	int j = 0;
    	int num_of_ands = getNumAnd(relNames, i);
    	int num_of_ands_counter = 0;

    	// Parses the statement 'number of ands' times
    	while(i < relNames.size() && num_of_ands_counter < num_of_ands){
  	    if(relNames.get(i).contains("'")){ //if in quotes
      		String t = getQuotedWords(relNames, i);
      		if(t.charAt(t.length() - 1) == ' '){
    		    t = t.substring(0, t.length() -1);
      		}
      		statement.add(t);
      		i += getNumQuotedWords(relNames, i) - 1;
  	    }
  	    else if((i+1) == relNames.size()){ //at end of line
      		String endOfLineStatement = relNames.get(i).replace(";", "");
      		statement.add(endOfLineStatement);
  	    }
  	    else{
          statement.add(relNames.get(i));
  	    }
  	    i++;
    	}

    	num_of_ands_counter++;
    	ArrayList<Integer> indexOfAllAnds = getIndexOfAnd(statement);
    	statement = truncateStatement(statement, indexOfAllAnds);
    	deleteWhereWithAnd(relName, statement);
    	j++;
    	i++;
    }


    // Grabs all statements surrounding the 'and' using indexing
    private ArrayList<String> truncateStatement(ArrayList<String> statement, ArrayList<Integer> allTheAndIndexs) {
    	ArrayList<String> newStatement = new ArrayList<String>();
    	int counter = 0;
    	int offset = 0;
    	while(counter < allTheAndIndexs.size()){
    	    if(counter > 0) {
        		int curAndIndx = allTheAndIndexs.get(counter);
        		int prevAndIndx = allTheAndIndexs.get(counter-1);

        		if((curAndIndx - 4) == prevAndIndx){
        		    newStatement.add(statement.get(allTheAndIndexs.get(counter) + 1));
        		    newStatement.add(statement.get(allTheAndIndexs.get(counter) + 2));
        		    newStatement.add(statement.get(allTheAndIndexs.get(counter) + 3));
        		}
        		else{
        		    newStatement.add(statement.get(allTheAndIndexs.get(counter) - 3));
        		    newStatement.add(statement.get(allTheAndIndexs.get(counter) - 2));
        		    newStatement.add(statement.get(allTheAndIndexs.get(counter) - 1));
        		    newStatement.add(statement.get(allTheAndIndexs.get(counter) + 1));
        		    newStatement.add(statement.get(allTheAndIndexs.get(counter) + 2));
        		    newStatement.add(statement.get(allTheAndIndexs.get(counter) + 3));
        		}
    	    }
    	    else{
        		newStatement.add(statement.get(allTheAndIndexs.get(counter) - 3));
        		newStatement.add(statement.get(allTheAndIndexs.get(counter) - 2));
        		newStatement.add(statement.get(allTheAndIndexs.get(counter) - 1));
        		newStatement.add(statement.get(allTheAndIndexs.get(counter) + 1));
        		newStatement.add(statement.get(allTheAndIndexs.get(counter) + 2));
        		newStatement.add(statement.get(allTheAndIndexs.get(counter) + 3));
    	    }
    	    counter++;
    	}
    	return newStatement;
    }

    // Grabs the actual indexs of the tuples that need to be checked, within the proper
    // Relation name. Passed to operatorHandlerWithAnd()
    private void deleteWhereWithAnd(String relationName, ArrayList<String> statement) {
    	int i = 0;
    	ArrayList<String> attributes = new ArrayList<String>();
    	ArrayList<String> operators  = new ArrayList<String>();
    	ArrayList<String> conditions = new ArrayList<String>();
    	ArrayList<Integer> indexOfAttributeNames = new ArrayList<Integer>();

    	while(i < statement.size()){
  	    attributes.add(statement.get(i));
  	    operators.add(statement.get(i+1));
  	    conditions.add(statement.get(i+2));
  	    i = i + 3;
    	}

    	Relation temp_rel = findRelation(relationName);

    	if(temp_rel != null){
  	    i = 0;
  	    while(i < attributes.size()){
      		int indexOfAttributeName  = getIndexOfAttributeName(attributes.get(i), temp_rel.getAttributeNameList());
      		indexOfAttributeNames.add(indexOfAttributeName);
      		i++;
  	    }
  	    operatorHandlerWithAnd(temp_rel, indexOfAttributeNames, attributes, operators, conditions);
    	}
    }

    // operatorHandlerWithAnd
    //
    // Finds all tuples that match the criteria of both statements on either side of 'and', then
    // compares those tuples and deletes only the ones that satisfy both conditions.
    //
    // Returns: void
    private void operatorHandlerWithAnd(Relation temp_rel, ArrayList<Integer> indexOfAttributeNames, ArrayList<String> attributes, ArrayList<String> operators, ArrayList<String> conditions){
    	ArrayList<Integer> tupeListLocationFirstStatement = new ArrayList<Integer>();
    	ArrayList<Integer> tupeListLocationSecondStatement = new ArrayList<Integer>();
    	ArrayList<ArrayList<Integer>> tupeListLocations = new ArrayList<ArrayList<Integer>>();
    	int counter = 0;
    	String operator = "";
    	ArrayList<Integer> tupeList;
    	int indexOfAttributeName = 0;
    	String condition = "";

    	while(counter < attributes.size()) {
  	    operator = operators.get(counter);
  	    indexOfAttributeName = indexOfAttributeNames.get(counter);
  	    condition = conditions.get(counter);

  	    if(operator.equals("=")){
          deleteWhereEqualsHandlerWithAnd(temp_rel, indexOfAttributeName, condition, tupeListLocations, counter);
  	    }
  	    else if(operator.equals("!=")){
          deleteWhereNotEqualsHandlerWithAnd(temp_rel, indexOfAttributeName, condition, tupeListLocations, counter);
  	    }
  	    else if(operator.equals("<")){
          deleteLessThanHandlerWithAnd(temp_rel, indexOfAttributeName, condition, tupeListLocations, counter);
  	    }
  	    else if(operator.equals(">")){
          deleteGreaterThanHandlerWithAnd(temp_rel, indexOfAttributeName, condition, tupeListLocations, counter);
  	    }
  	    else if(operator.equals("<=")){
          deleteLessThanEqualsHandlerWithAnd(temp_rel, indexOfAttributeName, condition, tupeListLocations, counter);
  	    }
  	    else if(operator.equals(">=")){
          deleteGreaterThanEqualsHandlerWithAnd(temp_rel, indexOfAttributeName, condition, tupeListLocations, counter);
  	    }
  	    else{
          System.out.println("Bad operator in DELETE WHERE statement");
  	    }
  	    counter++;
    	}

    	deleteMatchingTuples(temp_rel, tupeListLocations);
    }

    // Finds the tuples that have been marked for deletion and deletes them
    private void deleteMatchingTuples(Relation temp_rel, ArrayList<ArrayList<Integer>> tupeListLocations) {
    	ArrayList<ArrayList<Integer>> t = tupeListLocations;
    	int i = 0;
    	int deleted_counter = 0;
    	int verifyCounter = 0;

    	while(i < t.get(0).size()){
  	    int val = t.get(0).get(i);
  	    int j = 0;
  	    verifyCounter = 0;
  	    while(j < t.size()){
      		if(j < t.size() && j != 0 && t.get(j).contains(val)){
      		    verifyCounter++;
      		}
      		if(verifyCounter == t.size() - 1){
    		    int index = t.get(0).get(i);
    		    temp_rel.gettupeList().remove(index - deleted_counter);
    		    deleted_counter++;
      		}
      		j++;
  	    }
  	    i++;
    	}
    }

    // Adds the index values of tuples that need to be checked to tupeListLocation arraylist of integers
    private void deleteGreaterThanEqualsHandlerWithAnd(Relation temp_rel, int indexOfAttributeName, String condition, ArrayList<ArrayList<Integer>> tupeListLocation, int counter){
    	int i = 0;
    	ArrayList<Integer> tupeList = new ArrayList<Integer>();

    	// if value is int
    	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() == null){
  	    while(i < temp_rel.gettupeList().size()){
      		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
      		if(attribute >= Integer.parseInt(condition)){
    		    tupeList.add(i);
      		}
  		    i++;
  	    }
    	}
    	else{
  	    System.out.println("'>=' will not work with this attribute");
    	}
    	tupeListLocation.add(tupeList);
    }

    // Adds the index values of tuples that need to be checked to tupeListLocation arraylist of integers
    private void deleteLessThanEqualsHandlerWithAnd(Relation temp_rel, int indexOfAttributeName, String condition, ArrayList<ArrayList<Integer>> tupeListLocation, int counter){
    	int i = 0;
    	ArrayList<Integer> tupeList = new ArrayList<Integer>();

    	// if value is int
    	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() == null){
  	    while(i < temp_rel.gettupeList().size()) {
      		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
      		if(attribute <= Integer.parseInt(condition)) {
    		    tupeList.add(i);
      		}
      		i++;
  	    }
    	}
    	else{
  	    System.out.println("'<=' will not work with this attribute");
    	}
    	tupeListLocation.add(tupeList);
    }

    // Adds the index values of tuples that need to be checked to tupeListLocation arraylist of integers
    private void deleteGreaterThanHandlerWithAnd(Relation temp_rel, int indexOfAttributeName, String condition, ArrayList<ArrayList<Integer>> tupeListLocation, int counter){
    	int i = 0;
    	ArrayList<Integer> tupeList = new ArrayList<Integer>();

    	// if value is int
    	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() == null){
  	    while(i < temp_rel.gettupeList().size()) {
      		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
      		if(attribute > Integer.parseInt(condition)){
    		    tupeList.add(i);
      		}
      		i++;
  	    }
    	}
    	else{
  	    System.out.println("'>' will not work with this attribute");
    	}
    	tupeListLocation.add(tupeList);
    }

    // Adds the index values of tuples that need to be checked to tupeListLocation arraylist of integers
    private void deleteWhereNotEqualsHandlerWithAnd(Relation temp_rel, int indexOfAttributeName, String condition, ArrayList<ArrayList<Integer>> tupeListLocation, int counter){
    	int i = 0;
    	ArrayList<Integer> tupeList = new ArrayList<Integer>();

    	// if value is String
    	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() != null){
  	    while(i < temp_rel.gettupeList().size()){
      		String attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueString();
      		if(!attribute.equals(condition)){
    		    tupeList.add(i);
      		}
      		i++;
  	    }
    	}
    	else{ //if value is int
  	    while(i < temp_rel.gettupeList().size()){
      		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
      		if(attribute != Integer.parseInt(condition)){
    		    tupeList.add(i);
      		}
      		i++;
  	    }
    	}
    	tupeListLocation.add(tupeList);
    }

    // Adds the index values of tuples that need to be checked to tupeListLocation arraylist of integers
    private void deleteLessThanHandlerWithAnd(Relation temp_rel, int indexOfAttributeName, String condition, ArrayList<ArrayList<Integer>> tupeListLocation, int counter){
    	int i = 0;
    	ArrayList<Integer> tupeList = new ArrayList<Integer>();
    	// if value is int
    	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() == null){
    	    while(i < temp_rel.gettupeList().size()){
    		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
    		if(attribute < Integer.parseInt(condition)){
    		    tupeList.add(i);
    		}
    		i++;
    	    }
    	}
    	else{
    	    System.out.println("'<' will not work with this attribute");
    	}
    	tupeListLocation.add(tupeList);
    }

    // Adds the index values of tuples that need to be checked to tupeListLocation arraylist of integers
    private void deleteWhereEqualsHandlerWithAnd(Relation temp_rel, int indexOfAttributeName,
					String condition, ArrayList<ArrayList<Integer>> tupeListLocation, int counter){
	int i = 0;
	ArrayList<Integer> tupeList = new ArrayList<Integer>();

	// if value is String
	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() != null){
	    while(i < temp_rel.gettupeList().size()){
		String attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueString();
		if(Character.isWhitespace(attribute.charAt(0))){ //removes whitespace
		    attribute = attribute.substring(1);
		}
		if(attribute.equals(condition)){
		    tupeList.add(i);
		}
		i++;
	    }
	}
	else{ //if value is int
	    while(i < temp_rel.gettupeList().size()){
		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
		if(attribute == Integer.parseInt(condition)){
		    tupeList.add(i);
		}
		i++;
	    }
	}
	tupeListLocation.add(tupeList);
    }

    // Returns index of all the 'ands' in the statement
    private ArrayList<Integer> getIndexOfAnd(ArrayList<String> relNames){
	int AndIndex = 0;
	ArrayList<Integer> ands = new ArrayList<Integer>();
	while(AndIndex < relNames.size()){
	    if(relNames.get(AndIndex).equals("and")){
		ands.add(AndIndex);
	    }
	    AndIndex++;
	}

	return ands;
    }

    // Returns number of 'and's in the statement
    private int getNumAnd(LinkedList<String> relNames, int indexStartOfOperations){
	int i = indexStartOfOperations;
	int num_of_ands = 0;
	while(i < relNames.size()){
	    if(relNames.get(i).equals("and")){
		num_of_ands++;
	    }
	    i++;
	}
	return num_of_ands;
    }

    // Returns words surrounded by quotes
    private String getQuotedWords(LinkedList<String> relNames, int i){
	String words = "";
	int j = i;
	int counter = 0;
	int found_word = 0;
	int num_words = getNumQuotedWords(relNames, i);
	while(counter < num_words){
	    if(relNames.get(j).contains("'")){
		words += relNames.get(j).replace("'", "");
		if(words.contains(";")){
		    words = words.replace(";", "");
		}
		else{
		    words += " ";
		}
	    }
	    else{
		words += relNames.get(j) + " ";
	    }
	    j++;
	    counter++;
	}
	return words;
    }

    // Helper function for getQuotedWords, returns num of words
    private int getNumQuotedWords(LinkedList<String> relNames, int i){
	int count = 0;
	int j = i;
	boolean done = false;
	while(done == false && j < relNames.size()){
	    if(relNames.get(j).contains("'")){
		if(count > 0){
		    done = true;
		}
		count++;
	    }
	    else if(count > 0){
		count++;
	    }
	    j++;
	}
	return count;
    }

    // Returns number of 'and' or 'or' statements in the WHERE clause
    private int getNumOfOperations(LinkedList<String> relNames, int startIndex){
	int count = 0;
	int i = startIndex - 1;
	while(i < relNames.size()){
	    if(relNames.get(i).contains("and") || relNames.get(i).contains("or")){
		count++;
	    }
	    i++;
	}
	return count;
    }

    // Actually does the deleting of WHERE clause
    private void deleteWhereNoAnd(String relationName, ArrayList<String> statement){
	String attributeName = statement.get(0);
	String operator = statement.get(1);
	String condition = statement.get(2);
	Relation temp_rel = findRelation(relationName);
	LinkedList<Tuple> tupeList;

	if(temp_rel != null){
	    int indexOfAttributeName = getIndexOfAttributeName(attributeName, temp_rel.getAttributeNameList());

	    String a = temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString();

	    if(operator.equals("=")){
		deleteWhereEqualsHandler(temp_rel, indexOfAttributeName, condition);
	    }
	    else if(operator.equals("!=")){
		deleteWhereNotEqualsHandler(temp_rel, indexOfAttributeName, condition);
	    }
	    else if(operator.equals("<")){
		deleteLessThanHandler(temp_rel, indexOfAttributeName, condition);
	    }
	    else if(operator.equals(">")){
		deleteGreaterThanHandler(temp_rel, indexOfAttributeName, condition);
	    }
	    else if(operator.equals("<=")){
		deleteLessThanEqualsHandler(temp_rel, indexOfAttributeName, condition);
	    }
	    else if(operator.equals(">=")){
		deleteGreaterThanEqualsHandler(temp_rel, indexOfAttributeName, condition);
	    }
	    else{
		System.out.println("Bad operator in DELETE WHERE statement");
	    }
	}
    }

    // Does the actualy deleting with the WHERE clause by parsing through tuple values
    private void deleteGreaterThanEqualsHandler(Relation temp_rel, int indexOfAttributeName, String condition){
	int i = 0;
	// if value is int
	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() == null){
	    while(i < temp_rel.gettupeList().size()){
		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
		if(attribute >= Integer.parseInt(condition)){
		    temp_rel.gettupeList().remove(i);
		    i--;
		}
		i++;
	    }
	}
	else{
	    System.out.println("'>=' will not work with this attribute");
	}
    }


    // Does the actualy deleting with the WHERE clause by parsing through tuple values
    private void deleteLessThanEqualsHandler(Relation temp_rel, int indexOfAttributeName, String condition){
	int i = 0;
	// if value is int
	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() == null){
	    while(i < temp_rel.gettupeList().size()){
		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
		if(attribute <= Integer.parseInt(condition)){
		    temp_rel.gettupeList().remove(i);
		    i--;
		}
		i++;
	    }
	}
	else{
	    System.out.println("'<=' will not work with this attribute");
	}
    }

    // Does the actualy deleting with the WHERE clause by parsing through tuple values
    private void deleteGreaterThanHandler(Relation temp_rel, int indexOfAttributeName, String condition){
	int i = 0;
	// if value is int
	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() == null){
	    while(i < temp_rel.gettupeList().size()){
		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
		if(attribute > Integer.parseInt(condition)){
		    temp_rel.gettupeList().remove(i);
		    i--;
		}
		i++;
	    }
	}
	else{
	    System.out.println("'>' will not work with this attribute");
	}
    }


    // Does the actualy deleting with the WHERE clause by parsing through tuple values
    private void deleteLessThanHandler(Relation temp_rel, int indexOfAttributeName, String condition){
    	int i = 0;
    	// if value is int
    	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() == null){
  	    while(i < temp_rel.gettupeList().size()){
      		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
      		if(attribute < Integer.parseInt(condition)){
    		    temp_rel.gettupeList().remove(i);
    		    i--;
      		}
      		i++;
  	    }
    	}
    	else{
  	    System.out.println("'<' will not work with this attribute");
    	}
    }


    // Does the actualy deleting with the WHERE clause by parsing through tuple values
    private void deleteWhereEqualsHandler(Relation temp_rel, int indexOfAttributeName, String condition){
    	int i = 0;

    	// if value is String
    	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() != null){
  	    while(i < temp_rel.gettupeList().size()){
      		String attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueString();
      		if(Character.isWhitespace(attribute.charAt(0))){ //removes whitespace
    		    attribute = attribute.substring(1);
      		}
      		if(attribute.equals(condition)){
    		    temp_rel.gettupeList().remove(i);
    		    i--;
      		}
      		i++;
  	    }
    	}
    	else{ //if value is int
  	    while(i < temp_rel.gettupeList().size()){
      		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
      		if(attribute == Integer.parseInt(condition)){
    		    temp_rel.gettupeList().remove(i);
    		    i--;
      		}
      		i++;
  	    }
    	}
    }

    // Does the actualy deleting with the WHERE clause by parsing through tuple values
    private void deleteWhereNotEqualsHandler(Relation temp_rel, int indexOfAttributeName, String condition){
    	int i = 0;
    	// if value is String
    	if(temp_rel.gettupeList().get(0).getvalueList().get(indexOfAttributeName).getvalueString() != null){
  	    while(i < temp_rel.gettupeList().size()){
      		String attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueString();
      		if(!attribute.equals(condition)){
    		    temp_rel.gettupeList().remove(i);
    		    i--;
      		}
      		i++;
  	    }
    	}
    	else{ //if value is int
  	    while(i < temp_rel.gettupeList().size()){
      		int attribute = temp_rel.gettupeList().get(i).getvalueList().get(indexOfAttributeName).getvalueInt();
      		if(attribute == Integer.parseInt(condition)){
    		    temp_rel.gettupeList().remove(i);
    		    i--;
      		}
  		    i++;
  	    }
    	}
    }

    // Parses through attributeName list to get index of desired attribute name
    private int getIndexOfAttributeName(String attributeName, LinkedList<String> attributeNames){
    	int i = 0;
    	int index = 0;
    	while(i < attributeNames.size()){
  	    if(attributeNames.get(i).equals(attributeName)){
      		index = i;
      		break;
  	    }
  	    i++;
    	}
    	return index;
    }

    // Finds relation and returns it with desired String name that is given
    public Relation findRelation(String relName){
    	String fixed_name = relName;
    	fixed_name = fixName(fixed_name, relName);

    	int found = 0;
    	int iterator = 0;
    	Relation temp_rel = rList.get(iterator);
    	while((temp_rel != null && found == 0) && iterator < rList.size()){
  	    if(temp_rel.getName().equals(fixed_name)){
      		found = 1;
      		break;
  	    }
    	    else{
        		iterator++;
        		if (iterator == rList.size())
      		    return null;
        		temp_rel = rList.get(iterator);
    	    }
    	}
    	return temp_rel;
    }

    // This replaces any bad characters in the relName parameter. Need all if-statements
    // because the relName can have multiple bad characters in it.
    private String fixName(String fixed_name, String relName) {
    	if(relName.contains("(")){
    	    fixed_name = relName.replace("(", "");
    	}
    	if(relName.contains(")")){
    	    fixed_name = relName.replace(")", "");
    	}
    	if(relName.contains(";")){
    	    fixed_name = relName.replace(";", "");
    	}
    	if(relName.contains(",")){
    	    fixed_name = relName.replace(",", "");
    	}
    	if(relName.contains(" ")){
    	    fixed_name = relName.replace(" ", "");
    	}
    	return fixed_name;
   }
}
