package com.todolist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Tugas {
    private int id;
    private String deskripsi;
    private LocalDate tglDibuat;
    private LocalDate tglTarget;
    private List<SubTugas> daftarSubTugas = new ArrayList<>();

    public Tugas(int id, String deskripsi, LocalDate tglDibuat, LocalDate tglTarget) {
        this.id = id;
        this.deskripsi = deskripsi;
        this.tglDibuat = tglDibuat;
        this.tglTarget = tglTarget;
    }

    public double getProgres() {
        if (daftarSubTugas == null || daftarSubTugas.isEmpty()) {
            return 0.0;
        }
        long jumlahSelesai = daftarSubTugas.stream().filter(SubTugas::isSelesai).count();
        return (double) jumlahSelesai / daftarSubTugas.size();
    }

    // Getters dan Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public LocalDate getTglDibuat() { return tglDibuat; }
    public void setTglDibuat(LocalDate tglDibuat) { this.tglDibuat = tglDibuat; }
    public LocalDate getTglTarget() { return tglTarget; }
    public void setTglTarget(LocalDate tglTarget) { this.tglTarget = tglTarget; }
    public List<SubTugas> getDaftarSubTugas() { return daftarSubTugas; }
    public void setDaftarSubTugas(List<SubTugas> daftarSubTugas) { this.daftarSubTugas = daftarSubTugas; }
}