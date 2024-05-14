package com.perpustakaan.perpustakaan.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "peminjaman")
public class PeminjamanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mahasiswa_id", nullable = false)
    private MahasiswaEntity mahasiswa;

    @ManyToOne
    @JoinColumn(name = "buku_id", nullable = false)
    private BukuEntity buku;

    private LocalDate tanggalPeminjaman;
    private LocalDate tanggalBatasPengembalian;
    private LocalDate tanggalPengembalian;

    public enum Status {
        BELUM_DIKEMBALIKAN, SUDAH_DIKEMBALIKAN
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    // Constructor
    public PeminjamanEntity() {}

    public PeminjamanEntity(MahasiswaEntity mahasiswa, BukuEntity buku, LocalDate tanggalPeminjaman, LocalDate tanggalBatasPengembalian, LocalDate tanggalPengembalian, Status status) {
        this.mahasiswa = mahasiswa;
        this.buku = buku;
        this.tanggalPeminjaman = tanggalPeminjaman;
        this.tanggalBatasPengembalian = tanggalBatasPengembalian;
        this.tanggalPengembalian = tanggalPengembalian;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public MahasiswaEntity getMahasiswa() {
        return mahasiswa;
    }

    public void setMahasiswa(MahasiswaEntity mahasiswa) {
        this.mahasiswa = mahasiswa;
    }

    public BukuEntity getBuku() {
        return buku;
    }

    public void setBuku(BukuEntity buku) {
        this.buku = buku;
    }

    public LocalDate getTanggalPeminjaman() {
        return tanggalPeminjaman;
    }

    public void setTanggalPeminjaman(LocalDate tanggalPeminjaman) {
        this.tanggalPeminjaman = tanggalPeminjaman;
    }

    public LocalDate getTanggalBatasPengembalian() {
        return tanggalBatasPengembalian;
    }

    public void setTanggalBatasPengembalian(LocalDate tanggalBatasPengembalian) {
        this.tanggalBatasPengembalian = tanggalBatasPengembalian;
    }

    public LocalDate getTanggalPengembalian() {
        return tanggalPengembalian;
    }

    public void setTanggalPengembalian(LocalDate tanggalPengembalian) {
        this.tanggalPengembalian = tanggalPengembalian;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
