package org.nectarframework.www.comparators;

import java.util.Comparator;

import org.nectarframework.www.data.BlogPost;

public class NewestBlogPostFirst implements Comparator<BlogPost> {
	@Override
	public int compare(BlogPost o1, BlogPost o2) {
		return o2.getCreatedTimestamp().compareTo(o1.getCreatedTimestamp());
	}

}