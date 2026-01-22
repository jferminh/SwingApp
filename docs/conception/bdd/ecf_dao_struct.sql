CREATE DATABASE  IF NOT EXISTS `ecf_dao` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `ecf_dao`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: ecf_dao
-- ------------------------------------------------------
-- Server version	8.4.7

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `adresse`
--

DROP TABLE IF EXISTS `adresse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `adresse` (
  `id_adresse` int NOT NULL AUTO_INCREMENT,
  `numero_rue` varchar(10) NOT NULL,
  `nom_rue` varchar(100) NOT NULL,
  `code_postal` char(5) NOT NULL,
  `ville` varchar(50) NOT NULL,
  PRIMARY KEY (`id_adresse`),
  KEY `idx_code_postal` (`code_postal`),
  KEY `idx_ville` (`ville`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `client`
--

DROP TABLE IF EXISTS `client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `client` (
  `id_client` int NOT NULL AUTO_INCREMENT,
  `id_societe` int NOT NULL,
  `chiffre_affaires` int NOT NULL,
  `nb_employes` smallint NOT NULL,
  PRIMARY KEY (`id_client`),
  KEY `fk_client_societe` (`id_societe`),
  CONSTRAINT `fk_client_societe` FOREIGN KEY (`id_societe`) REFERENCES `societe` (`id_societe`),
  CONSTRAINT `chk_chiffre_affaires` CHECK ((`chiffre_affaires` >= 200)),
  CONSTRAINT `chk_nb_employes` CHECK ((`nb_employes` >= 1))
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contrat`
--

DROP TABLE IF EXISTS `contrat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contrat` (
  `id_contrat` int NOT NULL AUTO_INCREMENT,
  `client_id` int NOT NULL,
  `nom_contrat` varchar(100) NOT NULL,
  `montant` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_contrat`),
  KEY `idx_client_id` (`client_id`),
  KEY `idx_nom_contrat` (`nom_contrat`),
  CONSTRAINT `fk_contrat_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id_client`),
  CONSTRAINT `chk_montant` CHECK ((`montant` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prospect`
--

DROP TABLE IF EXISTS `prospect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prospect` (
  `id_prospect` int NOT NULL AUTO_INCREMENT,
  `id_societe` int NOT NULL,
  `date_prospection` date NOT NULL,
  `interesse` tinyint(1) NOT NULL,
  PRIMARY KEY (`id_prospect`),
  KEY `fk_prospect_societe` (`id_societe`),
  KEY `idx_date_prospection` (`date_prospection`),
  CONSTRAINT `fk_prospect_societe` FOREIGN KEY (`id_societe`) REFERENCES `societe` (`id_societe`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `societe`
--

DROP TABLE IF EXISTS `societe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `societe` (
  `id_societe` int NOT NULL AUTO_INCREMENT,
  `raison_sociale` varchar(100) NOT NULL,
  `adresse_id` int NOT NULL,
  `telephone` varchar(15) NOT NULL,
  `email` varchar(100) NOT NULL,
  `commentaires` text,
  PRIMARY KEY (`id_societe`),
  UNIQUE KEY `raison_sociale` (`raison_sociale`),
  KEY `fk_societe_adresse` (`adresse_id`),
  KEY `idx_raison_sociale` (`raison_sociale`),
  KEY `idx_email` (`email`),
  CONSTRAINT `fk_societe_adresse` FOREIGN KEY (`adresse_id`) REFERENCES `adresse` (`id_adresse`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-22 14:43:53
