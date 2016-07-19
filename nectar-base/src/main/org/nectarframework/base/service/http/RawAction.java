package org.nectarframework.base.service.http;

import org.nectarframework.base.action.Action;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.tools.ByteArray;
import org.nectarframework.base.tools.Tuple;


/**
 * A RawAction is a trick on the Action part of the framework, that allows you to generate raw data of any kind and bypass the Element framework.  
 * 
 * The Element returned by rawActionExecute() should include certain attributes to help the SimpleHttpRequestService to output that data properly, specifically regarding HTTP headers. 
 * 
 * a RawAction return Element should inculde the following attributes in it's base Element:  
 * 
 * - httpContentType : the MIME content type to set as a Header. defaults to "application/octet-stream".
 * - httpCompressible : true or false whether we should try to compress the output (when generating a gif for example, set this to false, cause it's already compressed). defaults to true.
 * - httpCacheUntil : a long timestamp (in milliseconds) telling proxies and caches when to forget this data.
 * - httpLastModified : a long timestamp (in milliseconds) telling proxies and caches when this data was last changed.
 * 
 * @author skander
 *
 */
public abstract class RawAction extends Action {

	private byte[] byteArray;
	
	
	@Override
	public final Element execute() {
		Tuple<byte[], Element> tuple = rawActionExecute();
		byteArray = tuple.getLeft();
		return tuple.getRight();
	}

	protected abstract Tuple<byte[], Element> rawActionExecute();

	public byte[] getRawByteArray() {
		return byteArray;
	}
}
