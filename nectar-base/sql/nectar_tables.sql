
CREATE TABLE `nectar_log_access` (
  `dateMs` bigint(20) NOT NULL,
  `path` text COLLATE utf8_unicode_ci NOT NULL,
  `formRaw` text COLLATE utf8_unicode_ci,
  `formValid` text COLLATE utf8_unicode_ci,
  `outputElm` text COLLATE utf8_unicode_ci,
  `duration` bigint(20) NOT NULL,
  `remoteIp` text COLLATE utf8_unicode_ci NOT NULL,
  `session` text COLLATE utf8_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `nectar_log_logger` (
  `date_ms` bigint(20) NOT NULL,
  `level` tinyint(4) NOT NULL,
  `thread_hash` int(11) NOT NULL,
  `thread_name` tinytext COLLATE utf8_unicode_ci NOT NULL,
  `message` text COLLATE utf8_unicode_ci,
  `throwable` text COLLATE utf8_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `w_lang` (
  `localeLanguage` varchar(2) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'en',
  `localeCountry` varchar(2) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'GB',
  `localeVariant` varchar(32) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `namespace` varchar(128) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `messageKey` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `pluralArgMap` tinyint(4) NOT NULL DEFAULT '0',
  `translatedText` text COLLATE utf8_unicode_ci NOT NULL,
  `lastUpdate` bigint(20) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `w_lang` (`localeLanguage`, `localeCountry`, `localeVariant`, `namespace`, `messageKey`, `pluralArgMap`, `translatedText`, `lastUpdate`) VALUES
('en', 'GB', '', '', 'common.header.slogan', 0, 'a simple and stupidly fast web application framework for massively scalable applications', 0),
('en', 'GB', '', '', 'common.header.title', 0, 'Nectar Framework', 0),
('en', 'GB', '', '', 'common.leftLinks.about', 0, 'About', 0),
('en', 'GB', '', '', 'common.leftLinks.documentation', 0, 'Documentation', 0),
('en', 'GB', '', '', 'common.leftLinks.download', 0, 'Download', 0),
('en', 'GB', '', '', 'common.leftLinks.home', 0, 'Home', 0),
('en', 'GB', '', '', 'common.leftLinks.overview', 0, 'Overview', 0),
('en', 'GB', '', '', 'common.sidebar.hbase', 0, 'Apache Hbase database', 0),
('en', 'GB', '', '', 'common.sidebar.maven', 0, 'Maven build system', 0),
('en', 'GB', '', '', 'common.sidebar.nectar_uses', 0, 'Nectar Framework uses', 0),
('en', 'GB', '', '', 'common.sidebar.simple', 0, 'Simple Framework HTTP Server', 0),
('en', 'GB', '', '', 'common.sidebar.thymeleaf', 0, 'Thymeleaf template engine', 0),
('en', 'GB', '', '', 'common.sidebar.xerces', 0, 'Apache Xerces2 XML Parser', 0),
('en', 'GB', '', '', 'common.title', 0, 'Nectar Framework', 0),
('en', 'GB', '', 'common.sidebar.nectar_uses', '', 0, 'Nectar Framework uses:', 0);

ALTER TABLE `w_lang`
  ADD UNIQUE KEY `key 1` (`localeLanguage`,`localeCountry`,`namespace`,`messageKey`) USING BTREE;
