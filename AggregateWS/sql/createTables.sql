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
-- Table structure for table `slicer`
--

DROP TABLE IF EXISTS `slicer`;
CREATE TABLE `slicer` (
  `sliceID` varchar(255) NOT NULL default '',
  `launchTime` bigint(20) default NULL,
  PRIMARY KEY  (`sliceID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

