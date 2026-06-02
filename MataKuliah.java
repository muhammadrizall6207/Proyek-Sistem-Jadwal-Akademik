public class MataKuliah {
    String kode;
    String nama;
    int sks;
    boolean wajib;
    int semester;
    String dosen;
    int prioritas;

    public MataKuliah(String kode, String nama, int sks, boolean wajib, int semester, String dosen) {
        this.kode = kode;
        this.nama = nama;
        this.sks = sks;
        this.wajib = wajib;
        this.semester = semester;
        this.dosen = dosen;
        this.prioritas = hitungPrioritas();
    }

    private int hitungPrioritas() {
        return (sks * 10) + (wajib ? 20 : 0) + (7 - semester);
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-20s | %d SKS | %-7s | Sem %d | %s | P=%d",
            kode, nama, sks, (wajib ? "Wajib" : "Pilihan"), semester, dosen, prioritas);
    }
}