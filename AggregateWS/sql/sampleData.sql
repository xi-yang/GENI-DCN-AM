USE aggregate;

--
-- Dumping data for table `capabilities`
--

INSERT INTO `capabilities` VALUES (1, 'DRAGON','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon','dynamic networks provisioning capability','https://idc.dragon.maxgigapop.net:8443/axis2/services/OSCARS?wsdl');
INSERT INTO `capabilities` VALUES (2, 'PlanetLab','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab','virtualized computational resources','https://max-myplc.dragon.maxgigapop.net/PLCAPI/');
INSERT INTO `capabilities` VALUES (3, 'NetFPGA','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=netfpga','netfpga cards','http://idc.dragon.maxgigapop.net:8443/axis2/services/NetFPGA?wsdl');

--
-- Dumping data for table `nodes`
--

INSERT INTO `nodes` VALUES (1, 1, 'urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB5','planetlab5.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES (2, 3, 'urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB4','planetlab4.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES (3, 5, 'urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB3','planetlab3.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES (4, 2, 'urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB2','planetlab2.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES (5, 0, 'urn:ogf:geni:domain=dragon.maxgigapop.net:node=IDC','MAX-DRAGON InterDomain Controller','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon');
INSERT INTO `nodes` VALUES (6, 0, 'urn:ogf:geni:domain=dragon.maxgigapop.net:node=STYX','Node with 2 NetFPGA cards','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=netfpga');


--
-- Dumping data for table `slices`
--

INSERT INTO `resources` VALUES (1, 'computeNode', 0);
INSERT INTO `resources` VALUES (2, 'computeNode', 0);
INSERT INTO `resources` VALUES (3, 'computeNode', 0);
INSERT INTO `resources` VALUES (4, 'computeNode', 0);
INSERT INTO `resources` VALUES (5, 'computeNode', 0);
INSERT INTO `resources` VALUES (6, 'computeNode', 0);


--
-- Dumping data for table `users`
--

INSERT INTO `users` VALUES (18, 'TomLehman', 'Tom', 'Lehman', 'tlehman@east.isi.edu', 'Project Leader at USC/ISI East');
INSERT INTO `users` VALUES (19, 'xyang', 'Xi', 'Yang', 'xyang@east.isi.edu', 'Computer Scientist at USC/ISI East');


--
-- Dumping data for table `resources`
--

