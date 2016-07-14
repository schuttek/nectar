CREATE DATABASE IF NOT EXISTS `nectar` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;
USE `nectar`;


CREATE TABLE IF NOT EXISTS `org_nectarframework_www_article` (
	`id` INT NOT NULL AUTO_INCREMENT, 
	`name` TEXT COLLATE utf8_unicode_ci, 
	`content` TEXT COLLATE utf8_unicode_ci, 
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE IF NOT EXISTS `org_nectarframework_www_user` (
	`id` BIGINT NOT NULL AUTO_INCREMENT, 
	`userType` TINYINT, 
	`homeGroup` BIGINT, 
	`login` TEXT COLLATE utf8_unicode_ci, 
	`publicUserName` TEXT COLLATE utf8_unicode_ci, 
	`permission` TEXT COLLATE utf8_unicode_ci, 
	`lastUserIp` TEXT COLLATE utf8_unicode_ci, 
	`registrationTimestamp` BIGINT, 
	`emailAddress` TEXT COLLATE utf8_unicode_ci, 
	`lastVisit` BIGINT, 
	`localeLanguage` TEXT COLLATE utf8_unicode_ci, 
	`localeCountry` TEXT COLLATE utf8_unicode_ci, 
	`localeVariant` TEXT COLLATE utf8_unicode_ci, 
	`options` TEXT COLLATE utf8_unicode_ci, 
	`timezone` TEXT COLLATE utf8_unicode_ci, 
	`avatar` TEXT COLLATE utf8_unicode_ci, 
	`avatarType` TINYINT, 
	`avatarWidth` TEXT COLLATE utf8_unicode_ci, 
	`avatarHeight` TEXT COLLATE utf8_unicode_ci, 
	`signature` TEXT COLLATE utf8_unicode_ci, 
	`about` TEXT COLLATE utf8_unicode_ci, 
	`registrationStatus` TINYINT, 
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE IF NOT EXISTS `org_nectarframework_www_blog_post` (
	`id` BIGINT NOT NULL AUTO_INCREMENT, 
	`authorUserId` BIGINT, 
	`createdTimestamp` BIGINT, 
	`lastEditTimestamp` BIGINT, 
	`title` TEXT COLLATE utf8_unicode_ci, 
	`message` TEXT COLLATE utf8_unicode_ci, 
	`tags` TEXT COLLATE utf8_unicode_ci, 
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE IF NOT EXISTS `org_nectarframework_www_post` (
	`postId` BIGINT NOT NULL AUTO_INCREMENT, 
	`threadId` BIGINT, 
	`authorUserId` BIGINT, 
	`createdTimestamp` BIGINT, 
	`lastEditTimestamp` BIGINT, 
	`message` TEXT COLLATE utf8_unicode_ci, 
	PRIMARY KEY (`postId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE IF NOT EXISTS `org_nectarframework_www_structure` (
	`structureId` BIGINT NOT NULL AUTO_INCREMENT, 
	`parentId` BIGINT, 
	`orderWeight` FLOAT, 
	`name` TEXT COLLATE utf8_unicode_ci, 
	`description` TEXT COLLATE utf8_unicode_ci, 
	PRIMARY KEY (`structureId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE IF NOT EXISTS `org_nectarframework_www_thread` (
	`threadId` BIGINT NOT NULL AUTO_INCREMENT, 
	`structureId` BIGINT, 
	`authorUserId` BIGINT, 
	`createdTimestamp` BIGINT, 
	`lastEditTimestamp` BIGINT, 
	`lastReplyTimestamp` BIGINT, 
	`sticky` TINYINT, 
	`flags` INT, 
	`subject` TEXT COLLATE utf8_unicode_ci, 
	`message` TEXT COLLATE utf8_unicode_ci, 
	PRIMARY KEY (`threadId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
