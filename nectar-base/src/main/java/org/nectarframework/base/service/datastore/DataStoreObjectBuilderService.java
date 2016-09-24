package org.nectarframework.base.service.datastore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.nectarframework.base.tools.StringTools;
import org.nectarframework.base.tools.Triple;

/**
 * This Service reads the dataObjects.xml config file, and outputs a set of Java
 * classes that implement the functionality of a DataServiceObject.
 * 
 * @author schuttek
 *
 */

// TODO remove the main function and argument handling, this should be run as
// part of the Nectar build process as a script

public class DataStoreObjectBuilderService extends Service {

	private String outputDir;
	private String inputFile;
	private XmlService xmlService;

	@Override
	public void checkParameters() throws ConfigurationException {
		inputFile = serviceParameters.getString("inputFile", "config/dataStoreObjects.xml");
		outputDir = serviceParameters.getString("outputDir", "src/");
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		xmlService = (XmlService) dependancy(XmlService.class);
		return true;
	}

	@Override
	protected boolean init() {
		return true;
	}

	@Override
	protected boolean run() {
		Log.info("[DataStoreObjectBuilderService]: buildDSOClasses()");
		buildDSOClasses(inputFile, outputDir);
		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	private String parseType(String typeStr) {
		// TODO: these relations should be in a static final array...
		typeStr = typeStr.toLowerCase();
		if (typeStr.equals("boolean")) {
			return "DataStoreObjectDescriptor.Type.BOOLEAN";
		} else if (typeStr.equals("byte")) {
			return "DataStoreObjectDescriptor.Type.BYTE";
		} else if (typeStr.equals("short")) {
			return "DataStoreObjectDescriptor.Type.SHORT";
		} else if (typeStr.equals("int")) {
			return "DataStoreObjectDescriptor.Type.INT";
		} else if (typeStr.equals("long")) {
			return "DataStoreObjectDescriptor.Type.LONG";
		} else if (typeStr.equals("float")) {
			return "DataStoreObjectDescriptor.Type.FLOAT";
		} else if (typeStr.equals("double")) {
			return "DataStoreObjectDescriptor.Type.DOUBLE";
		} else if (typeStr.equals("string")) {
			return "DataStoreObjectDescriptor.Type.STRING";
		} else if (typeStr.equals("blob")) {
			return "DataStoreObjectDescriptor.Type.BLOB";
		} else if (typeStr.equals("byte_array")) {
			return "DataStoreObjectDescriptor.Type.BYTE_ARRAY";
		} else if (typeStr.equals("short_array")) {
			return "DataStoreObjectDescriptor.Type.SHORT_ARRAY";
		} else if (typeStr.equals("int_array")) {
			return "DataStoreObjectDescriptor.Type.INT_ARRAY";
		} else if (typeStr.equals("long_array")) {
			return "DataStoreObjectDescriptor.Type.LONG_ARRAY";
		} else if (typeStr.equals("float_array")) {
			return "DataStoreObjectDescriptor.Type.FLOAT_ARRAY";
		} else if (typeStr.equals("double_array")) {
			return "DataStoreObjectDescriptor.Type.DOUBLE_ARRAY";
		} else if (typeStr.equals("string_array")) {
			return "DataStoreObjectDescriptor.Type.STRING_ARRAY";
		}
		Log.warn("DSOBuilder: type: " + typeStr + " is unknown.");
		return "UNKNOWN TYPE";
	}

	private String parseJavaType(String typeStr) {
		typeStr = typeStr.toLowerCase();
		if (typeStr.equals("boolean")) {
			return "Boolean";
		} else if (typeStr.equals("byte")) {
			return "Byte";
		} else if (typeStr.equals("short")) {
			return "Short";
		} else if (typeStr.equals("int")) {
			return "Integer";
		} else if (typeStr.equals("long")) {
			return "Long";
		} else if (typeStr.equals("float")) {
			return "Float";
		} else if (typeStr.equals("double")) {
			return "Double";
		} else if (typeStr.equals("string")) {
			return "String";
		} else if (typeStr.equals("blob")) {
			return "byte[]";
		} else if (typeStr.equals("byte_array")) {
			return "byte[]";
		} else if (typeStr.equals("short_array")) {
			return "short[]";
		} else if (typeStr.equals("int_array")) {
			return "int[]";
		} else if (typeStr.equals("long_array")) {
			return "long[]";
		} else if (typeStr.equals("float_array")) {
			return "float[]";
		} else if (typeStr.equals("double_array")) {
			return "double[]";
		} else if (typeStr.equals("string_array")) {
			return "String[]";
		}
		Log.warn("DSOBuilder: type: " + typeStr + " is unknown.");
		return "UNKNOWN TYPE";
	}

	private String getmysqlType(String typeStr) {
		typeStr = typeStr.toLowerCase();
		if (typeStr.equals("boolean")) {
			return "BOOLEAN";
		} else if (typeStr.equals("byte")) {
			return "TINYINT";
		} else if (typeStr.equals("short")) {
			return "SMALLINT";
		} else if (typeStr.equals("int")) {
			return "INT";
		} else if (typeStr.equals("long")) {
			return "BIGINT";
		} else if (typeStr.equals("float")) {
			return "FLOAT";
		} else if (typeStr.equals("double")) {
			return "DOUBLE";
		} else if (typeStr.equals("string")) {
			return "TEXT COLLATE utf8_unicode_ci";
		} else if (typeStr.equals("blob")) {
			return "BLOB";
		} else if (typeStr.equals("byte_array")) {
			return "BLOB";
		} else if (typeStr.equals("short_array")) {
			return "BLOB";
		} else if (typeStr.equals("int_array")) {
			return "BLOB";
		} else if (typeStr.equals("long_array")) {
			return "BLOB";
		} else if (typeStr.equals("float_array")) {
			return "BLOB";
		} else if (typeStr.equals("double_array")) {
			return "BLOB";
		} else if (typeStr.equals("string_array")) {
			return "BLOB";
		}
		Log.warn("DSOBuilder: type: " + typeStr + " is unknown.");
		return "UNKNOWN TYPE";
	}

	private String getpgsqlType(String typeStr) {
		typeStr = typeStr.toLowerCase();
		if (typeStr.equals("boolean")) {
			return "BOOLEAN";
		} else if (typeStr.equals("byte")) {
			return "SMALLINT";
		} else if (typeStr.equals("short")) {
			return "SMALLINT";
		} else if (typeStr.equals("int")) {
			return "INT";
		} else if (typeStr.equals("long")) {
			return "BIGINT";
		} else if (typeStr.equals("float")) {
			return "FLOAT";
		} else if (typeStr.equals("double")) {
			return "DOUBLE";
		} else if (typeStr.equals("string")) {
			return "TEXT";
		} else if (typeStr.equals("blob")) {
			return "BLOB";
		} else if (typeStr.equals("byte_array")) {
			return "BLOB";
		} else if (typeStr.equals("short_array")) {
			return "BLOB";
		} else if (typeStr.equals("int_array")) {
			return "BLOB";
		} else if (typeStr.equals("long_array")) {
			return "BLOB";
		} else if (typeStr.equals("float_array")) {
			return "BLOB";
		} else if (typeStr.equals("double_array")) {
			return "BLOB";
		} else if (typeStr.equals("string_array")) {
			return "BLOB";
		}
		Log.warn("DSOBuilder: type: " + typeStr + " is unknown.");
		return "UNKNOWN TYPE";
	}

	private void buildDSOClasses(String inputFile, String outputDir) {
		// TODO: getElement()
		// TODO: separate MySQL table create and abstract it so it can make any
		// database table.
		// TODO: better error handling

		try {
			Element elm = XmlService.fromXml(new FileInputStream(inputFile));

			Log.trace("DSOBuilder: read xml: " + elm.toString());

			buildMysqlTables(elm, new File(inputFile).getParent() + "/mysql_dso_create_tables.sql");
			buildPostgreSqlTables(elm, new File(inputFile).getParent() + "/postgresql_dso_create_tables.sql");

			List<Element> dsoElmList = elm.getChildren("dataStoreObject");
			List<String> classList = new LinkedList<String>();
			for (Element dsoElm : dsoElmList) {
				String packageName = dsoElm.get("package");
				String className = dsoElm.get("className");
				String tableName = dsoElm.get("tableName");
				String primaryKeyColName = dsoElm.get("primaryKeyColName");
				String primaryKeyType = dsoElm.get("primaryKeyType");
				String primaryKeyLength = dsoElm.get("primaryKeyLength");
				boolean primaryKeyAutoIncrement = (dsoElm.get("primaryKeyAutoIncrement") != null
						&& dsoElm.get("primaryKeyAutoIncrement").equals("true"));

				List<Element> colElmList = dsoElm.getChildren("column");
				List<Triple<String, String, Boolean>> colList = new LinkedList<Triple<String, String, Boolean>>();
				for (Element cel : colElmList) {
					String name = cel.get("name");
					String type = cel.get("type");
					String nullAllowed = cel.get("nullAllowed");
					boolean nullAb = false;
					if (nullAllowed != null && (nullAllowed.equalsIgnoreCase("true") || nullAllowed.equals("1"))) {
						nullAb = true;
					}
					colList.add(new Triple<String, String, Boolean>(name, type, nullAb));
				}

				FileOutputStream fos = new FileOutputStream(
						new File(outputDir + "/" + packageName.replace('.', '/') + "/" + className + ".java"), false);
				PrintWriter pw = new PrintWriter(fos);

				// DSOD
				String initLine = "dss.initDataStoreObjectDescriptor(new DataStoreObjectDescriptor(";
				initLine += "\"" + tableName + "\", ";
				initLine += "new DataStoreKey(\"" + primaryKeyColName + "\", " + parseType(primaryKeyType) + ", "
						+ primaryKeyLength + ", " + (primaryKeyAutoIncrement ? "true" : "false") + "), ";
				Vector<String> sa = new Vector<String>();
				for (Triple<String, String, Boolean> tup : colList) {
					sa.add("\"" + tup.getLeft() + "\"");
				}
				initLine += "new String[] { " + StringTools.implode(sa, ", ") + " }, ";
				sa.clear();
				for (Triple<String, String, Boolean> tup : colList) {
					sa.add(parseType(tup.getMiddle()));
				}
				initLine += "new DataStoreObjectDescriptor.Type[] {" + StringTools.implode(sa, ", ") + "}, ";
				sa.clear();
				for (Triple<String, String, Boolean> tup : colList) {
					sa.add((tup.getRight() ? "true" : "false"));
				}
				initLine += "new boolean[] {" + StringTools.implode(sa, ", ") + "}, ";
				initLine += className + ".class));";

				// the class file...
				pw.println("package " + packageName + ";");
				pw.println();
				pw.println("import org.nectarframework.base.service.ServiceRegister;");
				pw.println("import org.nectarframework.base.service.datastore.DataStoreKey;");
				pw.println("import org.nectarframework.base.service.datastore.DataStoreObject;");
				pw.println("import org.nectarframework.base.service.datastore.DataStoreObjectDescriptor;");
				pw.println("import org.nectarframework.base.service.datastore.DataStoreService;");
				pw.println("");
				pw.println(
						"// THIS CLASS IS AUTO GENERATED by nectar.base.service.datastore.dsobuilder.DataStoreObjectBuilder, and configured by config/dataStoreObjects.xml . ");
				pw.println("// Edits to this file will be overwritten!");
				pw.println("public class " + className + " extends DataStoreObject {");
				pw.println("	");
				// constructor
				pw.println("	public " + className + "() {");
				pw.println("	}");
				pw.println("	");

				// DataStoreObjectDescriptor object builder.
				pw.println("	@Override");
				pw.println("	public void initDataStoreObjectDescriptor(DataStoreService dss) {");
				pw.println("		" + initLine);
				pw.println("	}");
				pw.println("	");

				// static load method getArticle(long id);
				pw.println("	public static " + className + " load(" + parseJavaType(primaryKeyType)
						+ " key) throws Exception {");
				pw.println(
						"		DataStoreService dss = (DataStoreService) ServiceRegister.getService(DataStoreService.class);");
				pw.println("		DataStoreObject dso = dss.loadDSO(dss.getDataStoreObjectDescriptor(" + className
						+ ".class), key);");
				pw.println("		if (dso == null) ");
				pw.println("			return null;");
				pw.println("		return (" + className + ")dso;");
				pw.println("	}");
				pw.println("	");

				// getters and setters
				for (Triple<String, String, Boolean> tup : colList) {
					pw.println("	public " + parseJavaType(tup.getMiddle()) + " get"
							+ tup.getLeft().substring(0, 1).toUpperCase() + tup.getLeft().substring(1) + "() {");
					pw.println("		return get" + parseJavaType(tup.getMiddle()) + "(\"" + tup.getLeft() + "\");");
					pw.println("	}");
					pw.println("	");

					pw.println("	public void set" + tup.getLeft().substring(0, 1).toUpperCase()
							+ tup.getLeft().substring(1) + "(" + parseJavaType(tup.getMiddle()) + " " + tup.getLeft()
							+ ") {");
					pw.println("		set(\"" + tup.getLeft() + "\", " + tup.getLeft() + ");");
					pw.println("	}");
					pw.println("	");

				}

				// getters for relations
				for (Element relation : dsoElm.getChildren("relation")) {
					pw.println("	public " + relation.get("targetPackage") + "." + relation.get("targetClass")
							+ " get" + relation.get("methodName").substring(0, 1).toUpperCase()
							+ relation.get("methodName").substring(1) + "() throws Exception {");
					pw.println("		return " + relation.get("targetPackage") + "." + relation.get("targetClass")
							+ ".load(get" + this.parseJavaType(relation.get("type")) + "(\"" + relation.get("column")
							+ "\"));");
					pw.println("	}");
					pw.println("	");
				}

				pw.println("}");

				pw.close();
				Log.trace("DSOBuilder: " + className + " finished.");

				classList.add(className);

			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	private void buildMysqlTables(Element elm, String filepath) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new FileOutputStream(filepath, false));
		pw.println(
				"CREATE DATABASE IF NOT EXISTS `nectar` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;");
		pw.println("USE `nectar`;\n\n");

		List<Element> dsoElmList = elm.getChildren("dataStoreObject");
		for (Element dsoElm : dsoElmList) {
			String tableName = dsoElm.get("tableName");
			String primaryKeyColName = dsoElm.get("primaryKeyColName");
			List<Element> colElmList = dsoElm.getChildren("column");
			List<Triple<String, String, Boolean>> colList = new LinkedList<Triple<String, String, Boolean>>();
			for (Element cel : colElmList) {
				String name = cel.get("name");
				String type = cel.get("type");
				String nullAllowed = cel.get("nullAllowed");
				boolean nullAb = false;
				if (nullAllowed != null && (nullAllowed.equalsIgnoreCase("true") || nullAllowed.equals("1"))) {
					nullAb = true;
				}
				colList.add(new Triple<String, String, Boolean>(name, type, nullAb));
			}

			// Mysql Create Table
			pw.println("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");
			LinkedList<String> sqlColLinesList = new LinkedList<String>();
			for (Triple<String, String, Boolean> tup : colList) {
				String sqlstr = "	`" + tup.getLeft() + "` " + getmysqlType(tup.getMiddle());
				if (tup.getLeft().equals(primaryKeyColName)) {
					String typestr = tup.getMiddle().toLowerCase();
					if (typestr.equals("byte") || typestr.equals("short") || typestr.equals("int")
							|| typestr.equals("long")) {
						sqlstr += " NOT NULL AUTO_INCREMENT";
					} else {
						sqlstr += " NOT NULL";
					}
				} else if (!tup.getRight()) {
					sqlstr += " NOT NULL";
				}
				sqlColLinesList.add(sqlstr);
			}
			sqlColLinesList.add("	PRIMARY KEY (`" + primaryKeyColName + "`)");
			pw.println(StringTools.implode(sqlColLinesList, ", \n"));
			pw.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;");
		}
		pw.close();
	}

	private void buildPostgreSqlTables(Element elm, String filepath) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new FileOutputStream(filepath, false));

		List<Element> dsoElmList = elm.getChildren("dataStoreObject");
		for (Element dsoElm : dsoElmList) {
			String tableName = dsoElm.get("tableName");
			String primaryKeyColName = dsoElm.get("primaryKeyColName");
			List<Element> colElmList = dsoElm.getChildren("column");
			List<Triple<String, String, Boolean>> colList = new LinkedList<Triple<String, String, Boolean>>();
			for (Element cel : colElmList) {
				String name = cel.get("name");
				String type = cel.get("type");
				String nullAllowed = cel.get("nullAllowed");
				boolean nullAb = false;
				if (nullAllowed != null && (nullAllowed.equalsIgnoreCase("true") || nullAllowed.equals("1"))) {
					nullAb = true;
				}
				colList.add(new Triple<String, String, Boolean>(name, type, nullAb));
			}
			
			// Mysql Create Table
			pw.println("CREATE TABLE IF NOT EXISTS " + tableName + " (");
			LinkedList<String> sqlColLinesList = new LinkedList<String>();
			for (Triple<String, String, Boolean> tup : colList) {
				String sqlstr = "	" + tup.getLeft();
				if (tup.getLeft().equals(primaryKeyColName)) {
					String typestr = tup.getMiddle().toLowerCase();
					if (typestr.equals("byte") || typestr.equals("short")) {
						sqlstr += " SMALLSERIAL";
					} else if (typestr.equals("int")) {
						sqlstr += " SERIAL";
					} else if (typestr.equals("long")) {
						sqlstr += " BIGSERIAL ";
					} else {
						sqlstr += " NOT NULL";
					}
				} else {
					sqlstr += " "+ getpgsqlType(tup.getMiddle());
					if (!tup.getRight()) {
					sqlstr += " NOT NULL";
					}
				}
				sqlColLinesList.add(sqlstr);
			}
			sqlColLinesList.add("	PRIMARY KEY (" + primaryKeyColName + ")");
			pw.println(StringTools.implode(sqlColLinesList, ", \n\n"));
			pw.println(");");
		}
		pw.close();

	}

}
