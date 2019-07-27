import java.util.*;
import java.io.*;

public class Relation implements java.io.Serializable
{
    // properties
    // note: will be using parallel linked lists to store general schema information
    private LinkedList<String> _attributeName = new LinkedList<String>(); // stores attribute names
    private LinkedList<String> _type = new LinkedList<String>(); // stores attribute types
    private LinkedList<Integer> _length = new LinkedList<Integer>(); // store attribute lengths
    private LinkedList<Tuple> _tupeList = new LinkedList<Tuple>(); // stores tuples
    private LinkedList<Attribute> _attributeList = new LinkedList<Attribute>(); // stores attribute titles
    private int _numAttributes = 0; // count of how many attributes are in the relation
    private String _relationName; // name of relation
    private String _relationSchema = ""; // schema of relation
    private LinkedList<Value> _metaDataList = new LinkedList<Value>(); // to store name and schema
    private Tuple _metaData; // to go into catalog

    // constructor
    Relation(String relationName)
    {
	   this._relationName = relationName;
	   this._attributeName = new LinkedList<String>();
	   this._type = new LinkedList<String>();
	   this._length = new LinkedList<Integer>();
       this._metaDataList.add(new Value(relationName));
       this._metaData = new Tuple(this._metaDataList);
    }


    // Purpose: displays a message after a relation is created
    // Parameters: None
    // Returns: Nothing
    private void display()
    {
	   System.out.println("Creating " + _relationName + " with " +
			   _numAttributes + " attributes.");
    }

    // Purpose: adds a new attribute to our relation
    // Parameters: attribute to be added, type of attribute, length of attribute field
    // Returns: Nothing
    public void addAttribute(String attributeName, String type, int length)
    {
	   this._attributeName.add(attributeName);
	   this._type.add(type);
	   this._length.add(length);
       if (this._attributeName.size() == 1)
       {
            this._relationSchema += attributeName;
            this._metaData.getvalueList().add(new Value(this._relationSchema));
       }
       else
       {
            this._relationSchema += " " + attributeName; // update schema
            this._metaData.getvalueList().set(1, new Value(this._relationSchema));
        }

    }

    // Purpose: constructs an attribute from the parallel list values and adds it to the attribute linked list
    // Parameters: index to get values from parallel list
    // Returns: Nothing
    public void constructAttribute(int index)
    {
        String currName = this._attributeName.get(index);
        String currType = this._type.get(index);
        int currLength = this._length.get(index);
        String qualifiedTitle = this._relationName + "." + currName;
        Attribute attribute = new Attribute(currName, currType, currLength, qualifiedTitle);
        this._attributeList.add(attribute); // adds attribute to our list
        this._numAttributes++;
    }

    @Override
    public String toString()
    {
        return "Name: " + this._relationName + " Attributes: " + this._attributeList;
    }

    // Getter for numAttributes
    public int getnumAttributes()
    {
        return this._numAttributes;
    }

    // Setter for numAttributes
    public void setnumAttributes(int numAttributes)
    {
        this._numAttributes = numAttributes;
    }

    // Getter for _attributeList
    public LinkedList<Attribute> getattributeList()
    {
        return this._attributeList;
    }

    // Getter for _tupeList
    public LinkedList<Tuple> gettupeList()
    {
        return this._tupeList;
    }

    // Setter for _tupeList
    public void settupeList(LinkedList<Tuple> tupeList)
    {
        this._tupeList = tupeList;
    }

    // Getter for types
    public LinkedList<String> getAttributeType() {
      return this._type;
    }

    // Getter for length
    public LinkedList<Integer> getAttributeLength() {
      return this._length;
    }

    // Getter for name
    public String getName()
    {
	   return this._relationName;
    }

    public void setName(String name)
    {
        this._relationName = name;
    }

    // Getter for schema
    public String getrelationSchema()
    {
        return this._relationSchema;
    }

    // Getter for metadata
    public Tuple getmetaData()
    {
        return this._metaData;
    }

    public LinkedList<String> getAttributeNameList(){
	return _attributeName;
    }

}
