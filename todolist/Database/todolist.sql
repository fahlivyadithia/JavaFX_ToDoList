-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 19, 2025 at 08:43 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `todolist`
--

-- --------------------------------------------------------

--
-- Table structure for table `tb_sub_tugas`
--

CREATE TABLE `tb_sub_tugas` (
  `id_sub_tugas` int(11) NOT NULL,
  `deskripsi_sub_tugas` varchar(255) NOT NULL,
  `status_sub_tugas` tinyint(1) NOT NULL DEFAULT 0,
  `id_tugas_utama` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tb_sub_tugas`
--

INSERT INTO `tb_sub_tugas` (`id_sub_tugas`, `deskripsi_sub_tugas`, `status_sub_tugas`, `id_tugas_utama`) VALUES
(1, 'Memperbaiki revisi FIgma', 1, 7),
(2, 'Memperbaiki struktur user flow ', 1, 7),
(3, 'Memperbaiki ai ', 0, 7),
(4, 'Revisi Tampilan Figma', 1, 9),
(5, 'Membuat IA', 0, 9),
(6, 'Memperbaiki Sitemap', 1, 9),
(7, 'Menanbahkan Screenshoot pada laporan', 0, 9),
(8, 'Membuat lampiran untuk menampung gambar', 0, 9);

-- --------------------------------------------------------

--
-- Table structure for table `tb_tugas`
--

CREATE TABLE `tb_tugas` (
  `id_tugas` int(11) NOT NULL,
  `deskripsi_tugas` text NOT NULL,
  `tgl_dibuat` timestamp NOT NULL DEFAULT current_timestamp(),
  `tgl_target` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tb_tugas`
--

INSERT INTO `tb_tugas` (`id_tugas`, `deskripsi_tugas`, `tgl_dibuat`, `tgl_target`) VALUES
(1, 'Kerja Kelompok UX', '2025-06-10 06:21:25', '2025-06-10'),
(2, 'Menyelesaikan Tugas Besar Webpro', '2025-06-10 06:22:13', '2025-06-12'),
(3, 'Presentasi Tubes PBO 1', '2025-06-13 01:07:52', '2025-06-13'),
(4, 'Presentasi Tubes UX', '2025-06-13 06:51:42', '2025-06-17'),
(6, 'Presentasi Tubes Probis', '2025-06-13 06:52:57', '2025-06-18'),
(7, 'Evaluasi Tubes Webpro', '2025-06-12 17:00:00', '2025-07-01'),
(9, 'Revisi Tubes UX', '2025-06-19 06:40:48', '2025-06-19');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `tb_sub_tugas`
--
ALTER TABLE `tb_sub_tugas`
  ADD PRIMARY KEY (`id_sub_tugas`),
  ADD KEY `id_tugas_utama` (`id_tugas_utama`);

--
-- Indexes for table `tb_tugas`
--
ALTER TABLE `tb_tugas`
  ADD PRIMARY KEY (`id_tugas`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `tb_sub_tugas`
--
ALTER TABLE `tb_sub_tugas`
  MODIFY `id_sub_tugas` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `tb_tugas`
--
ALTER TABLE `tb_tugas`
  MODIFY `id_tugas` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `tb_sub_tugas`
--
ALTER TABLE `tb_sub_tugas`
  ADD CONSTRAINT `tb_sub_tugas_ibfk_1` FOREIGN KEY (`id_tugas_utama`) REFERENCES `tb_tugas` (`id_tugas`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
