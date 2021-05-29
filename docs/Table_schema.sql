-- MySQL dump 10.13  Distrib 5.7.20, for Win64 (x86_64)
--
-- Host: localhost    Database: hunter2
-- ------------------------------------------------------
-- Server version	5.7.20-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `action`
--

DROP TABLE IF EXISTS `action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `route` varchar(255) DEFAULT NULL,
  `taskdef_id` binary(16) DEFAULT NULL,
  `classe` varchar(255) DEFAULT NULL,
  `params` varchar(255) DEFAULT NULL,
  `taskstatus` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgtskya905rwh2g5bl0hsmc945` (`taskdef_id`),
  CONSTRAINT `FK5wxkor4wcjenpjc181tehko6o` FOREIGN KEY (`taskdef_id`) REFERENCES `taskdef` (`id`),
  CONSTRAINT `FKgtskya905rwh2g5bl0hsmc945` FOREIGN KEY (`taskdef_id`) REFERENCES `taskdef` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action`
--

LOCK TABLES `action` WRITE;
/*!40000 ALTER TABLE `action` DISABLE KEYS */;
INSERT INTO `action` VALUES ('=‹c†²\Ò@M–&Á\â„1\ë','RECREC1','Recebimento REC1','NOVO','2017-12-12 15:06:46','2017-12-13 16:10:52','fa-truck','/home/process/checkingprocess/RECREC1','IV \ÍMŸR§V¦o^(','com.gtp.hunter.process.wf.action.AttachProcessAction','REC,REC1','NOVO'),('¡ªS\ãq@„†»ü÷Š\Ã\n',NULL,'Imprimir na Impressora A',NULL,'2017-11-29 16:57:42','2017-11-29 16:57:42','fa-tags','/home/custom-descarpack/printTags/%%task%%/devicea','9¯–½uI5˜Þ„¸ù\"´','com.gtp.hunter.process.wf.action.DummyAction','IDDAIMPRESSORAA','IMPRIMINDO'),('°D£ò¨½O¤P‹~\Ým',NULL,'Imprimir na Impressora B',NULL,'2017-11-29 16:57:42','2017-11-29 16:57:42','fa-tags','/home/custom-descarpack/printTags/%%task%%/deviceb','9¯–½uI5˜Þ„¸ù\"´','com.gtp.hunter.process.wf.action.DummyAction','IDDAIMPRESSORAB','IMPRIMINDO');
/*!40000 ALTER TABLE `action` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coordinate`
--

DROP TABLE IF EXISTS `coordinate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coordinate` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `coordOrder` int(11) NOT NULL,
  `lat` float NOT NULL,
  `lng` float NOT NULL,
  `x` float NOT NULL,
  `y` float NOT NULL,
  `location_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq8f0pobmaykh5cc6lt7f7sxvc` (`location_id`),
  CONSTRAINT `FKq8f0pobmaykh5cc6lt7f7sxvc` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coordinate`
--

LOCK TABLES `coordinate` WRITE;
/*!40000 ALTER TABLE `coordinate` DISABLE KEYS */;
/*!40000 ALTER TABLE `coordinate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credential`
--

DROP TABLE IF EXISTS `credential`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `credential` (
  `type` varchar(31) NOT NULL,
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `lastLogin` datetime DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `salt` tinyblob,
  `pubKey` varchar(255) DEFAULT NULL,
  `user_id` binary(16) DEFAULT NULL,
  `unit_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpg7bdnqxpyhrt7f8soul9y7ne` (`user_id`),
  KEY `FK40mf22jm8h46pucdsjdweajwk` (`unit_id`),
  CONSTRAINT `FK40mf22jm8h46pucdsjdweajwk` FOREIGN KEY (`unit_id`) REFERENCES `unit` (`id`),
  CONSTRAINT `FKpg7bdnqxpyhrt7f8soul9y7ne` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credential`
--

LOCK TABLES `credential` WRITE;
/*!40000 ALTER TABLE `credential` DISABLE KEYS */;
INSERT INTO `credential` VALUES ('PWD','*¼ý¹\ÅHm»T­.«q',NULL,NULL,'2017-11-13 17:23:38','2017-11-13 17:23:38','\0',NULL,'admin','cab7054b123170484926727739dae51cb894b1fbd410c5e9ef1bfbb578cd7193b155bc2d7cc2671b73c2b91c4c4e80b19f6a160cb7eca37f5be8a5e646b54ab5','}Ç˜˜ô¬“\Ý\Ú\Î\r®$5K?£Êš\n¯1q1H}!Jg',NULL,',Ž\\•Ï‡Nø‡™$¬9»\É',NULL,NULL);
/*!40000 ALTER TABLE `credential` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `device`
--

DROP TABLE IF EXISTS `device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `device` (
  `id` int(11) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `src_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `antenna` int(11) NOT NULL,
  `classe` varchar(255) DEFAULT NULL,
  `connectionType` varchar(255) DEFAULT NULL,
  `port` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb35jynxmyn4c6sem69obtlf8t` (`src_id`),
  CONSTRAINT `FKb35jynxmyn4c6sem69obtlf8t` FOREIGN KEY (`src_id`) REFERENCES `source` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device`
--

LOCK TABLES `device` WRITE;
/*!40000 ALTER TABLE `device` DISABLE KEYS */;
/*!40000 ALTER TABLE `device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `document`
--

DROP TABLE IF EXISTS `document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `documentmodel_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2ddvpq5qnywkhrlh8pqn1iipl` (`documentmodel_id`),
  CONSTRAINT `FK2ddvpq5qnywkhrlh8pqn1iipl` FOREIGN KEY (`documentmodel_id`) REFERENCES `documentmodel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document`
--

LOCK TABLES `document` WRITE;
/*!40000 ALTER TABLE `document` DISABLE KEYS */;
/*!40000 ALTER TABLE `document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `documentfield`
--

DROP TABLE IF EXISTS `documentfield`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `documentfield` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `valor` varchar(255) DEFAULT NULL,
  `documentmodel_id` binary(16) DEFAULT NULL,
  `documentmodelfield_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmlqyxhe6ha16fdkcxhdubqrk9` (`documentmodel_id`),
  KEY `FK6kyt15md0yhywnr2ostl1aqkq` (`documentmodelfield_id`),
  CONSTRAINT `FK6kyt15md0yhywnr2ostl1aqkq` FOREIGN KEY (`documentmodelfield_id`) REFERENCES `documentmodelfield` (`id`),
  CONSTRAINT `FKmlqyxhe6ha16fdkcxhdubqrk9` FOREIGN KEY (`documentmodel_id`) REFERENCES `document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documentfield`
--

LOCK TABLES `documentfield` WRITE;
/*!40000 ALTER TABLE `documentfield` DISABLE KEYS */;
/*!40000 ALTER TABLE `documentfield` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `documentitem`
--

DROP TABLE IF EXISTS `documentitem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `documentitem` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `qty` double NOT NULL,
  `document_id` binary(16) DEFAULT NULL,
  `product_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp7yh32lq64iq0j7ldyie3ex04` (`document_id`),
  KEY `FKk2xpoq7j03ytyervdcsgh2t88` (`product_id`),
  CONSTRAINT `FKk2xpoq7j03ytyervdcsgh2t88` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKp7yh32lq64iq0j7ldyie3ex04` FOREIGN KEY (`document_id`) REFERENCES `document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documentitem`
--

LOCK TABLES `documentitem` WRITE;
/*!40000 ALTER TABLE `documentitem` DISABLE KEYS */;
/*!40000 ALTER TABLE `documentitem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `documentmodel`
--

DROP TABLE IF EXISTS `documentmodel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `documentmodel` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documentmodel`
--

LOCK TABLES `documentmodel` WRITE;
/*!40000 ALTER TABLE `documentmodel` DISABLE KEYS */;
INSERT INTO `documentmodel` VALUES ('&¡f	%E ¦ra‘iÕ–O','MO','Manufacture Order','2017-11-09 17:49:18','2017-11-09 17:49:18',NULL),('/\Zð€\nICœ‡X”»F)Y“','NFE-out','Nota Fiscal de Saida','2017-11-09 17:49:18','2017-11-09 17:49:18',NULL),('¾û)<.LË´yŒw¼c”','PO','Purchase Order','2017-11-09 17:49:18','2017-11-09 17:49:18',NULL),('\ê\ã;–yO÷²¯…´\ÞME\Z','NFE-in','Nota Fiscal de Entrada','2017-11-09 17:49:18','2017-11-09 17:49:18',NULL);
/*!40000 ALTER TABLE `documentmodel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `documentmodelfield`
--

DROP TABLE IF EXISTS `documentmodelfield`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `documentmodelfield` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `documentmodel_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKddrdclyytpai0phr8074xhxfb` (`documentmodel_id`),
  CONSTRAINT `FKddrdclyytpai0phr8074xhxfb` FOREIGN KEY (`documentmodel_id`) REFERENCES `documentmodel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documentmodelfield`
--

LOCK TABLES `documentmodelfield` WRITE;
/*!40000 ALTER TABLE `documentmodelfield` DISABLE KEYS */;
/*!40000 ALTER TABLE `documentmodelfield` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `documentthing`
--

DROP TABLE IF EXISTS `documentthing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `documentthing` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `document_id` binary(16) DEFAULT NULL,
  `thing_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsnsla5y57cev9spoajcv22ugc` (`document_id`),
  KEY `FKhv7qg7fu822q5v8yp6la4v2an` (`thing_id`),
  CONSTRAINT `FKhv7qg7fu822q5v8yp6la4v2an` FOREIGN KEY (`thing_id`) REFERENCES `thing` (`id`),
  CONSTRAINT `FKsnsla5y57cev9spoajcv22ugc` FOREIGN KEY (`document_id`) REFERENCES `document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documentthing`
--

LOCK TABLES `documentthing` WRITE;
/*!40000 ALTER TABLE `documentthing` DISABLE KEYS */;
/*!40000 ALTER TABLE `documentthing` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `groupjoin`
--

DROP TABLE IF EXISTS `groupjoin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `groupjoin` (
  `inside_id` binary(16) NOT NULL,
  `group_id` binary(16) NOT NULL,
  PRIMARY KEY (`group_id`,`inside_id`),
  KEY `FKsp33tgk0v2k0acodrky8smlh` (`inside_id`),
  CONSTRAINT `FK4krxhy8eqsu2i6oieu04v76wt` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`),
  CONSTRAINT `FKsp33tgk0v2k0acodrky8smlh` FOREIGN KEY (`inside_id`) REFERENCES `groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groupjoin`
--

LOCK TABLES `groupjoin` WRITE;
/*!40000 ALTER TABLE `groupjoin` DISABLE KEYS */;
/*!40000 ALTER TABLE `groupjoin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grouppermission`
--

DROP TABLE IF EXISTS `grouppermission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `grouppermission` (
  `permission_id` binary(16) NOT NULL,
  `group_id` binary(16) NOT NULL,
  PRIMARY KEY (`group_id`,`permission_id`),
  KEY `FK3s6bo92c3oe1cayp4ka1s8uy5` (`permission_id`),
  CONSTRAINT `FK2ljcovdlxoy2g56nfe0offxlt` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`),
  CONSTRAINT `FK3s6bo92c3oe1cayp4ka1s8uy5` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grouppermission`
--

LOCK TABLES `grouppermission` WRITE;
/*!40000 ALTER TABLE `grouppermission` DISABLE KEYS */;
INSERT INTO `grouppermission` VALUES ('\nö{\ÞQ B·®~(\ír\åŸ','=)=A(Ž«„…‚Œ·'),('OH,Í¡@c¾\á6o€€','=)=A(Ž«„…‚Œ·'),('!Œ$ðˆBLÀ½›\Ïx%;\Æ','=)=A(Ž«„…‚Œ·'),('8R\å	wNŸB:¶q04','=)=A(Ž«„…‚Œ·'),('9ó¶wFq¨j;\ÆÿQ','=)=A(Ž«„…‚Œ·'),('@~lL7”ñŠv–\Òe‡','=)=A(Ž«„…‚Œ·'),('D¹B–”:N`—_u?=š\è¥','=)=A(Ž«„…‚Œ·'),('aõyQ¾ƒLö º\Û\ÅIõ^','=)=A(Ž«„…‚Œ·'),('~t	D^J­Á%\\Bhx•','=)=A(Ž«„…‚Œ·'),('¯=Æ©uL°¹K*!','=)=A(Ž«„…‚Œ·'),('Â¾6f\Ù\ãH>¯yG\é\Ø~ö\Ï','=)=A(Ž«„…‚Œ·'),('\ìºOªLZ¤_q™lÐ³®','=)=A(Ž«„…‚Œ·'),('ó,4©2tE/µ‚Y[\Z\ãô','=)=A(Ž«„…‚Œ·'),('ý/L^¨¶IO«\è\É,\n¸Z','=)=A(Ž«„…‚Œ·'),('ÿ\Â4\Ñ5FO1ª\ät±\Òÿ\Ñ','=)=A(Ž«„…‚Œ·');
/*!40000 ALTER TABLE `grouppermission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `groups`
--

DROP TABLE IF EXISTS `groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `groups` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groups`
--

LOCK TABLES `groups` WRITE;
/*!40000 ALTER TABLE `groups` DISABLE KEYS */;
INSERT INTO `groups` VALUES ('=)=A(Ž«„…‚Œ·','ADMINS','HUNTER ADM','2017-11-16 15:28:44','2017-11-16 15:28:44',NULL),('Uµ˜^\ÖD¡Œ\ây7»õfc','USR','Users','2017-11-24 18:12:58','2017-11-24 18:12:58',NULL);
/*!40000 ALTER TABLE `groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `groupuser`
--

DROP TABLE IF EXISTS `groupuser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `groupuser` (
  `user_id` binary(16) NOT NULL,
  `group_id` binary(16) NOT NULL,
  PRIMARY KEY (`group_id`,`user_id`),
  KEY `FK60kwj3qfdta78uv7tv2d1wqnb` (`user_id`),
  CONSTRAINT `FK60kwj3qfdta78uv7tv2d1wqnb` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKcixsublfenkhxqh86e91iq0ot` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groupuser`
--

LOCK TABLES `groupuser` WRITE;
/*!40000 ALTER TABLE `groupuser` DISABLE KEYS */;
INSERT INTO `groupuser` VALUES (',Ž\\•Ï‡Nø‡™$¬9»\É','=)=A(Ž«„…‚Œ·'),(',Ž\\•Ï‡Nø‡™$¬9»\É','Uµ˜^\ÖD¡Œ\ây7»õfc');
/*!40000 ALTER TABLE `groupuser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `origin`
--

DROP TABLE IF EXISTS `origin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `origin` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `params` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `origin`
--

LOCK TABLES `origin` WRITE;
/*!40000 ALTER TABLE `origin` DISABLE KEYS */;
INSERT INTO `origin` VALUES ('!bJ»HAK:1¥}H_cM','STORE1','Armazenagem 1','2017-11-09 17:49:18','2017-12-12 18:25:55','48a75fe9-3fa4-4851-8c7a-d979277a353a,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),(')^¥\ÅBOM“Àh\Äô3\à©','PICK2','SeparaÃ§Ã£o 2','2017-11-09 17:49:18','2017-12-12 18:27:50','4d59e103-59f5-4fb2-9dec-aaad58ff4f4f,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('Pó\â8$\ÚE–š\Èw‚\Ì—','STORE2','Armazenagem 2','2017-11-09 17:49:18','2017-12-12 18:26:03','e416d2f2-b6af-41c7-b995-6e2dd11f430c,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('tC€BŽ¦ûü²\Íh{\è','REC3','Recebimento 3','2017-11-09 17:49:18','2017-12-12 18:27:27','ccf553d6-e3ba-4dbf-b0b1-b32a345f1d00,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('­ª$\äWKˆ¸;\ÌT¦','PICK1','SeparaÃ§Ã£o 1','2017-11-09 17:49:18','2017-12-12 18:27:45','53cb3b1a-2d83-4641-bbf0-0c8be61eaeef,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('®wˆ5MÜ¹¤¼‚\Å\r›','STORE3','Armazenagem 3','2017-11-09 17:49:18','2017-12-12 18:26:12','2abecc6c-01e9-4b98-995b-ad46f4dc65e0,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('¸\Ôkai	HP©b.µ‡—','SHIP1','ExpediÃ§Ã£o 1','2017-11-09 17:49:18','2017-12-12 18:26:57','b570d940-5016-412d-8e9a-355c0436c57a,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('Á÷?[G…˜{\çV™·','REC2','Recebimento 2','2017-11-09 17:49:18','2017-12-12 18:27:35','8e572310-c2b8-4e00-9ea8-d84dc28fa1d8,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('\Ã\Ý(…\ÛA\ååŒŽ°\0[»','SHIP2','ExpediÃ§Ã£o 2','2017-11-09 17:49:18','2017-12-12 18:27:03','2e18a6ee-23b2-4a8f-ba1a-b959456c4067,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('\È.tf-ŒHš„\í66f','REC4','Recebimento 4','2017-11-09 17:49:18','2017-12-12 18:27:40','2f56ebf7-a5d7-4ea5-90cd-fc2ae934d574,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('\ÌøRºe˜L¿þ\Å‡S˜K','REC1','Recebimento 1','2017-11-09 17:49:18','2017-12-12 18:27:31','5092ec1a-23f0-4333-9784-3bf65886cc97,10,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('\Û\Zl¢-,J¡\Ãþ\ÝqÁ','SHIP3','ExpediÃ§Ã£o 3','2017-11-09 17:49:18','2017-12-12 18:27:10','8e602fee-5872-4248-9468-d7361d057a3a,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL),('\ì\í2£ÀDÇ§£I\á\Éu','SHIP4','ExpediÃ§Ã£o 4','2017-11-09 17:49:18','2017-12-12 18:27:15','bd103b3a-1d0a-4993-880a-47ed21a67e05,1,IDENT','com.gtp.hunter.process.wf.origin.DeviceOrigin',NULL);
/*!40000 ALTER TABLE `origin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `permission` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `params` varchar(255) DEFAULT NULL,
  `route` varchar(255) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES ('\nö{\ÞQ B·®~(\ír\åŸ','PGRP','View Origin','2017-11-16 15:54:10','2017-11-16 15:54:10','asdf','/home/process/viewOrigin','home',NULL),('OH,Í¡@c¾\á6o€€','DEVICES','Manage Devices','2017-12-12 10:27:31','2017-12-12 10:27:31',NULL,'/home/core/device','cube','NOVO'),('!Œ$ðˆBLÀ½›\Ïx%;\Æ','TASKS','My Tasks','2017-11-29 17:16:50','2017-12-12 10:25:49',NULL,'/home/process/viewTasks','tasks','NOVO'),('8R\å	wNŸB:¶q04','SOURCES','Manage Sources','2017-12-12 10:21:37','2017-12-12 10:21:37',NULL,'/home/core/source','cubes','NOVO'),('9ó¶wFq¨j;\ÆÿQ','ORIGIN','Manage Origins','2017-12-12 18:24:08','2017-12-12 18:24:08',NULL,'/home/process/origin','cog','NOVO'),('@~lL7”ñŠv–\Òe‡','DOCMDL','Document Models','2017-12-12 10:36:35','2017-12-12 10:36:35',NULL,'/home/process/documentModel','file-o','NOVO'),('D¹B–”:N`—_u?=š\è¥','USERS','Manage Users','2017-12-12 10:16:19','2017-12-12 10:16:19',NULL,'/home/core/user','user','NOVO'),('aõyQ¾ƒLö º\Û\ÅIõ^','PERSON','Manage People','2017-12-12 10:17:06','2017-12-12 10:17:06',NULL,'/home/core/person','users','NOVO'),('~t	D^J­Á%\\Bhx•','ACTIONS','Manage Actions','2017-12-12 10:34:50','2017-12-12 10:34:50',NULL,'/home/process/action','check','NOVO'),('¯=Æ©uL°¹K*!','TSKDEF','Task Definitions','2017-12-12 10:37:26','2017-12-12 10:37:26',NULL,'/home/process/taskDef','pencil-square-o','NOVO'),('Â¾6f\Ù\ãH>¯yG\é\Ø~ö\Ï','GROUPS','Manage Groups','2017-12-12 10:30:50','2017-12-12 10:30:50',NULL,'/home/core/group','users','NOVO'),('\ìºOªLZ¤_q™lÐ³®','PUSR','View RTLS RawData','2017-11-16 15:54:10','2017-11-16 15:54:10','asdf','/home/core/viewRtls','home',NULL),('ó,4©2tE/µ‚Y[\Z\ãô','PERM','Manage Permissions','2017-12-12 10:15:39','2017-12-12 10:15:39',NULL,'/home/core/permission','reorder','NOVO'),('ý/L^¨¶IO«\è\É,\n¸Z','LOCS','Manage Locations','2017-12-12 10:17:51','2017-12-12 10:17:51',NULL,'/home/core/location','map-marker','NOVO'),('ÿ\Â4\Ñ5FO1ª\ät±\Òÿ\Ñ','PROCS','Manage Process','2017-12-12 15:58:07','2017-12-12 15:58:07',NULL,'/home/process/process','play','NOVO');
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = cp850 */ ;
/*!50003 SET character_set_results = cp850 */ ;
/*!50003 SET collation_connection  = cp850_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 trigger before_insert_permission
before insert on permission
for each row
begin
if new.id is null then
set new.id = unhex(replace(uuid(),'-',''));
end if;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `birthdate` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person`
--

LOCK TABLES `person` WRITE;
/*!40000 ALTER TABLE `person` DISABLE KEYS */;
/*!40000 ALTER TABLE `person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prefix`
--

DROP TABLE IF EXISTS `prefix`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prefix` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `count` bigint(20) DEFAULT NULL,
  `prefix` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prefix`
--

LOCK TABLES `prefix` WRITE;
/*!40000 ALTER TABLE `prefix` DISABLE KEYS */;
/*!40000 ALTER TABLE `prefix` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `process`
--

DROP TABLE IF EXISTS `process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `process` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `classe` varchar(255) DEFAULT NULL,
  `estadoDe` varchar(255) DEFAULT NULL,
  `estadoPara` varchar(255) DEFAULT NULL,
  `param` varchar(255) DEFAULT NULL,
  `origin_id` binary(16) DEFAULT NULL,
  `wf_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `cancelable` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnjldv8wfkn6bmt2l51lt4crsw` (`origin_id`),
  KEY `FK7xmdtlo34bousayy7m37e7pr9` (`wf_id`),
  CONSTRAINT `FK7xmdtlo34bousayy7m37e7pr9` FOREIGN KEY (`wf_id`) REFERENCES `workflow` (`id`),
  CONSTRAINT `FKnjldv8wfkn6bmt2l51lt4crsw` FOREIGN KEY (`origin_id`) REFERENCES `origin` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `process`
--

LOCK TABLES `process` WRITE;
/*!40000 ALTER TABLE `process` DISABLE KEYS */;
INSERT INTO `process` VALUES ('e{O©þþI‹Ÿ\Û¼2þ2z','TESTE','TESTE DE FILTER','2017-11-09 17:49:18','2017-12-13 16:27:37','com.gtp.hunter.process.wf.process.SimpleProcess','NOVO','RECEBIDO','',NULL,NULL,'NOVO','\0'),('¥¯{\ë:\ØNF±0òF\àÑ€Y','REC','RECEBIMENTO','2017-12-12 17:54:23','2017-12-13 16:10:33','com.gtp.hunter.process.wf.process.DocumentCheckingProcess','NOVO','RECEBIDO','%%doc%%,NOVO,RECEBIDO,ITEM NÃƒO ENCONTRADO,ITEM NÃƒO LISTADO NO DOCUMENTO,RECEBIDO',NULL,NULL,'NOVO','\0');
/*!40000 ALTER TABLE `process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `processfilter`
--

DROP TABLE IF EXISTS `processfilter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `processfilter` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `classe` varchar(255) DEFAULT NULL,
  `phase` varchar(255) DEFAULT NULL,
  `process_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKalwxw5qpayk29ojig77ph7xa9` (`process_id`),
  CONSTRAINT `FKalwxw5qpayk29ojig77ph7xa9` FOREIGN KEY (`process_id`) REFERENCES `process` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `processfilter`
--

LOCK TABLES `processfilter` WRITE;
/*!40000 ALTER TABLE `processfilter` DISABLE KEYS */;
INSERT INTO `processfilter` VALUES ('ZKÁ¶´Uù±U',NULL,'AuditFilter','2017-11-09 17:49:18','2017-11-09 17:49:18','com.gtp.hunter.process.wf.process.filter.AuditProcessFilter','PRETRANSFORM','e{O©þþI‹Ÿ\Û¼2þ2z',NULL);
/*!40000 ALTER TABLE `processfilter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `productmodel_id` binary(16) DEFAULT NULL,
  `parent_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `sku` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKloytllxbwyjwmtxlp5uf3utbv` (`productmodel_id`),
  KEY `FKgmb19wbjvpu06559t7w33wqoc` (`parent_id`),
  CONSTRAINT `FKgmb19wbjvpu06559t7w33wqoc` FOREIGN KEY (`parent_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKloytllxbwyjwmtxlp5uf3utbv` FOREIGN KEY (`productmodel_id`) REFERENCES `productmodel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES ('\0a„¾a F¨‘IFY$\Æ',NULL,'SUPORTE COL 03LT DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0050101'),('X\ÎbM@x‚}Í†æ²•\0',NULL,'COLETOR  MAT.PERFUROCORTANTE 7L RIGIDO','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0146101'),('GmE}jOB¨œ/D\ç^}9',NULL,'CX MASTER COLETOR NR 7 422X374X135MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160202'),('X¡\n\àfJ¸¤¨$À­2',NULL,'LUVA VINIL SEM PO G (100UNID/CART) CX20CT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0541301'),('8A\èG¯J“¹É‡™×‚',NULL,'DESCONECTADOR DE AGULHA','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160505'),('ñ6¶\êJ¢³t®\íw9p',NULL,'EMBALAGEM PEBD M','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370114'),('©x7OŽ¨\àOñ¦¬',NULL,'LUVA LATEX SEM PO P PROC USO MEDICO(100UNID/CART) CX20CT -CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0122201'),('SR€C,¢,…ŸŒ»\Úô',NULL,'AGULHA 25 X 0.80 ESTERIL (100UNID/CART) CX100CT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0353301'),('·Žš\çcF›…FHkª',NULL,'LUVA NITRILICA SEM PO M AZ (100UN/CT) CX20CT CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0433301'),('XvDW´OÖ›¤$Q·Fe',NULL,'CX COLETOR E BANDEJA NR 1.5','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160503'),('\Ôì€¼¶N‚ŽòðF)~5º',NULL,'CATETER PERIFERICO IV 18G TEFLON DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0360301'),('Cwú“Mn¦·`Yù=]',NULL,'SUPORTE COL 07LT DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0050201'),('\ZN5v)DFŽU–‚t\Ô:',NULL,'LUVA VINIL SEM PO P (100UNID/CART) CX20CT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0541101'),('™Ž;Fa£\"`;E¡\Ã',NULL,'FOLHA DE PAPELAO 70GR 800X1000MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160105'),('\Û\Î] \èEƒû¼\Ãe^\Ê',NULL,'MASCARA TRIPLA COM ELASTICO (50UNID/CART) CX150CT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0110601'),('\Ù-S˜cOO‘Jð~nñ³',NULL,'SERINGA S/AG SLIP 60 ML DESCARTAVEL','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0323101'),(' )ž*0tC_ŠÁÍ®K8À\Â',NULL,'CATETER PERIFERICO IV 16G TEFLON DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0360201'),(' s±\Ù\ËHN¯ \0\é\Ê\'ðö',NULL,'SERINGA S/AG LUER LOCK 3ML (UNID) CX4000UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0324201'),('!b\Õ\\D1¢Ø–¿|',NULL,'AGULHA 30 X 0.70 ESTERIL (100UNID/CART) CX100CT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0353401'),('!Ñ‡\ÑgnMCª@GK)­\ï',NULL,'FITA TR5331-2 VERDE 2.4CM LARGX500MTRILAMINADA HIGIFIX ESQ/D','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370109'),('\"Jz¸1ŠBëŒž›†²ÌŽr',NULL,'LUVA LATEX COM PO G PROC USO MEDICO DPK (100UNID/CART)CX20CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0531401'),('\"zx4U\ÊJ~“‰ºZ‰Ô†›',NULL,'PANO MULTIUSO 28CM X 300M AZUL (ROLO) CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0572101'),('\"›\Õu¤€H²—v—\Ù(tr',NULL,'EMBALAGEM PEBD G','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370204'),('#\Ø\í\Ú>Iò¡¨ÿ˜K¦]',NULL,'SERINGA C/AG LUER SLIP 5 ML 25X0.7MM (UNID) CX2800UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0341201'),('$\Zó\"Î¢A\nŠ(^òj¢¦\Æ',NULL,'LUVA NITRILICA SEM PO G AZ (100UN/CT) CX20CT CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0433401'),('$:f¼OJÖ„²mD€˜šŒ',NULL,'PELICULA REVESTIMENTO COLETOR 20L 600X710MM X 0.06 MICRAS','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160404'),('%E@Œd@\ë§Å—lþ',NULL,'LENCOL COM ELASTICO (UNID) CX500UNID - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0511101'),('\'>ñ3¶MÜŽœ1¢ŒÎ¸L',NULL,'PAPEL ABSORVENTE INFANTIL 19G 10 X 60CM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070110'),('\'\ÈuŒ\ÉBJ‹Šƒ‰ƒR\Î%ú',NULL,'CARTUCHO 330G/M2 MASCARA PFF2 4 CORES','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0170104'),('(E¿\Õ\æ\ÄN4³W™:|lE\à',NULL,'CELULOSE IMP LARG 250MM DIAM 1245MM','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370115'),('**<\r/G*ª†3‡,',NULL,'LUVA CIRURGICA ESTERIL 7.5 DPK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0210301'),('+T\àcUJôÒ*C',NULL,'LUVA CIRURGICA ESTERIL 8.0 DPK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0210401'),('-IGY\Ä@s„¿‰G\â\×¥',NULL,'LUVA LATEX PP PROC INDUSTRIAL (100UNID/CART) CX20CT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0550101'),('-\Új]\ÓG«ˆB\ßOë ',NULL,'LUVA CIRURGICA ESTERIL 7.0 DPK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0210201'),('.þ:4OF¥¡\nñjN\Ô',NULL,'PEBD POL LEITOSO EG 19G 810MM D.EXT.430MM A 460MM TB 77MM','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370302'),('1a†q\ïGô²\Ö6‘\ØÏšX',NULL,'CATETER PERIFERICO IV 20G TEFLON DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0360401'),('2ftošùE\æ»\îH”|Ñ²',NULL,'PELICULA REVESTIMENTO COLETOR  3L 330X470MM X 0.05 MICRAS','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160104'),('3Q6€¿a@\íœ4RŠuO',NULL,'SACO TARA MAX PFF2 150MMX130MMX0.07 DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0170106'),('9ª’YoSNÁ‚v¯\ã*˜F',NULL,'COLA BRANCA PVA R-410','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0162210'),(':ðºüOŸ‰!Ktˆ‹:',NULL,'MASCARA TRIPLA COM TIRAS (50UNID/CART) CX100CT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0100701'),('>P:-Bñ Ž†\ï“wFL',NULL,'GEL SUPER ABSORVENTE IMP','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370116'),('>\Î9o<fK»·Š\Ú&o',NULL,'MASCARA DUPLA COM ELASTICO (100UNID/PT) CX120PT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0112301'),('?ö\ÐÜ‚I]»\èVN8DL',NULL,'AGULHA 13 X 0.45 ESTERIL (100UNID/CART) CX100CT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0353101'),('@¡5O\ç	@+‘Þ‡w\Þ1',NULL,'LUVA LATEX SEM PO P PROC USO MEDICO DPK(100UNID/CART)CX20CT','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0123201'),('CÏ€\ÒjmNg…em&½a',NULL,'SERINGA S/AG LUER LOCK 5ML (UNID) CX2800UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0324401'),('D6VA(K½¥R5­t^¢ò',NULL,'PANO MULTIUSO 30CM X 30M AZUL (ROLO) 24ROLOS/CAIXA','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0571301'),('H¥1ŸN!”do\ëOak',NULL,'COLA BRANCA GALAO KG','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0162205'),('LÍš§²sDÞ‡ˆ\rŒ%\äò0',NULL,'TORNEIRA 3 VIAS LUER LOCK VERMELHA','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0380201'),('M2_õþHê™—bºº²',NULL,'EMBALAGEM INFANTIL PEBD P','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070104'),('Mº\Ë._@Ú»m ÷.Ü¡\à',NULL,'EQUIPO DE NUTRICAO ENTERAL (UND) CX400UNID','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0411101'),('P¥\Ïù\îôA¨\Ñ:Ÿ£h',NULL,'CAIXA BRANCA 180 LITROS','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','97020058'),('SS§\æa\ÍB\n€²À\í$\Úbh',NULL,'EQUIPO LUER SLIP SIMPLES MACROGOTAS DESCARPACK CX600 UNID','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0412101'),('U–\áÿ}@¥¼~\ä–ß¶€',NULL,'SERINGA S/AG LUER SLIP 10ML (UNID) CX2000UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0324501'),('WYo«WG¿—\Ê:hR¯',NULL,'LUVA LATEX G PROC INDUSTRIAL (100UNID/CART) CX20CT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0550401'),('X9±Z%tC`—3\ã]\Ï+‚',NULL,'PANO MULTIUSO 28CM X 300M LARANJA (ROLO) CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0572301'),('Y*\ÂHSCÍŸ.škR\';I',NULL,'CX COLETOR E BANDEJA NR 20','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160403'),('Z\ÒMó8\Ð@¦”\'‰Aµ\ÞC½',NULL,'LUVA POLIETILENO TAM UNICO (100UNID/PT) CX350PT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0590401'),('_0W,xJº½‹Ô©?—',NULL,'SUPORTE COL 1.5LT DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0050601'),('_ûp\Æ^uO®Á&µ´™‘£',NULL,'EMBALAGEM PEBD P','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370122'),('`.•\Å\â}K²\ã\æRX\Ö',NULL,'LUVA LATEX M PROC INDUSTRIAL (100UNID/CART) CX20CT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0550301'),('dyƒ4‡G!\ç›\ÚB«n',NULL,'SERINGA S/AG LUER LOCK 10ML (UNID) CX2000UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0324601'),('f8hœ†Jß¨\à\Å2ŸüZ—',NULL,'LUVA LATEX COM PO P PROC USO MEDICO (100UNID/CART) CX20CT CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0530201'),('f¡F¼!!IÖ¶L&>\ÊW',NULL,'LUVA CIRURGICA ESTERIL 8.0','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0084401'),('fð;ƒ»O\ç™v\È&ý2ý\î',NULL,'SERINGA S/AG CATETER 60 ML DESCARTAVEL','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0323301'),('jOQžŸpFQµö:R7Wx',NULL,'SERINGA DE INSULINA 1ML 13X0.45  CX3000UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0341001'),('pzxDWNº\Ã\Ì~œ^¼þ',NULL,'LUVA LATEX SEM PO M PROC USO MEDICO DPK(100UNID/CART) CX20CT','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0123301'),('r]6¿Ý´J‰¯\âS\Ôb\ì—8',NULL,'GAZE 7.5 X 7.5CM 13 FIOS S/ RX EST (10UNID/PT) CX1000PT','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0444301'),('r²¶(AKŠ‚/ðZ\×',NULL,'CX COLETOR E BANDEJA NR 13','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160303'),('sú…-\Â8KÊªz&ò¼$r,',NULL,'HOT MELT INDICADOR DE UMIDADE','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0374103'),('tN£CpD(œu\rE‰ðd}',NULL,'CAMPO OPERATORIO 25X28CM ESTERIL (5UNID/PT) CX150PT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0446301'),('to\Æ¶¥LN¾\çr\Û&\â}\Ü',NULL,'LUVA LATEX COM PO G PROC USO MEDICO (100UNID/CART) CX20CT CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0530401'),('zHgy\ËAÀ•¯v:k¡¬',NULL,'PELICULA REVESTIMENTO COLETOR 1','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160504'),('}_‚m¼·Lƒ\\Có\Òp',NULL,'COLETOR PLASTICO QUIMIOTERAPICO 7L DESCARPACK III IMP','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0144201'),('~\Û:š\ã\ÃO£ŸE\æœö¼Ý¬',NULL,'LUVA CIRURGICA ESTERIL 8.5','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0084501'),('t|žMõ†.8T\â&\Ï',NULL,'LUVA LATEX COM PO PP PROC USO MEDICO (100UNID/CART)CX20CT CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0530101'),('€\é\nˆJ´E¡”K¬‡¬Z',NULL,'PANO MULTIUSO 30CM X 30M LARANJA (ROLO) 24ROLOS/CAIXA','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0571201'),('€òsk\ÕGÑ¬‚L=óg ',NULL,'FRONTAL TAPE INFANTIL 230MM X 3000','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070105'),('‚Àò–ULE¨¤ŸxºŒ',NULL,'SERINGA S/AG LOCK 60 ML DESCARTAVEL','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0323201'),('ƒ\êI—L\n\Ô\"Š; V',NULL,'FITA BILAMINADA HYG AZUL - 50MM X 750M','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070106'),('†2F\Î÷þ@Ü«³B~N+¶ö',NULL,'CX MASTER COLETOR NR 1.5 292X241X134MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160502'),('†rrS|D\nˆ)\r­\ÒGq\Ø',NULL,'LUVA NITRILICA SEM PO P AZ (100UN/CT) CX20CT CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0433201'),('‰#d\à\ÖIÉ»+h”yH',NULL,'CX MASTER COLETOR NR 13 512X512X130MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160302'),('‰I\Ìµ‹J—®-B„\Æp\Ä',NULL,'CX MASTER COLETOR NR 3 369X275X124MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160102'),('‰¬ý™ÁGõµ\í\à\Íxˆ\ì',NULL,'HOT MELT CONSTRUCAO IMP','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370118'),('ŒD%ju‚FŽªdŠ¦aõ-',NULL,'HOT MELT ELASTANO IMP','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370119'),('=&b”Gs¿†\ã\ØYþüˆ',NULL,'CAIXA MASTER PARA REPOSICAO','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','97060003'),('\Æ\è²\äLD©¥ö\ávŸ¢˜\Æ',NULL,'EMBALAGEM INFANTIL PEBD G','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070205'),('\É8Äˆ\ÌMûŠž\ëù<œy',NULL,'MASCARA DESCARTAVEL AZUL (CAIXA C/ 640 UNID)','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0170402'),('‘…sfôIŒ­yJ…¯ý™ ',NULL,'SERINGA C/AG LUER SLIP 20 ML 25X0.7MM (UNID) CX1200UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0341401'),('“%m(\ÃñB€–-5‘\Ô9\åo',NULL,'LUVA CIRURGICA ESTERIL 6.5','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0084101'),('•r\Z\'ZyBo¸{S*\ä8ø',NULL,'SPUN HIDROFILO BCO G 9G/M2 IMP LRG 780MM DIAM 760MM','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370205'),('– üƒR_C‡ªdÉš\Üø',NULL,'COLETOR PARA RESIDUOS TOXICOS IMP','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0145201'),('–ø‚J\åª\\†ö°»ñ',NULL,'AGULHA 25 X 0.70 ESTERIL (100UNID/CART) CX100CT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0353201'),('—Ö¬•\ãûEÒ¾NªfgkŠ',NULL,'FARDO PEBD PARA FRALDA','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370111'),('˜£>9ŒFŽm]{\Æ)',NULL,'SUPORTE COL 20LT DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0050401'),('še”GY¨‹£]kP\á',NULL,'TOUCA (100UNID/PT) CX100PT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0093201'),('šýÒ¾)\nN”§(”$He',NULL,'PEBD POL LEITOSO M 19G 610MM D.EXT.430MM A 460MM TB 77MM','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370104'),('›\æNX\á\ÈG%•\Ó5±”Wÿ\Å',NULL,'FOLHA DE PAPELAO 65GR 900X1100MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160405'),('œ\å0ô	÷Eù™\Ð\0ÿ\ÐX',NULL,'LUVA LATEX COM PO M PROC USO MEDICO DPK (100UNID/CART)CX20CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0531301'),('¡a°\å\î‡Iñœ+[\'(p\×',NULL,'CATETER PERIFERICO IV 14G TEFLON DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0360101'),('§;+»–\ÓEœY®›:sœ,',NULL,'SERINGA C/AG LUER SLIP 3ML 25X0.7MM (UNID) CX4000UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0341101'),('§y“\é?Oƒ½^\é?Ê†š',NULL,'COLETOR PARA RESIDUOS TOXICOS','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0145101'),('¨$&¥ˆ\ÈC%Ž\ÏbüÁ¤:(',NULL,'LUVA CIRURGICA ESTERIL 6.5 DPK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0210101'),('ªo\çš$QOÐ½¼)G‰­|',NULL,'AGULHA 40 X 1.20 ESTERIL (100UNID/CART) CX100CT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0353601'),('«\ÚoY£\ÛO×¹†Mø„¿P',NULL,'PAPEL ABSORVENTE BRANCO LARG 16MM DIAM 590 A 620MM TB 77MM','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370113'),('¬IQ\nAþª_;\îr>.',NULL,'FITA BILAMINADA ADULTO VERDE -1-401-0186 BS 5030 VD','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370121'),('®¤¿«\ÊûB¼©m1\Ç\É\Â\Ð',NULL,'BOLSA COLETORA DE URINA 2L (UNID) CX160UND','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0490101'),('±ˆ&”1\ÙH³¶—¸·¨®5',NULL,'SPUN HIDROFILO BCO M 9G/M2 IMP LRG 610MM DIAM 760MM','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370117'),('³\Û\âC\ÔKB}®Ÿy\Ð2Q.}',NULL,'SUPORTE PANO MULTIUSO','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0051101'),('¸/nÁ5.D9¸N£üj\'lô',NULL,'LUVA LATEX SEM PO G PROC USO MEDICO DPK(100UNID/CART) CX20CT','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0123401'),('¸\ê¿­6@ú†\Þ	¼•\ËYŸ',NULL,'LUVA CIRURGICA ESTERIL 7.5','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0084301'),('¹¦\Ø\à–¸IµÆˆ·ºxO\ß',NULL,'LUVA LATEX COM PO P PROC USO MEDICO DPK (100UNID/CART)CX20CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0531201'),('¾²kŽ`jMÿ¸›ðu•·«',NULL,'EQUIPO LUER SLIP C/ FILTRO E INJ MACROGOTAS (UNID) CX500UNID','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0410201'),('¿OÙ¯UAÕœ\Ðlr–<F',NULL,'COLETOR MAT. PERFUROCORTANTES 7L RIGIDO IMP','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0146201'),('Áw«Õ‰H±§Iš/ƒ§:6',NULL,'PELICULA REVESTIMENTO COLETOR  7L 430X560MM X 0.05 MICRAS','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160204'),('Á\êVZM@ñ\Ãb÷Ð¢Vu',NULL,'SPUN HIDROFILO BCO EG 9G/M2 IMP LRG 810MM DIAM 760MM','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370305'),('\ÃAÿ{–C¸s\06 ¾iÿ',NULL,'EQUIPO DE NUTRICAO ESCALONADO (UNID) CX 400UNID','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0411201'),('\Äz¨\â-\×D‹…Á\É0\ÖF',NULL,'PELICULA REVESTIMENTO COLETOR 13L 560X630MM X 0.06 MICRAS','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160304'),('\Å7\ZË¨@\"¶\ØDp³N¬',NULL,'LUVA LATEX COM PO PP PROC USO MEDICO DPK(100UNID/CART)CX20CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0531101'),('\Åhö½\ÙK\ç¨,c•¹j\éd',NULL,'CX MASTER MASCARA N95 546X356X206MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0170105'),('\Å\Üð?\çùD » ô\êu>Bj',NULL,'LUVA LATEX COM PO M PROC USO MEDICO (100UNID/CART) CX20CT CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0530301'),('\Ç\Êq\ë\Ü\ìO “\Å	y°“4',NULL,'PEBD POL LEITOSO P 20G 260MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070103'),('\Ë5%b\"/A¸·8¿\ÓñO½',NULL,'FARDO PLASTICO INFANTIL 56X88X0.11','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070109'),('Ì€_\ÌCŒHoŒÙ¡¦\í·',NULL,'COLETOR PLASTICO QUIMIOTERAPICO  7L DESCARPACK III','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0144101'),('\ÎP7“£BIŽ‘\ÑÝ‹…\Ï',NULL,'PEBD POL LEITOSO G 19G 780MM D.EXT.430MM A 460MM TB 77MM','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370202'),('\Ð{\ê\Â\Ã\ÅF‰©\é?x¨y',NULL,'LUVA LATEX SEM PO G PROC USO MEDICO(100UNID/CART) CX20CT -CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0122401'),('\Ðõ`û\ÓJ-™o´}\0\ì{',NULL,'AVENTAL NORMAL MANGA LONGA (UNID) CX700UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0190601'),('\ÒôÁIø·o’‡±¤û',NULL,'SPUN HIDROFILO BCO M/G/EG 9G/M2 LG 320MM DIAM 760MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070202'),('Ó€³/Y E¯HJ˜¡\Åõ',NULL,'LUVA LATEX SEM PO M PROC USO MEDICO(100UNID/CART) CX20CT -CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0122301'),('\Ó\Ø\Ì\Þ\ÒMa¬š\á1\Ä\Ùa',NULL,'SERINGA S/AG LUER LOCK 20ML (UNID) CX1200UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0324801'),('\Ô®½·LÐ\ÉZ7n\ÂpE',NULL,'EMBALAGEM INFANTIL PEBD EG','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070206'),('\Ôü`#¼<@g‘¬\Êk&\ß',NULL,'CATETER PERIFERICO IV 24G TEFLON DESCARPACK','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0360601'),('\Ö]”yg~O\0½§GI¦²',NULL,'CX COLETOR E BANDEJA NR 7','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160203'),('\ÖõDjNG[šÅŠ\â}\×\Æt',NULL,'SERINGA C/AG LUER SLIP 10 ML 25X0.7MM (UNID) CX2000UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0341301'),('\ØJûtƒ9K#³\ÐÔ£¸y#',NULL,'SUPORTE COL 13LT DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0050301'),('\Ø\àþ\ÃùI%‰SÇ¬\ÞZ:	',NULL,'EMBALAGEM INFANTIL PEBD M','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070204'),('\Ø\æ/IAµœ…\ÂMš',NULL,'PANO MULTIUSO 28CM X 300M BRANCO (ROLO) CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0572201'),('\Ù:A¸\í\ÌEx‹ˆRôˆ®,¥',NULL,'LUVA LATEX P PROC INDUSTRIAL (100UNID/CART) CX20CT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0550201'),('\Ú@\å]Í†G+ŽYˆ\"',NULL,'LUVA VINIL SEM PO M (100UNID/CART) CX20CT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0541201'),('\Ý$\î#ö\ÏEœK\êù\á',NULL,'CATETER PERIFERICO IV 22G TEFLON DESCARPACK','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0360501'),('Ýš	f\0-D ¬\éÌ¤f\Æ\ëm',NULL,'AGULHA 30 X 0.80 ESTERIL (100UNID/CART) CX100CT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0353501'),('\Þs\×÷·D¨Mg/J',NULL,'LUVA CIRURGICA ESTERIL 7.0','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0084201'),('ß«\æ=\Ò6KÚ™x\nõ\ê7%½',NULL,'SERINGA S/AG LUER SLIP 3ML (UNID) CX4000UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0324101'),('\àoD¡\×sI@²C\ÂøcË—',NULL,'PEBD POL LEITOSO M/G/EG 20G 320MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0070203'),('\âÕ•g@\0ž\ç§ø£$¥',NULL,'EMBALAGEM PEBD EG','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370304'),('\âó\ÐEV\ßLZŽ@‘O»»',NULL,'BARREIRA BCO 12G/M2 IMP LRG 120MM DIAM 760MM','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370120'),('\ãb\ÕÛ¯^MQ²°\0<†d\Z',NULL,'SERINGA S/AG LUER SLIP 5ML (UNID) CX2800UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0324301'),('\ã³[\'Jd¸3¦ƒGÚ´#',NULL,'LUVA VINIL COM PO G (100UNID/CART) CX20CT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0540301'),('\ã¸X­\Ø\ÕE¡\\uù³\í',NULL,'LUVA VINIL COM PO P (100UNID/CART) CX20CT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0540101'),('\æ/*~ý„JMš\Ã\â\Ýe¼­r',NULL,'FIO DE ELASTANO EMBALAGEM 6 RL  TB 77MM','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0370105'),('\æ\Ô“\Ï|Ll©\ä\noD•Iü',NULL,'PANO MULTIUSO 30CM X 30M VERDE (ROLO) 24ROLOS/CAIXA','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0571101'),('\ëK.V\éB3ª‹œc´‘§9',NULL,'CX COLETOR E BANDEJA NR 3','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160103'),('\ë™\â\ØMmG8˜	\Øˆ;¼G',NULL,'PRO-PE (200UNID/PT) CX24PT - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0150601'),('\îaa5B‡º0§ýšÀ\á',NULL,'SERINGA S/AG LUER SLIP 20ML (UNID) CX1200UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0324701'),('\ï\0sžBTAb˜Ò’\Ê:_\å',NULL,'TORNEIRA 3 VIAS LUER SLIP VERMELHA','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0380101'),('ô„Fs\rL˜³‡ú«ZL\å',NULL,'LANCETA DE SEGURANCA DESCARPACK 28G CX 4.000','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0600101'),('ø\é\íZ]¸OÈŸXûŒ\\\ÆLe',NULL,'CX MASTER COLETOR NR 20 612X558X150MM','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0160402'),('ú|IXÀN¯¶\Z)\Óò	',NULL,'GAZE 7.5 X 7.5CM 11 FIOS S/ RX ESTERIL (10UNID/PT) CX1000PT','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0444201'),('ú¨uS¿–Ckˆþöd`ýµ',NULL,'LUVA VINIL COM PO M (100UNID/CART) CX20CT - CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0540201'),('ü`\ï\æˆiD†–\Óm>×‘Á',NULL,'AVENTAL ESPECIAL MANGA LONGA (UNID) CX700UNID - CN','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0191401'),('ý¯^q\èHŸ¦z\ìÁ3¡t',NULL,'SUPORTE COL RIGIDO 07LT DESCARPACK','2017-12-13 19:19:18','2017-12-13 19:19:18','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0050701'),('þK\Ð\Î\r¡CÆµ·s·£úi',NULL,'PANO MULTIUSO 28CM X 300M VERDE (ROLO) CN','2017-12-13 19:19:19','2017-12-13 19:19:19','ühÎ»\áHæŽ½—tr/',NULL,'NOVO','0572401');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productfield`
--

DROP TABLE IF EXISTS `productfield`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `productfield` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `productmodelfield_id` binary(16) DEFAULT NULL,
  `product_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9jw8y01ac6m8xbq7cme86hroo` (`productmodelfield_id`),
  KEY `FK91kbaoa2odfwic5nxixd98ylb` (`product_id`),
  CONSTRAINT `FK91kbaoa2odfwic5nxixd98ylb` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FK9jw8y01ac6m8xbq7cme86hroo` FOREIGN KEY (`productmodelfield_id`) REFERENCES `productmodelfield` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productfield`
--

LOCK TABLES `productfield` WRITE;
/*!40000 ALTER TABLE `productfield` DISABLE KEYS */;
/*!40000 ALTER TABLE `productfield` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productmodel`
--

DROP TABLE IF EXISTS `productmodel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `productmodel` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `propertymodel_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnwews5eddqywe9h75ec1hb3xi` (`propertymodel_id`),
  CONSTRAINT `FK96h2rpfwngcdx1odsvvg6bn2l` FOREIGN KEY (`propertymodel_id`) REFERENCES `propertymodel` (`id`),
  CONSTRAINT `FKnwews5eddqywe9h75ec1hb3xi` FOREIGN KEY (`propertymodel_id`) REFERENCES `propertymodel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productmodel`
--

LOCK TABLES `productmodel` WRITE;
/*!40000 ALTER TABLE `productmodel` DISABLE KEYS */;
INSERT INTO `productmodel` VALUES ('JùDinAÏÓ¾u}“$ö','AUTH','Itens de AutenticaÃ§Ã£o','2017-11-09 17:49:18','2017-11-09 17:49:18',NULL,NULL),('ühÎ»\áHæŽ½—tr/','WAREITEM','Item de Armazenamento Perecivel','2017-11-09 17:49:18','2017-12-01 11:53:01',NULL,'\í\Ó\ÖEGS‡HÒ¼ªü');
/*!40000 ALTER TABLE `productmodel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productmodelfield`
--

DROP TABLE IF EXISTS `productmodelfield`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `productmodelfield` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `productmodel_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9496rpqjpy18m0qm4vtm7l0vy` (`productmodel_id`),
  CONSTRAINT `FK9496rpqjpy18m0qm4vtm7l0vy` FOREIGN KEY (`productmodel_id`) REFERENCES `productmodel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productmodelfield`
--

LOCK TABLES `productmodelfield` WRITE;
/*!40000 ALTER TABLE `productmodelfield` DISABLE KEYS */;
/*!40000 ALTER TABLE `productmodelfield` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property`
--

DROP TABLE IF EXISTS `property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `property` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `propertymodelfield_id` binary(16) DEFAULT NULL,
  `thing_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtc65t7k8325a9vd02oc6s2o6x` (`propertymodelfield_id`),
  KEY `FKpvdh9j08xfb0xplifbdm2m39s` (`thing_id`),
  CONSTRAINT `FKpvdh9j08xfb0xplifbdm2m39s` FOREIGN KEY (`thing_id`) REFERENCES `thing` (`id`),
  CONSTRAINT `FKtc65t7k8325a9vd02oc6s2o6x` FOREIGN KEY (`propertymodelfield_id`) REFERENCES `propertymodelfield` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property`
--

LOCK TABLES `property` WRITE;
/*!40000 ALTER TABLE `property` DISABLE KEYS */;
/*!40000 ALTER TABLE `property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `propertymodel`
--

DROP TABLE IF EXISTS `propertymodel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `propertymodel` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `propertymodel`
--

LOCK TABLES `propertymodel` WRITE;
/*!40000 ALTER TABLE `propertymodel` DISABLE KEYS */;
INSERT INTO `propertymodel` VALUES ('\í\Ó\ÖEGS‡HÒ¼ªü',NULL,'ITENS PERECIVEIS','2017-12-01 11:53:01','2017-12-01 11:53:01',NULL);
/*!40000 ALTER TABLE `propertymodel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `propertymodelfield`
--

DROP TABLE IF EXISTS `propertymodelfield`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `propertymodelfield` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `propertymodel_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlk4gg62x5ar634gk2kbi29nd5` (`propertymodel_id`),
  CONSTRAINT `FKlk4gg62x5ar634gk2kbi29nd5` FOREIGN KEY (`propertymodel_id`) REFERENCES `propertymodel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `propertymodelfield`
--

LOCK TABLES `propertymodelfield` WRITE;
/*!40000 ALTER TABLE `propertymodelfield` DISABLE KEYS */;
INSERT INTO `propertymodelfield` VALUES ('!\íù\Ú.Hc™É¹\Í\è»','VALIDADE','VALIDADE','2017-12-01 11:53:01','2017-12-01 11:53:01','DATE','\í\Ó\ÖEGS‡HÒ¼ªü',NULL),('t£Cb\æH‚\ëo_/\Ýa¡','LOTE','LOTE','2017-12-01 11:53:01','2017-12-01 11:53:01','TEXT','\í\Ó\ÖEGS‡HÒ¼ªü',NULL);
/*!40000 ALTER TABLE `propertymodelfield` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `source`
--

DROP TABLE IF EXISTS `source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `source` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `source`
--

LOCK TABLES `source` WRITE;
/*!40000 ALTER TABLE `source` DISABLE KEYS */;
INSERT INTO `source` VALUES ('^\ã•˜DD˜\ìsR1õ™\â','PORTALRECEB','DMS PortÃµes Recebimento','2017-12-12 11:35:28','2017-12-12 11:35:28','NOVO');
/*!40000 ALTER TABLE `source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `user_id` binary(16) DEFAULT NULL,
  `doc_id` binary(16) DEFAULT NULL,
  `taskdef_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8d76eif6wo7d4klhbkjubcaw3` (`doc_id`),
  KEY `FK87yaeyomong7aynx6g5u08mvj` (`taskdef_id`),
  CONSTRAINT `FK87yaeyomong7aynx6g5u08mvj` FOREIGN KEY (`taskdef_id`) REFERENCES `taskdef` (`id`),
  CONSTRAINT `FK8d76eif6wo7d4klhbkjubcaw3` FOREIGN KEY (`doc_id`) REFERENCES `document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `taskdef`
--

DROP TABLE IF EXISTS `taskdef`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `taskdef` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `docmodel_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgiutheq0ei1rdqmpksmngucor` (`docmodel_id`),
  CONSTRAINT `FK7tjnpkute1pg5pujpfgj7nb92` FOREIGN KEY (`docmodel_id`) REFERENCES `documentmodel` (`id`),
  CONSTRAINT `FKgiutheq0ei1rdqmpksmngucor` FOREIGN KEY (`docmodel_id`) REFERENCES `documentmodel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taskdef`
--

LOCK TABLES `taskdef` WRITE;
/*!40000 ALTER TABLE `taskdef` DISABLE KEYS */;
INSERT INTO `taskdef` VALUES ('I´¼LšI\á•d!Ò­F«&','REIMPETQ','ReimpressÃ£o de Etiquetas',NULL,'2017-11-24 18:30:35','2017-11-24 18:30:35','IMPRESSO','¾û)<.LË´yŒw¼c”'),('9¯–½uI5˜Þ„¸ù\"´','IMPETQ','ImpressÃ£o de Etiquetas',NULL,'2017-11-24 18:30:35','2017-11-24 18:30:35','PARAIMPRESSAO','¾û)<.LË´yŒw¼c”'),('IV \ÍMŸR§V¦o^(','RECEBIMENTO','Recebimento','NOVO','2017-12-12 11:39:40','2017-12-12 11:39:40','IMPRESSO','¾û)<.LË´yŒw¼c”');
/*!40000 ALTER TABLE `taskdef` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `taskdefpermission`
--

DROP TABLE IF EXISTS `taskdefpermission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `taskdefpermission` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `permission` binary(16) DEFAULT NULL,
  `taskdef_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnhw2a5q9xyxou585dp0jourkk` (`taskdef_id`),
  CONSTRAINT `FKnhw2a5q9xyxou585dp0jourkk` FOREIGN KEY (`taskdef_id`) REFERENCES `taskdef` (`id`),
  CONSTRAINT `FKqgpqu6y3kr4gvcub0wxvmydo5` FOREIGN KEY (`taskdef_id`) REFERENCES `taskdef` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taskdefpermission`
--

LOCK TABLES `taskdefpermission` WRITE;
/*!40000 ALTER TABLE `taskdefpermission` DISABLE KEYS */;
INSERT INTO `taskdefpermission` VALUES ('}¡¡\Ò9SE«\Öa~$.\î',NULL,NULL,NULL,'2017-11-24 19:10:10','2017-11-24 19:10:10','=)=A(Ž«„…‚Œ·','I´¼LšI\á•d!Ò­F«&'),('\éUvq\ßC\ç˜\ë\\&\nj–“',NULL,NULL,NULL,NULL,NULL,'=)=A(Ž«„…‚Œ·','IV \ÍMŸR§V¦o^('),('þ\Ê\êŸ\èH-¡m\àú£\Î	ý',NULL,NULL,NULL,'2017-11-24 19:10:10','2017-11-24 19:10:10','Uµ˜^\ÖD¡Œ\ây7»õfc','9¯–½uI5˜Þ„¸ù\"´');
/*!40000 ALTER TABLE `taskdefpermission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thing`
--

DROP TABLE IF EXISTS `thing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `thing` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `propertymodel_id` binary(16) DEFAULT NULL,
  `parent_id` binary(16) DEFAULT NULL,
  `product_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd0wx689970r5nntw17ewqq267` (`propertymodel_id`),
  KEY `FKo685pq6qm1j4ltdhpb4cdbh8d` (`parent_id`),
  KEY `FKhj1ap0dkcvskemkw33giwr2f` (`product_id`),
  CONSTRAINT `FKd0wx689970r5nntw17ewqq267` FOREIGN KEY (`propertymodel_id`) REFERENCES `propertymodel` (`id`),
  CONSTRAINT `FKhj1ap0dkcvskemkw33giwr2f` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKo685pq6qm1j4ltdhpb4cdbh8d` FOREIGN KEY (`parent_id`) REFERENCES `thing` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thing`
--

LOCK TABLES `thing` WRITE;
/*!40000 ALTER TABLE `thing` DISABLE KEYS */;
/*!40000 ALTER TABLE `thing` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thing_units`
--

DROP TABLE IF EXISTS `thing_units`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `thing_units` (
  `Thing_id` binary(16) NOT NULL,
  `units` binary(255) DEFAULT NULL,
  KEY `FKqmxvdy2mrxa08hg90i2glslr2` (`Thing_id`),
  CONSTRAINT `FKqmxvdy2mrxa08hg90i2glslr2` FOREIGN KEY (`Thing_id`) REFERENCES `thing` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thing_units`
--

LOCK TABLES `thing_units` WRITE;
/*!40000 ALTER TABLE `thing_units` DISABLE KEYS */;
/*!40000 ALTER TABLE `thing_units` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unit`
--

DROP TABLE IF EXISTS `unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unit` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `tagId` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `thing_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `unit_tagid_idx` (`tagId`),
  KEY `FK9r7bb5i8hab0i77ggx8y68fs9` (`thing_id`),
  CONSTRAINT `FK9r7bb5i8hab0i77ggx8y68fs9` FOREIGN KEY (`thing_id`) REFERENCES `thing` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unit`
--

LOCK TABLES `unit` WRITE;
/*!40000 ALTER TABLE `unit` DISABLE KEYS */;
/*!40000 ALTER TABLE `unit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `device_id` int(11) DEFAULT NULL,
  `person_id` binary(16) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKndcphsq4uj55r61wpu5dxlupf` (`device_id`),
  KEY `FKir5g7yucydevmmc84i788jp79` (`person_id`),
  CONSTRAINT `FKir5g7yucydevmmc84i788jp79` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`),
  CONSTRAINT `FKndcphsq4uj55r61wpu5dxlupf` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (',Ž\\•Ï‡Nø‡™$¬9»\É',NULL,'Admin','2017-11-13 17:23:38','2017-11-13 17:23:38',NULL,NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `userlocation`
--

DROP TABLE IF EXISTS `userlocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userlocation` (
  `user_id` binary(16) NOT NULL,
  `location_id` binary(16) NOT NULL,
  PRIMARY KEY (`user_id`,`location_id`),
  KEY `FKxr72d3xjv7vsjdrk5ndisewo` (`location_id`),
  CONSTRAINT `FKh8wj42nevsf82cqvgcnc7cdvf` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKxr72d3xjv7vsjdrk5ndisewo` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `userlocation`
--

LOCK TABLES `userlocation` WRITE;
/*!40000 ALTER TABLE `userlocation` DISABLE KEYS */;
/*!40000 ALTER TABLE `userlocation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `userpermission`
--

DROP TABLE IF EXISTS `userpermission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userpermission` (
  `permission_id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  PRIMARY KEY (`user_id`,`permission_id`),
  KEY `FK5j6kxx2g0pxrd8ht2ss9e0uoe` (`permission_id`),
  CONSTRAINT `FK5j6kxx2g0pxrd8ht2ss9e0uoe` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`),
  CONSTRAINT `FKs5wddn2j2872axd91k4heuvoe` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `userpermission`
--

LOCK TABLES `userpermission` WRITE;
/*!40000 ALTER TABLE `userpermission` DISABLE KEYS */;
/*!40000 ALTER TABLE `userpermission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workflow`
--

DROP TABLE IF EXISTS `workflow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `workflow` (
  `id` binary(16) NOT NULL,
  `metaname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workflow`
--

LOCK TABLES `workflow` WRITE;
/*!40000 ALTER TABLE `workflow` DISABLE KEYS */;
INSERT INTO `workflow` VALUES ('\Þ?Jü\\.FÉ¯¹\ê(x\åg',NULL,'Hunter Descarpack','2017-11-09 17:49:18','2017-11-09 17:49:18',NULL);
/*!40000 ALTER TABLE `workflow` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-12-13 21:29:48
