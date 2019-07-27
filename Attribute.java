public class Attribute implements java.io.Serializable
{
	private String _title; // name of attribute
	private String _value; // value of attribute
	private int _length; // length of attribute
	private String _qualifiedTitle; // qualified title of attribute

	public Attribute(String title, String value, int length, String qualifiedTitle)
	{
		this._title = title; 
		this._value = value;
		this._length = length;
		this._qualifiedTitle = qualifiedTitle;
	}

	// for debugging
	@Override
	public String toString()
	{
		return "Title: " + this._title + " Value: " + this._value + " Length: " + this._length;
	}

	// get length
	public int getlength()
	{
		return this._length;
	}

	// get title
	public String gettitle()
	{
		return this._title;
	}

    public String getValue(){
		return _value;
    }

    public String getqualifiedTitle()
    {
    	return this._qualifiedTitle;
    }

    public void setqualifiedTitle(String title)
    {
    	this._qualifiedTitle = title;
    }
}
