package com.example.examendein.controller;

import com.example.examendein.DAO.ProductoDAO;
import com.example.examendein.model.Producto;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sql.rowset.serial.SerialBlob;

public class productosController implements Initializable {

    @FXML
    private MenuItem acercaDe;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnCrear;

    @FXML
    private Button btnLimpiar;

    @FXML
    private CheckBox chxDisponible;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, Float> colPrecio;

    @FXML
    private TableColumn<Producto, Boolean> colDisponible;

    @FXML
    private ImageView ivImagenProducto;

    @FXML
    private TableView<Producto> tabla;

    @FXML
    private TextField tfCodigo;

    @FXML
    private TextField tfNombre;

    @FXML
    private TextField tfPrecio;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar columnas para enlazarlas con los atributos de Producto
        colCodigo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCodigo()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colPrecio.setCellValueFactory(cellData -> new SimpleFloatProperty(cellData.getValue().getPrecio()).asObject());

        //hacer que este a la derecha el precio
        colPrecio.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Configurar la columna de disponible para mostrar un CheckBox en vez de 0/1
        // Convertir el valor disponible a un valor booleano (1 -> true, 0 -> false)
        colDisponible.setCellValueFactory(cellData -> {
            // Comprobar si el valor disponible es 1 o 0 y asignar el valor booleano correspondiente
            boolean isDisponible = (cellData.getValue().getDisponible() == 1); // true si 1, false si 0
            return new SimpleBooleanProperty(isDisponible);
        });

        // Usar CheckBoxTableCell para la columna disponible
        colDisponible.setCellFactory(CheckBoxTableCell.forTableColumn(colDisponible));

        // Cargar datos en la tabla
        loadProductos();
    }

    private void loadProductos() {
        ObservableList<Producto> productos = ProductoDAO.findAll();
        tabla.setItems(productos);
    }

    @FXML
    private void darDeAlta() {
        String codigo = tfCodigo.getText().trim();
        String nombre = tfNombre.getText().trim();
        String precioStr = tfPrecio.getText().trim();
        boolean disponible = chxDisponible.isSelected();

        StringBuilder errorMessages = new StringBuilder();

        // Validaciones
        if (codigo.isEmpty() || codigo.length() != 5) {
            errorMessages.append("El código debe tener exactamente 5 caracteres.\n");
        }

        if (nombre.isEmpty()) {
            errorMessages.append("El nombre del producto es obligatorio.\n");
        }

        // Validación para el precio como número decimal
        float precio = 0;
        try {
            precio = Float.parseFloat(precioStr);
        } catch (NumberFormatException e) {
            errorMessages.append("El precio debe ser un número decimal válido.\n");
        }

        if (precio <= 0) {
            errorMessages.append("El precio debe ser mayor que cero.\n");
        }

        // Si hay errores, mostramos el mensaje de alerta
        if (errorMessages.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errores de validación");
            alert.setHeaderText(null);
            alert.setContentText(errorMessages.toString());
            alert.showAndWait();
            return;
        }

        // Convertir la imagen a un Blob si está presente
        Blob imagenBlob = null;
        if (ivImagenProducto.getImage() != null) {
            try {
                // Obtener la URL de la imagen como String
                String imageUrlString = ivImagenProducto.getImage().getUrl();

                // Convertir el String en un objeto URL
                URL imageUrl = new URL(imageUrlString);

                // Abrir un InputStream desde la URL
                InputStream inputStream = imageUrl.openStream();

                // Leer los datos de la imagen y escribirlos en un ByteArrayOutputStream
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                // Convertir el ByteArrayOutputStream a un array de bytes
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Crear un Blob a partir del array de bytes
                imagenBlob = new SerialBlob(imageBytes);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error al crear producto");
                alert.setHeaderText(null);
                alert.setContentText("error generico de base de datos");
                alert.showAndWait();
            }
        }

        // Crear un objeto Producto
        Producto nuevoProducto = new Producto(codigo, nombre, precio, disponible ? 1 : 0, imagenBlob);

        // Llamar al metodo de inserción en el DAO
        boolean exito = ProductoDAO.addProducto(nuevoProducto);

        if (exito) loadProductos();
        // Mostrar un mensaje al usuario según el resultado de la inserción
        Alert alert = new Alert(exito ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(exito ? "Producto creado" : "Error al crear producto");
        alert.setHeaderText(null);
        alert.setContentText(exito ? "El producto ha sido creado exitosamente." : "error generico de BBDD.");
        alert.showAndWait();
    }

    public void seleccionarImagen(ActionEvent event) {

        // Crear un objeto FileChooser
        FileChooser fileChooser = new FileChooser();

        // Establecer un filtro para archivos .png
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        // Abrir el diálogo para seleccionar un archivo
        Stage stage = (Stage) btnCrear.getScene().getWindow();  // Obtener la ventana actual
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);

        // Verificar si se seleccionó un archivo
        if (selectedFile != null) {
            // Cargar la imagen seleccionada en el ImageView
            String imagePath = selectedFile.toURI().toString();
            ivImagenProducto.setImage(new Image(imagePath));
        }
    }
}
