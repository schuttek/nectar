package org.nectarframework.www.action;

import java.util.Collections;
import java.util.List;

import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.tools.StringTools;
import org.nectarframework.www.comparators.NewestBlogPostFirst;
import org.nectarframework.www.data.BlogPost;
import org.nectarframework.www.data.User;

public class IndexAction extends BaseAction {

	@SuppressWarnings("unchecked")
	@Override
	public Element _execute() {
		Element elm = new Element("index");
		try {
			List<BlogPost> bpl;
			bpl = (List<BlogPost>) dss().loadAll(BlogPost.class);

			Collections.sort(bpl, new NewestBlogPostFirst());

			for (BlogPost b : bpl) {
				Element bpElm = new Element("blogPost");
				bpElm.add("id", b.getId());
				bpElm.add("title", b.getTitle());
				bpElm.add("message", b.getMessage());
				bpElm.add("createdTimestamp", StringTools.msTimeToDatetime(b.getCreatedTimestamp()));
				bpElm.add("lastEditTimestamp", StringTools.msTimeToDatetime(b.getLastEditTimestamp()));
				bpElm.add("tags", b.getTags());
				bpElm.add("authorId", b.getAuthorUserId());
				User user = b.getAuthor();
				bpElm.add("authorName", user.getPublicUserName());
				elm.add(bpElm);
			}
		} catch (Exception e) {
			Log.warn(e);
			return null;
		}

		return elm;
	}

}
