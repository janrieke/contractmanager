import de.janrieke.contractmanager.server.DBSupportH2Impl;

/**
 * New flag to snap period ends to end of selected period, e.g., MONTHs.
 */
public class update0023 extends AbstractUpdate {
	/**
	 * Default constructor
	 */
	public update0023() {
		// Update for H2
		statements.put(DBSupportH2Impl.class.getName(),
				"ALTER TABLE contract ADD COLUMN runtime_snap_to_end int(1);\n");
	}

	/**
	 * @see de.willuhn.sql.version.Update#getName()
	 */
	@Override
	public String getName() {
		return "Database update v22 to v23 (add snap to end of period flag)";
	}
}