package com.example.examendein.DAO;

import com.example.examendein.model.Producto;
import com.example.examendein.BBDD.ConexionBBDD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Clase que maneja las operaciones relacionadas con la tabla de productos en la base de datos.
 * Realiza operaciones como agregar, actualizar, eliminar y obtener productos.
 */
public class ProductoDAO {

    /**
     * Inserta un nuevo producto en la base de datos.
     *
     * @param p El producto a insertar.
     * @return true si la inserción fue exitosa, false en caso contrario.
     */
    public static boolean addProducto(Producto p) {
        ConexionBBDD connection = null;
        PreparedStatement pstmt = null;

        try {
            connection = new ConexionBBDD();
            String sql = "INSERT INTO productos (codigo, nombre, precio, disponible, imagen) VALUES (?, ?, ?, ?, ?)";
            pstmt = connection.getConnection().prepareStatement(sql);

            // Setear los valores en el PreparedStatement
            pstmt.setString(1, p.getCodigo());  // Setear código del producto
            pstmt.setString(2, p.getNombre());  // Setear nombre del producto
            pstmt.setFloat(3, p.getPrecio());  // Setear precio
            pstmt.setInt(4, p.getDisponible());  // Setear disponibilidad (1 o 0)

            // Setear imagen como un blob
            if (p.getImagen() != null) {
                pstmt.setBlob(5, p.getImagen());
            } else {
                pstmt.setNull(5, Types.BLOB);  // Si no hay imagen, setear NULL
            }

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (connection != null) connection.CloseConexion();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Actualiza un producto en la base de datos.
     *
     * @param p El producto con los nuevos datos.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public static boolean updateProducto(Producto p) {
        ConexionBBDD connection = null;
        PreparedStatement pstmt = null;

        try {
            connection = new ConexionBBDD();
            String sql = "UPDATE productos SET nombre = ?, precio = ?, disponible = ?, imagen = ? WHERE codigo = ?";
            pstmt = connection.getConnection().prepareStatement(sql);
            pstmt.setString(1, p.getNombre());
            pstmt.setFloat(2, p.getPrecio());
            pstmt.setInt(3, p.getDisponible());
            pstmt.setBlob(4, p.getImagen());  // Guardar la imagen como Blob
            pstmt.setString(5, p.getCodigo());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (connection != null) connection.CloseConexion();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Elimina un producto de la base de datos mediante su código.
     *
     * @param codigo El código del producto a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public static boolean deleteProducto(String codigo) {
        ConexionBBDD connection = null;
        PreparedStatement pstmt = null;

        try {
            connection = new ConexionBBDD();
            pstmt = connection.getConnection().prepareStatement("DELETE FROM productos WHERE codigo = ?");
            pstmt.setString(1, codigo);
            return pstmt.executeUpdate() > 0;  // Retorna true si se eliminó al menos un registro
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Retorna false en caso de error
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (connection != null) connection.CloseConexion();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtiene todos los productos de la base de datos.
     *
     * @return Una lista observable con todos los productos.
     */
    public static ObservableList<Producto> findAll() {
        ObservableList<Producto> productos = FXCollections.observableArrayList();
        try {
            ConexionBBDD connection = new ConexionBBDD();
            String consulta = "SELECT codigo, nombre, precio, disponible FROM productos";
            PreparedStatement pstmt = connection.getConnection().prepareStatement(consulta);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String codigo = rs.getString("codigo");
                String nombre = rs.getString("nombre");
                float precio = rs.getFloat("precio");
                int disponible = rs.getInt("disponible");

                Producto producto = new Producto(codigo, nombre, precio, disponible, null); // Imagen es null
                productos.add(producto);
            }

            rs.close();
            connection.CloseConexion();
        } catch (SQLException e) {
            System.err.println("Error al obtener los productos: " + e.getMessage());
        }
        return productos;
    }
}
