import java.util.*;

public class Print
{


    Print(){
    }
        // in charge of displaying relation
	public void displayRelation(Relation relation)
	{
		System.out.println();
		if (relation == null)
		{
			System.out.println("Print error: Relation does not exist.");
			return;
		}
		int totalWidth = findWidth(relation);

		printOuterBorder(totalWidth);	// print header
		displayHeader(relation, totalWidth);

		displayAttributeHeader(relation, totalWidth); // print attribute section
		printInnerBorder(totalWidth);

		displayTupleSpace(relation, totalWidth); // print tuples

		printOuterBorder(totalWidth); 
	}

	// prints an entire relation
	private int findWidth(Relation relation)
	{
		LinkedList<Attribute> attributeList = new LinkedList<Attribute>();
		attributeList = relation.getattributeList();

		// find widths needed for each atttribute
		int widths[] = new int[relation.getnumAttributes()];
		for (int i = 0; i < widths.length; i++)
		{
			Attribute currAttribute = attributeList.get(i);
			if (currAttribute.getlength() > currAttribute.gettitle().length())
			{
				widths[i] = currAttribute.getlength();
			}
			else
			{
				widths[i] = currAttribute.gettitle().length();
			}
		}

		// find total width of table
		int attributeWidths = sumValue(widths);
		int padding = 2 * relation.getnumAttributes(); // add padding to our text
		int borders = 1 + relation.getnumAttributes(); // add space for borders
		int totalWidth = attributeWidths + padding + borders; // total table width

		if (relation.getName().length() > totalWidth)
			return relation.getName().length() + padding + borders;
		else
			return totalWidth;
	}


	// prints the header
	private void displayHeader(Relation relation, int totalWidth)
	{
		// print three rows for heading
		int spaces = totalWidth - 2;
		int wordIndex = 0; 
		int currIndex = 0;

		System.out.print("|");
		for (int i = 0; i < spaces; i++)
		{	
			if (i != 1)
				System.out.print(" ");
			else
			{
				currIndex = i;
				while(currIndex - 1 != relation.getName().length())
				{
					System.out.print(relation.getName().charAt(currIndex - 1));
					currIndex++;
				}
				i = currIndex - 1;
			}
			
		}
		System.out.println("|");

		printInnerBorder(totalWidth);
	}

	private void displayAttributeHeader(Relation relation, int totalWidth)
	{
		int numValues = relation.getnumAttributes();
		int padding = 2 * relation.getnumAttributes(); // add padding to our text
		int borders = 1 + relation.getnumAttributes(); // add space for borders
		int attributeLengths = getAttributeLengths(relation);
		int originalTotalWidth = attributeLengths + padding + borders; // if we just use attribute lengths
		int remainderDist = totalWidth - attributeLengths - padding - borders;		

		for(int i = 0; i < relation.getnumAttributes(); i++) // print attribute header
		{
			displayAttributes(relation, i);
		}

		if (totalWidth > originalTotalWidth)
		{
			for (int j = 0; j < remainderDist; j++)
				System.out.print(" ");
			System.out.println("|");
		}
		else
			System.out.println("|");
	}

	// prints attribute header
	private void displayAttributes(Relation relation, int index)
	{
		int currIndex = 0;
		String attributeTitle = relation.getattributeList().get(index).gettitle();
		int attributeLength = relation.getattributeList().get(index).getlength();
		int spaceLength = attributeLength;
		if (attributeTitle.length() > attributeLength)
			spaceLength = attributeTitle.length();
		int spaces = spaceLength + 2;

		System.out.print("|");
		for (int i = 0; i < spaces; i++)
		{	
			if (i != 1)
				System.out.print(" ");
			else
			{
				currIndex = i;
				while(currIndex - 1 != attributeTitle.length())
				{
					System.out.print(attributeTitle.charAt(currIndex - 1));
					currIndex++;
				}
				i = currIndex - 1;
			}
		}
	}

	// displays entire space containing tuples
	private void displayTupleSpace(Relation relation, int totalWidth)
	{
		int numValues = relation.getnumAttributes();
		int padding = 2 * relation.getnumAttributes(); // add padding to our text
		int borders = 1 + relation.getnumAttributes(); // add space for borders
		int attributeLengths = getAttributeLengths(relation);
		int originalTotalWidth = attributeLengths + padding + borders; // if we just use attribute lengths
		int remainderDist = totalWidth - attributeLengths - padding - borders;

		// print out tuples until there are none left
		int tupleCount = relation.gettupeList().size();
		for (int i = 0; i < tupleCount; i++)
		{
			displayTuple(relation, relation.gettupeList().get(i), totalWidth);

			if (totalWidth > originalTotalWidth)
			{
				for (int j = 0; j < remainderDist; j++)
					System.out.print(" ");
				System.out.println("|");
			}
			else
				System.out.println("|");
		}
	}

	// looks at value list of relation and displays
	private void displayTuple(Relation relation, Tuple tuple, int totalWidth)
	{
		int numValues = relation.getnumAttributes();

		for (int i = 0; i < numValues; i++)
			displayTupleValue(relation, tuple.getvalueList().get(i), i);
	}

	// displays individual values
	private void displayTupleValue(Relation relation, Value value, int index)
	{
		String valueForDisplay = "";

		int attributeLength = relation.getattributeList().get(index).getlength();
		int spaceLength = attributeLength;
		String attributeTitle = relation.getattributeList().get(index).gettitle();
		if (attributeTitle.length() > attributeLength)
			spaceLength = attributeTitle.length();
		int spaces = spaceLength + 2;
		int currIndex = 0;

		if (value.getvalueInt() == null)					// obtain value
			valueForDisplay = value.getvalueString();
		else
			valueForDisplay = value.getvalueInt().toString();

		System.out.print("|");
		for (int i = 0; i < spaces; i++)
		{	
			if (i != 1)
				System.out.print(" ");
			else
			{
				currIndex = i;
				while(currIndex - 1 != valueForDisplay.length())
				{
					System.out.print(valueForDisplay.charAt(currIndex - 1));
					currIndex++;
				}
				i = currIndex - 1;
			}
		}
	}

	// prints the top and bottom borders of the relation 
	private void printOuterBorder(int totalWidth)
	{
		for (int i = 0; i < totalWidth; i++)
			System.out.print("*");
		System.out.println();

	}

	// prints the borders within the relation 
	private void printInnerBorder(int totalWidth)
	{
		for (int i = 0; i < totalWidth; i++)
			System.out.print("-");
		System.out.println();
	}

	// helper function for displayRelation()
	private int sumValue(int[] intArr)
	{
		int total = 0;
		for (int i = 0; i < intArr.length; i++)
		{
			total += intArr[i];
		}
		return total;
	}

	// helper function to find attribute lengths
	private int getAttributeLengths(Relation relation)
	{
		int numValues = relation.getnumAttributes();
		int totalLength = 0;
		int currAttributeLength = 0;
		int currentAttributeTitleLength = 0;
		for (int i = 0; i < numValues; i++)
		{
			currAttributeLength = relation.getattributeList().get(i).getlength();
			currentAttributeTitleLength = relation.getattributeList().get(i).gettitle().length();
			if (currAttributeLength > currentAttributeTitleLength)
				totalLength += currAttributeLength;
			else
				totalLength += currentAttributeTitleLength;
		}
		return totalLength;
	}
}
