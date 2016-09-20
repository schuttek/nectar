CREATE TABLE IF NOT EXISTS org_nectarframework_www_article (
	id SERIAL, 

	name TEXT NOT NULL, 

	content TEXT NOT NULL, 

	PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS org_nectarframework_www_user (
	id BIGSERIAL , 

	userType SMALLINT NOT NULL, 

	homeGroup BIGINT NOT NULL, 

	login TEXT NOT NULL, 

	publicUserName TEXT NOT NULL, 

	permission TEXT, 

	lastUserIp TEXT, 

	registrationTimestamp BIGINT, 

	emailAddress TEXT, 

	lastVisit BIGINT, 

	localeLanguage TEXT, 

	localeCountry TEXT, 

	localeVariant TEXT, 

	options TEXT, 

	timezone TEXT, 

	avatar TEXT, 

	avatarType SMALLINT, 

	avatarWidth TEXT, 

	avatarHeight TEXT, 

	signature TEXT, 

	about TEXT, 

	registrationStatus SMALLINT, 

	PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS org_nectarframework_www_blog_post (
	id BIGSERIAL , 

	authorUserId BIGINT NOT NULL, 

	createdTimestamp BIGINT NOT NULL, 

	lastEditTimestamp BIGINT, 

	title TEXT NOT NULL, 

	message TEXT NOT NULL, 

	tags TEXT, 

	PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS org_nectarframework_www_post (
	postId BIGSERIAL , 

	threadId BIGINT NOT NULL, 

	authorUserId BIGINT NOT NULL, 

	createdTimestamp BIGINT NOT NULL, 

	lastEditTimestamp BIGINT, 

	message TEXT NOT NULL, 

	PRIMARY KEY (postId)
);
CREATE TABLE IF NOT EXISTS org_nectarframework_www_structure (
	structureId BIGSERIAL , 

	parentId BIGINT NOT NULL, 

	orderWeight FLOAT NOT NULL, 

	name TEXT NOT NULL, 

	description TEXT, 

	PRIMARY KEY (structureId)
);
CREATE TABLE IF NOT EXISTS org_nectarframework_www_thread (
	threadId BIGSERIAL , 

	structureId BIGINT NOT NULL, 

	authorUserId BIGINT NOT NULL, 

	createdTimestamp BIGINT NOT NULL, 

	lastEditTimestamp BIGINT, 

	lastReplyTimestamp BIGINT, 

	sticky SMALLINT, 

	flags INT, 

	subject TEXT NOT NULL, 

	message TEXT NOT NULL, 

	PRIMARY KEY (threadId)
);
