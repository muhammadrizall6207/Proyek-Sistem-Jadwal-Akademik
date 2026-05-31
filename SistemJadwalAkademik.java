import java.util.*;

// ============================================================
//  SISTEM JADWAL AKADEMIK
//  Universitas Bumigora - Algoritma dan Struktur Data
//  Muhammad Rizal · I Komang Yoga Winarna · Ardiansyah
// ============================================================

// ─────────────────────────────────────────────
// MODEL: Data Mata Kuliah
// ─────────────────────────────────────────────
class MataKuliah {
    String kode;
    String nama;
    int sks;
    boolean wajib;
    int semester;
    String dosen;
    int prioritas;

    public MataKuliah(String kode, String nama, int sks, boolean wajib, int semester, String dosen) {
        this.kode     = kode;
        this.nama     = nama;
        this.sks      = sks;
        this.wajib    = wajib;
        this.semester = semester;
        this.dosen    = dosen;
        this.prioritas = hitungPrioritas();
    }

    // Formula prioritas sesuai desain:
    // (sks x 10) + (wajib ? 20 : 0) + (7 - semester)
    private int hitungPrioritas() {
        return (sks * 10) + (wajib ? 20 : 0) + (7 - semester);
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-20s | %d SKS | %-7s | Sem %d | %s | P=%d",
            kode, nama, sks, (wajib ? "Wajib" : "Pilihan"), semester, dosen, prioritas);
    }
}

// ─────────────────────────────────────────────
// STRUKTUR DATA 1: GRAF (Adjacency List)
// Merepresentasikan konflik antar mata kuliah
// ─────────────────────────────────────────────
class GrafKonflik {
    // Adjacency list: key = kode MK, value = daftar MK yang konflik
    private Map<String, List<String>> adjacencyList;
    // Menyimpan alasan konflik untuk tiap edge
    private Map<String, String> alasanKonflik;

    public GrafKonflik() {
        adjacencyList = new LinkedHashMap<>();
        alasanKonflik = new HashMap<>();
    }

    // Tambah simpul (node) baru
    public void tambahSimpul(String kodeMK) {
        adjacencyList.putIfAbsent(kodeMK, new ArrayList<>());
    }

    // Tambah sisi (edge) konflik antara dua MK
    public void tambahKonflik(String mk1, String mk2, String alasan) {
        tambahSimpul(mk1);
        tambahSimpul(mk2);
        // Graf tidak berarah: tambahkan ke dua arah
        if (!adjacencyList.get(mk1).contains(mk2)) {
            adjacencyList.get(mk1).add(mk2);
        }
        if (!adjacencyList.get(mk2).contains(mk1)) {
            adjacencyList.get(mk2).add(mk1);
        }
        // Simpan alasan, key diurutkan agar konsisten
        String edgeKey = mk1.compareTo(mk2) < 0 ? mk1 + "-" + mk2 : mk2 + "-" + mk1;
        alasanKonflik.put(edgeKey, alasan);
    }

    // Cek apakah dua MK berkonflik
    public boolean adaKonflik(String mk1, String mk2) {
        List<String> tetangga = adjacencyList.getOrDefault(mk1, new ArrayList<>());
        return tetangga.contains(mk2);
    }

    // Ambil semua tetangga (MK yang konflik) dari satu MK
    public List<String> getTetangga(String kodeMK) {
        return adjacencyList.getOrDefault(kodeMK, new ArrayList<>());
    }

    // Ambil semua simpul
    public Set<String> getSimpul() {
        return adjacencyList.keySet();
    }

    // Tampilkan adjacency list
    public void tampilkanAdjacencyList() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║         ADJACENCY LIST - KONFLIK JADWAL              ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            String mk = entry.getKey();
            List<String> konflik = entry.getValue();
            System.out.printf("  %-6s  →  %s%n", mk, konflik.isEmpty() ? "(tidak ada konflik)" : konflik);
        }
        System.out.println("\n  Detail Alasan Konflik:");
        for (Map.Entry<String, String> entry : alasanKonflik.entrySet()) {
            String[] parts = entry.getKey().split("-");
            System.out.printf("  %-6s ↔ %-6s : %s%n", parts[0], parts[1], entry.getValue());
        }
    }

    // Hitung derajat (degree) tiap simpul — digunakan untuk Graph Coloring
    public int getDegree(String kodeMK) {
        return adjacencyList.getOrDefault(kodeMK, new ArrayList<>()).size();
    }
}

// ─────────────────────────────────────────────
// ALGORITMA: GRAPH COLORING (Greedy)
// Menentukan slot waktu agar tidak ada konflik
// Kompleksitas: O(V²)
// ─────────────────────────────────────────────
class GraphColoring {
    private GrafKonflik graf;
    private String[] slotWaktu = {
        "Senin 08:00",
        "Selasa 10:00",
        "Rabu 13:00",
        "Kamis 08:00",
        "Jumat 10:00"
    };

    public GraphColoring(GrafKonflik graf) {
        this.graf = graf;
    }

    // Greedy Graph Coloring
    // Langkah: urutkan MK berdasarkan degree (konflik terbanyak dulu),
    //          lalu assign slot yang tidak dipakai tetangganya
    public Map<String, String> warnai(List<MataKuliah> daftarMK) {
        // Urutkan dari degree tertinggi (yang paling banyak konflik didahulukan)
        List<MataKuliah> terurut = new ArrayList<>(daftarMK);
        terurut.sort((a, b) -> graf.getDegree(b.kode) - graf.getDegree(a.kode));

        Map<String, String> hasilSlot = new LinkedHashMap<>();

        for (MataKuliah mk : terurut) {
            // Kumpulkan slot yang sudah dipakai tetangga
            Set<String> slotTerpakai = new HashSet<>();
            for (String tetangga : graf.getTetangga(mk.kode)) {
                if (hasilSlot.containsKey(tetangga)) {
                    slotTerpakai.add(hasilSlot.get(tetangga));
                }
            }

            // Assign slot pertama yang tersedia
            String slotDipilih = null;
            for (String slot : slotWaktu) {
                if (!slotTerpakai.contains(slot)) {
                    slotDipilih = slot;
                    break;
                }
            }

            if (slotDipilih != null) {
                hasilSlot.put(mk.kode, slotDipilih);
            } else {
                hasilSlot.put(mk.kode, "SLOT TIDAK TERSEDIA");
            }
        }

        return hasilSlot;
    }

    public void tampilkanHasil(Map<String, String> hasilSlot, List<MataKuliah> daftarMK) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║        HASIL GRAPH COLORING - ALOKASI SLOT           ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.printf("  %-6s | %-20s | %-16s | %s%n",
            "Kode", "Mata Kuliah", "Slot Waktu", "Konflik dgn");
        System.out.println("-------------------------------------------------------------------------------");

        Map<String, MataKuliah> mapMK = new HashMap<>();
        for (MataKuliah mk : daftarMK) mapMK.put(mk.kode, mk);

        for (Map.Entry<String, String> entry : hasilSlot.entrySet()) {
            String kode = entry.getKey();
            String slot = entry.getValue();
            MataKuliah mk = mapMK.get(kode);
            List<String> konflik = graf.getTetangga(kode);
            System.out.printf("  %-6s | %-20s | %-16s | %s%n",
                kode, mk != null ? mk.nama : "-", slot,
                konflik.isEmpty() ? "-" : konflik.toString());
        }

        // Verifikasi: pastikan tidak ada tetangga yang dapat slot sama
        System.out.println("\n  >> Verifikasi tidak ada konflik slot:");
        boolean valid = true;
        for (String mk1 : hasilSlot.keySet()) {
            for (String mk2 : graf.getTetangga(mk1)) {
                if (hasilSlot.containsKey(mk2) &&
                    hasilSlot.get(mk1).equals(hasilSlot.get(mk2))) {
                    System.out.printf("  [KONFLIK!] %s dan %s sama-sama di slot %s%n",
                        mk1, mk2, hasilSlot.get(mk1));
                    valid = false;
                }
            }
        }
        if (valid) {
            System.out.println("  [OK] Semua mata kuliah berhasil dijadwalkan tanpa konflik!");
        }
    }
}

// ─────────────────────────────────────────────
// STRUKTUR DATA 2: HASH TABLE
// Menyimpan jadwal final dengan key unik
// Format key: HARI_JAM_RUANGAN
// Kompleksitas: O(1) rata-rata
// ─────────────────────────────────────────────
class HashTableJadwal {

    // Representasi satu entri jadwal
    static class EntriJadwal {
        String key;
        MataKuliah mk;
        String ruangan;

        public EntriJadwal(String key, MataKuliah mk, String ruangan) {
            this.key     = key;
            this.mk      = mk;
            this.ruangan = ruangan;
        }

        @Override
        public String toString() {
            return String.format("%-28s → %-20s | %-12s | %s",
                key, mk.nama, ruangan, mk.dosen);
        }
    }

    // Implementasi hash table dengan separate chaining (untuk handle collision)
    private static final int UKURAN_BUCKET = 11; // bilangan prima untuk distribusi merata
    private LinkedList<EntriJadwal>[] buckets;
    private int jumlahData = 0;

    @SuppressWarnings("unchecked")
    public HashTableJadwal() {
        buckets = new LinkedList[UKURAN_BUCKET];
        for (int i = 0; i < UKURAN_BUCKET; i++) {
            buckets[i] = new LinkedList<>();
        }
    }

    // Fungsi hash: mengubah key string menjadi indeks bucket
    private int hash(String key) {
        int hash = 0;
        for (char c : key.toCharArray()) {
            hash = (hash * 31 + c) % UKURAN_BUCKET;
        }
        return Math.abs(hash);
    }

    // Buat key dari komponen jadwal
    public static String buatKey(String hari, String jam, String ruangan) {
        return hari.toUpperCase() + "_" + jam + "_" + ruangan.toUpperCase();
    }

    // INSERT jadwal — O(1) rata-rata
    public boolean insert(String hari, String jam, String ruangan, MataKuliah mk) {
        String key = buatKey(hari, jam, ruangan);
        int idx = hash(key);

        // Cek apakah slot sudah terisi (konflik jadwal)
        for (EntriJadwal entri : buckets[idx]) {
            if (entri.key.equals(key)) {
                System.out.printf("  [TOLAK] Slot '%s' sudah dipakai oleh %s!%n", key, entri.mk.nama);
                return false;
            }
        }

        // Kalau ada collision (bucket sama tapi key beda), gunakan chaining
        buckets[idx].add(new EntriJadwal(key, mk, ruangan));
        jumlahData++;
        return true;
    }

    // SEARCH jadwal berdasarkan key — O(1) rata-rata
    public EntriJadwal search(String hari, String jam, String ruangan) {
        String key = buatKey(hari, jam, ruangan);
        int idx = hash(key);

        for (EntriJadwal entri : buckets[idx]) {
            if (entri.key.equals(key)) {
                return entri;
            }
        }
        return null; // tidak ditemukan
    }

    // DELETE jadwal — O(1) rata-rata
    public boolean delete(String hari, String jam, String ruangan) {
        String key = buatKey(hari, jam, ruangan);
        int idx = hash(key);

        Iterator<EntriJadwal> it = buckets[idx].iterator();
        while (it.hasNext()) {
            if (it.next().key.equals(key)) {
                it.remove();
                jumlahData--;
                return true;
            }
        }
        return false;
    }

    // Tampilkan isi hash table + visualisasi bucket
    public void tampilkanHashTable() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║            HASH TABLE - JADWAL TERSIMPAN             ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.printf("  Ukuran bucket: %d | Data tersimpan: %d | Load factor: %.2f%n%n",
            UKURAN_BUCKET, jumlahData, (double) jumlahData / UKURAN_BUCKET);

        System.out.println("  Visualisasi Bucket Array:");
        System.out.println("-------------------------------------------------------------------------");
        for (int i = 0; i < UKURAN_BUCKET; i++) {
            if (buckets[i].isEmpty()) {
                System.out.printf("  Bucket[%2d] → (kosong)%n", i);
            } else {
                for (EntriJadwal entri : buckets[i]) {
                    System.out.printf("  Bucket[%2d] → %s%n", i, entri);
                }
                // Tanda collision jika ada lebih dari 1 entri di bucket
                if (buckets[i].size() > 1) {
                    System.out.printf("             ^ COLLISION: %d entri di bucket ini (ditangani dengan chaining)%n",
                        buckets[i].size());
                }
            }
        }
    }

    // Demo test: cek slot terisi, cek slot kosong
    public void demoSearch() {
        System.out.println("\n  >> Demo Operasi Search:");
        String[][] testCases = {
            {"SENIN", "08:00", "R101"},
            {"SELASA", "10:00", "R202"},
            {"MINGGU", "09:00", "R999"}, // tidak ada
        };
        for (String[] tc : testCases) {
            EntriJadwal hasil = search(tc[0], tc[1], tc[2]);
            String key = buatKey(tc[0], tc[1], tc[2]);
            if (hasil != null) {
                System.out.printf("  SEARCH %-28s → DITEMUKAN: %s (%s)%n",
                    key, hasil.mk.nama, hasil.mk.dosen);
            } else {
                System.out.printf("  SEARCH %-28s → TIDAK ADA (slot kosong)%n", key);
            }
        }
    }
}

// ─────────────────────────────────────────────
// STRUKTUR DATA 3: PRIORITY QUEUE (Max-Heap)
// Menentukan urutan pemrosesan mata kuliah
// Kompleksitas: enqueue/dequeue O(log n), peek O(1)
// ─────────────────────────────────────────────
class PriorityQueueHeap {
    private List<MataKuliah> heap;

    public PriorityQueueHeap() {
        heap = new ArrayList<>();
    }

    // Indeks parent, child kiri, child kanan
    private int parent(int i) { return (i - 1) / 2; }
    private int kiri(int i)   { return 2 * i + 1; }
    private int kanan(int i)  { return 2 * i + 2; }

    // Tukar dua elemen di heap
    private void swap(int i, int j) {
        MataKuliah tmp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, tmp);
    }

    // Heapify ke atas (setelah insert)
    private void heapifyUp(int i) {
        while (i > 0 && heap.get(i).prioritas > heap.get(parent(i)).prioritas) {
            swap(i, parent(i));
            i = parent(i);
        }
    }

    // Heapify ke bawah (setelah remove root)
    private void heapifyDown(int i) {
        int terbesar = i;
        int l = kiri(i);
        int r = kanan(i);

        if (l < heap.size() && heap.get(l).prioritas > heap.get(terbesar).prioritas)
            terbesar = l;
        if (r < heap.size() && heap.get(r).prioritas > heap.get(terbesar).prioritas)
            terbesar = r;

        if (terbesar != i) {
            swap(i, terbesar);
            heapifyDown(terbesar);
        }
    }

    // ENQUEUE — O(log n)
    public void enqueue(MataKuliah mk) {
        heap.add(mk);
        heapifyUp(heap.size() - 1);
    }

    // DEQUEUE (ambil prioritas tertinggi) — O(log n)
    public MataKuliah dequeue() {
        if (heap.isEmpty()) return null;
        MataKuliah root = heap.get(0);
        MataKuliah last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }
        return root;
    }

    // PEEK (lihat root tanpa hapus) — O(1)
    public MataKuliah peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    public boolean isEmpty() { return heap.isEmpty(); }
    public int size()        { return heap.size(); }

    // Tampilkan struktur heap saat ini
    public void tampilkanHeap() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║         PRIORITY QUEUE (MAX-HEAP) - ANTRIAN          ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        if (heap.isEmpty()) {
            System.out.println("  Heap kosong.");
            return;
        }

        System.out.printf("  %-4s | %-6s | %-20s | %3s | %-7s | Sem | %s%n",
            "Idx", "Kode", "Nama", "SKS", "Status", "Prioritas");
        System.out.println("-------------------------------------------------------------------");

        for (int i = 0; i < heap.size(); i++) {
            MataKuliah mk = heap.get(i);
            String role = (i == 0) ? " ← ROOT (tertinggi)" : "";
            System.out.printf("  [%2d] | %-6s | %-20s | %3d | %-7s |  %d  | P=%-3d%s%n",
                i, mk.kode, mk.nama, mk.sks,
                (mk.wajib ? "Wajib" : "Pilihan"), mk.semester, mk.prioritas, role);
        }

        System.out.println("\n  Visualisasi Pohon Heap:");
        tampilkanPohon(0, "", true);

        System.out.printf("%n  >> PEEK: Prioritas tertinggi saat ini = %s (P=%d)%n",
            peek().nama, peek().prioritas);
    }

    // Rekursif print pohon ke console
    private void tampilkanPohon(int i, String prefix, boolean isRoot) {
        if (i >= heap.size()) return;
        MataKuliah mk = heap.get(i);
        System.out.printf("  %s%s[%s P=%d]%n",
            prefix, isRoot ? "" : "├── ", mk.kode, mk.prioritas);
        String childPrefix = prefix + (isRoot ? "  " : "│   ");
        if (kiri(i) < heap.size())  tampilkanPohon(kiri(i),  childPrefix, false);
        if (kanan(i) < heap.size()) tampilkanPohon(kanan(i), childPrefix, false);
    }

    // Demo: dequeue semua untuk lihat urutan pemrosesan
    public void demoDequeue() {
        System.out.println("\n  >> Demo Urutan Pemrosesan (dequeue satu per satu):");
        System.out.println("----------------------------------------------------------------------");
        // Buat salinan agar data asli tidak hilang
        PriorityQueueHeap salinan = new PriorityQueueHeap();
        for (MataKuliah mk : heap) salinan.enqueue(mk);

        int urutan = 1;
        while (!salinan.isEmpty()) {
            MataKuliah mk = salinan.dequeue();
            System.out.printf("  Urutan %d → %-6s | %-20s | P=%d%n",
                urutan++, mk.kode, mk.nama, mk.prioritas);
        }
    }
}

// ─────────────────────────────────────────────
// MAIN PROGRAM
// ─────────────────────────────────────────────
public class SistemJadwalAkademik {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║        SISTEM JADWAL AKADEMIK                        ║");
        System.out.println("║        Universitas Bumigora - ASD 2025/2026          ║");
        System.out.println("║        Muhammad Rizal · Yoga Winarna · Ardiansyah    ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        // ── 1. INISIALISASI DATA MATA KULIAH ──────────────────
        System.out.println("\n=== DATA MATA KULIAH ===");
        List<MataKuliah> daftarMK = new ArrayList<>();
        daftarMK.add(new MataKuliah("MK01", "Algoritma",   3, true,  2, "Bu Dasriani"));
        daftarMK.add(new MataKuliah("MK02", "Basis Data",  2, true,  2, "Pak Wira"));
        daftarMK.add(new MataKuliah("MK03", "Jaringan",    3, true,  5, "Bu Sari"));
        daftarMK.add(new MataKuliah("MK04", "Pemrograman", 2, false, 2, "Bu Dasriani"));
        daftarMK.add(new MataKuliah("MK05", "Statistika",  2, false, 3, "Pak Budi"));

        for (MataKuliah mk : daftarMK) {
            System.out.println("  " + mk);
        }

        // ── 2. GRAF KONFLIK ───────────────────────────────────
        System.out.println("\n=== STRUKTUR DATA 1: GRAF KONFLIK ===");
        GrafKonflik graf = new GrafKonflik();

        // Tambah semua simpul
        for (MataKuliah mk : daftarMK) graf.tambahSimpul(mk.kode);

        // Tambah konflik berdasarkan desain
        graf.tambahKonflik("MK01", "MK02", "Dosen sama & ruangan bentrok");
        graf.tambahKonflik("MK01", "MK03", "Ruangan sama (R101)");
        graf.tambahKonflik("MK01", "MK04", "Dosen sama (Bu Dasriani)");
        graf.tambahKonflik("MK02", "MK03", "Dosen sama & waktu bentrok");
        graf.tambahKonflik("MK02", "MK04", "Waktu bentrok");
        graf.tambahKonflik("MK03", "MK05", "Ruangan sama (R202)");

        graf.tampilkanAdjacencyList();

        // ── 3. GRAPH COLORING ─────────────────────────────────
        System.out.println("\n=== ALGORITMA GRAPH COLORING (GREEDY) ===");
        GraphColoring coloring = new GraphColoring(graf);
        Map<String, String> hasilSlot = coloring.warnai(daftarMK);
        coloring.tampilkanHasil(hasilSlot, daftarMK);

        // ── 4. HASH TABLE ─────────────────────────────────────
        System.out.println("\n=== STRUKTUR DATA 2: HASH TABLE ===");
        HashTableJadwal hashTable = new HashTableJadwal();

        // Ruangan yang tersedia
        Map<String, String> ruanganMK = new HashMap<>();
        ruanganMK.put("MK01", "R101");
        ruanganMK.put("MK02", "R202");
        ruanganMK.put("MK03", "R103");
        ruanganMK.put("MK04", "R101");
        ruanganMK.put("MK05", "R202");

        // Masukkan hasil graph coloring ke hash table
        Map<String, MataKuliah> mapMK = new HashMap<>();
        for (MataKuliah mk : daftarMK) mapMK.put(mk.kode, mk);

        System.out.println("\n  >> Memasukkan jadwal ke Hash Table:");
        for (Map.Entry<String, String> entry : hasilSlot.entrySet()) {
            String kode   = entry.getKey();
            String slot   = entry.getValue(); // contoh: "Senin 08:00"
            String[] parts = slot.split(" ");
            String hari  = parts[0];
            String jam   = parts.length > 1 ? parts[1] : "00:00";
            String ruangan = ruanganMK.getOrDefault(kode, "R999");
            MataKuliah mk = mapMK.get(kode);

            boolean berhasil = hashTable.insert(hari, jam, ruangan, mk);
            System.out.printf("  INSERT %-6s → %-28s : %s%n",
                kode, HashTableJadwal.buatKey(hari, jam, ruangan),
                berhasil ? "BERHASIL" : "GAGAL (slot penuh)");
        }

        // Demo collision: coba masukkan jadwal di slot yang sama
        System.out.println("\n  >> Demo Deteksi Konflik (coba insert slot yang sama):");
        hashTable.insert("Senin", "08:00", "R101", mapMK.get("MK03")); // harus ditolak

        hashTable.tampilkanHashTable();
        hashTable.demoSearch();

        // ── 5. PRIORITY QUEUE ─────────────────────────────────
        System.out.println("\n=== STRUKTUR DATA 3: PRIORITY QUEUE (MAX-HEAP) ===");
        PriorityQueueHeap pq = new PriorityQueueHeap();

        System.out.println("\n  >> Memasukkan mata kuliah ke Priority Queue:");
        for (MataKuliah mk : daftarMK) {
            pq.enqueue(mk);
            System.out.printf("  ENQUEUE %-6s (P=%d) → heap size=%d%n",
                mk.kode, mk.prioritas, pq.size());
        }

        pq.tampilkanHeap();
        pq.demoDequeue();

        // ── RINGKASAN JADWAL FINAL ────────────────────────────
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              JADWAL FINAL (BEBAS KONFLIK)            ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.printf("  %-16s | %-6s | %-20s | %-10s | %s%n",
            "Slot Waktu", "Kode", "Mata Kuliah", "Ruangan", "Dosen");
        System.out.println("--------------------------------------------------------------------------");

        // Urutkan hasilSlot berdasarkan slot waktu
        List<Map.Entry<String, String>> sortedSlot = new ArrayList<>(hasilSlot.entrySet());
        sortedSlot.sort(Comparator.comparing(Map.Entry::getValue));

        for (Map.Entry<String, String> entry : sortedSlot) {
            String kode   = entry.getKey();
            String slot   = entry.getValue();
            MataKuliah mk = mapMK.get(kode);
            String ruangan = ruanganMK.getOrDefault(kode, "-");
            System.out.printf("  %-16s | %-6s | %-20s | %-10s | %s%n",
                slot, kode, mk.nama, ruangan, mk.dosen);
        }

        System.out.println("\n  Implementasi selesai! Semua struktur data berjalan sesuai desain.");
        System.out.println("  Universitas Bumigora · ASD 2025/2026");
    }
}