package com.perpustakaan.perpustakaan.repository;

import com.perpustakaan.perpustakaan.model.MahasiswaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MahasiswaRepository extends JpaRepository<MahasiswaEntity, Long> {
}
