USE aggregate;

--
-- Dumping data for table `capabilities`
--

INSERT INTO `capabilities` VALUES ('DRAGON','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon',1,'dynamic networks provisioning capability','https://idc.dragon.maxgigapop.net:8443/axis2/services/OSCARS?wsdl');
INSERT INTO `capabilities` VALUES ('PlanetLab','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab',2,'virtualized computational resources','https://max-myplc.dragon.maxgigapop.net/PLCAPI/');
INSERT INTO `capabilities` VALUES ('OpenFlow','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=openflow',3,'demo entry for OpenFlow controller','http://idc.dragon.maxgigapop.net:8443/axis2/services/OpenFlowDemo?wsdl');
INSERT INTO `capabilities` VALUES ('Eucalyptus','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=eucalyptus',4,'demo entry for Eucalyptus cloud controller','http://cloud.dragon.maxgigapop.net/axis2/services/EucalyptusCtrl?wsdl');
INSERT INTO `capabilities` VALUES ('PASTA','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta',5,'demo entry for PASTA wireless sensor network','http://pasta.east.isi.edu/axis2/services/PastaNet?wsdl');

--
-- Dumping data for table `nodes`
--

INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB5',5,'planetlab5.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB4',6,'planetlab4.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB3',7,'planetlab3.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PLANETLAB2',8,'planetlab2.dragon.maxgigapop.net','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=planetlab');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=DEMO1',9,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=openflow');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=LAUREL',10,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=openflow,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=eucalyptus');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=HARDY',11,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=openflow,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=eucalyptus');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR1',12,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=eucalyptus');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR2',13,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR3',14,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR4',15,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR5',16,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR6',17,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR7',18,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR8',19,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR9',20,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR10',21,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR11',22,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR12',23,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR13',24,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR14',25,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR15',26,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR16',27,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR17',28,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR18',29,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR19',30,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR20',31,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR21',32,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR22',33,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR23',34,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR24',35,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR25',36,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR26',37,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR27',38,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR28',39,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR29',40,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR30',41,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR31',42,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR32',43,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR33',44,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR34',45,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR35',46,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR36',47,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR37',48,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR38',49,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR39',50,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR40',51,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR41',52,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR42',53,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR43',54,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR44',55,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR45',56,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR46',57,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR47',58,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR48',59,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR49',60,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=PASTA_SENSOR50',61,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=pasta');
INSERT INTO `nodes` VALUES ('urn:ogf:geni:domain=dragon.maxgigapop.net:node=DEMO2',62,'demo node to demonstrate the ease of adding new components','urn:ogf:geni:domain=dragon.maxgigapop.net:capability=dragon,urn:ogf:geni:domain=dragon.maxgigapop.net:capability=openflow');


--
-- Dumping data for table `slices`
--

INSERT INTO `slices` VALUES ('max-gec7-demo-slice1', 1, 'http://geni.maxgigapop.net', 'Planetlab Slice#1 for MAX-GENI demo at GEC7', 100, UNIX_TIMESTAMP('2010-03-10 16:14:53'), UNIX_TIMESTAMP('2010-03-25 00:14:54'));
INSERT INTO `slices` VALUES ('max-gec7-demo-slice2', 2, 'http://geni.maxgigapop.net', 'Planetlab Slice#2 for MAX-GENI demo at GEC7', 100, UNIX_TIMESTAMP('2010-03-15 01:49:32'), UNIX_TIMESTAMP('2010-03-30 17:49:33'));


--
-- Dumping data for table `users`
--

INSERT INTO `users` VALUES (98, 'TomLehman', 'Tom', 'Lehman', 'tlehman@east.isi.edu', 'Project Leader at USC/ISI East');
INSERT INTO `users` VALUES (100, 'xyang', 'Xi', 'Yang', 'xyang@east.isi.edu', 'Computer Scientist at USC/ISI East');


