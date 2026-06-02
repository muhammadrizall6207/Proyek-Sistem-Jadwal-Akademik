import java.util.*;

public class HashTableJadwal {
    static class EntriJadwal {
        String kunci;
        MataKuliah mataKuliah;
        String ruangan;

        public EntriJadwal(String kunci, MataKuliah mataKuliah, String ruangan) {
            this.kunci = kunci;
            this.mataKuliah = mataKuliah;
            this.ruangan = ruangan;
        }

        @Override
        public String toString() {
            return String.format("  %-25s → %-20s | Ruangan: %-5s | Dosen: %s",
                kunci, mataKuliah.nama, ruangan, mataKuliah.dosen);
        }
    }

    private static final int UKURAN = 11;
    private LinkedList<EntriJadwal>[] tempatPenyimpanan;
    private int jumlahData = 0;

    @SuppressWarnings("unchecked")
    public HashTableJadwal() {
        tempatPenyimpanan = new LinkedList[UKURAN];
        for (int i = 0; i < UKURAN; i++) tempatPenyimpanan[i] = new LinkedList<>();
    }

    public static String buatKunci(String hari, String jam, String ruangan) {
        return hari.toUpperCase() + "_" + jam + "_" + ruangan.toUpperCase();
    }

    private int fungsiHash(String kunci) {
        int nilai = 0;
        for (char c : kunci.toCharArray()) nilai = (nilai * 31 + c) % UKURAN;
        return Math.abs(nilai);
    }

    public boolean tambahData(String hari, String jam, String ruangan, MataKuliah mk) {
        String kunci = buatKunci(hari, jam, ruangan);
        int indeks = fungsiHash(kunci);

        for (EntriJadwal entri : tempatPenyimpanan[indeks]) {
            if (entri.kunci.equals(kunci)) {
                System.out.println("  ❌ GAGAL: Slot " + kunci + " sudah terisi!");
                return false;
            }
        }
        tempatPenyimpanan[indeks].add(new EntriJadwal(kunci, mk, ruangan));
        jumlahData++;
        return true;
    }

    public void tampilkanSemua() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║            HASH TABLE - JADWAL TERSIMPAN             ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.printf("  Ukuran Bucket: %d | Jumlah Data: %d | Load Factor: %.2f%n%n",
            UKURAN, jumlahData, (double)jumlahData/UKURAN);

        for (int i = 0; i < UKURAN; i++) {
            if (tempatPenyimpanan[i].isEmpty()) {
                System.out.printf("  Bucket[%2d] → (Kosong)%n", i);
            } else {
                for (EntriJadwal entri : tempatPenyimpanan[i]) {
                    System.out.printf("  Bucket[%2d] → %s%n", i, entri);
                }
                if (tempatPenyimpanan[i].size() > 1) {
                    System.out.println("               ⚠️  Collision: ada " + tempatPenyimpanan[i].size() + " data di sini");
                }
            }
        }
    }
}