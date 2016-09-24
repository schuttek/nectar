package org.nectarframework.base.service.pathfinder;

import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.xml.Element;

public abstract class IPathFinder extends Service {
	public abstract Element getPathConfigElement();
}
