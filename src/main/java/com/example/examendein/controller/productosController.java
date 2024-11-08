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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
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
    private MenuItem miEliminar;

    @FXML
    private MenuItem miVerImagen;

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

    public void rellenarCampos(MouseEvent mouseEvent) {
        // Obtener el producto seleccionado de la tabla
        Producto productoSeleccionado = tabla.getSelectionModel().getSelectedItem();

        if (productoSeleccionado != null) {
            // Cargar los datos del producto seleccionado en los campos de la UI

            // Rellenar los campos de texto
            tfCodigo.setText(productoSeleccionado.getCodigo());
            tfNombre.setText(productoSeleccionado.getNombre());
            tfPrecio.setText(String.valueOf(productoSeleccionado.getPrecio()));

            // Marcar el CheckBox si el producto está disponible (1 es disponible)
            chxDisponible.setSelected(productoSeleccionado.getDisponible() == 1);

            // Verificar si el producto tiene una imagen y cargarla
            if (productoSeleccionado.getImagen() != null) {
                Blob imagenBlob = productoSeleccionado.getImagen();
                try {
                    // Obtener los bytes de la imagen desde el Blob
                    byte[] imageBytes = imagenBlob.getBytes(1, (int) imagenBlob.length());

                    // Crear un InputStream desde los bytes leídos
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);

                    // Crear la imagen a partir del InputStream
                    Image image = new Image(byteArrayInputStream);

                    // Mostrar imagen en el ImageView
                    ivImagenProducto.setImage(image);

                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error al cargar imagen");
                    alert.setContentText("Hubo un problema al cargar la imagen del producto.");
                    alert.showAndWait();
                }
            } else {
                // Si no hay imagen, limpiar el ImageView
                ivImagenProducto.setImage(null);
            }

            // Deshabilitar el botón "Crear" y habilitar el botón "Actualizar"
            btnCrear.setDisable(true);
            btnActualizar.setDisable(false);
            tfCodigo.setDisable(true);

        } else {
            // Si no hay un producto seleccionado, habilitar el botón "Crear" y deshabilitar el botón "Actualizar"
            btnCrear.setDisable(false);
            btnActualizar.setDisable(true);
        }
    }

    public void actualizarProducto(ActionEvent event) {
        // Obtener los valores de los campos del formulario
        String codigo = tfCodigo.getText().trim();
        String nombre = tfNombre.getText().trim();
        String precioStr = tfPrecio.getText().trim();
        boolean disponible = chxDisponible.isSelected();

        // Obtener el producto seleccionado en la tabla
        Producto productoSeleccionado = tabla.getSelectionModel().getSelectedItem();

        // Verificar si se ha seleccionado un producto
        if (productoSeleccionado == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se ha seleccionado ningún producto.");
            alert.showAndWait();
            return;
        }

        // Validaciones de los datos
        StringBuilder errorMessages = new StringBuilder();

        if (codigo.isEmpty() || codigo.length() != 5) {
            errorMessages.append("El código debe tener exactamente 5 caracteres.\n");
        }

        if (nombre.isEmpty()) {
            errorMessages.append("El nombre del producto es obligatorio.\n");
        }

        float precio = 0;
        try {
            precio = Float.parseFloat(precioStr);
        } catch (NumberFormatException e) {
            errorMessages.append("El precio debe ser un número decimal válido.\n");
        }

        if (precio <= 0) {
            errorMessages.append("El precio debe ser mayor que cero.\n");
        }

        // Si hay errores de validación, mostramos la alerta
        if (errorMessages.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errores de validación");
            alert.setHeaderText(null);
            alert.setContentText(errorMessages.toString());
            alert.showAndWait();
            return;
        }

        // Si el producto es el mismo, mostrar una alerta
        if (productoSeleccionado.getCodigo().equals(codigo) &&
                productoSeleccionado.getNombre().equals(nombre) &&
                productoSeleccionado.getPrecio() == precio &&
                productoSeleccionado.getDisponible() == (disponible ? 1 : 0) &&
                (productoSeleccionado.getImagen() == null && ivImagenProducto.getImage() == null ||
                        productoSeleccionado.getImagen() != null && ivImagenProducto.getImage() == null)) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sin cambios");
            alert.setHeaderText(null);
            alert.setContentText("El producto no ha cambiado, no se actualizó.");
            alert.showAndWait();
            return;
        }

        // Si el producto tiene una imagen y se quiere cambiar, no se podrá dejar en blanco
        Blob imagenBlob = null;
        if (ivImagenProducto.getImage() != null) {
            try {
                // Convertir la imagen a un Blob si está presente
                byte[] imageBytes = imagenToBytes(ivImagenProducto.getImage());
                imagenBlob = new SerialBlob(imageBytes);
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error al guardar imagen");
                alert.setContentText("Error al convertir la imagen.");
                alert.showAndWait();
                return;
            }
        } else if (productoSeleccionado.getImagen() == null) {
            // Si el producto original no tiene imagen, no es obligatorio actualizarla
            imagenBlob = null;
        } else {
            // Si el producto ya tiene una imagen, no puede dejarse en blanco
            imagenBlob = productoSeleccionado.getImagen();
        }

        // Crear el objeto Producto con los nuevos datos
        Producto productoActualizado = new Producto(codigo, nombre, precio, disponible ? 1 : 0, imagenBlob);

        // Llamar al DAO para actualizar el producto
        boolean exito = ProductoDAO.updateProducto(productoActualizado);

        // Mostrar mensaje dependiendo del resultado de la actualización
        if (exito) {
            // Si la actualización fue exitosa, recargar la tabla
            loadProductos();

            // Limpiar los campos del formulario
            tfCodigo.clear();
            tfNombre.clear();
            tfPrecio.clear();
            chxDisponible.setSelected(false);
            ivImagenProducto.setImage(null);

            // Deshabilitar el botón "Actualizar" y habilitar el botón "Crear"
            btnActualizar.setDisable(true);
            btnCrear.setDisable(false);
            tfCodigo.setDisable(false);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Producto actualizado");
            alert.setHeaderText(null);
            alert.setContentText("El producto ha sido actualizado exitosamente.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error en la BBDD");
            alert.setHeaderText(null);
            alert.setContentText("Hubo un problema al actualizar el producto.");
            alert.showAndWait();
        }
    }

    public byte[] imagenToBytes(Image image) {
        // Crear un BufferedImage vacío donde dibujaremos los píxeles de la imagen
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Obtener el PixelReader de la imagen de JavaFX
        PixelReader pixelReader = image.getPixelReader();

        // Recorrer los píxeles de la imagen y copiarlos al BufferedImage
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = pixelReader.getColor(x, y);
                // Convertir el color a un formato de color compatible con BufferedImage
                int argb = (int) (color.getOpacity() * 255) << 24 | (int) (color.getRed() * 255) << 16 |
                        (int) (color.getGreen() * 255) << 8 | (int) (color.getBlue() * 255);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        // Convertir el BufferedImage a un array de bytes
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            // Usamos ImageIO para convertir la imagen a bytes en formato PNG
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];  // En caso de error, retornamos un array vacío
        }
    }

    public void verImagen(ActionEvent event) {
    }

    public void eliminar(ActionEvent event) {
        // Obtener el producto seleccionado de la tabla
        Producto productoSeleccionado = tabla.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Selección de producto");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, seleccione un producto para eliminar.");
            alert.showAndWait();
            return;
        }

        // Mostrar el cuadro de confirmación para eliminar el producto
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmación de eliminación");
        confirmacion.setHeaderText("¿Estás seguro de que deseas eliminar este producto?");
        confirmacion.setContentText("El producto no podrá recuperarse una vez eliminado.");

        // Esperar la respuesta del usuario
        ButtonType respuesta = confirmacion.showAndWait().orElse(ButtonType.CANCEL);

        if (respuesta == ButtonType.OK) {
            // Si el usuario confirma la eliminación, proceder con la eliminación
            boolean exito = ProductoDAO.deleteProducto(productoSeleccionado.getCodigo());

            if (exito) {
                // Si la eliminación es exitosa, mostrar un mensaje y recargar la tabla
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Producto eliminado");
                alert.setHeaderText(null);
                alert.setContentText("El producto ha sido eliminado correctamente.");
                alert.showAndWait();

                // Recargar la tabla de productos
                loadProductos();
            } else {
                // Si ocurre un error, mostrar un mensaje de error
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error en la BBDD");
                alert.setHeaderText(null);
                alert.setContentText("Ocurrió un error al eliminar el producto de la base de datos.");
                alert.showAndWait();
            }
        } else {
            // Si el usuario cancela, no hacer nada
            return;
        }
    }
}
