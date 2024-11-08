package com.example.examendein;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de la aplicación que arranca la interfaz gráfica de usuario (GUI).
 * Extiende la clase Application de JavaFX para gestionar el ciclo de vida de la aplicación.
 */
public class examenDein extends Application {

    /**
     * Inicia la aplicación y configura la ventana principal.
     *
     * @param stage La ventana principal de la aplicación.
     * @throws IOException Si ocurre un error al cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Cargar el archivo FXML que define la interfaz de usuario
        FXMLLoader fxmlLoader = new FXMLLoader(examenDein.class.getResource("/com/example/examendein/fxml/productos.fxml"));

        // Crear la escena utilizando el archivo FXML cargado
        Scene scene = new Scene(fxmlLoader.load());

        // Establecer el ícono de la ventana principal
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/examendein/img/carrito.png")));

        // Establecer el título de la ventana
        stage.setTitle("Productos");

        // Establecer la escena en el escenario (ventana)
        stage.setScene(scene);

        // Mostrar la ventana principal
        stage.show();
    }

    /**
     * Punto de entrada de la aplicación. Llama al método launch() de la clase Application
     * para iniciar la aplicación JavaFX.
     *
     * @param args Argumentos de la línea de comandos (no utilizados en este caso).
     */
    public static void main(String[] args) {
        launch();
    }
}
