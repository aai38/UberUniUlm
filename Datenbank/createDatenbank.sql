-- phpMyAdmin SQL Dump
-- version 4.9.2
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Erstellungszeit: 11. Dez 2019 um 16:49
-- Server-Version: 10.4.10-MariaDB
-- PHP-Version: 7.3.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `uberUniUlm`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `bookedRides`
--

CREATE TABLE `bookedRides` (
  `userId` int(255) NOT NULL,
  `rideId` int(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `offeredRides`
--

CREATE TABLE `offeredRides` (
  `userId` int(255) NOT NULL,
  `rideId` int(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `rides`
--

CREATE TABLE `rides` (
  `id` int(255) NOT NULL,
  `departure` varchar(255) NOT NULL,
  `destination` varchar(255) NOT NULL,
  `route` varchar(255) NOT NULL,
  `parkingspot` varchar(255) DEFAULT NULL,
  `price` int(255) NOT NULL,
  `date` date NOT NULL,
  `time` time(6) NOT NULL,
  `places` int(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `user`
--

CREATE TABLE `user` (
  `id` int(255) NOT NULL,
  `prename` varchar(255) NOT NULL,
  `lastname` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `gender` varchar(255) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rating` int(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `bookedRides`
--
ALTER TABLE `bookedRides`
  ADD PRIMARY KEY (`userId`,`rideId`),
  ADD KEY `FK_rideId_bookedRides` (`rideId`);

--
-- Indizes für die Tabelle `offeredRides`
--
ALTER TABLE `offeredRides`
  ADD PRIMARY KEY (`userId`,`rideId`),
  ADD KEY `FK_rideId_offeredRides` (`rideId`);

--
-- Indizes für die Tabelle `rides`
--
ALTER TABLE `rides`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `rides`
--
ALTER TABLE `rides`
  MODIFY `id` int(255) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT für Tabelle `user`
--
ALTER TABLE `user`
  MODIFY `id` int(255) NOT NULL AUTO_INCREMENT;

--
-- Constraints der exportierten Tabellen
--

--
-- Constraints der Tabelle `bookedRides`
--
ALTER TABLE `bookedRides`
  ADD CONSTRAINT `FK_rideId_bookedRides` FOREIGN KEY (`rideId`) REFERENCES `rides` (`id`),
  ADD CONSTRAINT `FK_userId_bookedRides` FOREIGN KEY (`userId`) REFERENCES `user` (`id`);

--
-- Constraints der Tabelle `offeredRides`
--
ALTER TABLE `offeredRides`
  ADD CONSTRAINT `FK_rideId_offeredRides` FOREIGN KEY (`rideId`) REFERENCES `rides` (`id`),
  ADD CONSTRAINT `FK_userID_offeredRides` FOREIGN KEY (`userId`) REFERENCES `user` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
