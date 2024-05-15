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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void getAllMahasiswa(GetAllMahasiswaRequest request, StreamObserver<GetAllMahasiswaResponse> responseObserver) {
        System.out.println("getAllMahasiswa method called");

        List<MahasiswaEntity> mahasiswaEntities = mahasiswaRepository.findAll();

        List<Mahasiswa> mahasiswaList = mahasiswaEntities.stream()
                .map(entity -> Mahasiswa.newBuilder()
                        .setNama(entity.getNama())
                        .setNim(entity.getNim())
                        .setJurusan(entity.getJurusan())
                        .build())
                .collect(Collectors.toList());

        GetAllMahasiswaResponse response = GetAllMahasiswaResponse.newBuilder()
                .addAllMahasiswa(mahasiswaList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void getMahasiswa(GetMahasiswaRequest request, StreamObserver<GetMahasiswaResponse> responseObserver) {
        System.out.println("getMahasiswa method called");

        // Find Mahasiswa by NIM
        MahasiswaEntity mahasiswaEntity = mahasiswaRepository.findByNim(request.getNim())
                .orElseThrow(() -> new RuntimeException("Mahasiswa not found"));

        // Build the response
        Mahasiswa mahasiswaResponse = Mahasiswa.newBuilder()
                .setNama(mahasiswaEntity.getNama())
                .setNim(mahasiswaEntity.getNim())
                .setJurusan(mahasiswaEntity.getJurusan())
                .build();

        GetMahasiswaResponse response = GetMahasiswaResponse.newBuilder().setMahasiswa(mahasiswaResponse).build();
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
        System.out.println("updateMahasiswa method called");

        Mahasiswa mahasiswaRequest = request.getMahasiswa();
        MahasiswaEntity mahasiswaEntity = mahasiswaRepository.findByNim(mahasiswaRequest.getNim())
                .orElseThrow(() -> new RuntimeException("Mahasiswa not found"));

        if (!mahasiswaEntity.getNim().equals(mahasiswaRequest.getNim())) {
            responseObserver.onError(new RuntimeException("Cannot update NIM"));
            return;
        }

        // Check if nama is provided, if not, use the existing nama
        if (!mahasiswaRequest.getNama().isEmpty()) {
            mahasiswaEntity.setNama(mahasiswaRequest.getNama());
        }

        // Check if jurusan is provided, if not, use the existing jurusan
        if (!mahasiswaRequest.getJurusan().isEmpty()) {
            mahasiswaEntity.setJurusan(mahasiswaRequest.getJurusan());
        }

        mahasiswaRepository.save(mahasiswaEntity);

        Mahasiswa mahasiswaResponse = Mahasiswa.newBuilder()
                .setNama(mahasiswaEntity.getNama())
                .setNim(mahasiswaEntity.getNim())
                .setJurusan(mahasiswaEntity.getJurusan())
                .build();

        UpdateMahasiswaResponse response = UpdateMahasiswaResponse.newBuilder().setMahasiswa(mahasiswaResponse).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteMahasiswa(DeleteMahasiswaRequest request, StreamObserver<DeleteMahasiswaResponse> responseObserver) {
        System.out.println("deleteMahasiswa method called");

        String nim = request.getNim();
        try {
            MahasiswaEntity mahasiswaEntity = mahasiswaRepository.findByNim(nim)
                    .orElseThrow(() -> new RuntimeException("Mahasiswa not found"));

            // Cek jika ada peminjaman dengan status BELUM_DIKEMBALIKAN
            List<PeminjamanEntity> peminjamanEntities = peminjamanRepository.findByMahasiswaAndStatus(mahasiswaEntity, PeminjamanEntity.Status.BELUM_DIKEMBALIKAN);
            if (!peminjamanEntities.isEmpty()) {
                responseObserver.onError(new RuntimeException("Mahasiswa cannot be deleted, there are unreturned books."));
                return;
            }

            // Hapus peminjaman yang terkait dengan mahasiswa ini jika ada
            List<PeminjamanEntity> peminjamans = peminjamanRepository.findByMahasiswa(mahasiswaEntity);
            if (!peminjamans.isEmpty()) {
                peminjamanRepository.deleteAll(peminjamans);
            }
            
            mahasiswaRepository.delete(mahasiswaEntity);

            DeleteMahasiswaResponse response = DeleteMahasiswaResponse.newBuilder().setSuccess(true).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Error deleting Mahasiswa: " + e.getMessage());
            responseObserver.onError(new RuntimeException("Error deleting Mahasiswa: " + e.getMessage()));
        }
    }

    // Buku CRUD methods
    @Override
    public void getAllBuku(GetAllBukuRequest request, StreamObserver<GetAllBukuResponse> responseObserver) {
        System.out.println("getAllBuku method called");

        List<BukuEntity> bukuEntities = bukuRepository.findAll();

        List<Buku> bukuList = bukuEntities.stream()
                .map(bukuEntity -> Buku.newBuilder()
                        .setId(bukuEntity.getId())
                        .setJudul(bukuEntity.getJudul())
                        .setPenulis(bukuEntity.getPenulis())
                        .setKuantitas(bukuEntity.getKuantitas())
                        .setTempatPenyimpanan(bukuEntity.getTempatPenyimpanan())
                        .build())
                .collect(Collectors.toList());

        GetAllBukuResponse response = GetAllBukuResponse.newBuilder()
                .addAllBukuList(bukuList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getBuku(GetBukuRequest request, StreamObserver<GetBukuResponse> responseObserver) {
        System.out.println("getBuku method called");

        // Find Buku by Id
        BukuEntity bukuEntity = bukuRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Buku not found"));

        // Build the response
        Buku bukuResponse = Buku.newBuilder()
                .setId(bukuEntity.getId())
                .setJudul(bukuEntity.getJudul())
                .setPenulis(bukuEntity.getPenulis())
                .setKuantitas(bukuEntity.getKuantitas())
                .setTempatPenyimpanan(bukuEntity.getTempatPenyimpanan())
                .build();

        GetBukuResponse response = GetBukuResponse.newBuilder().setBuku(bukuResponse).build();
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
        System.out.println("updateBuku method called");

        Buku bukuRequest = request.getBuku();
        BukuEntity bukuEntity = bukuRepository.findById(bukuRequest.getId())
                .orElseThrow(() -> new RuntimeException("Buku not found"));

        if (!bukuRequest.getJudul().isEmpty()) {
            bukuEntity.setJudul(bukuRequest.getJudul());
        }
    
        if (!bukuRequest.getPenulis().isEmpty()) {
            bukuEntity.setPenulis(bukuRequest.getPenulis());
        }
    
        if (bukuRequest.getKuantitas() != 0) {
            bukuEntity.setKuantitas(bukuRequest.getKuantitas());
        }
    
        if (!bukuRequest.getTempatPenyimpanan().isEmpty()) {
            bukuEntity.setTempatPenyimpanan(bukuRequest.getTempatPenyimpanan());
        }

        bukuRepository.save(bukuEntity);

        Buku bukuResponse = Buku.newBuilder()
                .setId(bukuEntity.getId())
                .setJudul(bukuEntity.getJudul())
                .setPenulis(bukuEntity.getPenulis())
                .setKuantitas(bukuEntity.getKuantitas())
                .setTempatPenyimpanan(bukuEntity.getTempatPenyimpanan())
                .build();

        UpdateBukuResponse response = UpdateBukuResponse.newBuilder().setBuku(bukuResponse).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBuku(DeleteBukuRequest request, StreamObserver<DeleteBukuResponse> responseObserver) {
        System.out.println("deleteBuku method called");

        Long id = request.getId();
        try {
            BukuEntity bukuEntity = bukuRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Buku not found"));

            // Cek jika ada peminjaman dengan status BELUM_DIKEMBALIKAN
            List<PeminjamanEntity> peminjamanEntities = peminjamanRepository.findByBukuAndStatus(bukuEntity, PeminjamanEntity.Status.BELUM_DIKEMBALIKAN);
            if (!peminjamanEntities.isEmpty()) {
                responseObserver.onError(new RuntimeException("Buku cannot be deleted, there are unreturned books."));
                return;
            }

            // Hapus peminjaman yang terkait dengan buku ini jika ada
            List<PeminjamanEntity> peminjamans = peminjamanRepository.findByBuku(bukuEntity);
            if (!peminjamans.isEmpty()) {
                peminjamanRepository.deleteAll(peminjamans);
            }
            
            bukuRepository.delete(bukuEntity);

            DeleteBukuResponse response = DeleteBukuResponse.newBuilder().setSuccess(true).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Error deleting Buku: " + e.getMessage());
            responseObserver.onError(new RuntimeException("Error deleting Buku: " + e.getMessage()));
        }
    }

    // Peminjaman CRUD methods
    @Override
    public void getAllPeminjaman(GetAllPeminjamanRequest request, StreamObserver<GetAllPeminjamanResponse> responseObserver) {
        System.out.println("getAllPeminjaman method called");

        List<PeminjamanEntity> peminjamanEntities = peminjamanRepository.findAll();

        List<Peminjaman> peminjamanList = peminjamanEntities.stream()
                .map(peminjamanEntity -> Peminjaman.newBuilder()
                        .setId(peminjamanEntity.getId())
                        .setMahasiswaId(peminjamanEntity.getMahasiswa().getId())
                        .setBukuId(peminjamanEntity.getBuku().getId())
                        .setTanggalPeminjaman(peminjamanEntity.getTanggalPeminjaman().toString())
                        .setTanggalBatasPengembalian(peminjamanEntity.getTanggalBatasPengembalian().toString())
                        .setTanggalPengembalian(peminjamanEntity.getTanggalPengembalian() != null ? peminjamanEntity.getTanggalPengembalian().toString() : "")
                        .setStatus(peminjamanEntity.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        // Build the response
        GetAllPeminjamanResponse response = GetAllPeminjamanResponse.newBuilder()
                .addAllPeminjamanList(peminjamanList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void getPeminjaman(GetPeminjamanRequest request, StreamObserver<GetPeminjamanResponse> responseObserver) {
        System.out.println("getPeminjaman method called");

        // Find Peminjaman by Id
        PeminjamanEntity peminjamanEntity = peminjamanRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Peminjaman not found"));

        // Build the response
        Peminjaman peminjamanResponse = Peminjaman.newBuilder()
                .setId(peminjamanEntity.getId())
                .setMahasiswaId(peminjamanEntity.getMahasiswa().getId())
                .setBukuId(peminjamanEntity.getBuku().getId())
                .setTanggalPeminjaman(peminjamanEntity.getTanggalPeminjaman().toString())
                .setTanggalBatasPengembalian(peminjamanEntity.getTanggalBatasPengembalian().toString())
                .setTanggalPengembalian(peminjamanEntity.getTanggalPengembalian() != null ? peminjamanEntity.getTanggalPengembalian().toString() : "")
                .setStatus(peminjamanEntity.getStatus().name())
                .build();

        GetPeminjamanResponse response = GetPeminjamanResponse.newBuilder().setPeminjaman(peminjamanResponse).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createPeminjaman(CreatePeminjamanRequest request, StreamObserver<CreatePeminjamanResponse> responseObserver) {
        System.out.println("createPeminjaman method called");
        
        Peminjaman peminjamanRequest = request.getPeminjaman();

        MahasiswaEntity mahasiswaEntity = mahasiswaRepository.findById(peminjamanRequest.getMahasiswaId())
                .orElseThrow(() -> new RuntimeException("Mahasiswa not found"));

        BukuEntity bukuEntity = bukuRepository.findById(peminjamanRequest.getBukuId())
                .orElseThrow(() -> new RuntimeException("Buku not found"));

        if (bukuEntity.getKuantitas() <= 0) {
            System.out.println("Buku not available for borrowing");
            throw new RuntimeException("Buku not available for borrowing");
        }

        LocalDate tanggalPeminjaman = LocalDate.parse(peminjamanRequest.getTanggalPeminjaman());

        // Cek jumlah peminjaman dalam satu bulan dengan status BELUM_DIKEMBALIKAN
        long jumlahPeminjamanDalamBulan = peminjamanRepository.countByMahasiswaIdAndTanggalPeminjamanBetweenAndStatus(
                mahasiswaEntity.getId(),
                tanggalPeminjaman.withDayOfMonth(1),
                tanggalPeminjaman.withDayOfMonth(tanggalPeminjaman.lengthOfMonth()),
                PeminjamanEntity.Status.BELUM_DIKEMBALIKAN
        );

        if (jumlahPeminjamanDalamBulan == 10) {
            System.out.println("Mahasiswa sudah meminjam 10 buku dalam bulan ini");
            responseObserver.onError(new RuntimeException("Mahasiswa sudah meminjam 10 buku dalam bulan ini"));
            return;
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
                .setId(savedPeminjaman.getId())
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

    @Override
    public void updatePeminjaman(UpdatePeminjamanRequest request, StreamObserver<UpdatePeminjamanResponse> responseObserver) {
        System.out.println("updatePeminjaman method called");

        Peminjaman peminjamanRequest = request.getPeminjaman();
        PeminjamanEntity peminjamanEntity = peminjamanRepository.findById(peminjamanRequest.getId())
                .orElseThrow(() -> new RuntimeException("Peminjaman not found"));

        // Check and update Mahasiswa if provided
        if (peminjamanRequest.getMahasiswaId() != 0) {
            MahasiswaEntity mahasiswaEntity = mahasiswaRepository.findById(peminjamanRequest.getMahasiswaId())
                    .orElseThrow(() -> new RuntimeException("Mahasiswa not found"));
            peminjamanEntity.setMahasiswa(mahasiswaEntity);
        }

        // Check and update Buku if provided
        if (peminjamanRequest.getBukuId() != 0) {
            BukuEntity bukuEntity = bukuRepository.findById(peminjamanRequest.getBukuId())
                    .orElseThrow(() -> new RuntimeException("Buku not found"));

            // Cek apakah buku_id berubah
            boolean isBukuIdChanged = !peminjamanEntity.getBuku().getId().equals(peminjamanRequest.getBukuId());

            if (isBukuIdChanged) {
                // Cek kuantitas buku yang akan dipinjam jika buku_id berubah
                if (bukuEntity.getKuantitas() < 1) {
                    responseObserver.onError(new RuntimeException("Buku not available for borrowing"));
                    return;
                }

                // Perbarui kuantitas buku jika buku_id berubah
                BukuEntity bukuLama = peminjamanEntity.getBuku();
                bukuLama.setKuantitas(bukuLama.getKuantitas() + 1);
                bukuRepository.save(bukuLama);

                bukuEntity.setKuantitas(bukuEntity.getKuantitas() - 1);
                bukuRepository.save(bukuEntity);

                peminjamanEntity.setBuku(bukuEntity);
            }
        }

        // Check and update tanggalPeminjaman if provided
        if (!peminjamanRequest.getTanggalPeminjaman().isEmpty()) {
            peminjamanEntity.setTanggalPeminjaman(LocalDate.parse(peminjamanRequest.getTanggalPeminjaman()));
        }

        // Check and update tanggalBatasPengembalian if provided
        if (!peminjamanRequest.getTanggalBatasPengembalian().isEmpty()) {
            peminjamanEntity.setTanggalBatasPengembalian(LocalDate.parse(peminjamanRequest.getTanggalBatasPengembalian()));
        }

        peminjamanRepository.save(peminjamanEntity);

        Peminjaman peminjamanResponse = Peminjaman.newBuilder()
                .setId(peminjamanEntity.getId())
                .setMahasiswaId(peminjamanEntity.getMahasiswa().getId())
                .setBukuId(peminjamanEntity.getBuku().getId())
                .setTanggalPeminjaman(peminjamanEntity.getTanggalPeminjaman().toString())
                .setTanggalBatasPengembalian(peminjamanEntity.getTanggalBatasPengembalian().toString())
                .setStatus(peminjamanEntity.getStatus().name())
                .build();

        UpdatePeminjamanResponse response = UpdatePeminjamanResponse.newBuilder().setPeminjaman(peminjamanResponse).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void deletePeminjaman(DeletePeminjamanRequest request, StreamObserver<DeletePeminjamanResponse> responseObserver) {
        System.out.println("deletePeminjaman method called");

        Long id = request.getId();
        PeminjamanEntity peminjamanEntity = peminjamanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Peminjaman not found"));

        // Update book quantity if the book was borrowed
        if (peminjamanEntity.getStatus() == PeminjamanEntity.Status.BELUM_DIKEMBALIKAN) {
            BukuEntity bukuEntity = peminjamanEntity.getBuku();
            bukuEntity.setKuantitas(bukuEntity.getKuantitas() + 1);
            bukuRepository.save(bukuEntity);
        }

        peminjamanRepository.delete(peminjamanEntity);

        DeletePeminjamanResponse response = DeletePeminjamanResponse.newBuilder().setSuccess(true).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void pengembalianBuku(PengembalianBukuRequest request, StreamObserver<PengembalianBukuResponse> responseObserver) {
        System.out.println("pengembalianBuku method called");

        Long peminjamanId = request.getPeminjamanId();
        LocalDate tanggalPengembalian = LocalDate.parse(request.getTanggalPengembalian());

        try {
            PeminjamanEntity peminjamanEntity = peminjamanRepository.findById(peminjamanId)
                    .orElseThrow(() -> new RuntimeException("Peminjaman not found"));

            if (peminjamanEntity.getStatus() == PeminjamanEntity.Status.SUDAH_DIKEMBALIKAN) {
                responseObserver.onError(new RuntimeException("Peminjaman sudah dikembalikan"));
                return;
            }

            // Update status peminjaman
            peminjamanEntity.setTanggalPengembalian(tanggalPengembalian);
            peminjamanEntity.setStatus(PeminjamanEntity.Status.SUDAH_DIKEMBALIKAN);
            peminjamanRepository.save(peminjamanEntity);

            // Kembalikan kuantitas buku
            BukuEntity bukuEntity = peminjamanEntity.getBuku();
            bukuEntity.setKuantitas(bukuEntity.getKuantitas() + 1);
            bukuRepository.save(bukuEntity);

            // Hitung denda
            int denda = peminjamanEntity.hitungDenda(tanggalPengembalian);

            // Build response
            Peminjaman peminjamanResponse = Peminjaman.newBuilder()
                    .setId(peminjamanEntity.getId())
                    .setMahasiswaId(peminjamanEntity.getMahasiswa().getId())
                    .setBukuId(peminjamanEntity.getBuku().getId())
                    .setTanggalPeminjaman(peminjamanEntity.getTanggalPeminjaman().toString())
                    .setTanggalBatasPengembalian(peminjamanEntity.getTanggalBatasPengembalian().toString())
                    .setTanggalPengembalian(peminjamanEntity.getTanggalPengembalian().toString())
                    .setStatus(peminjamanEntity.getStatus().name())
                    .build();

            PengembalianBukuResponse response = PengembalianBukuResponse.newBuilder()
                    .setPeminjaman(peminjamanResponse)
                    .setDenda(denda)
                    .build();

            System.out.println("PengembalianBukuResponse built successfully");

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Error returning book: " + e.getMessage());
            responseObserver.onError(new RuntimeException("Error returning book: " + e.getMessage()));
        }
    }
}
