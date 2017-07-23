import de.janrieke.contractmanager.server.DBSupportH2Impl;

/**
 * New flag to snap period ends to end of selected period, e.g., MONTHs.
 */
public class update0024 extends AbstractUpdate {
	/**
	 * Default constructor
	 */
	public update0024() {
		// Update for H2
		statements.put(DBSupportH2Impl.class.getName(),
				"ALTER TABLE costs ADD COLUMN payday date;\n");
	}

	/**
	 * @see de.willuhn.sql.version.Update#getName()
	 */
	@Override
	public String getName() {
		return "Database update v23 to v24 (add payday for costs)";
	}
}