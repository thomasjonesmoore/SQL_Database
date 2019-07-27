import java.util.LinkedList;
import java.util.ArrayList;

public class JoinHandler
{
	public static final int CONDITIONPARTS = 3;

	Relation _joinedRelation = null;

	public JoinHandler()
	{
	}

	// begins the process of joining two relations
	public int beginJoin(ArrayList<String> everything, int currIndex, Parser p)
	{
		// get left and right relations
		currIndex++;
		Relation leftRelation = getRelation(everything.get(currIndex), p);
		if (leftRelation == null)
			return -1;
		currIndex++;
		Relation rightRelation = getRelation(everything.get(currIndex), p);
		if (rightRelation == null)
			return -1;

		String joinedName = leftRelation.getName() + "/" + rightRelation.getName();

		currIndex++;
		if (!everything.get(currIndex).equals("ON"))
		{
			System.out.println("Join error: JOIN <LeftRelation>, <RightRelation> ON <JoinCondition>");
			return -1;
		}
		currIndex++;

		LinkedList<String> conditions = new LinkedList<String>();

		for (int i = 0; i < CONDITIONPARTS; i++) // set up condition
		{
			String currString = everything.get(currIndex);
			if (i == (CONDITIONPARTS - 1))
				currString = currString.replaceAll(";", "");

			conditions.add(currString);
			currIndex++;
		}

		Join join = new Join(leftRelation, rightRelation, joinedName, conditions);
		this._joinedRelation = join.getjoinedRelation();
		if (this._joinedRelation == null)
			return -1;

		return currIndex;	
	}

	// gets relation given a relation name
	private Relation getRelation(String relName, Parser p)
	{
		relName = relName.replace(",", "");
		Relation relation = p.findRelation(relName);
		if (relation == null)
		{
		    relation = p.findTempRelation(relName);

		    if(relation == null){
				System.out.println("Join error: Relation does not exist");
				return null;
		    }
		}
		return relation;
	}

	public Relation getjoinedRelation()
	{
		return this._joinedRelation;
	}

}
