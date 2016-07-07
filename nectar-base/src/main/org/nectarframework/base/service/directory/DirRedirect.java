package org.nectarframework.base.service.directory;

import java.util.HashMap;
import java.util.List;

public class DirRedirect implements DirPath {

	public String path;
	public String toPath;
	public HashMap<String, List<String>> variables;
}
