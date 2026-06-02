import java.util.*;

public class SistemJadwalAkademik {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║        SISTEM PENJADWALAN MATA KULIAH                ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        // 1. DATA MATA KULIAH
        System.out.println("\n=== DATA MATA KULIAH ===");
        List<MataKuliah> daftarMK = new ArrayList<>();
        daftarMK.add(new MataKuliah("MK01", "Algoritma", 3, true, 2, "Bu Dasriani"));
        daftarMK.add(new MataKuliah("MK02", "Basis Data", 2, true, 2, "Pak Wira"));
        daftarMK.add(new MataKuliah("MK03", "Jaringan Komputer", 3, true, 5, "Bu Sari"));
        daftarMK.add(new MataKuliah("MK04", "Pemrograman", 2, false, 2, "Bu Dasriani"));
        daftarMK.add(new MataKuliah("MK05", "Statistika", 2, false, 3, "Pak Budi"));

        for (MataKuliah mk : daftarMK) System.out.println("  " + mk);


        // 2. GRAF KONFLIK
        GrafKonflik graf = new GrafKonflik();
        graf.tambahKonflik("MK01", "MK02", "Dosen sama & ruangan bentrok");
        graf.tambahKonflik("MK01", "MK03", "Ruangan sama (R101)");
        graf.tambahKonflik("MK01", "MK04", "Dosen sama (Bu Dasriani)");
        graf.tambahKonflik("MK02", "MK03", "Dosen sama & waktu bentrok");
        graf.tambahKonflik("MK02", "MK04", "Waktu bentrok");
        graf.tambahKonflik("MK03", "MK05", "Ruangan sama (R202)");
        graf.tampilkanSemua();


        // 3. GRAPH COLORING (TENTUKAN JAM)
        GraphColoring pewarnaan = new GraphColoring(graf);
        Map<String, String> hasilWaktu = pewarnaan.tentukanWaktu(daftarMK);
        pewarnaan.tampilkanHasil(hasilWaktu, daftarMK);


        // 4. SIMPAN KE HASH TABLE
        System.out.println("\n=== PROSES PENYIMPANAN KE HASH TABLE ===");
        HashTableJadwal hashTable = new HashTableJadwal();
        String[] ruangan = {"R101", "R202", "R103", "R101", "R202"};
        Map<String, MataKuliah> petaMK = new HashMap<>();
        for (MataKuliah mk : daftarMK) petaMK.put(mk.kode, mk);

        int idx = 0;
        for (Map.Entry<String, String> entry : hasilWaktu.entrySet()) {
            String kode = entry.getKey();
            String waktu = entry.getValue();
            String[] bagian = waktu.split(" ");
            String hari = bagian[0];
            String jam = bagian[1];
            String ruang = ruangan[idx++];

            boolean sukses = hashTable.tambahData(hari, jam, ruang, petaMK.get(kode));
            System.out.println("  INSERT " + kode + " → " + (sukses ? "BERHASIL" : "GAGAL"));
        }

        // Coba masukkan data bentrok (uji coba error)
        System.out.println("\n  >> UJI COBA: Masukkan jadwal bentrok...");
        hashTable.tambahData("Senin", "08:00", "R101", petaMK.get("MK03"));


        // 5. TAMPILKAN SEMUA ISI HASH TABLE
        hashTable.tampilkanSemua();

        System.out.println("\n✅ PROGRAM SELESAI DIJALANKAN DENGAN LENGKAP!");
    }
}