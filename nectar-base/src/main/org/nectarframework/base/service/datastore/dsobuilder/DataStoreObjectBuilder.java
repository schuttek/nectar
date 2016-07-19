package org.nectarframework.base.service.datastore.dsobuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.nectarframework.base.Main;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.nectarframework.base.tools.StringTools;
import org.nectarframework.base.tools.Triple;
import org.nectarframework.base.tools.Tuple;

public class DataStoreObjectBuilder {
	public static void main(String[] args) {

		try {

			CommandLineParser parser = new DefaultParser();
			Options options = buildArgumentOptions();

			// parse the command line arguments
			CommandLine line;
			try {
				line = parser.parse(options, args);
			} catch (ParseException e) {
				e.printStackTrace();
				return;
			}

			// startup
			if (line.hasOption("version")) {
				runVersion();
			} else if (line.hasOption("help")) {
				runHelp(options);
			} else if (line.hasOption("input") && line.hasOption("output")) {
				String inputFile = line.getOptionValue("input");
				String outputDir = line.getOptionValue("output");

				new DataStoreObjectBuilder().run(inputFile, outputDir);
				return;

			}
			runHelp(options);

		} catch (Throwable t) {
			Log.fatal("CRASH", t);
			System.exit(-1);
		}
		// main thread ends after startup.
	}

	private static Options buildArgumentOptions() {
		Options options = new Options();

		options.addOption("h", "help", false, "print this message");
		Option opt = new Option("i", "input", true, "path to the dataStoreObjects XML file");
		opt.setArgName("INPUT");
		options.addOption(opt);
		opt = new Option("o", "output", true, "output directory");
		opt.setArgName("OUTPUT");
		options.addOption(opt);
		return options;
	}

	public static void runHelp(Options opts) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java nectar.base.service.datastore.dsobuilder.DataStoreObjectBuilder", opts);
	}

	private static void runVersion() {
		System.out.println("Nectar Web Platform DataStoreObject Builder");
		System.out.println("Version: " + Main.VERSION);
	}

	private String parseType(String typeStr) {
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
		System.err.println("type: " + typeStr + " is unknown.");
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
		System.err.println("type: " + typeStr + " is unknown.");
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
		System.err.println("type: " + typeStr + " is unknown.");
		return "UNKNOWN TYPE";
	}

	private void run(String inputFile, String outputDir) {
		try {
			byte[] ba = Files.readAllBytes(new File(inputFile).toPath());
			
			Element elm = XmlService.fromXml(ba);

			System.err.println("read xml: " + elm.toString());

			FileOutputStream mysqlfos = new FileOutputStream( new File(inputFile).getParent() + "/mysql_dso_create_tables.sql", false);
			PrintWriter mysqlpw = new PrintWriter(mysqlfos);
			
			mysqlpw.println("CREATE DATABASE IF NOT EXISTS `nectar` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;");
			mysqlpw.println("USE `nectar`;\n\n");

			List<Element> dsoElmList = elm.getChildren("dataStoreObject");
			List<String> classList = new LinkedList<String>();
			for (Element dsoElm : dsoElmList) {
				String packageName = dsoElm.get("package");
				String className = dsoElm.get("className");
				String tableName = dsoElm.get("tableName");
				String primaryKeyColName = dsoElm.get("primaryKeyColName");
				String primaryKeyType = dsoElm.get("primaryKeyType");
				String primaryKeyLength = dsoElm.get("primaryKeyLength");
				boolean primaryKeyAutoIncrement = (dsoElm.get("primaryKeyAutoIncrement") != null && dsoElm.get("primaryKeyAutoIncrement").equals("true"));

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

				FileOutputStream fos = new FileOutputStream(new File(outputDir + "/" + packageName.replace('.', '/') + "/" + className + ".java"), false);
				PrintWriter pw = new PrintWriter(fos);

				// DSOD 
				String initLine = "dss.initDataStoreObjectDescriptor(new DataStoreObjectDescriptor(";
				initLine += "\"" + tableName + "\", ";
				initLine += "new DataStoreKey(\"" + primaryKeyColName + "\", " + parseType(primaryKeyType) + ", " + primaryKeyLength + ", "+(primaryKeyAutoIncrement?"true":"false")+"), ";
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
					sa.add((tup.getRight()?"true":"false"));
				}
				initLine += "new boolean[] {" + StringTools.implode(sa, ", ") +"}, ";
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
				pw.println("// THIS CLASS IS AUTO GENERATED by nectar.base.service.datastore.dsobuilder.DataStoreObjectBuilder, and configured by config/dataStoreObjects.xml . ");
				pw.println("// Edits to this file will be overwritten!");
				pw.println("public class " + className + " extends DataStoreObject {");
				pw.println("	");
				pw.println("	public " + className + "() {");
				pw.println("	}");
				pw.println("	");
				pw.println("	@Override");
				pw.println("	public void initDataStoreObjectDescriptor(DataStoreService dss) {");
				pw.println("		" + initLine);
				pw.println("	}");
				pw.println("	");

				pw.println("	public static " + className + " load(" + parseJavaType(primaryKeyType) + " key) throws Exception {");
				pw.println("		DataStoreService dss = (DataStoreService) ServiceRegister.getService(DataStoreService.class);");
				pw.println("		DataStoreObject dso = dss.loadDSO(dss.getDataStoreObjectDescriptor(" + className + ".class), key);");
				pw.println("		if (dso == null) ");
				pw.println("			return null;");
				pw.println("		return ("+className+")dso;");
				pw.println("	}");
				pw.println("	");

				for (Triple<String, String, Boolean> tup : colList) {
					pw.println("	public " + parseJavaType(tup.getMiddle()) + " get" + tup.getLeft().substring(0, 1).toUpperCase() + tup.getLeft().substring(1) + "() {");
					pw.println("		return get" + parseJavaType(tup.getMiddle()) + "(\"" + tup.getLeft() + "\");");
					pw.println("	}");
					pw.println("	");
					
					pw.println("	public void set" + tup.getLeft().substring(0, 1).toUpperCase() + tup.getLeft().substring(1) + "(" + parseJavaType(tup.getMiddle()) + " "+tup.getLeft()+") {");
					pw.println("		set(\""+tup.getLeft()+"\", "+tup.getLeft()+");");
					pw.println("	}");
					pw.println("	");
					
				}
				
				for (Element relation: dsoElm.getChildren("relation")) {
					pw.println("	public "+relation.get("targetPackage")+"."+relation.get("targetClass")+" get"+relation.get("methodName").substring(0, 1).toUpperCase() + relation.get("methodName").substring(1)+"() throws Exception {");
					pw.println("		return "+relation.get("targetPackage")+"."+relation.get("targetClass")+ ".load(get"+this.parseJavaType(relation.get("type"))+"(\""+relation.get("column")+"\"));");
					pw.println("	}");
					pw.println("	");
				}
				

				pw.println("}");

				pw.close();
				System.err.println(className + " finished.");

				classList.add(className);
				
				// Mysql Create Table
				
				mysqlpw.println("CREATE TABLE IF NOT EXISTS `"+tableName+"` (");
				
				LinkedList<String> sqlColLinesList = new LinkedList<String>();
				
				
				for (Triple<String, String, Boolean> tup: colList) {
					String sqlstr = "	`"+tup.getLeft()+"` "+getmysqlType(tup.getMiddle());
					if (tup.getLeft().equals(primaryKeyColName)) {
						String typestr = tup.getMiddle().toLowerCase();
						if (typestr.equals("byte") ||typestr.equals("short") ||typestr.equals("int") ||typestr.equals("long")) {
							sqlstr += " NOT NULL AUTO_INCREMENT";
						} else {
							sqlstr += " NOT NULL";
						}
					} else if (!tup.getRight()) {
						sqlstr += " NOT NULL";
					}
					sqlColLinesList.add(sqlstr);
				}
				
				sqlColLinesList.add("	PRIMARY KEY (`"+primaryKeyColName+"`)");
				
				mysqlpw.println(StringTools.implode(sqlColLinesList, ", \n"));
				
				mysqlpw.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;");
				
			}

			
			
			mysqlpw.close();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

}
