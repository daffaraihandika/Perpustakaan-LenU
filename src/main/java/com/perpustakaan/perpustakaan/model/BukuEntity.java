package com.perpustakaan.perpustakaan.model;

import jakarta.persistence.*;

@Entity
@Table(name = "buku")
public class BukuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String judul;
    private String penulis;
    private int kuantitas;
    private String tempatPenyimpanan;

    // Constructors
    public BukuEntity() {}

    public BukuEntity(String judul, String penulis, int kuantitas, String tempatPenyimpanan) {
        this.judul = judul;
        this.penulis = penulis;
        this.kuantitas = kuantitas;
        this.tempatPenyimpanan = tempatPenyimpanan;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getPenulis() {
        return penulis;
    }

    public void setPenulis(String penulis) {
        this.penulis = penulis;
    }

    public int getKuantitas() {
        return kuantitas;
    }

    public void setKuantitas(int kuantitas) {
        this.kuantitas = kuantitas;
    }

    public String getTempatPenyimpanan() {
        return tempatPenyimpanan;
    }

    public void setTempatPenyimpanan(String tempatPenyimpanan) {
        this.tempatPenyimpanan = tempatPenyimpanan;
    }
}
