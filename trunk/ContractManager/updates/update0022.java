import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import de.janrieke.contractmanager.rmi.ContractDBService;
import de.janrieke.contractmanager.server.DBSupportH2Impl;
import de.janrieke.contractmanager.server.ContractDBUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Remind me later function
 */
public class update0022 implements Update {
	private Map<String, String> statements = new HashMap<String, String>();

	/**
	 * Default constructor
	 */
	public update0022() {
		// Update for H2
		statements.put(DBSupportH2Impl.class.getName(),
				"ALTER TABLE settings ALTER COLUMN key RENAME TO mkey;\n" +
				"\n");
	}

	/**
	 * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
	 */
	public void execute(UpdateProvider provider) throws ApplicationException {
		ContractDBUpdateProvider myProvider = (ContractDBUpdateProvider) provider;
		I18N i18n = myProvider.getResources().getI18N();

		// Get the SQL dialect
		String driver = ContractDBService.SETTINGS.getString("database.driver",
				DBSupportH2Impl.class.getName());
		String sql = (String) statements.get(driver);
		if (sql == null)
			throw new ApplicationException(i18n.tr(
					"Database {0} not supported", driver));

		try {
			ScriptExecutor
					.execute(new StringReader(sql), myProvider.getConnection(),
							myProvider.getProgressMonitor());
			myProvider.getProgressMonitor().log(i18n.tr("Database updated."));
		} catch (ApplicationException ae) {
			throw ae;
		} catch (Exception e) {
			Logger.error("unable to execute update", e);
			throw new ApplicationException(
					i18n.tr("Error during database update"), e);
		}
	}

	/**
	 * @see de.willuhn.sql.version.Update#getName()
	 */
	public String getName() {
		return "Database update v21 to v22 (add support for mysql)";
	}

}