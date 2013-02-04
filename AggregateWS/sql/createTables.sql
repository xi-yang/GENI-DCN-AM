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
  `requestXml` text default NULL,,
  `manifestXml` text default NULL,,
  `status` varchar(255) NOT NULL,
  `deleted` int(1) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `capabilities`
--

DROP TABLE IF EXISTS `capabilities`;
CREATE TABLE `capabilities` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `urn` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `controllerURL` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`,`urn`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `nodes`
--

DROP TABLE IF EXISTS `nodes`;
CREATE TABLE `nodes` (
  `id` int(11) NOT NULL,
  `nodeId` int(11) NOT NULL,
  `urn` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `capabilities` text NOT NULL,
  PRIMARY KEY  (`id`,`nodeId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `interfaces`
--

DROP TABLE IF EXISTS `interfaces`;
CREATE TABLE `interfaces` (
  `id` int(11) NOT NULL,
  `pnid` int(11) NOT NULL,
  `urn` varchar(255) NOT NULL,
  `deviceType` varchar(255) NOT NULL,
  `deviceName` varchar(255) NOT NULL,
  `capacity` varchar(255) NOT NULL,
  `ipAddress` varchar(255) NOT NULL,
  `vlanRanges`  text NOT NULL,
  `attachedLinks`  text NOT NULL,
  `peerInterfaces`  text NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `slices`
--

DROP TABLE IF EXISTS `slices`;
CREATE TABLE `slices` (
  `id` int(11) NOT NULL,
  `sliceId` int(11) NOT NULL,
  `sliceName` varchar(255) NOT NULL default '',
  `url` text NOT NULL,
  `description` text NOT NULL,
  `users` text NOT NULL,
  `nodes` text NOT NULL,
  `creatorId` int(11) NOT NULL,
  `createdTime` bigint(20) default NULL,
  `expiredTime` bigint(20) default NULL,
  `addedSlice` int(1) default NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`, `sliceId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Table structure for table `p2pvlans`
--

DROP TABLE IF EXISTS `p2pvlans`;
CREATE TABLE `p2pvlans` (
  `id` int(11) NOT NULL,
  `vlanTag` varchar(11) NOT NULL,
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
-- Table structure for table `ext_resources`
--

DROP TABLE IF EXISTS `ext_resources`;
CREATE TABLE `ext_resources` (
  `id` int(11) NOT NULL,
  `urn` varchar(255) NOT NULL,
  `subType` varchar(255) NOT NULL,
  `smUri` varchar(255) NOT NULL,
  `amUri` varchar(255) NOT NULL,
  `rspecData` text NOT NULL,
  `status` varchar(255) NOT NULL default '',
  PRIMARY KEY (`id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL default '',
  `role` varchar(40) NOT NULL default '',
  `certSubject` varchar(255) NOT NULL default '',
  `firstName` varchar(40) NOT NULL default '',
  `lastName` varchar(40) NOT NULL default '',
  `email` varchar(40) NOT NULL default '',
  `description` text NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
