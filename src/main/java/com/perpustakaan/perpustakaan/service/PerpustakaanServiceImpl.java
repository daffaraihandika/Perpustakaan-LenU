package com.perpustakaan.perpustakaan.service;

import com.perpustakaan.lib.*;
import com.perpustakaan.perpustakaan.model.BukuEntity;
import com.perpustakaan.perpustakaan.model.MahasiswaEntity;
import com.perpustakaan.perpustakaan.model.PeminjamanEntity;
import com.perpustakaan.perpustakaan.repository.BukuRepository;
import com.perpustakaan.perpustakaan.repository.MahasiswaRepository;
import com.perpustakaan.perpustakaan.repository.PeminjamanRepository;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class PerpustakaanServiceImpl extends PerpustakaanServiceGrpc.PerpustakaanServiceImplBase {

    @Autowired
    private MahasiswaRepository mahasiswaRepository;

    @Autowired
    private BukuRepository bukuRepository;

    @Autowired
    private PeminjamanRepository peminjamanRepository;

    
    private final Map<String, Mahasiswa> mahasiswaMap = new HashMap<>();
    private final Map<String, Buku> bukuMap = new HashMap<>();
    private final Map<String, Peminjaman> peminjamanMap = new HashMap<>();

    // Mahasiswa CRUD methods
    @Override
    public void getMahasiswa(GetMahasiswaRequest request, StreamObserver<GetMahasiswaResponse> responseObserver) {
        Mahasiswa mahasiswa = mahasiswaMap.get(request.getNim());
        GetMahasiswaResponse response = GetMahasiswaResponse.newBuilder().setMahasiswa(mahasiswa).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createMahasiswa(CreateMahasiswaRequest request, StreamObserver<CreateMahasiswaResponse> responseObserver) {
        MahasiswaEntity mahasiswaEntity = new MahasiswaEntity();
        mahasiswaEntity.setNim(request.getMahasiswa().getNim());
        mahasiswaEntity.setNama(request.getMahasiswa().getNama());
        mahasiswaEntity.setJurusan(request.getMahasiswa().getJurusan());

        mahasiswaRepository.save(mahasiswaEntity);

        CreateMahasiswaResponse response = CreateMahasiswaResponse.newBuilder()
            .setMahasiswa(request.getMahasiswa())
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateMahasiswa(UpdateMahasiswaRequest request, StreamObserver<UpdateMahasiswaResponse> responseObserver) {
        Mahasiswa mahasiswa = request.getMahasiswa();
        mahasiswaMap.put(mahasiswa.getNim(), mahasiswa);
        UpdateMahasiswaResponse response = UpdateMahasiswaResponse.newBuilder().setMahasiswa(mahasiswa).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteMahasiswa(DeleteMahasiswaRequest request, StreamObserver<DeleteMahasiswaResponse> responseObserver) {
        boolean success = mahasiswaMap.remove(request.getNim()) != null;
        DeleteMahasiswaResponse response = DeleteMahasiswaResponse.newBuilder().setSuccess(success).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Buku CRUD methods
    @Override
    public void getBuku(GetBukuRequest request, StreamObserver<GetBukuResponse> responseObserver) {
        Buku buku = bukuMap.get(request.getJudul());
        GetBukuResponse response = GetBukuResponse.newBuilder().setBuku(buku).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createBuku(CreateBukuRequest request, StreamObserver<CreateBukuResponse> responseObserver) {
        System.out.println("createBuku method called");
        Buku bukuRequest = request.getBuku();
        BukuEntity bukuEntity = new BukuEntity(
            bukuRequest.getJudul(),
            bukuRequest.getPenulis(),
            bukuRequest.getKuantitas(),
            bukuRequest.getTempatPenyimpanan()
        );

        BukuEntity savedBuku = bukuRepository.save(bukuEntity);

        Buku bukuResponse = Buku.newBuilder()
            .setJudul(savedBuku.getJudul())
            .setPenulis(savedBuku.getPenulis())
            .setKuantitas(savedBuku.getKuantitas())
            .setTempatPenyimpanan(savedBuku.getTempatPenyimpanan())
            .build();

        CreateBukuResponse response = CreateBukuResponse.newBuilder().setBuku(bukuResponse).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateBuku(UpdateBukuRequest request, StreamObserver<UpdateBukuResponse> responseObserver) {
        Buku buku = request.getBuku();
        bukuMap.put(buku.getJudul(), buku);
        UpdateBukuResponse response = UpdateBukuResponse.newBuilder().setBuku(buku).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBuku(DeleteBukuRequest request, StreamObserver<DeleteBukuResponse> responseObserver) {
        boolean success = bukuMap.remove(request.getJudul()) != null;
        DeleteBukuResponse response = DeleteBukuResponse.newBuilder().setSuccess(success).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Peminjaman CRUD methods
    // @Override
    // public void getPeminjaman(GetPeminjamanRequest request, StreamObserver<GetPeminjamanResponse> responseObserver) {
    //     Peminjaman peminjaman = peminjamanMap.get(request.getmaha());
    //     GetPeminjamanResponse response = GetPeminjamanResponse.newBuilder().setPeminjaman(peminjaman).build();
    //     responseObserver.onNext(response);
    //     responseObserver.onCompleted();
    // }

    @Override
    public void createPeminjaman(CreatePeminjamanRequest request, StreamObserver<CreatePeminjamanResponse> responseObserver) {
        System.out.println("createPeminjaman method called");
        
        Peminjaman peminjamanRequest = request.getPeminjaman();

        MahasiswaEntity mahasiswaEntity = mahasiswaRepository.findById(peminjamanRequest.getMahasiswaId())
                .orElseThrow(() -> new RuntimeException("Mahasiswa not found"));

        BukuEntity bukuEntity = bukuRepository.findById(peminjamanRequest.getBukuId())
                .orElseThrow(() -> new RuntimeException("Buku not found"));

        if (bukuEntity.getKuantitas() <= 0) {
            throw new RuntimeException("Buku not available for borrowing");
        }
    
        bukuEntity.setKuantitas(bukuEntity.getKuantitas() - 1);
        bukuRepository.save(bukuEntity);

        PeminjamanEntity peminjamanEntity = new PeminjamanEntity(
                mahasiswaEntity,
                bukuEntity,
                LocalDate.parse(peminjamanRequest.getTanggalPeminjaman()),
                LocalDate.parse(peminjamanRequest.getTanggalBatasPengembalian()),
                peminjamanRequest.getTanggalPengembalian().isEmpty() ? null : LocalDate.parse(peminjamanRequest.getTanggalPengembalian()),
                PeminjamanEntity.Status.BELUM_DIKEMBALIKAN
        );

        PeminjamanEntity savedPeminjaman = peminjamanRepository.save(peminjamanEntity);

        Peminjaman peminjamanResponse = Peminjaman.newBuilder()
                .setMahasiswaId(savedPeminjaman.getMahasiswa().getId())
                .setBukuId(savedPeminjaman.getBuku().getId())
                .setTanggalPeminjaman(savedPeminjaman.getTanggalPeminjaman().toString())
                .setTanggalBatasPengembalian(savedPeminjaman.getTanggalBatasPengembalian().toString())
                .setTanggalPengembalian(savedPeminjaman.getTanggalPengembalian() != null ? savedPeminjaman.getTanggalPengembalian().toString() : "")
                .setStatus(savedPeminjaman.getStatus().name())
                .build();

        CreatePeminjamanResponse response = CreatePeminjamanResponse.newBuilder().setPeminjaman(peminjamanResponse).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    // @Override
    // public void createPeminjaman(CreatePeminjamanRequest request, StreamObserver<CreatePeminjamanResponse> responseObserver) {
    //     Peminjaman peminjaman = request.getPeminjaman();
    //     peminjamanMap.put(peminjaman.getMahasiswaNim(), peminjaman);
    //     CreatePeminjamanResponse response = CreatePeminjamanResponse.newBuilder().setPeminjaman(peminjaman).build();
    //     responseObserver.onNext(response);
    //     responseObserver.onCompleted();
    // }

    // @Override
    // public void updatePeminjaman(UpdatePeminjamanRequest request, StreamObserver<UpdatePeminjamanResponse> responseObserver) {
    //     Peminjaman peminjaman = request.getPeminjaman();
    //     peminjamanMap.put(peminjaman.getMahasiswaNim(), peminjaman);
    //     UpdatePeminjamanResponse response = UpdatePeminjamanResponse.newBuilder().setPeminjaman(peminjaman).build();
    //     responseObserver.onNext(response);
    //     responseObserver.onCompleted();
    // }

    // @Override
    // public void deletePeminjaman(DeletePeminjamanRequest request, StreamObserver<DeletePeminjamanResponse> responseObserver) {
    //     boolean success = peminjamanMap.remove(request.getMahasiswaNim()) != null;
    //     DeletePeminjamanResponse response = DeletePeminjamanResponse.newBuilder().setSuccess(success).build();
    //     responseObserver.onNext(response);
    //     responseObserver.onCompleted();
    // }
}
