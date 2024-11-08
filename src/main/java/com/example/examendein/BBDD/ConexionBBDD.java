package com.example.examendein.BBDD;

import javafx.scene.control.Alert;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase para gestionar la conexión a la base de datos MariaDB.
 * Proporciona métodos para establecer, mantener y cerrar la conexión
 * con la base de datos, así como cargar configuraciones desde un archivo
 * de propiedades.
 */
public class ConexionBBDD {

    /** Conexión activa a la base de datos. */
    private static Connection connection;

    /**
     * Constructor que establece la conexión con la base de datos.
     * Carga las propiedades necesarias desde el archivo
     * de configuración `bbdd.properties` y se conecta a la base de datos
     * MariaDB utilizando los parámetros especificados.
     *
     * @throws SQLException Si ocurre un error al establecer la conexión,
     *                      como si la base de datos no está disponible
     *                      o las credenciales son incorrectas.
     */
    public ConexionBBDD() throws SQLException {
        Properties connConfig = loadProperties();
        String url = connConfig.getProperty("dburl");
        connection = DriverManager.getConnection(url, connConfig);
        connection.setAutoCommit(true);
    }

    /**
     * Devuelve la conexión activa a la base de datos.
     *
     * @return La conexión activa a la base de datos.
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Cierra la conexión activa con la base de datos.
     *
     * @return La conexión cerrada, útil para otros procesos si se requiere.
     * @throws SQLException Si ocurre un error al cerrar la conexión,
     *                      por ejemplo, si la conexión ya está cerrada.
     */
    public Connection closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close(); // Cierra la conexión
        }
        return connection;
    }

    /**
     * Carga las propiedades de configuración desde el archivo `configuration.properties`.
     * Este archivo debe contener la configuración de conexión necesaria para la base de datos.
     *
     * @return Un objeto {@link Properties} con las configuraciones cargadas,
     *         o {@code null} si ocurre un error al leer el archivo.
     */
    public static Properties loadProperties() {
        try (FileInputStream fs = new FileInputStream("configuration.properties")) {
            Properties props = new Properties();
            props.load(fs);
            return props;
        } catch (IOException e) {
            showAlert("Error al cargar configuración", "No se ha podido cargar el archivo de configuración. Verifique la conexión o la ruta del archivo.");
        }
        return null;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait(); // Muestra la alerta de manera modal
    }

    /**
     * Cierra la conexión activa a la base de datos.
     *
     * @return La conexión cerrada, útil para otros procesos si se requiere.
     * @throws SQLException Si ocurre un error al cerrar la conexión,
     *                      por ejemplo, si la conexión ya está cerrada.
     */
    public Connection CloseConexion() throws SQLException {
        connection.close();
        return connection;
    }
}
