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
		return loadAll();
	}

	private boolean loadAll() {
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

		String keyStr1 = "translate:" + locale.getLanguage() + "-" + namespace + "/" + key;
		String keyStr2 = "translate:" + locale.getLanguage() + "." + locale.getCountry() + "-" + namespace + "/" + key;
		String keyStr3 = "translate:" + locale.getLanguage() + "." + locale.getCountry() + "." + locale.getVariant()
				+ "-" + namespace + "/" + key;

		byte[] barr = cacheService.getByteArray(keyStr3, true);
		if (barr != null) {
			ByteArray ba = new ByteArray(barr);
			return ba.getString();
		} else {
			barr = cacheService.getByteArray(keyStr2, true);
			if (barr != null) {
				ByteArray ba = new ByteArray(barr);
				return ba.getString();
			} else {
				barr = cacheService.getByteArray(keyStr1, true);
				if (barr != null) {
					ByteArray ba = new ByteArray(barr);
					return ba.getString();
				}
			}
		}

		PrSt st = new PrSt(
				"SELECT localeLanguage, localeCountry, localeVariant, namespace, messageKey, translatedText FROM w_lang WHERE namespace = ? AND messageKey = ?");

		st.setString(1, namespace);
		st.setString(2, key);

		String[] bestCandidate = new String[3];
		bestCandidate[0] = bestCandidate[1] = bestCandidate[2] = null;
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

				if (locale.getLanguage().equals(rr.getString("localeLanguage"))) {
					if (locale.getCountry().equals(rr.getString("localeCountry"))) {
						if (locale.getVariant().equals(rr.getString("localeVariant"))) {
							bestCandidate[2] = rr.getString("translatedText");
						}
						bestCandidate[1] = rr.getString("translatedText");
					}
					bestCandidate[0] = rr.getString("translatedText");
				}
			}
		} catch (SQLException e) {
			Log.warn(e);
			return null;
		}

		if (bestCandidate[2] != null)
			return bestCandidate[2];
		if (bestCandidate[1] != null)
			return bestCandidate[1];
		if (bestCandidate[0] != null)
			return bestCandidate[0];

		Log.warn("TranslationService couldn't find a match for " + locale.getLanguage() + "." + locale.getCountry()
				+ "." + locale.getVariant() + "-" + namespace + "/" + key);
		
		return "??"+locale.getLanguage() + "." + locale.getCountry() + "." + locale.getVariant() + "-" + namespace + "/" + key+"??";
		
	}

}
