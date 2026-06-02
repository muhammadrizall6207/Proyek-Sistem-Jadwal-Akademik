import java.util.*;

public class GraphColoring {
    private GrafKonflik graf;
    private String[] daftarWaktu = {"Senin 08:00", "Selasa 10:00", "Rabu 13:00", "Kamis 08:00", "Jumat 10:00"};

    public GraphColoring(GrafKonflik graf) {
        this.graf = graf;
    }

    public Map<String, String> tentukanWaktu(List<MataKuliah> daftarMK) {
        List<MataKuliah> terurut = new ArrayList<>(daftarMK);
        terurut.sort((a, b) -> graf.getJumlahKonflik(b.kode) - graf.getJumlahKonflik(a.kode));

        Map<String, String> hasil = new LinkedHashMap<>();

        for (MataKuliah mk : terurut) {
            Set<String> waktuTerpakai = new HashSet<>();
            for (String teman : graf.getTemanKonflik(mk.kode)) {
                if (hasil.containsKey(teman)) {
                    waktuTerpakai.add(hasil.get(teman));
                }
            }

            String waktuDipilih = null;
            for (String waktu : daftarWaktu) {
                if (!waktuTerpakai.contains(waktu)) {
                    waktuDipilih = waktu;
                    break;
                }
            }
            hasil.put(mk.kode, waktuDipilih != null ? waktuDipilih : "Tidak Ada Waktu");
        }
        return hasil;
    }

    public void tampilkanHasil(Map<String, String> hasil, List<MataKuliah> daftarMK) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║        HASIL PENENTUAN WAKTU PELAKSANAAN             ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.printf("  %-6s | %-20s | %-16s | %s%n", "Kode", "Mata Kuliah", "Waktu Pelaksanaan", "Berkonflik dengan");
        System.out.println("  -----------------------------------------------------------------");

        Map<String, MataKuliah> peta = new HashMap<>();
        for (MataKuliah mk : daftarMK) peta.put(mk.kode, mk);

        for (Map.Entry<String, String> entry : hasil.entrySet()) {
            String kode = entry.getKey();
            String waktu = entry.getValue();
            List<String> konflik = graf.getTemanKonflik(kode);
            System.out.printf("  %-6s | %-20s | %-16s | %s%n", kode, peta.get(kode).nama, waktu, konflik);
        }

        System.out.println("\n  >> VERIFIKASI TANPA KONFLIK:");
        boolean aman = true;
        for (String mk1 : hasil.keySet()) {
            for (String mk2 : graf.getTemanKonflik(mk1)) {
                if (hasil.containsKey(mk2) && hasil.get(mk1).equals(hasil.get(mk2))) {
                    System.out.printf("  ⚠️  KONFLIK! %s dan %s sama jam: %s%n", mk1, mk2, hasil.get(mk1));
                    aman = false;
                }
            }
        }
        if (aman) System.out.println("  ✅ [BERHASIL] Semua mata kuliah aman, tidak ada yang bentrok!");
    }
}