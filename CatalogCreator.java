public class CatalogCreator
{
	// creates the catalog
	public static Relation createCatalog(Parser p)
	{
		Relation catalog = new Relation("CATALOG");

		Attribute relationName = new Attribute("RELNAME", "CHAR", 30, "CATALOG.RELNAME");
		Attribute relationSchema = new Attribute("RELSCHEMA", "CHAR", 100, "CATALOG.RELSCHEMA");

		catalog.getattributeList().add(relationName);
		catalog.getattributeList().add(relationSchema);

		catalog.setnumAttributes(2);
		p.getRList().add(catalog);

		return catalog;
	}
}
