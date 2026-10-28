package com.dev.msclientes.domain.model;

public class Cliente extends Persona {

    private final String clienteId;
    private String contrasena;
    private boolean estado;

    private Cliente(Long id, String clienteId, String nombre, Gender genero, Integer edad,
                    String identificacion, String direccion, String telefono,
                    String contrasena, boolean estado) {
        super(id, nombre, genero, edad, identificacion, direccion, telefono);
        this.clienteId = required(clienteId, "El clienteId");
        this.contrasena = required(contrasena, "La contraseña");
        this.estado = estado;
    }

    /** Creates a new cliente, always active. The contrasena must already be hashed. */
    public static Cliente create(String clienteId, String nombre, Gender genero, Integer edad,
                                 String identificacion, String direccion, String telefono,
                                 String hashedPassword) {
        return new Cliente(null, clienteId, nombre, genero, edad, identificacion,
                direccion, telefono, hashedPassword, true);
    }

    /** Rebuilds an already persisted cliente (meant for outbound adapters only). */
    public static Cliente rehydrate(Long id, String clienteId, String nombre, Gender genero, Integer edad,
                                    String identificacion, String direccion, String telefono,
                                    String hashedPassword, boolean estado) {
        return new Cliente(id, clienteId, nombre, genero, edad, identificacion,
                direccion, telefono, hashedPassword, estado);
    }

    public void update(String nombre, Gender genero, Integer edad, String identificacion,
                       String direccion, String telefono, boolean estado) {
        updatePersonalData(nombre, genero, edad, identificacion, direccion, telefono);
        this.estado = estado;
    }

    public void changePassword(String hashedPassword) {
        this.contrasena = required(hashedPassword, "La contraseña");
    }

    public void activate() {
        this.estado = true;
    }

    public void deactivate() {
        this.estado = false;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getContrasena() {
        return contrasena;
    }

    public boolean isEstado() {
        return estado;
    }
}
