import java.util.*;

public class NestedExpressions
{
    private Relation retRelation = null;

    private Relation currRelation;
    private Project project;
    private Select selector;
    private Parser parser;
    private JoinHandler joiner;

    public void NestedExpressions() {}

    // Called once for each nested statement. Sets the fields for the particular relation
    // that allow the recursiveness to access previously stored Relations from the inner
    // expressions. Returns the index for Parser.java to continue parsing file.
    public int setup(int currIndex, ArrayList<String> everything, Project project, Select selector, Parser parser, JoinHandler joiner) // sets up nested expressions
    {
    	this.project = project;
    	this.selector = selector;
    	this.parser = parser;
    	this.joiner = joiner;

    	// for storing our first nested expression
    	ArrayList<String> nestedExpression = getInitialStatement(currIndex, everything);

    	// find matching parenthesis, collecting the nested expression
    	if (nestedExpression == null)
        return -1;

    	this.retRelation = findMatchingParen(nestedExpression);

    	currIndex += nestedExpression.size();
    	return currIndex;
    }

    // Returns the initial full nested statement taken from the 'everything' array
    private ArrayList<String> getInitialStatement(int currIndex, ArrayList<String> everything){
    	ArrayList<String> statement = new ArrayList<String>();
    	int i = currIndex;
    	String currWord = everything.get(i);
    	i++;
    	statement.add(currWord);
    	while(i < everything.size() && !everything.get(i).contains(";")){
    	    currWord = everything.get(i);
    	    statement.add(currWord);
    	    i++;
    	}
    	if(i < everything.size() && everything.get(i).contains(";")){ //if at end of statement
  	    currWord = everything.get(i).replace(";", "");

  	    if(currWord.charAt(currWord.length() - 1) != ')'){
      		System.out.println("Nested expression error: Bad parenthesis placement."); // if no ending parenthesis
      		return null;
  	    }

  	    statement.add(currWord.substring(0, currWord.length()));
    	}
    	return statement;
    }

    // The recursive call that keeps calling itself until inner most expression.
    // Checks for correct parenthesis while parsing.
    // Returns a Relation for the outer expression to use.
    private Relation findMatchingParen(ArrayList<String> everything)
    {
    	ArrayList<String> nestedExpression = new ArrayList<String>();
    	int currIndex = getFirstParenthesisIndex(everything);
    	int paren = 0;
    	int wordIndex = 0;
    	boolean searching = true;

    	// take care of beginning
    	String currWord = everything.get(currIndex);
    	if (currWord.charAt(0) != '(') {
    		System.out.println("Nested expression error: Parenthesis in the middle of a word.");
    		return null;
    	}
    	currWord = currWord.substring(1, currWord.length());
    	nestedExpression.add(currWord);
    	paren++; currIndex++;

    	while(searching) {
    		if (currIndex == everything.size()) {
    			System.out.println("Unmatched (");
    			return null;
    		}
    		currWord = everything.get(currIndex);
    		currWord = currWord.replace(";", "");

    		int parenEndIndex = 0;
    		for (int i = 0; i < currWord.length(); i++) {
          if (currWord.charAt(i) == '(')
    			    paren++;
    			else if (currWord.charAt(i) == ')')
    			    paren--;

    			if (paren == 0) {
    				parenEndIndex = i;
    				break;
    			}
    		}

    		if (paren != 0) { // still searching
    			nestedExpression.add(currWord);
    			currIndex++;
    		}
    		else { // done!
          currWord = currWord.substring(0, parenEndIndex);
          nestedExpression.add(currWord);
          searching = false;
          currIndex--;
          break;
        }
    	}

    	if(getNumOfParenthesis(nestedExpression) != 0){ //recursive call until at innermost expression
  	    findMatchingParen(nestedExpression);
    	}

    	if(nestedExpression.contains("SELECT")){ // If operation is select
  	    selector.selectHandler(nestedExpression, 0, parser, selector);
  	    currRelation = selector.getTempRelation();
        searching = false;
    	}
    	else if(nestedExpression.contains("PROJECT")){ // If operation is project
  	    project.projectHandler(nestedExpression, 0, parser, selector, project);
  	    currRelation = project.getTempRelation();
        searching = false;
    	}
	     else if(nestedExpression.contains("JOIN")){ // If operation is join
  	    joiner.beginJoin(nestedExpression, 0, parser);
  	    currRelation = joiner.getjoinedRelation();
  	    TempHandler tempInit = new TempHandler(currRelation, "Query", parser);
  	    System.out.println(currRelation.getName());
        searching = false;
    	}
    	return currRelation;
    }

    // Returns the index of first parenthesis in nested expression
    private int getFirstParenthesisIndex(ArrayList<String> nestedExpression) {
    	int i = 0;
    	boolean found = false;
    	while(found == false && i < nestedExpression.size()){
    	  if(nestedExpression.get(i).contains("(")){
      		found = true;
      		break;
    	  }
    	  i++;
    	}
    	return i;
    }

    // Returns number of parenthesis left in the nested expression
    private int getNumOfParenthesis(ArrayList<String> nestedExpression){
    	int count = 0;
    	int i = 0;
    	while(i < nestedExpression.size()){
  	    if(nestedExpression.get(i).contains("(") || nestedExpression.get(i).contains(")")){
          count++;
  	    }
    	  i++;
    	}
    	return count;
    }

    // Returns the current relation, called recursively up the tree
    public Relation getretRelation()
    {
      return this.retRelation;
    }
}
