import java.util.LinkedList;

public class Join
{
	private Relation _leftRelation = null;
    private Relation _rightRelation = null;
    private Relation _joinedRelation = null;
    private String _relationName = null;
    private LinkedList<String> _conditions = null;

	// constructor
	public Join(Relation leftRelation, Relation rightRelation, String relationName, LinkedList<String> conditions)
	{
		this._leftRelation = leftRelation;
		this._rightRelation = rightRelation;
		this._relationName = relationName;
		this._conditions = conditions;
		this.createJoinedRelation();
	}

	// creates joined relation
	private void createJoinedRelation()
	{
		int retVal = 0;
		this._joinedRelation = new Relation(this._relationName);
		retVal = this.addAttributes();
		if (retVal == -1)
		{
			this.reset();
			return;
		}
		retVal = this.insertTuples();
		if (retVal == -1)
		{
			this.reset();
			return;
		}
	}

	// adds al attributes to one table
	private int addAttributes()
	{
		// first check to see if each relation has at least one tuple
		if (this._leftRelation.gettupeList().size() == 0)
		{
			System.out.println("Join error: Tuples must have at least one relation.");
			return -1;
		}
		else if (this._leftRelation.gettupeList().size() == 0)
		{
			System.out.println("Join error: Tuples must have at least one relation");
			return -1;
		}
		// add from left table
		for (int i = 0; i < this._leftRelation.getnumAttributes(); i++)
		{

			String currName = this._leftRelation.getAttributeNameList().get(i);
			String currType = this._leftRelation.getAttributeType().get(i);
			int currLength = this._leftRelation.getAttributeLength().get(i);
			this._joinedRelation.addAttribute(currName, currType, currLength);
			this._joinedRelation.constructAttribute(i);
		}

		// now add from right table
		int overallIndex = this._leftRelation.getnumAttributes();
		int relationIndex = 0;
		while(relationIndex < this._rightRelation.getnumAttributes())
		{
			String currName = this._rightRelation.getAttributeNameList().get(relationIndex);
			String currType = this._rightRelation.getAttributeType().get(relationIndex);
			int currLength = this._rightRelation.getAttributeLength().get(relationIndex);
			this._joinedRelation.addAttribute(currName, currType, currLength);
			this._joinedRelation.constructAttribute(overallIndex);
			relationIndex++;
			overallIndex++;
		}

		return 0;
	}

	// inserts tuples into our mega-relation
	private int insertTuples()
	{
		String operator = "" + this._conditions.get(1);
		if (!operator.equals("="))
		{
			System.out.println("Join error: Invalid operator");
			return -1;
		}

		// check for ambiguity
		if (this._conditions.get(0).equals(this._conditions.get(2)))
		{
			System.out.println("Join error: Ambiguous case- use qualifiers of different relations.");
			return -1;
		}

		// set up for making the big tuples
		int leftIndex = findLeftIndex();
		if (leftIndex == -1)
			return -1;

		int rightIndex = findRightIndex();
		if (rightIndex == -1)
			return -1;

		String leftValue = this._leftRelation.getattributeList().get(leftIndex).getValue();
		String rightValue = this._rightRelation.getattributeList().get(rightIndex).getValue();

		if (!leftValue.equals(rightValue))
		{
			System.out.println("Join error: Operands must be of the same type.");
			return -1;
		}

		int leftTupeCount = this._leftRelation.gettupeList().size();
		int rightTupeCount = this._rightRelation.gettupeList().size();

		LinkedList<Tuple> leftTupeList = copyList(this._leftRelation.gettupeList());
		LinkedList<Tuple> rightTupeList = copyList(this._rightRelation.gettupeList());

		int retVal = 0;

		retVal = findTuples(leftTupeList, rightTupeList, leftIndex, rightIndex);
		if (retVal == -1)
			return -1;
		else
			return 0;
	}

	// finds index of relevant attribute on left table
	private int findLeftIndex()
	{
		// find index of attribute on left side
		int leftIndex = -1;
		String leftOperand = this._conditions.get(0);

		for(int i = 0; i < this._leftRelation.getnumAttributes(); i++)
		{
			Attribute currAttribute = this._leftRelation.getattributeList().get(i);
			if (leftOperand.equals(currAttribute.gettitle()) || leftOperand.equals(currAttribute.getqualifiedTitle()))
			{
				leftIndex = i;
				break;
			}
		}

		if (leftIndex == -1)
			System.out.println("Join error: Invalid left operand");
		return leftIndex;
	}

	// finds index of relevant attribute on right table
	private int findRightIndex()
	{
		// find index of attribute on right side
		int rightIndex = 0;
		String rightOperand = this._conditions.get(2);
		for(int i = 0; i < this._leftRelation.getnumAttributes(); i++)
		{
			Attribute currAttribute = this._rightRelation.getattributeList().get(i);
			if (rightOperand.equals(currAttribute.gettitle()) || rightOperand.equals(currAttribute.getqualifiedTitle()))
			{
				rightIndex = i;
				break;
			}
		}

		if (rightIndex == -1)
			System.out.println("Join error: Invalid right operand");
		return rightIndex;	
	}

	// make tuples to put into the combined relations
	private int findTuples(LinkedList<Tuple> leftTupeList, LinkedList<Tuple> rightTupeList, int leftIndex, int rightIndex)
	{
		LinkedList<Value> workingTuple = new LinkedList<Value>();
		Value testValue = leftTupeList.get(0).getvalueList().get(leftIndex);
		boolean isInt = false;
		if (testValue.getvalueInt() != null)
			isInt = true;

		// go through tuple list of left table
		for (int i = 0; i < leftTupeList.size(); i++)
		{
			Tuple currLeftTuple = leftTupeList.get(i);
			for (int j = 0; j < rightTupeList.size(); j++) // go through right table
			{
				Tuple currRightTuple = rightTupeList.get(j);
				Value currLeftValue = currLeftTuple.getvalueList().get(leftIndex);
				Value currRightValue = currRightTuple.getvalueList().get(rightIndex);
				LinkedList<Value> currRightValueList = currRightTuple.getvalueList();
				LinkedList<Value> currLeftValueList = currLeftTuple.getvalueList();
				if (isInt) // NUM
				{
					if (currLeftValue.getvalueInt() == currRightValue.getvalueInt())
					{
						workingTuple = addElements(currLeftValueList, currRightValueList); // combines both tuples
						Tuple combinedTuple = new Tuple(workingTuple);
						this._joinedRelation.gettupeList().add(combinedTuple);
						rightTupeList.remove(j); j--;
					}
					else
						continue;
				}
				else // CHAR
				{
					if (currLeftValue.getvalueString().equals(currRightValue.getvalueString()))
					{
						workingTuple = addElements(currLeftValueList, currRightValueList); // combines both tuples
						Tuple combinedTuple = new Tuple(workingTuple);
						this._joinedRelation.gettupeList().add(combinedTuple);
						rightTupeList.remove(j); j--;
					}
					else
						continue;
				}
			}
		}
		return 0;
	}

	// adds two value lists together
	private LinkedList<Value> addElements(LinkedList<Value> linkedList1, LinkedList<Value> linkedList2)
	{
		if ((linkedList1.size() == 0) || (linkedList2.size() == 0))
			return linkedList1;

		LinkedList<Value> bigList = new LinkedList<Value>();

		for (int i = 0; i < linkedList1.size(); i++)
		{
			bigList.add(linkedList1.get(i));
		}


		for (int i = 0; i < linkedList2.size(); i++)
		{
			bigList.add(linkedList2.get(i));
		}
		return bigList;
	}

	// gives a deep copy of a linked list
	private LinkedList<Tuple> copyList(LinkedList<Tuple> linkedList)
	{
		LinkedList<Tuple> copy = new LinkedList<Tuple>();
		for (int i = 0; i < linkedList.size(); i++)
		{
			copy.add(linkedList.get(i));
		}
		return copy;
	}

	// resets object
	private void reset()
	{
    	this._leftRelation = null;
    	this._rightRelation = null;
    	this._joinedRelation = null;
    	this._relationName = null;
    	this._conditions = null;
	}

	public Relation getjoinedRelation()
	{
		return this._joinedRelation;
	}

}