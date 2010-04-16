USE aggregate;

--
-- Dumping data for table `capabilities`
--

INSERT INTO `capabilities` VALUES ('DRAGON','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon',1,'dynamic networks provisioning capability','https://idc.dragon.maxgigapop.net:8443/axis2/services/OSCARS?wsdl');
INSERT INTO `capabilities` VALUES ('PlanetLab','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab',2,'virtualized computational resources','https://max-myplc.dragon.maxgigapop.net/PLCAPI/');
INSERT INTO `capabilities` VALUES ('NetFPGA','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=netfpga',3,'netfpga cards','http://idc.dragon.maxgigapop.net:8443/axis2/services/NetFPGA?wsdl');

--
-- Dumping data for table `nodes`
--

INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB5',5,'planetlab5.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB4',6,'planetlab4.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB3',7,'planetlab3.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB2',8,'planetlab2.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=IDC',9,'MAX-DRAGON InterDomain Controller','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=STYX',10,'Node with 2 NetFPGA cards','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=netfpga');


--
-- Dumping data for table `slices`
--

INSERT INTO `slices` VALUES ('maxpl_xi_test1', 1, 'http://geni.maxgigapop.net', 'Planetlab Slice#1 for MAX-GENI demo at GEC7', '', 'urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB2,urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB3,urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB4,urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB5', 100, UNIX_TIMESTAMP('2010-03-09 16:14:53'), UNIX_TIMESTAMP('2011-04-18 00:14:54'), "active");
INSERT INTO `slices` VALUES ('maxpl_xi_test2', 2, 'http://geni.maxgigapop.net', 'Planetlab Slice#2 for MAX-GENI demo at GEC7', '', 'urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB3,urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB4,urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB5', 100, UNIX_TIMESTAMP('2010-03-10 01:49:32'), UNIX_TIMESTAMP('2011-04-18 17:49:33'), "active");
INSERT INTO `slices` VALUES ('maxpl_dynamic_net', 3, 'http://geni.maxgigapop.net', 'Dynamic Networked Slice for MAX-GENI demo at GEC7', '', 'urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB2,urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB3,urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB4,urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB5', 98, UNIX_TIMESTAMP('2010-03-11 10:23:00'), UNIX_TIMESTAMP('2010-04-23 10:43:00'), "active");


--
-- Dumping data for table `users`
--

INSERT INTO `users` VALUES (18, 'TomLehman', 'Tom', 'Lehman', 'tlehman@east.isi.edu', 'Project Leader at USC/ISI East');
INSERT INTO `users` VALUES (19, 'xyang', 'Xi', 'Yang', 'xyang@east.isi.edu', 'Computer Scientist at USC/ISI East');


