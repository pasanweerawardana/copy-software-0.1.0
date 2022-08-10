package controller;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

public class MainFormController {
    public Label lblProgress;
    public Label lblSize;
    public JFXButton btnSelectFile;
    public Label lblFile;
    public JFXButton btnSelectDir;
    public Label lblFolder;
    public Rectangle pgbContainer;
    public Rectangle pgbBar;
    public JFXButton btnCopy;

    private File srcFile;
    private File destDir;

    public void initialize() {
        btnCopy.setDisable(true);
    }

    public void btnSelectFileOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Select a file to copy");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*.*"));

        srcFile = fileChooser.showOpenDialog(lblFolder.getScene().getWindow());
        if (srcFile != null) {
            lblFile.setText(srcFile.getName() + ". " + (srcFile.length() / 1024.0) + "Kb");
        } else {
            lblFile.setText("No file selected");
        }

        btnCopy.setDisable(srcFile == null || destDir == null);
    }

    public void btnSelectDirOnAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a destination folder");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        destDir = directoryChooser.showDialog(lblFolder.getScene().getWindow());

        if (destDir != null) {
            lblFolder.setText(destDir.getAbsolutePath());
        } else {
            lblFolder.setText("No folder selected");
        }

        btnCopy.setDisable(srcFile == null || destDir == null);
    }

    public void btnCopyOnAction(ActionEvent actionEvent) throws IOException {
        File destFile = new File(destDir, srcFile.getName());
        if (!destFile.exists()) {
            destFile.createNewFile();
        } else {
            Optional<ButtonType> result = new Alert(Alert.AlertType.INFORMATION,
                    "File already exists. Do you want to overwrite?",
                    ButtonType.YES, ButtonType.NO).showAndWait();
            if (result.get() == ButtonType.NO) {
                return;
            }
        }

        new Thread(()->{
            try {
                FileInputStream fis = new FileInputStream(srcFile);
                FileOutputStream fos = new FileOutputStream(destFile);

                long fileSize = srcFile.length();
                for (int i = 0; i < fileSize; i++) {
                    File[] fArray = srcFile.listFiles();
                    for (int j = 0; j < srcFile.listFiles().length; j++) {
                        fArray[i] = srcFile;
                        int readByte = fis.read();
                        fos.write(readByte);
                        int k = i;
                        Platform.runLater(()->{
                            pgbBar.setWidth(pgbContainer.getWidth() / fileSize * k);
                            lblProgress.setText("Progress: " + (k * 1.0 / fileSize * 100) + "%");
                            lblSize.setText((k / 1024.0) + " / " + (fileSize / 1024.0) + " Kb");
                        });
                    }
                }

                fos.close();
                fis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Platform.runLater(()->{
                pgbBar.setWidth(pgbContainer.getWidth());
                new Alert(Alert.AlertType.INFORMATION, "File has been copied successfully").show();
                lblFolder.setText("No folder selected");
                lblFile.setText("No file selected");
                btnCopy.setDisable(true);
                srcFile = null;
                destDir = null;
            });
        }).start();
    }
}
