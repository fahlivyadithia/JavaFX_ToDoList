package com.todolist;

public class SubTugas {
    private int id;
    private String deskripsi;
    private boolean selesai;
    private int idTugasUtama;

    public SubTugas(int id, String deskripsi, boolean selesai, int idTugasUtama) {
        this.id = id;
        this.deskripsi = deskripsi;
        this.selesai = selesai;
        this.idTugasUtama = idTugasUtama;
    }

    // Getters dan Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public boolean isSelesai() { return selesai; }
    public void setSelesai(boolean selesai) { this.selesai = selesai; }
    public int getIdTugasUtama() { return idTugasUtama; }
    public void setIdTugasUtama(int idTugasUtama) { this.idTugasUtama = idTugasUtama; }
}