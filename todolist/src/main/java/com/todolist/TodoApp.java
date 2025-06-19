package com.todolist;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TodoApp extends Application {

    // --- Pastikan detail koneksi ini sudah benar ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/todolist";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private final TableView<Tugas> tableViewTugas = new TableView<>();
    private final TableView<SubTugas> tableViewSubTugas = new TableView<>();
    private final ObservableList<Tugas> daftarTugasObservable = FXCollections.observableArrayList();
    private final ObservableList<SubTugas> daftarSubTugasObservable = FXCollections.observableArrayList();

    private Label subTugasHeader;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Aplikasi To-Do List dengan Progres");

        VBox panelTugasUtama = buildPanelTugasUtama();
        VBox panelSubTugas = buildPanelSubTugas();

        tableViewTugas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                subTugasHeader.setText("Sub-Aktivitas untuk: " + newSelection.getDeskripsi());
                muatSubTugas(newSelection);
            } else {
                subTugasHeader.setText("Pilih sebuah tugas utama");
                daftarSubTugasObservable.clear();
            }
        });

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(panelTugasUtama, panelSubTugas);
        splitPane.setDividerPositions(0.6);

        Scene scene = new Scene(splitPane, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshTabelTugas();
    }
    
    private VBox buildPanelTugasUtama() {
        // --- Kolom Tabel Tugas Utama ---
        // PropertyValueFactory harus cocok dengan nama variabel di Tugas.java
        TableColumn<Tugas, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id")); // cocok dengan `private int id;` di Tugas.java

        TableColumn<Tugas, String> descCol = new TableColumn<>("Deskripsi Tugas");
        descCol.setCellValueFactory(new PropertyValueFactory<>("deskripsi")); // cocok dengan `private String deskripsi;`
        descCol.setPrefWidth(250);
        
        TableColumn<Tugas, LocalDate> tglDibuatCol = new TableColumn<>("Tgl Dibuat");
        tglDibuatCol.setCellValueFactory(new PropertyValueFactory<>("tglDibuat")); // cocok dengan `private LocalDate tglDibuat;`

        TableColumn<Tugas, LocalDate> tglTargetCol = new TableColumn<>("Tgl Target");
        tglTargetCol.setCellValueFactory(new PropertyValueFactory<>("tglTarget")); // cocok dengan `private LocalDate tglTarget;`

        TableColumn<Tugas, Double> progresCol = new TableColumn<>("Progres");
        progresCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getProgres()).asObject()); // Menggunakan method getProgres()
        progresCol.setCellFactory(ProgressBarTableCell.forTableColumn());
        progresCol.setPrefWidth(150);
        
        tableViewTugas.getColumns().addAll(idCol, descCol, tglDibuatCol, tglTargetCol, progresCol);
        tableViewTugas.setItems(daftarTugasObservable);

        // ... Sisa dari method buildPanelTugasUtama tetap sama ...
        TextField inputDeskripsi = new TextField();
        inputDeskripsi.setPromptText("Deskripsi tugas utama baru");
        inputDeskripsi.setPrefWidth(300);
        DatePicker inputTglTarget = new DatePicker();
        inputTglTarget.setPromptText("Pilih tgl target");
        Button btnTambah = new Button("Tambah Tugas");
        btnTambah.setOnAction(e -> {
            if (!inputDeskripsi.getText().trim().isEmpty()) {
                tambahTugasKeDb(inputDeskripsi.getText(), inputTglTarget.getValue());
                refreshTabelTugas();
                inputDeskripsi.clear();
                inputTglTarget.setValue(null);
            }
        });
        HBox formTambah = new HBox(10, inputDeskripsi, inputTglTarget, btnTambah);
        Button btnHapus = new Button("Hapus Tugas");
        btnHapus.setOnAction(e -> {
            Tugas tugas = tableViewTugas.getSelectionModel().getSelectedItem();
            if (tugas != null) {
                hapusTugasDariDb(tugas.getId());
                refreshTabelTugas();
            }
        });
        HBox aksiBox = new HBox(10, btnHapus);
        Label header = new Label("Daftar Tugas Utama");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        VBox panel = new VBox(15, header, tableViewTugas, formTambah, aksiBox);
        panel.setPadding(new Insets(15));
        return panel;
    }

    private VBox buildPanelSubTugas() {
        subTugasHeader = new Label("Pilih sebuah tugas utama");
        subTugasHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // PropertyValueFactory harus cocok dengan nama variabel di SubTugas.java
        TableColumn<SubTugas, String> descSubCol = new TableColumn<>("Deskripsi Sub-Aktivitas");
        descSubCol.setCellValueFactory(new PropertyValueFactory<>("deskripsi")); // cocok dengan `private String deskripsi;`
        descSubCol.setPrefWidth(250);

        TableColumn<SubTugas, Boolean> selesaiSubCol = new TableColumn<>("Selesai");
        selesaiSubCol.setCellValueFactory(cellData -> {
            SubTugas subTugas = cellData.getValue();
            SimpleBooleanProperty prop = new SimpleBooleanProperty(subTugas.isSelesai()); // menggunakan method isSelesai()
            prop.addListener((obs, wasSelesai, isSelesai) -> {
                subTugas.setSelesai(isSelesai);
                updateStatusSubTugas(subTugas);
                tableViewTugas.refresh();
            });
            return prop;
        });
        selesaiSubCol.setCellFactory(CheckBoxTableCell.forTableColumn(selesaiSubCol));
        
        tableViewSubTugas.getColumns().addAll(descSubCol, selesaiSubCol);
        tableViewSubTugas.setItems(daftarSubTugasObservable);
        tableViewSubTugas.setEditable(true);

        // ... Sisa dari method buildPanelSubTugas tetap sama ...
        TextField inputSubDeskripsi = new TextField();
        inputSubDeskripsi.setPromptText("Deskripsi sub-aktivitas baru");
        inputSubDeskripsi.setPrefWidth(200);
        Button btnTambahSub = new Button("Tambah Sub-Aktivitas");
        btnTambahSub.setOnAction(e -> {
            Tugas tugasUtama = tableViewTugas.getSelectionModel().getSelectedItem();
            if (tugasUtama != null && !inputSubDeskripsi.getText().trim().isEmpty()) {
                tambahSubTugasKeDb(inputSubDeskripsi.getText(), tugasUtama.getId());
                int selectedIndex = tableViewTugas.getSelectionModel().getSelectedIndex();
                refreshTabelTugas();
                tableViewTugas.getSelectionModel().select(selectedIndex);
                inputSubDeskripsi.clear();
            }
        });
        HBox formTambahSub = new HBox(10, inputSubDeskripsi, btnTambahSub);
        VBox panel = new VBox(15, subTugasHeader, tableViewSubTugas, formTambahSub);
        panel.setPadding(new Insets(15));
        return panel;
    }

    // ===================== FUNGSI DATABASE (DIPERIKSA ULANG) ========================

    private void refreshTabelTugas() {
        try {
            daftarTugasObservable.setAll(ambilSemuaTugasDariDb());
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Gagal Memuat Data", "Tidak bisa mengambil data dari database.", e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Tugas> ambilSemuaTugasDariDb() throws SQLException {
        // Nama kolom di query SELECT ini harus SAMA PERSIS dengan di database
        String sql = "SELECT id_tugas, deskripsi_tugas, tgl_dibuat, tgl_target FROM tb_tugas ORDER BY id_tugas";
        List<Tugas> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // String di dalam rs.get...() harus SAMA PERSIS dengan nama kolom di atas
                int id = rs.getInt("id_tugas");
                String deskripsi = rs.getString("deskripsi_tugas");
                Timestamp timestampDibuat = rs.getTimestamp("tgl_dibuat");
                LocalDate tglDibuat = (timestampDibuat != null) ? timestampDibuat.toLocalDateTime().toLocalDate() : null;
                Date dateTarget = rs.getDate("tgl_target");
                LocalDate tglTarget = (dateTarget != null) ? dateTarget.toLocalDate() : null;
                
                Tugas tugas = new Tugas(id, deskripsi, tglDibuat, tglTarget);
                tugas.setDaftarSubTugas(ambilSemuaSubTugasUntuk(id, conn));
                list.add(tugas);
            }
        }
        return list;
    }

    private void tambahTugasKeDb(String deskripsi, LocalDate tglTarget) {
        // Nama kolom di query INSERT ini harus SAMA PERSIS dengan di database
        String sql = "INSERT INTO tb_tugas(deskripsi_tugas, tgl_target) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, deskripsi);
            if (tglTarget != null) {
                pstmt.setDate(2, Date.valueOf(tglTarget));
            } else {
                pstmt.setNull(2, Types.DATE);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Gagal Menambah Tugas", "Tidak bisa menyimpan tugas utama baru ke database.", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private List<SubTugas> ambilSemuaSubTugasUntuk(int idTugasUtama, Connection conn) throws SQLException {
        // Nama kolom di query SELECT ini harus SAMA PERSIS dengan di database
        String sql = "SELECT id_sub_tugas, deskripsi_sub_tugas, status_sub_tugas FROM tb_sub_tugas WHERE id_tugas_utama = ?";
        List<SubTugas> list = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTugasUtama);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // String di dalam rs.get...() harus SAMA PERSIS dengan nama kolom di atas
                    list.add(new SubTugas(
                        rs.getInt("id_sub_tugas"),
                        rs.getString("deskripsi_sub_tugas"),
                        rs.getBoolean("status_sub_tugas"),
                        idTugasUtama
                    ));
                }
            }
        }
        return list;
    }

    // ... sisa method database (hapus, tambah sub-tugas, update status) tidak ada perubahan kritis ...
    private void hapusTugasDariDb(int id) {
        String sql = "DELETE FROM tb_tugas WHERE id_tugas = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Gagal Menghapus Tugas", "Tidak bisa menghapus tugas utama dari database.", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void muatSubTugas(Tugas tugasUtama) {
        daftarSubTugasObservable.setAll(tugasUtama.getDaftarSubTugas());
    }
    
    private void tambahSubTugasKeDb(String deskripsi, int idTugasUtama) {
        String sql = "INSERT INTO tb_sub_tugas(deskripsi_sub_tugas, id_tugas_utama) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, deskripsi);
            pstmt.setInt(2, idTugasUtama);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Gagal Menambah Sub-Tugas", "Tidak bisa menyimpan sub-tugas baru ke database.", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStatusSubTugas(SubTugas subTugas) {
        String sql = "UPDATE tb_sub_tugas SET status_sub_tugas = ? WHERE id_sub_tugas = ?";
         try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, subTugas.isSelesai());
            pstmt.setInt(2, subTugas.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Gagal Update Status", "Tidak bisa mengubah status sub-tugas di database.", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}