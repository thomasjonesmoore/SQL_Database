import java.io.*;

// main purpose is to create and store temporary variables
public class TempHandler implements java.io.Serializable
{
	private Relation _opRel = null; // temporary relation just received from an operation

	public TempHandler(Relation relation, String relName, Parser p)
	{
		this._opRel = deepCopy(relation);
		this._opRel.setName(relName);
		addNewTemp(p, relName); // adds to temp relation list
	}

	// Adds new temp relation
	private void addNewTemp(Parser p, String oldrelName)
	{
		int index = 0; // relation index
		String relName = this._opRel.getName();
		Relation tempRelation = this._opRel;

		// replaces attributes
		for (int i = 0; i < tempRelation.getattributeList().size(); i++)
		{
			String newTitle = tempRelation.getattributeList().get(i).getqualifiedTitle().replace(oldrelName, tempRelation.getName());
			tempRelation.getattributeList().get(i).setqualifiedTitle(newTitle);
		}

		// first check to see if a relation exists
		if ((p.tempRList.isEmpty()) || (findTempRelation(relName, p) == null))
		{
			p.tempRList.add(tempRelation);
		} // overWrite
		else
		{
			index = findTempRelationIndex(relName, p);
			p.tempRList.set(index, tempRelation);
		}
	}

	private Relation findTempRelation(String relName, Parser p)
	{
		int found = 0;
		int iterator = 0;
		Relation temp_rel = p.tempRList.get(iterator);
		while((temp_rel != null && found == 0) && iterator < p.tempRList.size())
		{
		    if(temp_rel.getName().equals(relName))
		    {
				found = 1;
				break;
		    }
		    else
		    {
			    iterator++;
				if (iterator == p.tempRList.size())
				    return null;
				temp_rel = p.tempRList.get(iterator);
		    }
		}
		return temp_rel;
    }

	private int findTempRelationIndex(String relName, Parser p)
	{
		int found = 0;
		int iterator = 0;
		Relation temp_rel = p.tempRList.get(iterator);
		while((temp_rel != null && found == 0) && iterator < p.tempRList.size())
		{
		    if(temp_rel.getName().equals(relName))
		    {
				found = 1;
				break;
		    }
		    else
		    {
			    iterator++;
				if (iterator == p.tempRList.size())
				    return 0;
				temp_rel = p.tempRList.get(iterator);
		    }
		}
		return iterator;
    }

    // returns a deep copy of relation
    private Relation deepCopy(Relation relation)
    {
    	try
    	{
	    	// convert relation to stream of bytes
	 		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	 		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
	 		objectOutputStream.writeObject(relation);
	 		objectOutputStream.flush();
	 		objectOutputStream.close();
	 		byteArrayOutputStream.close();
	 		byte[] byteData = byteArrayOutputStream.toByteArray();

	 		// restore relation from stream of bytes
	 		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteData);
	 		relation = (Relation) new ObjectInputStream(byteArrayInputStream).readObject();
	 		return relation;
 		}
 		catch(Exception e)
 		{
 			System.out.println(e.toString());
 			System.exit(1);
 		}
 		return null;
    }
}
