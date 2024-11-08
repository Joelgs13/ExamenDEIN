package com.example.examendein.model;

import java.sql.Blob;

/**
 * Clase que representa un producto con atributos como código, nombre, precio, disponibilidad e imagen.
 */
public class Producto {

    private String codigo;
    private String nombre;
    private float precio;
    private int disponible;
    private Blob imagen;

    /**
     * Constructor vacío para crear una instancia de Producto sin inicializar los atributos.
     */
    public Producto() {}

    /**
     * Constructor que inicializa todos los atributos del producto.
     *
     * @param codigo El código del producto.
     * @param nombre El nombre del producto.
     * @param precio El precio del producto.
     * @param disponible Indica si el producto está disponible (1) o no (0).
     * @param imagen La imagen del producto almacenada como Blob.
     */
    public Producto(String codigo, String nombre, float precio, int disponible, Blob imagen) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.disponible = disponible;
        this.imagen = imagen;
    }

    /**
     * Obtiene el código del producto.
     *
     * @return El código del producto.
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Establece el código del producto.
     *
     * @param codigo El código del producto.
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /**
     * Obtiene el nombre del producto.
     *
     * @return El nombre del producto.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del producto.
     *
     * @param nombre El nombre del producto.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el precio del producto.
     *
     * @return El precio del producto.
     */
    public float getPrecio() {
        return precio;
    }

    /**
     * Establece el precio del producto.
     *
     * @param precio El precio del producto.
     */
    public void setPrecio(float precio) {
        this.precio = precio;
    }

    /**
     * Obtiene la disponibilidad del producto.
     *
     * @return 1 si el producto está disponible, 0 si no lo está.
     */
    public int getDisponible() {
        return disponible;
    }

    /**
     * Establece la disponibilidad del producto.
     *
     * @param disponible 1 si el producto está disponible, 0 si no lo está.
     */
    public void setDisponible(int disponible) {
        this.disponible = disponible;
    }

    /**
     * Obtiene la imagen del producto almacenada como Blob.
     *
     * @return La imagen del producto como un Blob.
     */
    public Blob getImagen() {
        return imagen;
    }

    /**
     * Establece la imagen del producto.
     *
     * @param imagen La imagen del producto almacenada como Blob.
     */
    public void setImagen(Blob imagen) {
        this.imagen = imagen;
    }

    /**
     * Devuelve una representación en texto del objeto Producto.
     *
     * @return Una cadena con los valores de los atributos del producto.
     */
    @Override
    public String toString() {
        return "Producto{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", disponible=" + disponible +
                '}';
    }
}
