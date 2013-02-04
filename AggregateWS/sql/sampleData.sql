USE aggregate;

--
-- Dumping data for table `capabilities`
--

INSERT INTO `capabilities` VALUES (1, 'DRAGON','urn:publicid:IDN+maxpl+capability+dragon','dynamic networks provisioning capability','https://idc.dragon.maxgigapop.net:8443/OSCARS');
INSERT INTO `capabilities` VALUES (2, 'PlanetLab','urn:publicid:IDN+maxpl+capability+planetlab','virtualized computational resources','https://max-myplc.dragon.maxgigapop.net/PLCAPI/');
INSERT INTO `capabilities` VALUES (3, 'NetFPGA','urn:publicid:IDN+maxpl+capability+netfpga','netfpga cards','TBD');

--
-- Dumping data for table `nodes`
--

INSERT INTO `nodes` VALUES (1, 1, 'urn:publicid:IDN+maxpl+node+planetlab5.dragon.maxgigapop.net','','planetlab5.dragon.maxgigapop.net','urn:publicid:IDN+maxpl+capability+dragon,urn:publicid:IDN+maxpl+capability+planetlab');
INSERT INTO `nodes` VALUES (2, 3, 'urn:publicid:IDN+maxpl+node+planetlab4.dragon.maxgigapop.net','','planetlab4.dragon.maxgigapop.net','urn:publicid:IDN+maxpl+capability+dragon,urn:publicid:IDN+maxpl+capability+planetlab');
INSERT INTO `nodes` VALUES (3, 5, 'urn:publicid:IDN+maxpl+node+planetlab3.dragon.maxgigapop.net','','planetlab3.dragon.maxgigapop.net','urn:publicid:IDN+maxpl+capability+dragon,urn:publicid:IDN+maxpl+capability+planetlab');
INSERT INTO `nodes` VALUES (4, 2, 'urn:publicid:IDN+maxpl+node+planetlab2.dragon.maxgigapop.net','','planetlab2.dragon.maxgigapop.net','urn:publicid:IDN+maxpl+capability+dragon,urn:publicid:IDN+maxpl+capability+planetlab');
INSERT INTO `nodes` VALUES (5, 0, 'urn:publicid:IDN+maxpl+node+idc.dragon.maxgigapop.net','','MAX-DRAGON InterDomain Controller','urn:publicid:IDN+maxpl+capability+dragon');


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

--
-- Dumping data for table `resources`
--

