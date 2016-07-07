CREATE DATABASE IF NOT EXISTS `nectar` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;
USE `nectar`;


CREATE TABLE IF NOT EXISTS `org_nectarframework_www_article` (
	`id` INT NOT NULL AUTO_INCREMENT, 
	`name` TEXT COLLATE utf8_unicode_ci, 
	`content` TEXT COLLATE utf8_unicode_ci, 
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
