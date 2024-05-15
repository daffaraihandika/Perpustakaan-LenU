package com.perpustakaan.perpustakaan.repository;

import com.perpustakaan.perpustakaan.model.BukuEntity;
import com.perpustakaan.perpustakaan.model.MahasiswaEntity;
import com.perpustakaan.perpustakaan.model.PeminjamanEntity;
import com.perpustakaan.perpustakaan.model.PeminjamanEntity.Status;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeminjamanRepository extends JpaRepository<PeminjamanEntity, Long> {
    List<PeminjamanEntity> findByMahasiswaAndStatus(MahasiswaEntity mahasiswa, PeminjamanEntity.Status status);
    List<PeminjamanEntity> findByBukuAndStatus(BukuEntity buku, PeminjamanEntity.Status status);
    List<PeminjamanEntity> findByMahasiswa(MahasiswaEntity mahasiswa);
    List<PeminjamanEntity> findByBuku(BukuEntity buku);
    void deleteByMahasiswa(MahasiswaEntity mahasiswa);
    void deleteByBuku(BukuEntity buku);
    long countByMahasiswaIdAndTanggalPeminjamanBetweenAndStatus(Long mahasiswaId, LocalDate startDate, LocalDate endDate, PeminjamanEntity.Status status);
    List<PeminjamanEntity> findByTanggalBatasPengembalianBeforeAndStatus(LocalDate date, PeminjamanEntity.Status status);
}

