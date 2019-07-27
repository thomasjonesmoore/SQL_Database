import java.util.*;
import java.io.*;

public class Tuple implements java.io.Serializable
{
    private LinkedList<Value> _valueList; // list of values 
    // note: length should match attribute length
    
    public Tuple(LinkedList<Value> valueList)
    {
	   this._valueList = valueList; 
    }
    
    // getter for value list
    public LinkedList<Value> getvalueList()
    {
	   return this._valueList;
    } 

    @Override
    public String toString()
    {
        return this._valueList.toString() + "\n";
    }
}
