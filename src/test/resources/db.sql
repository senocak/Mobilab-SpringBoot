-- phpMyAdmin SQL Dump
-- version 5.1.0
-- https://www.phpmyadmin.net/
--
-- Host: db
-- Generation Time: Jul 22, 2021 at 06:24 PM
-- Server version: 8.0.1-dmr
-- PHP Version: 7.4.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `hibernate`
--

-- --------------------------------------------------------

--
-- Table structure for table `accounts`
--

CREATE TABLE `accounts` (
  `id` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `balance` decimal(19,2) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `accounts`
--

INSERT INTO `accounts` (`id`, `created_at`, `updated_at`, `balance`, `currency`, `deleted`, `name`, `user_id`) VALUES
('636c1e45-eac1-4c42-b4c5-ffb6883dee12', '2021-07-22 18:24:01', '2021-07-22 18:24:30', '110.10', 'TRY', b'0', 'TR Account', 1),
('bcd674d3-9a95-453e-8f11-9f341f9c7d47', '2021-07-22 18:24:01', '2021-07-22 18:24:30', '298.82', 'USD', b'0', 'USD Account', 2),
('d5dc1164-8ef7-474d-8e53-665390dde1c7', '2021-07-22 18:24:01', '2021-07-22 18:24:01', '200.00', 'EUR', b'0', 'EUR Account', 1),
('e20d3526-afcd-43db-a717-7a2959b1afbc', '2021-07-22 18:24:01', '2021-07-22 18:24:01', '400.00', 'EUR', b'0', 'EUR Account', 2);

-- --------------------------------------------------------

--
-- Table structure for table `transfers`
--

CREATE TABLE `transfers` (
  `id` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `amount` decimal(19,2) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `from_account_id` varchar(255) NOT NULL,
  `to_account_id` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `transfers`
--

INSERT INTO `transfers` (`id`, `created_at`, `updated_at`, `amount`, `currency`, `from_account_id`, `to_account_id`) VALUES
('2d856571-3cd9-4500-a014-258faf799b41', '2021-07-22 18:24:18', '2021-07-22 18:24:18', '2.00', 'EUR', 'bcd674d3-9a95-453e-8f11-9f341f9c7d47', '636c1e45-eac1-4c42-b4c5-ffb6883dee12'),
('60307110-8d88-451d-8e82-d996a1c1c92d', '2021-07-22 18:24:01', '2021-07-22 18:24:01', '1.00', 'EUR', 'bcd674d3-9a95-453e-8f11-9f341f9c7d47', '636c1e45-eac1-4c42-b4c5-ffb6883dee12'),
('8ab12012-b2bc-4c8e-83d8-d718c1c1b38f', '2021-07-22 18:24:25', '2021-07-22 18:24:25', '2.00', 'EUR', '636c1e45-eac1-4c42-b4c5-ffb6883dee12', 'bcd674d3-9a95-453e-8f11-9f341f9c7d47');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `email` varchar(40) DEFAULT NULL,
  `name` varchar(40) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `username` varchar(15) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `created_at`, `updated_at`, `email`, `name`, `password`, `username`) VALUES
(1, '2021-07-22 18:24:01', '2021-07-22 18:24:01', 'anil1@senocak.com', 'Reina', '$2a$10$RHOnbby.uiRYVjebjWULZ.P/UsAuJL1BH5/rU66nv5GlFYp/tnfLS', 'anil1'),
(2, '2021-07-22 18:24:01', '2021-07-22 18:24:01', 'anil2@senocak.com', 'Evan', '$2a$10$9MCYbVsMPv.dpaBqirFXaOVZAck3EiHqACH66iG.3uYa5hCVUB4lu', 'anil2');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `accounts`
--
ALTER TABLE `accounts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKnjuop33mo69pd79ctplkck40n` (`user_id`);

--
-- Indexes for table `transfers`
--
ALTER TABLE `transfers`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK7i7kboanveneetad7jyhbr0a7` (`from_account_id`),
  ADD KEY `FKra0an432c5wjo76mojluk0v28` (`to_account_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
  ADD UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `accounts`
--
ALTER TABLE `accounts`
  ADD CONSTRAINT `FKnjuop33mo69pd79ctplkck40n` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `transfers`
--
ALTER TABLE `transfers`
  ADD CONSTRAINT `FK7i7kboanveneetad7jyhbr0a7` FOREIGN KEY (`from_account_id`) REFERENCES `accounts` (`id`),
  ADD CONSTRAINT `FKra0an432c5wjo76mojluk0v28` FOREIGN KEY (`to_account_id`) REFERENCES `accounts` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
