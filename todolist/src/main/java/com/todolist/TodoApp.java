package com.todolist;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TodoApp extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/todolist?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private final TableView<Tugas> tableView = new TableView<>();
    private final ObservableList<Tugas> daftarTugasObservable = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Aplikasi To-Do List");

        // Kolom ID
        TableColumn<Tugas, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Kolom Deskripsi
        TableColumn<Tugas, String> descCol = new TableColumn<>("Deskripsi Tugas");
        descCol.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        descCol.setPrefWidth(250);

        // Kolom Status (Checkbox)
        TableColumn<Tugas, Boolean> statusCol = new TableColumn<>("Selesai?");
        statusCol.setCellValueFactory(param -> {
            Tugas tugas = param.getValue();
            return new SimpleBooleanProperty(tugas.getStatus() == Status.SELESAI);
        });

        statusCol.setCellFactory(CheckBoxTableCell.forTableColumn(statusCol));
        statusCol.setEditable(true);
        tableView.setEditable(true);

        // Listener untuk update status di database saat checkbox diubah
        statusCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tugas, Boolean>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Tugas, Boolean> param) {
                Tugas tugas = param.getValue();
                SimpleBooleanProperty prop = new SimpleBooleanProperty(tugas.getStatus() == Status.SELESAI);
                prop.addListener((observable, oldValue, newValue) -> {
                    tugas.setStatus(newValue ? Status.SELESAI : Status.BELUM_SELESAI);
                    updateStatusDiDb(tugas.getId(), tugas.getStatus());
                });
                return prop;
            }
        });

        tableView.getColumns().addAll(idCol, descCol, statusCol);

        // Form Tambah
        TextField inputDeskripsi = new TextField();
        inputDeskripsi.setPromptText("Masukkan deskripsi tugas");
        inputDeskripsi.setPrefWidth(300);
        Button btnTambah = new Button("Tambah");
        btnTambah.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnTambah.setOnAction(e -> {
            String deskripsi = inputDeskripsi.getText();
            if (!deskripsi.trim().isEmpty()) {
                tambahTugasKeDb(deskripsi);
                refreshTabelTugas();
                inputDeskripsi.clear();
            }
        });

        HBox formTambah = new HBox(10, inputDeskripsi, btnTambah);

        // Area Aksi (hapus, edit)
        Button btnHapus = new Button("Hapus");
        btnHapus.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        btnHapus.setOnAction(e -> {
            Tugas tugas = tableView.getSelectionModel().getSelectedItem();
            if (tugas != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Konfirmasi Hapus");
                alert.setHeaderText("Hapus tugas: " + tugas.getDeskripsi());
                alert.setContentText("Apakah Anda yakin?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    hapusTugasDariDb(tugas.getId());
                    refreshTabelTugas();
                }
            }
        });

        Button btnEdit = new Button("Edit");
        btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnEdit.setOnAction(e -> {
            Tugas tugas = tableView.getSelectionModel().getSelectedItem();
            if (tugas != null) {
                TextField inputEdit = new TextField(tugas.getDeskripsi());
                inputEdit.setPrefWidth(300);
                Alert alertEdit = new Alert(Alert.AlertType.CONFIRMATION);
                alertEdit.setTitle("Edit Tugas");
                alertEdit.setHeaderText("Edit deskripsi tugas:");
                alertEdit.getDialogPane().setContent(inputEdit);
                Optional<ButtonType> result = alertEdit.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    String deskripsiBaru = inputEdit.getText();
                    if (!deskripsiBaru.trim().isEmpty()) {
                        updateDeskripsiTugas(tugas.getId(), deskripsiBaru);
                        refreshTabelTugas();
                    }
                }
            }
        });

        HBox aksiBox = new HBox(10, btnEdit, btnHapus);

        // Layout utama
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        Label header = new Label("Daftar Tugas");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        layout.getChildren().addAll(header, tableView, formTambah, aksiBox);

        Scene scene = new Scene(layout, 550, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshTabelTugas();
    }

    // ===================== FUNGSI DATABASE ========================

    private void refreshTabelTugas() {
        try {
            List<Tugas> daftar = ambilSemuaTugasDariDb();
            daftarTugasObservable.setAll(daftar);
            tableView.setItems(daftarTugasObservable);
        } catch (SQLException e) {
            showAlertError("Gagal Memuat Data", e.getMessage());
        }
    }

    private List<Tugas> ambilSemuaTugasDariDb() throws SQLException {
        String sql = "SELECT id_tugas, deskripsi_tugas, status_tugas FROM tb_tugas ORDER BY id_tugas";
        List<Tugas> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id_tugas");
                String deskripsi = rs.getString("deskripsi_tugas");
                Status status = rs.getInt("status_tugas") == 1 ? Status.SELESAI : Status.BELUM_SELESAI;
                list.add(new Tugas(id, deskripsi, status));
            }
        }
        return list;
    }

    private void tambahTugasKeDb(String deskripsi) {
        String sql = "INSERT INTO tb_tugas(deskripsi_tugas, status_tugas) VALUES (?, 0)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, deskripsi);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlertError("Gagal Menambah", e.getMessage());
        }
    }

    private void updateStatusDiDb(int id, Status status) {
        String sql = "UPDATE tb_tugas SET status_tugas = ? WHERE id_tugas = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, status == Status.SELESAI ? 1 : 0);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlertError("Gagal Update Status", e.getMessage());
        }
    }

    private void updateDeskripsiTugas(int id, String deskripsiBaru) {
        String sql = "UPDATE tb_tugas SET deskripsi_tugas = ? WHERE id_tugas = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, deskripsiBaru);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlertError("Gagal Edit", e.getMessage());
        }
    }

    private void hapusTugasDariDb(int id) {
        String sql = "DELETE FROM tb_tugas WHERE id_tugas = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlertError("Gagal Menghapus", e.getMessage());
        }
    }

    private void showAlertError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
