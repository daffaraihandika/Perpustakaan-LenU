package com.perpustakaan.perpustakaan.repository;

import com.perpustakaan.perpustakaan.model.BukuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BukuRepository extends JpaRepository<BukuEntity, Long> {
}
