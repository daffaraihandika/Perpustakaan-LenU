package com.perpustakaan.perpustakaan.repository;

import com.perpustakaan.perpustakaan.model.PeminjamanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeminjamanRepository extends JpaRepository<PeminjamanEntity, Long> {
}
