/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.5.2-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: javamodel
-- ------------------------------------------------------
-- Server version	11.5.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `memo`
--

DROP TABLE IF EXISTS `memo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `memo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci DEFAULT NULL,
  `created_at` datetime GENERATED ALWAYS AS (current_timestamp()) VIRTUAL,
  `index` int(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id_idx` (`user_id`),
  CONSTRAINT `user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `memo`
--

LOCK TABLES `memo` WRITE;
/*!40000 ALTER TABLE `memo` DISABLE KEYS */;
INSERT INTO `memo` VALUES
(1,5,'1123','2025-05-24 09:33:57',8),
(4,5,'12331231222123123132','2025-05-24 09:33:57',5),
(5,5,'2213231','2025-05-24 09:33:57',6),
(6,5,'321123231312','2025-05-24 09:33:57',7),
(7,19,'22','2025-05-24 09:33:57',1),
(8,5,'ffㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ','2025-05-24 09:33:57',2),
(18,5,'ㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ\nㅇㄴㅁㅇㅁㄴ','2025-05-24 09:33:57',1),
(19,5,'123123132','2025-05-24 09:33:57',4),
(20,5,'123413241432','2025-05-24 09:33:57',3),
(21,5,'324141241321243','2025-05-24 09:33:57',9),
(22,5,'234534523423','2025-05-24 09:33:57',10),
(23,5,'1431243312','2025-05-24 09:33:57',11),
(24,5,'23452345552','2025-05-24 09:33:57',13),
(26,5,'dsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsadsaddasd','2025-05-24 09:33:57',12);
/*!40000 ALTER TABLE `memo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES
(5,'33','33'),
(9,'332','222133'),
(10,'1123123','123'),
(18,'2231','123132'),
(19,'123','123');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2025-05-24 18:33:57
