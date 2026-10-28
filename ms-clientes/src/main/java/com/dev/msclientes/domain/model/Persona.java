package com.dev.msclientes.domain.model;

import com.dev.msclientes.domain.exception.DomainValidationException;

public abstract class Persona {

    private static final int MAX_AGE = 150;

    private final Long id;
    private String nombre;
    private Gender genero;
    private Integer edad;
    private String identificacion;
    private String direccion;
    private String telefono;

    protected Persona(Long id, String nombre, Gender genero, Integer edad,
                      String identificacion, String direccion, String telefono) {
        this.id = id;
        updatePersonalData(nombre, genero, edad, identificacion, direccion, telefono);
    }

    protected final void updatePersonalData(String nombre, Gender genero, Integer edad,
                                            String identificacion, String direccion, String telefono) {
        this.nombre = required(nombre, "El nombre");
        this.identificacion = required(identificacion, "La identificación");
        this.edad = validateAge(edad);
        this.genero = genero;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    protected static String required(String value, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(fieldLabel + " es obligatorio");
        }
        return value.trim();
    }

    private static Integer validateAge(Integer edad) {
        if (edad != null && (edad < 0 || edad > MAX_AGE)) {
            throw new DomainValidationException("La edad debe estar entre 0 y " + MAX_AGE);
        }
        return edad;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Gender getGenero() {
        return genero;
    }

    public Integer getEdad() {
        return edad;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }
}
