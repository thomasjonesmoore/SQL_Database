import java.util.Scanner;
import java.io.File;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.text.*;
import java.util.*;


public class Parser {

    public LinkedList<Relation> rList;
    public LinkedList<Relation> tempRList;
    public Relation catalog;

    Parser(){
      rList = new LinkedList<Relation>();
      tempRList = new LinkedList<Relation>();
    }

    public LinkedList<Relation> getRList(){
      return rList;
    }

    // Handles different cases for parsing the file
    public void parse_file(String[] args, Parser p) throws Exception{
	if(args.length != 1){
	    System.out.println("Please specify test file name");
	    System.exit(1);
	}
	
	ArrayList<String> everything = FileMaker.make_file(args[0]);

	catalog = CatalogCreator.createCatalog(p);
    	int array_parser = 0;
    	boolean legal = false;
    	boolean setupTemp = false;
    	Insert inserter = new Insert();
    	Select selector = new Select();
    	Project project = new Project();

    	JoinHandler joiner = new JoinHandler();
    	String tempRelName = "";
    	while (array_parser < everything.size()) {
    	    if (everything.get(array_parser).contains("/*")) {
            array_parser = comment_handler(everything, array_parser);
    	    }
    	    else if (everything.get(array_parser).contains("RELATION")) {
    	    	int origVal = array_parser + 3; // past parentheses
            array_parser = parseComment(everything, legal, array_parser);
      			if (array_parser == -1) {
      				array_parser = origVal;
      				continue;
      			}
    	    }
    	    else if (everything.get(array_parser).contains("(") && !everything.get(array_parser).contains("RELATION")) {// for nested statements
            NestedExpressions nestedExpressions = new NestedExpressions();
    	    	int origVal = array_parser+1;
            array_parser = nestedExpressions.setup(array_parser-1, everything, project, selector, p, joiner);
    	    	if (array_parser == -1)
    	    	{
    	    		array_parser = origVal;
    	    		continue;
    	    	}
    	    }
    	    else if (everything.get(array_parser).contains("INSERT")) {
            array_parser = inserter.insert_handler(everything, array_parser, p);
    	    }
    	    else if (everything.get(array_parser).contains("PRINT")) {
            parsePrint(everything, legal, array_parser);
    	    }
    	    else if (everything.get(array_parser).contains("DESTROY")){
            array_parser = destroy_handler(everything, array_parser);
    	    }
    	    else if (everything.get(array_parser).contains("DELETE")){
            array_parser = delete_handler(everything, array_parser);
    	    }
    	    else if (everything.get(array_parser).contains("SELECT")){
            array_parser = selector.selectHandler(everything, array_parser, p, selector);
    	    }
    	    else if (everything.get(array_parser).contains("PROJECT")) {
            array_parser = project.projectHandler(everything, array_parser, p, selector, project);
    	    }
    	    else if (everything.get(array_parser).contains("JOIN")) {
    		    if (setupTemp == false)
    	    	{
    	    		System.out.println("Join error: There must be a temporary variable.");
    	    		array_parser++;
    	    		continue;
    	    	}
    	    	else
    	    	{
    		    	int origVal = array_parser + 1;
    		    	array_parser = joiner.beginJoin(everything, array_parser, p);
    		    	Relation tempRelation = joiner.getjoinedRelation();
    		    	if (array_parser == -1)
    		    	{
    		    		array_parser = origVal;
    		    		continue;
    		    	}
    		    	else
    		    	{
    		    		array_parser--;
    		    		TempHandler tempInit = new TempHandler(tempRelation, tempRelName, p);
    		    	}
    		    	setupTemp = false;
    	    	}
    	    }
    	    else if (everything.get(array_parser).equals("=") && ((array_parser > 0) && (array_parser < everything.size() - 1)))
    	    {
    	    	String commandName = everything.get(array_parser + 1);
    	    	tempRelName = everything.get(array_parser - 1);
    	    	if ((commandName.equals("JOIN") || commandName.equals("SELECT")) || commandName.equals("PROJECT"))
    	    	{
    	    		if (findRelation(tempRelName) != null) // relation already exists
    	    		{
    	    			System.out.println("Error: Relation names cannot be overWritten");
    	    			setupTemp = false;
    	    		}
    	    		else
    	    			setupTemp = true;
    	    	}
    	    	else
    	    		setupTemp = false;
    	    }
    	    array_parser++;
    	}
    }

    // Used by parse_file to handle when 'PRINT' is found in the input file
    private void parsePrint(ArrayList<String> everything, boolean legal, int array_parser) {
      legal = SyntaxChecker.printSyntax(everything, array_parser);
    	if (legal == true) {
        array_parser = print_handler(everything, array_parser);
    	  array_parser--;
    	}
    	else {
        System.out.println("Error: Not a legal input");
    	}
    }

    // Used by parse_file to handle when a comment is found in the file
    private int parseComment(ArrayList<String> everything, boolean legal, int array_parser){
      legal = SyntaxChecker.relationSyntax(everything, array_parser);
    	if (legal == true) {
        array_parser = relation_handler(everything, array_parser);
    	  return array_parser;
    	}
    	else {
        System.out.println("Error: Not a legal input");
    	  return -1;
    	}
    }

    // Returns index of the end comment
    private int comment_handler(ArrayList < String > file, int index_location) {
    	int currIndex = index_location;
    	while (!file.get(currIndex).contains("*/"))
    	    currIndex++;

    	return currIndex;
    }

    // Handles action regarding the relation keyword
    private int relation_handler(ArrayList <String> file, int index_location) {
    	ArrayList <String> attributeName = new ArrayList < String > ();
    	ArrayList<String> temp_atr_holder = new ArrayList<String>();
    	int currIndex = index_location + 1;
    	int length = get_length(file, index_location);
    	String type = getType(file, currIndex);
    	int counter = 1;
    	String temp = "";
    	int att = attribute_counter(file, index_location);
    	currIndex++;

    	getParenthesisValues(counter, length, temp, currIndex, attributeName, file);
    	System.out.println("Creating " + type + " with " + (att / 3) + " attributes.");

    	temp_atr_holder = getAttributes(file, index_location);
    	temp_atr_holder = fixAtrHolder(temp_atr_holder);

    	Relation new_rel = new Relation(type);
    	createNewRelation(type, temp_atr_holder, new_rel);

    	// add the appropriate tuple to the catalog
    	catalog.gettupeList().add(new_rel.getmetaData());

    	currIndex = resetCurrIndex(currIndex, file, index_location);
    	return currIndex;
    }

    // Finds where the next keyword is in the file and returns that index
    private int resetCurrIndex(int currIndex, ArrayList<String> file, int index_location){
    	currIndex = index_location + 1;
    	while (!file.get(currIndex).contains(";")) {
    	    currIndex++;
    	}
    	currIndex--;
    	return currIndex;
    }

    /* Creates new Relation objects with the temp array values
       and adds that object to the LinkedList holding all the
       Relation objects */
    public void createNewRelation(String type, ArrayList<String> temp_atr_holder, Relation new_rel) {
    	int c = 0;
    	rList.add(new_rel);
    	String tempVal = "";

    	while(c < temp_atr_holder.size()) {
        tempVal = temp_atr_holder.get(c+2);
  	    if(tempVal.contains(";")) { // take care of semicolons
          tempVal = tempVal.replace(";", "");
  	    }
    	  new_rel.addAttribute(temp_atr_holder.get(c), temp_atr_holder.get(c+1), Integer.parseInt(tempVal));
    	  new_rel.constructAttribute(new_rel.getnumAttributes());

    	  c = c + 3;
    	}
    }

    /* Parse through array to remove any null or space values */
    private ArrayList<String> fixAtrHolder(ArrayList<String> temp_atr_holder) {
    	for(int l = 0; l < temp_atr_holder.size(); l++) {
        if(temp_atr_holder.get(l).equals("")){
          temp_atr_holder.remove(l);
        }
    	}
    	return temp_atr_holder;
    }

    private void getParenthesisValues(int counter, int length, String temp, int currIndex, ArrayList<String> attributeName, ArrayList<String> file){
    	while (counter <= length) {
    	    temp = file.get(currIndex).replace("(", ""); // deals with content in parentheses
    	    attributeName.add(temp);
    	    currIndex = currIndex + 3;
    	    counter = counter + 3;
    	}
    }

    private String getType(ArrayList <String> file, int currIndex) {
    	String type = "";
    	if (file.get(currIndex).contains("*/")) { // if we are dealing with a comment
        type = file.get(currIndex + 1);
    	}
    	else {
        type = file.get(currIndex);
    	}
    	return type;
    }

    // Handles action involving the print keyword
    private int print_handler(ArrayList < String > file, int index_location) {
    	int currIndex = index_location + 1;
    	int endIndex = index_location + 1;
    	int fileIndex = index_location + 1;
    	int size = 1;
    	String element = ""; // current token from file
    	String temp = "";
    	Relation relation = null;
    	Print p = new Print();
    	NestedExpressions nestedExpression = new NestedExpressions();
    	Project projector = new Project();
    	Select selector = new Select();
    	JoinHandler joiner = new JoinHandler();

    	index_location++;
    	if (file.get(index_location).contains("(")) {
        index_location = nestedExpression.setup(index_location, file, projector, selector, this, joiner);
    		if (index_location == -1)
    			return -1;
    		else
    		{
    			p.displayRelation(nestedExpression.getretRelation());
    			return index_location;
    		}
    	}

    	while (!file.get(fileIndex).contains(";")) { // until we reach a semicolon
        size++;
  	    fileIndex++;
    	}
    	while (currIndex < (endIndex+size)) { // prints relations
        element = file.get(currIndex);
  	    if (element.contains(";")) { // if we find a stray semicolon
  		    temp = element.replace(";", ""); // delete semicolon
  		    relation = findRelation(temp);
  		    if (relation == null)
  		    {
  		    	relation = findTempRelation(temp);
  		    	if (relation == null)
  		    	{
  		    		System.out.println("This relation does not exist. 1");
  		    		return endIndex + size;
  		    	}
  		    }
  		    p.displayRelation(relation);
  		    System.out.println();
    		}
    	  else
    		{
  		    temp = file.get(currIndex);
  		    temp = temp.replace(",", "");
  		    relation = findRelation(temp);
  		    if (relation == null)
  		    {
  		    	relation = findTempRelation(temp);
  		    	if (relation == null)
  		    	{
  		    		System.out.println("This relation does not exist. 2");
  		    		return endIndex + size;
  		    	}
  		    }
  		    p.displayRelation(relation);
  		    System.out.println();
    		}
    	    element = file.get(currIndex);

    	    currIndex++;
    	}
    	return currIndex;
    }

    // returns number of string tokens between the parenthesis
    private int get_length(ArrayList < String > file, int index_location) {
    	int length = 1;
    	int currIndex = index_location;
    	while (!file.get(currIndex).contains("(")) {
  	    currIndex++;
    	}
    	while (!file.get(currIndex).contains(";")) {
        length++;
  	    currIndex++;
    	}
    	return length;
    }

    // counts number of attributes used following keywords
    private int attribute_counter(ArrayList < String > file, int index_location) {
    	int length = 1;
    	int currIndex = index_location;
    	while (!file.get(currIndex).contains("(")) {
  	    currIndex++;
    	}
    	while (!file.get(currIndex).contains(";")) {
    	    if (file.get(currIndex).contains("/*")) {
        		while (!file.get(currIndex).contains("*/") && (!file.get(currIndex).contains(";"))) {
      		    currIndex++;
        		}
    	    }
    	    else {
        		length++;
        		currIndex++;
    	    }
    	}
    	return length;
    }

    // Completely takes out the corresponding relation from the catalog.
    private int destroy_handler(ArrayList < String > file, int index_location) {
    	int currIndex = index_location;
    	LinkedList<String> relNames = getRelNames(currIndex, file);

    	String relName;
    	Relation temp_rel;
    	for(int k = 0; k < relNames.size(); k++){
  	    relName = relNames.get(k);
  	    temp_rel = findRelation(relName);
  	    if (temp_rel == null){
  	    	continue;
  	    }
  	    if(temp_rel.getName().equals("CATALOG")){
      		System.out.println("Cannot destroy the catalog itself.");
      		continue;
  	    }
  	    catalog.gettupeList().remove(temp_rel.getmetaData());
  	    rList.remove(temp_rel);
  	    System.out.println("DESTROYED " + temp_rel.getName());
  	    temp_rel = null;
    	}

    	while(!file.get(currIndex).contains(";")){
  	    currIndex++;
    	}
    	return currIndex;
    }

    private LinkedList<String> getRelNames(int currIndex, ArrayList<String> file){
    	LinkedList<String> relNames = new LinkedList<String>();
    	int i = currIndex + 1;
    	while(!file.get(i).contains(";")){
  	    relNames.add(file.get(i));
  	    i++;
    	}
    	relNames.add(file.get(i));
    	return relNames;
    }

    // Similar to destroy but clears the tuple list instead of the whole
    // relation.
    private int delete_handler(ArrayList < String > file, int index_location) {
    	int currIndex = index_location;
    	LinkedList<String> relNames = getRelNames(currIndex, file);

    	if(relNames.contains("WHERE")) { //checks to see if WHERE clause is included
  	    try{
          deleteWhereHandler(relNames);
  	    }
  	    catch(Exception e){
          System.out.println("Bad syntax.");
  	    }
    	}
    	else{
  	    String relName;
  	    Relation temp_rel;
  	    for(int k = 0; k < relNames.size(); k++) {
      		relName = relNames.get(k);
      		temp_rel = findRelation(relName);
      		if(temp_rel != null && !temp_rel.getName().equals("CATALOG")) { //checks if relation even exists
    		    temp_rel.gettupeList().clear();
    		    System.out.println("DELETED " + temp_rel.getName());
    		    temp_rel = null;
      		}
      		else {
    		    System.out.println("Relation doesn't exist OR trying to delete catalog. Bad input.");
      		}
  	    }
    	}
    	while(!file.get(currIndex).contains(";")){
  	    currIndex++;
    	}
    	return currIndex;
    }

    // Handles when the WHERE clause is in a DELETE statement
    private void deleteWhereHandler(LinkedList<String> relNames){
      DeleteWhere dw = new DeleteWhere();
      dw.start(relNames, rList);
    }

    // Parses through attributeName list to get index of desired attribute name
    private int getIndexOfAttributeName(String attributeName, LinkedList<String> attributeNames){
    	int i = 0;
    	int index = 0;
    	while(i < attributeNames.size()){
  	    if(attributeNames.get(i).equals(attributeName)) {
          index = i;
          break;
  	    }
    	  i++;
    	}
    	return index;
    }

    // Returns ArrayList of attributes for the corresponding command to use later.
    private ArrayList<String> getAttributes(ArrayList < String > file, int index_location) {
      ArrayList<String> attributes = new ArrayList<String>();
      int currIndex = index_location;

      attributes = addAttValues(file, attributes, currIndex);
      attributes = fixAttributesArray(attributes);

      return attributes;
    }

    // Replaces all bad characters in the attributes array
    private ArrayList<String> fixAttributesArray(ArrayList<String> attributes) {
      String temp = "";

      for(int i = 0; i < attributes.size(); i++){
        if(attributes.get(i).contains("(")) {
    	    temp = attributes.get(i).replace("(", "");
    	    attributes.set(i, temp);
      	}
        else if(attributes.get(i).contains(")")){
        	temp = attributes.get(i).replace(")", "");
        	attributes.set(i, temp);
        }
        else if(attributes.get(i).contains(";")){
        	temp = attributes.get(i).replace(";", "");
        	attributes.set(i, temp);
        }
        else if(attributes.get(i).contains(",")){
        	temp = attributes.get(i).replace(",", "");
        	attributes.set(i, temp);
        }
        else if(attributes.get(i).contains(" ")){
        	temp = attributes.get(i).replace(" ", "");
        	attributes.remove(i);
        }
      }
      return attributes;
    }

    // Adds values to the attribute array
    private ArrayList<String> addAttValues(ArrayList<String> file, ArrayList<String> attributes, int currIndex) {
    	while (!file.get(currIndex).contains("(")) {
        currIndex++;
    	}

    	while (!file.get(currIndex).contains(";")) {
    	  if (file.get(currIndex).contains("/*")) {
      		while (!file.get(currIndex).contains("*/") && (!file.get(currIndex).contains(";"))) {
    		    currIndex++;
      		}
  	    }
  	    else {
      		attributes.add(file.get(currIndex));
      		currIndex++;
  	    }
    	}
    	attributes.add(file.get(currIndex)); //adding last attribute with the ; in it
    	return attributes;
    }

    public Relation findRelation(String relName) {
    	String fixed_name = relName;
    	fixed_name = fixName(fixed_name, relName);

    	int found = 0;
    	int iterator = 0;
    	Relation temp_rel = rList.get(iterator);
    	while((temp_rel != null && found == 0) && iterator < rList.size()) {
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

	public Relation findTempRelation(String relName) {
		int found = 0;
		int iterator = 0;
		if (tempRList.isEmpty())
			return null;
		Relation temp_rel = tempRList.get(iterator);
		while((temp_rel != null && found == 0) && iterator < tempRList.size()) {
		    if(temp_rel.getName().equals(relName))
		    {
  				found = 1;
  				break;
		    }
		    else
		    {
			    iterator++;
  				if (iterator == tempRList.size())
  				    return null;
				  temp_rel = tempRList.get(iterator);
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

    // Parses through whole catalog and prints each relation name.
    // Use for testing.
    public void listParser() {
    	for(int i = 0; i < rList.size(); i++) {
  	    Relation temp_rel = rList.get(i);
  	    System.out.println("Relation name: " + temp_rel.getName());
  	    temp_rel = rList.get(i);
    	}
    }
}
