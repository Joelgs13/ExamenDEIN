package com.example.examendein.DAO;

import com.example.examendein.model.Producto;
import com.example.examendein.BBDD.ConexionBBDD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ProductoDAO {

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
