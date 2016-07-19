package org.nectarframework.base.form;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nectarframework.base.service.directory.DirForm;
import org.nectarframework.base.service.directory.DirFormvar;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.pathfinder.FormResolution;
import org.nectarframework.base.service.pathfinder.FormvarResolution;
import org.nectarframework.base.service.session.Session;
import org.nectarframework.base.service.xml.Element;
import org.simpleframework.http.Request;

public class Form {

	private Element element;

	private List<ValidationError> validationErrors = null;

	private Session session = null;

	public enum VarType {
		Long, String, StringArray, IntegerArray, Double;

		public static VarType getByString(String typeStr) {
			if (typeStr.equalsIgnoreCase("double"))
				return Double;
			if (typeStr.equalsIgnoreCase("integer") || typeStr.equalsIgnoreCase("long") || typeStr.equalsIgnoreCase("int"))
				return Long;
			if (typeStr.equalsIgnoreCase("string"))
				return String;
			if (typeStr.equalsIgnoreCase("integer_array") || typeStr.equalsIgnoreCase("int_array")  || typeStr.equalsIgnoreCase("long_array"))
				return IntegerArray;
			if (typeStr.equalsIgnoreCase("string_array"))
				return StringArray;
			Log.warn("invalid typeStr " + typeStr + " while parsing formvars, defaulting to String.");
			return String;
		}
	};

	public Form(String name) {
		element = new Element(name);
	}

	public Form(FormResolution formRes, Map<String, List<String>> parameters) {
		element = new Element(formRes.getName());
		for (FormvarResolution dfv : formRes.getFormvars()) {
			List<String> valueList = parameters.get(dfv.getName());
			validateFormVariable(dfv.getName(), dfv.getType(), dfv.isNullAllowed(), valueList);
		}
	}
	
	public Form(DirForm dirForm, Map<String, List<String>> parameters) {
		element = new Element(dirForm.name);
		for (DirFormvar dfv : dirForm.formvars) {
			List<String> valueList = parameters.get(dfv.name);
			validateFormVariable(dfv.name, dfv.type, dfv.nullAllowed, valueList);
		}
	}

	public void validateFormVariable(String name, Form.VarType type, boolean nullAllowed, List<String> valueList) {
		if (valueList == null && !nullAllowed) {
			addValidationError(new ValidationError(name, ValidationError.ErrorType.NULL_NOT_ALLOWED));
			return;
		}
		String strValue = null;
		switch (type) {
		case String:
			strValue = valueList.get(0);
			if (strValue == null && !nullAllowed) {
				addValidationError(new ValidationError(name, ValidationError.ErrorType.NULL_NOT_ALLOWED));
			}
			element.add(name, strValue);
			break;
		case Long:
			strValue = valueList.get(0);
			if (strValue == null && !nullAllowed) {
				addValidationError(new ValidationError(name, ValidationError.ErrorType.NULL_NOT_ALLOWED));
			}

			try {
				long l = Long.parseLong(strValue);
				element.add(name, l);
			} catch (NumberFormatException e) {
				addValidationError(new ValidationError(name, ValidationError.ErrorType.NUMBER_PARSING_ERROR, strValue + " is not an Integer"));
			}
			break;
		case Double:
			strValue = valueList.get(0);
			if (strValue == null && !nullAllowed) {
				addValidationError(new ValidationError(name, ValidationError.ErrorType.NULL_NOT_ALLOWED));
			}

			try {
				double d = Double.parseDouble(strValue);
				element.add(name, d);
			} catch (NumberFormatException e) {
				addValidationError(new ValidationError(name, ValidationError.ErrorType.NUMBER_PARSING_ERROR, strValue + " is not a Double"));
			}
			break;
		case StringArray:
			if (valueList.isEmpty() && !nullAllowed) {
				addValidationError(new ValidationError(name, ValidationError.ErrorType.NULL_NOT_ALLOWED));
			}
			for (String strElm : valueList) {
				if (strElm == null && !nullAllowed) {
					addValidationError(new ValidationError(name, ValidationError.ErrorType.NULL_NOT_ALLOWED));
					break;
				} else {
					element.add(new Element(name).add("value", strElm));
				}
			}
			break;
		case IntegerArray:
			if (valueList.isEmpty() && !nullAllowed) {
				addValidationError(new ValidationError(name, ValidationError.ErrorType.NULL_NOT_ALLOWED));
			}
			for (String intElm : valueList) {
				if (intElm == null && !nullAllowed) {
					addValidationError(new ValidationError(name, ValidationError.ErrorType.NULL_NOT_ALLOWED));
				} else {
					try {
						Long intElmInt = Long.parseLong(intElm);
						element.add(new Element(name).add("value", intElmInt));
					} catch (NumberFormatException e) {
						addValidationError(new ValidationError(name, ValidationError.ErrorType.NULL_NOT_ALLOWED));
						addValidationError(new ValidationError(name, ValidationError.ErrorType.NUMBER_PARSING_ERROR, intElm + " is not an Integer"));
					}
				}
			}
			break;
		}

	}

	public String getString(String name) {
		String ret = element.get(name);
		if (ret != null) {
			return ret;
		} else if (element.getChildren().size() > 1) {
			for (Element e : element.getChildren()) {
				if (e.isName(name)) {
					return e.get("value");
				}
			}
		}
		return null;
	}


	public Double getDouble(String name) {
		String s = getString(name);
		if (s != null) {
			return Double.parseDouble(s);
		}
		return null;
	}
	
	public Long getLong(String name) {
		String s = getString(name);
		if (s != null) {
			return Long.parseLong(s);
		}
		return null;
	}

	public Integer getInt(String name) {
		String s = getString(name);
		if (s != null) {
			return Integer.parseInt(s);
		}
		return null;
	}
	

	public Byte getByte(String name) {
		String s = getString(name);
		if (s != null) {
			return Byte.parseByte(s);
		}
		return null;
	}


	public Set<String> getVariableNames() {
		Set<String> set = element.getAttributes().keySet();
		for (Element e : element.getChildren()) {
			String n = e.getName();
			if (set.contains(n)) {
				set.add(n);
			}
		}
		return set;
	}

	public String getName() {
		return element.getName();
	}


	public String toString() {
		return "Form " + element.toString();
	}

	public String debugString() {
		// TODO: add in request information.
		return "Form " + element.toString();
	}

	
	public boolean isValid() {
		return (validationErrors == null);
	}

	public List<ValidationError> getValidationErrors() {
		return validationErrors;
	}

	public void addValidationError(ValidationError ve) {
		if (ve != null) {
			if (validationErrors == null) {
				validationErrors = new LinkedList<ValidationError>();
			}
			validationErrors.add(ve);
		}
	}

	public Long uid() {
		return Long.parseLong(session.getVars().get("userId"));
	}
	
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Element getElement() {
		return element;
	}

	public void setHttpRequest(Request request) {
		// TODO Auto-generated method stub
		
	}

}
