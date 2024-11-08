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
import javafx.scene.Scene;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;

/**
 * Controlador encargado de gestionar la interfaz de productos en la aplicación.
 * Esta clase maneja la interacción con los elementos de la UI relacionados con la gestión de productos,
 * como la creación, actualización, eliminación, visualización de imágenes y más.
 */
public class productosController implements Initializable {

    // Elementos de la interfaz
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

    /**
     * Inicializa los componentes de la UI cuando se carga la vista.
     * Enlaza las columnas de la tabla con los atributos correspondientes de la clase Producto
     * y configura la presentación de la tabla, incluyendo la alineación de las columnas y la conversión de
     * valores de tipo booleano para la columna de disponibilidad.
     *
     * @param location La ubicación relativa del archivo FXML que se ha cargado.
     * @param resources El conjunto de recursos que se utilizan en la UI.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar columnas para enlazarlas con los atributos de Producto
        colCodigo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCodigo()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colPrecio.setCellValueFactory(cellData -> new SimpleFloatProperty(cellData.getValue().getPrecio()).asObject());

        // Alineación a la derecha de la columna precio
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

    /**
     * Carga los productos desde la base de datos y los muestra en la tabla de productos.
     * Se obtiene la lista de productos a través del DAO y luego se asignan a la tabla.
     */
    private void loadProductos() {
        ObservableList<Producto> productos = ProductoDAO.findAll();
        tabla.setItems(productos);
    }

    /**
     * Este metodo es llamado cuando se hace clic en el botón "Crear". Valida los campos del formulario
     * y, si son correctos, crea un nuevo producto en la base de datos. Si la operación es exitosa,
     * se recarga la lista de productos. Si ocurre un error, se muestra una alerta con el mensaje correspondiente.
     */
    @FXML
    private void darDeAlta() {
        // Obtener los valores de los campos del formulario
        String codigo = tfCodigo.getText().trim();
        String nombre = tfNombre.getText().trim();
        String precioStr = tfPrecio.getText().trim();
        boolean disponible = chxDisponible.isSelected();

        StringBuilder errorMessages = new StringBuilder();

        // Validaciones de los campos de entrada
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

        // Si existen errores en los campos, se muestra una alerta
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
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error al crear producto");
                alert.setHeaderText(null);
                alert.setContentText("error generico de base de datos");
                alert.showAndWait();
            }
        }

        // Crear un objeto Producto con los datos validados
        Producto nuevoProducto = new Producto(codigo, nombre, precio, disponible ? 1 : 0, imagenBlob);

        // Llamar al metodo de inserción en el DAO
        boolean exito = ProductoDAO.addProducto(nuevoProducto);

        // Recargar los productos y mostrar una alerta con el resultado de la operación
        if (exito) loadProductos();
        Alert alert = new Alert(exito ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(exito ? "Producto creado" : "Error al crear producto");
        alert.setHeaderText(null);
        alert.setContentText(exito ? "El producto ha sido creado exitosamente." : "error generico de BBDD.");
        alert.showAndWait();
    }

    /**
     * Permite al usuario seleccionar una imagen desde el sistema de archivos.
     * El archivo seleccionado debe ser una imagen en formato PNG. Una vez seleccionada,
     * la imagen se carga en el ImageView para ser visualizada.
     *
     * @param event El evento de acción generado al hacer clic en el botón de selección de imagen.
     */
    public void seleccionarImagen(ActionEvent event) {
        // Crear un objeto FileChooser para permitir al usuario seleccionar un archivo
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


    /**
     * Este metodo se ejecuta cuando se hace clic en un producto de la tabla.
     * Carga los datos del producto seleccionado en los campos del formulario para su actualización.
     * Si no hay producto seleccionado, los botones "Actualizar" y "Crear" se deshabilitan según corresponda.
     *
     * @param mouseEvent El evento de acción generado al hacer clic en un producto de la tabla.
     */
    public void rellenarCampos(MouseEvent mouseEvent) {
        // Obtener el producto seleccionado de la tabla
        Producto productoSeleccionado = tabla.getSelectionModel().getSelectedItem();

        if (productoSeleccionado != null) {
            // Rellenar los campos de texto con los datos del producto seleccionado
            tfCodigo.setText(productoSeleccionado.getCodigo());
            tfNombre.setText(productoSeleccionado.getNombre());
            tfPrecio.setText(String.valueOf(productoSeleccionado.getPrecio()));

            // Marcar el CheckBox si el producto está disponible (1 es disponible)
            chxDisponible.setSelected(productoSeleccionado.getDisponible() == 1);

            // Verificar si el producto tiene una imagen y cargarla en el ImageView
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

    /**
     * Este metodo se ejecuta cuando se hace clic en el botón "Actualizar".
     * Valida los datos del formulario y actualiza el producto seleccionado en la base de datos.
     * Si no hay cambios o si los datos son incorrectos, se muestra un mensaje de error o advertencia.
     *
     * @param event El evento de acción generado al hacer clic en el botón "Actualizar".
     */
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


    /**
     * Convierte una imagen de JavaFX a un array de bytes en formato PNG.
     *
     * @param image La imagen de tipo {@link Image} que se desea convertir a bytes.
     * @return Un array de bytes que representa la imagen en formato PNG, o un array vacío en caso de error.
     */
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


    /**
     * Muestra la imagen del producto seleccionado en una ventana modal.
     * Si el producto no tiene imagen, se muestra una alerta informando al usuario.
     *
     * @param event El evento generado al hacer clic en el botón para ver la imagen del producto.
     */
    public void verImagen(ActionEvent event) {
        // Obtener el producto seleccionado de la tabla
        Producto productoSeleccionado = tabla.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            // Si no hay producto seleccionado, mostrar un mensaje de advertencia
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Seleccionar producto");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, seleccione un producto para ver la imagen.");
            alert.showAndWait();
            return;
        }

        // Verificar si el producto tiene una imagen
        if (productoSeleccionado.getImagen() != null) {
            try {
                // Convertir el Blob de imagen en un Image
                Blob imagenBlob = productoSeleccionado.getImagen();
                byte[] imageBytes = imagenBlob.getBytes(1, (int) imagenBlob.length());
                Image image = new Image(new ByteArrayInputStream(imageBytes));

                // Crear un ImageView para mostrar la imagen
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(300);  // Establecer el tamaño de la imagen
                imageView.setFitHeight(300);
                imageView.setPreserveRatio(true);  // Mantener la relación de aspecto

                // Crear un contenedor para la imagen
                StackPane root = new StackPane();
                root.getChildren().add(imageView);

                // Crear una nueva ventana modal
                Stage stage = new Stage();
                stage.setTitle("Imagen del Producto");
                stage.initModality(Modality.APPLICATION_MODAL);  // Hacer la ventana modal
                stage.setResizable(false);  // No permitir redimensionar
                stage.setScene(new Scene(root, 300, 300));  // Establecer tamaño de la ventana
                stage.showAndWait();  // Mostrar la ventana modal

            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error al cargar imagen");
                alert.setContentText("Hubo un problema al cargar la imagen del producto.");
                alert.showAndWait();
            }
        } else {
            // Si el producto no tiene imagen, mostrar una alerta
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sin imagen");
            alert.setHeaderText(null);
            alert.setContentText("Este producto no tiene imagen asociada.");
            alert.showAndWait();
        }
    }


    /**
     * Elimina el producto seleccionado de la base de datos después de confirmar la acción.
     * Si no hay un producto seleccionado, se muestra una alerta.
     * Si la eliminación es exitosa, se recarga la tabla y se muestra un mensaje de éxito.
     *
     * @param event El evento generado al hacer clic en el botón para eliminar el producto.
     */
    public void eliminar(ActionEvent event) {
        // Obtener el producto seleccionado de la tabla
        Producto productoSeleccionado = tabla.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            // Si no hay producto seleccionado, mostrar un mensaje de advertencia
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


    /**
     * Limpia todos los campos del formulario de entrada, incluyendo los campos de texto, el
     * CheckBox y la imagen, y restablece los controles a su estado predeterminado.
     * Este metodo también habilita el botón "Crear", deshabilita el botón "Actualizar",
     * y asegura que el campo de código esté habilitado para permitir nuevas entradas.
     *
     * @param event El evento generado al hacer clic en el botón para limpiar los campos del formulario.
     */
    public void limpiar(ActionEvent event) {
        tfCodigo.clear();
        tfNombre.clear();
        tfPrecio.clear();
        chxDisponible.setSelected(false);
        ivImagenProducto.setImage(null);
        tfCodigo.setDisable(false);
        btnCrear.setDisable(false);
        btnActualizar.setDisable(true);
    }

    /**
     * Muestra una ventana de ayuda con información sobre la versión de la aplicación y el autor.
     *
     * @param event El evento generado al hacer clic en el botón de ayuda.
     */
    public void ayuda(ActionEvent event) {
        // Crear una alerta informativa
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configurar el título y el contenido de la alerta
        alert.setTitle("Información de la aplicación");
        alert.setHeaderText("Gestión de productos 1.0");
        alert.setContentText("Desarrollado por: Joel González Salgado");

        // Mostrar la alerta
        alert.showAndWait();
    }

}
