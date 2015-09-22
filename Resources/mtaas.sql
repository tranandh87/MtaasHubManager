CREATE DATABASE  IF NOT EXISTS `MTAAS` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `MTAAS`;
-- MySQL dump 10.13  Distrib 5.5.35, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: MTAAS
-- ------------------------------------------------------
-- Server version	5.5.35-0ubuntu0.12.04.2

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
-- Table structure for table `TestSuite`
--

DROP TABLE IF EXISTS `TestSuite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TestSuite` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  KEY `index` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TestSuite`
--

LOCK TABLES `TestSuite` WRITE;
/*!40000 ALTER TABLE `TestSuite` DISABLE KEYS */;
/*!40000 ALTER TABLE `TestSuite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Tracking`
--

DROP TABLE IF EXISTS `Tracking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Tracking` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `step` varchar(45) NOT NULL,
  `stepDescription` varchar(60000) DEFAULT NULL,
  `result` varchar(200) NOT NULL,
  `testMethodExecutionId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_testMehodExecId` (`id`),
  KEY `fk_testMethodExec_id` (`testMethodExecutionId`),
  CONSTRAINT `fk_testMethodExec_id` FOREIGN KEY (`testMethodExecutionId`) REFERENCES `TestMethodExecutionResult` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Tracking`
--

LOCK TABLES `Tracking` WRITE;
/*!40000 ALTER TABLE `Tracking` DISABLE KEYS */;
/*!40000 ALTER TABLE `Tracking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TestMethod`
--

DROP TABLE IF EXISTS `TestMethod`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TestMethod` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `testId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index` (`name`,`testId`),
  KEY `fk_testMethod_testCaseId` (`testId`),
  CONSTRAINT `fk_testMethod_testCaseId` FOREIGN KEY (`testId`) REFERENCES `Test` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TestMethod`
--

LOCK TABLES `TestMethod` WRITE;
/*!40000 ALTER TABLE `TestMethod` DISABLE KEYS */;
/*!40000 ALTER TABLE `TestMethod` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Hub`
--

DROP TABLE IF EXISTS `Hub`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Hub` (
  `hubId` int(11) NOT NULL AUTO_INCREMENT,
  `platformSupported` enum('Android','iOS') NOT NULL,
  `status` enum('Available','Busy','Unavailable') NOT NULL,
  `hubUrl` varchar(45) NOT NULL,
  PRIMARY KEY (`hubId`),
  UNIQUE KEY `uqPlatformUrl` (`platformSupported`,`hubUrl`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Hub`
--

LOCK TABLES `Hub` WRITE;
/*!40000 ALTER TABLE `Hub` DISABLE KEYS */;
/*!40000 ALTER TABLE `Hub` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `AUTDetails`
--

DROP TABLE IF EXISTS `AUTDetails`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AUTDetails` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `appName` varchar(45) DEFAULT NULL,
  `appActivity` varchar(200) NOT NULL,
  `packageName` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniqueAut` (`appName`,`appActivity`,`packageName`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AUTDetails`
--

LOCK TABLES `AUTDetails` WRITE;
/*!40000 ALTER TABLE `AUTDetails` DISABLE KEYS */;
/*!40000 ALTER TABLE `AUTDetails` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Request`
--

DROP TABLE IF EXISTS `Request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Request` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `requestTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `AUTDetailId` int(11) NOT NULL,
  `OriginalRequestId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Request_executionId_fk` (`requestTime`),
  KEY `fk_AUT_Details` (`AUTDetailId`),
  CONSTRAINT `fk_AUT_Details` FOREIGN KEY (`AUTDetailId`) REFERENCES `AUTDetails` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Request`
--

LOCK TABLES `Request` WRITE;
/*!40000 ALTER TABLE `Request` DISABLE KEYS */;
/*!40000 ALTER TABLE `Request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TestExecutionResult`
--

DROP TABLE IF EXISTS `TestExecutionResult`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TestExecutionResult` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `testId` int(11) NOT NULL,
  `testSuiteExecutionId` int(11) NOT NULL,
  `deviceId` int(11) NOT NULL,
  `status` enum('PASS','FAIL','SKIP','INPROGRESS','SUCCESS_PERCENTAGE_FAILURE') DEFAULT NULL,
  `executionStartTime` timestamp NULL DEFAULT NULL,
  `executionEndTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_testResult_testId` (`testId`),
  KEY `fk_testResult_testSuiteId` (`testSuiteExecutionId`),
  KEY `fk_testResult_deviceId` (`deviceId`),
  KEY `fk_testResult_testSuiteExecId` (`testSuiteExecutionId`),
  CONSTRAINT `fk_testResult_deviceId` FOREIGN KEY (`deviceId`) REFERENCES `Device` (`deviceId`) ON UPDATE CASCADE,
  CONSTRAINT `fk_testResult_testId` FOREIGN KEY (`testId`) REFERENCES `Test` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_testResult_testSuiteExecId` FOREIGN KEY (`testSuiteExecutionId`) REFERENCES `TestSuiteExecutionResult` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TestExecutionResult`
--

LOCK TABLES `TestExecutionResult` WRITE;
/*!40000 ALTER TABLE `TestExecutionResult` DISABLE KEYS */;
/*!40000 ALTER TABLE `TestExecutionResult` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Device`
--

DROP TABLE IF EXISTS `Device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Device` (
  `deviceId` int(11) NOT NULL AUTO_INCREMENT,
  `platform` enum('Android','iOS') NOT NULL,
  `osVersion` varchar(15) NOT NULL,
  `hubId` int(11) NOT NULL,
  `manufacturer` varchar(25) DEFAULT NULL,
  `appiumIp` varchar(45) NOT NULL,
  `isEmulator` int(11) NOT NULL,
  `status` enum('Available','Busy','Unavailable') DEFAULT NULL,
  `model` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`deviceId`),
  UNIQUE KEY `uniqueDevice` (`platform`,`osVersion`,`hubId`,`manufacturer`,`appiumIp`,`isEmulator`,`model`),
  KEY `hubId` (`hubId`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Device`
--

LOCK TABLES `Device` WRITE;
/*!40000 ALTER TABLE `Device` DISABLE KEYS */;
/*!40000 ALTER TABLE `Device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Test`
--

DROP TABLE IF EXISTS `Test`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Test` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `testSuiteId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index3` (`name`,`testSuiteId`),
  KEY `fk_Test_TestSuiteId` (`testSuiteId`),
  CONSTRAINT `fk_Test_TestSuiteId` FOREIGN KEY (`testSuiteId`) REFERENCES `TestSuite` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Test`
--

LOCK TABLES `Test` WRITE;
/*!40000 ALTER TABLE `Test` DISABLE KEYS */;
/*!40000 ALTER TABLE `Test` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TestMethodExecutionResult`
--

DROP TABLE IF EXISTS `TestMethodExecutionResult`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TestMethodExecutionResult` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `testMethodId` int(11) NOT NULL,
  `deviceId` int(11) NOT NULL,
  `status` enum('PASS','FAIL','SKIP','INPROGRESS','SUCCESS_PERCENTAGE_FAILURE') NOT NULL,
  `executionStartTime` timestamp NULL DEFAULT NULL,
  `executionEndTime` timestamp NULL DEFAULT NULL,
  `testExecutionId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_testMethodEx_testMethodId` (`testMethodId`),
  KEY `fk_testMethodEx_deviceId` (`deviceId`),
  KEY `fk_testMethodEx_testExecId` (`testExecutionId`),
  CONSTRAINT `fk_testMethodEx_deviceId` FOREIGN KEY (`deviceId`) REFERENCES `Device` (`deviceId`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_testMethodEx_testExecId` FOREIGN KEY (`testExecutionId`) REFERENCES `TestExecutionResult` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_testMethodEx_testMethodId` FOREIGN KEY (`testMethodId`) REFERENCES `TestMethod` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TestMethodExecutionResult`
--

LOCK TABLES `TestMethodExecutionResult` WRITE;
/*!40000 ALTER TABLE `TestMethodExecutionResult` DISABLE KEYS */;
/*!40000 ALTER TABLE `TestMethodExecutionResult` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TestSuiteExecutionResult`
--

DROP TABLE IF EXISTS `TestSuiteExecutionResult`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TestSuiteExecutionResult` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `testSuiteId` int(11) NOT NULL,
  `deviceId` int(11) NOT NULL,
  `status` enum('PASS','FAIL','SKIP','INPROGRESS','SUCCESS_PERCENTAGE_FAILURE') DEFAULT NULL,
  `executionStartTime` timestamp NULL DEFAULT NULL,
  `executionEndTime` timestamp NULL DEFAULT NULL,
  `requestId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_deviceId` (`deviceId`),
  KEY `fk_testSuiteId` (`testSuiteId`),
  KEY `fk_requestId` (`requestId`),
  CONSTRAINT `fk_deviceId` FOREIGN KEY (`deviceId`) REFERENCES `Device` (`deviceId`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_requestId` FOREIGN KEY (`requestId`) REFERENCES `Request` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_testSuiteId` FOREIGN KEY (`testSuiteId`) REFERENCES `TestSuite` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TestSuiteExecutionResult`
--

LOCK TABLES `TestSuiteExecutionResult` WRITE;
/*!40000 ALTER TABLE `TestSuiteExecutionResult` DISABLE KEYS */;
/*!40000 ALTER TABLE `TestSuiteExecutionResult` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'MTAAS'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-04-12 15:15:52
