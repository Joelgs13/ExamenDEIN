package com.example.examendein.DAO;

import com.example.examendein.model.Producto;
import com.example.examendein.BBDD.ConexionBBDD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ProductoDAO {
    public static boolean addProducto(Producto p) {
        ConexionBBDD connection = null;
        PreparedStatement pstmt = null;

        try {
            connection = new ConexionBBDD();
            String sql = "INSERT INTO productos (codigo, nombre, precio, disponible, imagen) VALUES (?, ?, ?, ?, ?)";
            pstmt = connection.getConnection().prepareStatement(sql);

            // Set fields in the prepared statement
            pstmt.setString(1, p.getCodigo());  // Set product code
            pstmt.setString(2, p.getNombre());  // Set product name
            pstmt.setFloat(3, p.getPrecio());  // Set price (as int)
            pstmt.setInt(4, p.getDisponible());  // Set available (1 or 0)

            // Set image as a blob
            if (p.getImagen() != null) {
                pstmt.setBlob(5, p.getImagen());
            } else {
                pstmt.setNull(5, Types.BLOB);  // If no image, set it to NULL
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
