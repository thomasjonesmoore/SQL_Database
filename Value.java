public class Value implements java.io.Serializable
{
	private String _valueString;
	private Integer _valueInt;

	// Constructor if value is a string
	public Value(String valueString)
	{
		this._valueString = valueString;
	}

	// Constructor if value is a int
	public Value(Integer valueInt)
	{
		this._valueInt = valueInt;
	}

	// get valueString
	public String getvalueString()
	{
		return this._valueString;
	}

	// set valueString
	public void setvalueString(String stringRep)
	{
		this._valueString = stringRep;
	}

	// get valueInt
	public Integer getvalueInt()
	{
		return this._valueInt;
	}

	@Override
	public String toString()
	{
		if (this._valueInt == null)
			return this._valueString + " ";
		else
			return "" + this._valueInt + " ";
	}
}
