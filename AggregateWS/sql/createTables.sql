CREATE DATABASE IF NOT EXISTS aggregate;
USE aggregate;

--
-- Table structure for table `resources`
--

DROP TABLE IF EXISTS `resources`;
CREATE TABLE `resources` (
  `id` int(11) NOT NULL auto_increment,
  `type` varchar(255) NOT NULL,
  `reference` int(11) NOT NULL,
  `rspecId` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `rspecs`
--

DROP TABLE IF EXISTS `rspecs`;
CREATE TABLE `rspecs` (
  `id` int(11) NOT NULL auto_increment,
  `rspecName` varchar(255) NOT NULL,
  `aggregateName` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `startTime` bigint(20) default NULL,
  `endTime` bigint(20) default NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

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
  `users` text NOT NULL,
  `nodes` text NOT NULL,
  `creatorId` int(11) NOT NULL,
  `createdTime` bigint(20) default NULL,
  `expiredTime` bigint(20) default NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `p2pvlans`
--

DROP TABLE IF EXISTS `p2pvlans`;
CREATE TABLE `p2pvlans` (
  `id` int(11) NOT NULL auto_increment,
  `vlanTag` int(11) NOT NULL,
  `sliceName` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL default '',
  `source` varchar(255) NOT NULL default '',
  `destination` varchar(255) NOT NULL default '',
  `srcInterface` varchar(255) NOT NULL default '',
  `dstInterface` varchar(255) NOT NULL default '',
  `srcIpAndMask` varchar(255) NOT NULL default '',
  `dstIpAndMask` varchar(255) NOT NULL default '',
  `bandwidth` float NOT NULL,
  `globalReservationId` varchar(255) NOT NULL default '',
  `status` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `networks`
--

DROP TABLE IF EXISTS `networks`;
CREATE TABLE `networks` (
  `id` int(11) NOT NULL auto_increment,
  `sliceName` varchar(255) NOT NULL,
  `vlanPool` text NOT NULL default '',
  `ipPool` text NOT NULL default '',
  `status` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `firstName` varchar(40) NOT NULL default '',
  `lastName` varchar(40) NOT NULL default '',
  `email` varchar(40) NOT NULL default '',
  `description` text NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
