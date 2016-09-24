package org.nectarframework.base.service.websocket;

import java.util.List;
import java.util.Map;

public class WebSocketRawRequest {
	private int _requestId;
	private String _path;
	private Map<String, List<String>> _parameters;

	public String getPath() {
		return _path;
	}

	public Map<String, List<String>> getParameters() {
		return _parameters;
	}

	public int getRequestId() {
		return _requestId;
	}

	public void setRequestId(int requestId) {
		this._requestId = requestId;
	}

	public void setPath(String path) {
		this._path = path;
	}

	public void setParameters(Map<String, List<String>> parameters) {
		this._parameters = parameters;
	}

}
