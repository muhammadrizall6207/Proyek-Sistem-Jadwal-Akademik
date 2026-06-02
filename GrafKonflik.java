import java.util.*;

public class GrafKonflik {
    private Map<String, List<String>> daftarKonflik;
    private Map<String, String> alasanKonflik;

    public GrafKonflik() {
        daftarKonflik = new LinkedHashMap<>();
        alasanKonflik = new HashMap<>();
    }

    public void tambahMK(String kode) {
        daftarKonflik.putIfAbsent(kode, new ArrayList<>());
    }

    public void tambahKonflik(String mk1, String mk2, String alasan) {
        tambahMK(mk1);
        tambahMK(mk2);

        if (!daftarKonflik.get(mk1).contains(mk2)) {
            daftarKonflik.get(mk1).add(mk2);
        }
        if (!daftarKonflik.get(mk2).contains(mk1)) {
            daftarKonflik.get(mk2).add(mk1);
        }

        String kunci = mk1.compareTo(mk2) < 0 ? mk1 + "-" + mk2 : mk2 + "-" + mk1;
        alasanKonflik.put(kunci, alasan);
    }

    public List<String> getTemanKonflik(String kode) {
        return daftarKonflik.getOrDefault(kode, new ArrayList<>());
    }

    public int getJumlahKonflik(String kode) {
        return daftarKonflik.getOrDefault(kode, new ArrayList<>()).size();
    }

    public void tampilkanSemua() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║         DAFTAR KONFLIK ANTAR MATA KULIAH             ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        
        for (Map.Entry<String, List<String>> entry : daftarKonflik.entrySet()) {
            String kode = entry.getKey();
            List<String> daftar = entry.getValue();
            System.out.printf("  %-6s  →  %s%n", kode, daftar.isEmpty() ? "(tidak ada konflik)" : daftar);
        }

        System.out.println("\n  RINCIAN ALASAN KONFLIK:");
        for (Map.Entry<String, String> entry : alasanKonflik.entrySet()) {
            String[] bagian = entry.getKey().split("-");
            System.out.printf("  %-6s ↔ %-6s : %s%n", bagian[0], bagian[1], entry.getValue());
        }
    }
}