package org.nectarframework.base.service.translation;

import java.sql.SQLException;
import java.util.Locale;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.cache.CacheService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.mysql.MysqlService;
import org.nectarframework.base.service.mysql.PrSt;
import org.nectarframework.base.service.mysql.ResultRow;
import org.nectarframework.base.service.mysql.ResultTable;
import org.nectarframework.base.tools.ByteArray;

public class TranslationService extends Service {

	private boolean useCache = false;
	private String databaseTable = "w_lang";

	private MysqlService my;
	private CacheService cacheService;

	@Override
	public void checkParameters() throws ConfigurationException {
		useCache = serviceParameters.getBoolean("useCache", useCache);
		databaseTable = serviceParameters.getString("databaseTable", databaseTable);
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		my = (MysqlService) dependancy(MysqlService.class);
		cacheService = (CacheService) dependancy(CacheService.class);
		return true;
	}

	@Override
	protected boolean init() {
		return true;
	}

	@Override
	protected boolean run() {
		PrSt st = new PrSt(
				"SELECT localeLanguage, localeCountry, localeVariant, namespace, messageKey, translatedText FROM w_lang");
		try {
			ResultTable rt = my.select(st);
			ByteArray ba = new ByteArray();
			for (ResultRow rr : rt) {
				String keyStr = "translate:" + rr.getString("localeLanguage") + "." + rr.getString("localeCountry")
						+ "." + rr.getString("localeVariant") + "-" + rr.getString("namespace") + "/"
						+ rr.getString("messageKey");
				ba.reset();
				ba.add(rr.getString("translatedText"));
				cacheService.set(keyStr, ba.getBytes());
			}
		} catch (SQLException e) {
			Log.fatal(e);
			return false;
		}

		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	public String get(Locale locale, String namespace, String key, Object[] messageParameters) {
		// TODO implement cache
		// TODO implement pluralArgMap
		
		String keyStr = locale.getLanguage() + "." + locale.getCountry() + "." + locale.getVariant() + "-" + namespace
				+ "/" + key;

		byte[] barr = cacheService.getByteArray("translate:" + keyStr, true);

		if (barr != null) {
			ByteArray ba = new ByteArray(barr);
			return ba.getString();
		}

		PrSt st = new PrSt(
				"SELECT translatedText FROM w_lang WHERE localeLanguage = ? AND localeCountry = ? AND localeVariant = ? AND namespace = ? AND messageKey = ?");

		
		st.setString(1, locale.getLanguage());
		st.setString(2, locale.getCountry());
		st.setString(3, locale.getVariant());
		st.setString(4, namespace);
		st.setString(5, key);

		try {
			ResultTable rt = my.select(st, 100000);
			if (rt.rowCount() <= 0) {
				Log.warn("TranslationService.get: did not find " + keyStr);
				return "??-" + keyStr + "-??";
			}

			ByteArray ba = new ByteArray();
			ba.add(rt.getString(0, "translatedText"));
			cacheService.set("translate:" + keyStr, ba.getBytes());
			
			return rt.getString(0, "translatedText");

		} catch (SQLException e) {
			Log.warn(e);
			return null;
		}

	}

}
