CREATE DATABASE IF NOT EXISTS aggregate;
USE aggregate;

--
-- Table structure for table `capabilities`
--

DROP TABLE IF EXISTS `capabilities`;
CREATE TABLE `capabilities` (
  `name` varchar(255) NOT NULL,
  `urn` varchar(255) NOT NULL,
  `id` int(11) NOT NULL auto_increment,
  `description` text NOT NULL,
  `controllerURL` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`,`urn`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `front_end`
--

DROP TABLE IF EXISTS `front_end`;
CREATE TABLE `front_end` (
  `requestID` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL default 'no such job',
  `statusMsg` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`requestID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `nodes`
--

DROP TABLE IF EXISTS `nodes`;
CREATE TABLE `nodes` (
  `urn` varchar(255) NOT NULL,
  `id` int(11) NOT NULL auto_increment,
  `description` text NOT NULL,
  `capabilities` text,
  PRIMARY KEY  (`id`,`urn`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `slices`
--

DROP TABLE IF EXISTS `slices`;
CREATE TABLE `slices` (
  `sliceName` varchar(255) NOT NULL default '',
  `id` int(11) NOT NULL auto_increment,
  `url` text NOT NULL,
  `description` text NOT NULL,
  `creatorId` int(11) NOT NULL,
  `createdTime` bigint(20) default NULL,
  `expiredTime` bigint(20) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

