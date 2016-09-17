import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import de.janrieke.contractmanager.rmi.ContractDBService;
import de.janrieke.contractmanager.server.ContractDBUpdateProvider;
import de.janrieke.contractmanager.server.DBSupportH2Impl;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

public abstract class AbstractUpdate implements Update {

	protected Map<String, String> statements = new HashMap<>();

	public AbstractUpdate() {
		super();
	}

	/**
	 * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
	 */
	@Override
	public void execute(UpdateProvider provider) throws ApplicationException {
		ContractDBUpdateProvider myProvider = (ContractDBUpdateProvider) provider;
		I18N i18n = myProvider.getResources().getI18N();

		// Get the SQL dialect
		String driver = ContractDBService.SETTINGS.getString("database.driver",
				DBSupportH2Impl.class.getName());
		String sql = statements.get(driver);
		if (sql == null) {
			throw new ApplicationException(i18n.tr(
					"Database {0} not supported", driver));
		}

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

}