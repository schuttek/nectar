package org.nectarframework.base.service.translation;

import java.sql.SQLException;
import java.util.Locale;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.mysql.MysqlService;
import org.nectarframework.base.service.mysql.PrSt;
import org.nectarframework.base.service.mysql.ResultTable;

public class TranslationService extends Service {

	private boolean useCache = false;
	private String databaseTable = "w_lang";

	private MysqlService my;

	@Override
	public void checkParameters() throws ConfigurationException {
		useCache = serviceParameters.getBoolean("useCache", useCache);
		databaseTable = serviceParameters.getString("databaseTable", databaseTable);
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		my = (MysqlService) dependancy(MysqlService.class);
		return true;
	}

	@Override
	protected boolean init() {
		return true;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	public String get(Locale locale, String namespace, String key, Object[] messageParameters) {
		// TODO implement cache
		// TODO implement pluralArgMap
		PrSt st = new PrSt("SELECT translatedText FROM w_lang WHERE localeLanguage = ? AND localeCountry = ? AND localeVariant = ? AND namespace = ? AND messageKey = ?");

		
		
		st.setString(1, locale.getLanguage());
		st.setString(2, locale.getCountry());
		st.setString(3, locale.getVariant());
		st.setString(4, namespace);
		st.setString(5, key);

		try {
			ResultTable rt = my.select(st);
			if (rt.rowCount() <= 0) {
				Log.trace("TranslationService.get: did not find "+locale.getLanguage()+" "+locale.getCountry()+" "+locale.getVariant()+" "+namespace+" "+key);
				return null;
			}

			return rt.getString(0, "translatedText");

		} catch (SQLException e) {
			Log.warn(e);
			return null;
		}

	}

}
