
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