package com.dev.msclientes.domain.model;

import com.dev.msclientes.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteTest {

    private Cliente validCliente() {
        return Cliente.create("jlema", "Jose Lema", Gender.MALE, 30, "0102030405",
                "Otavalo sn y principal", "098254785", "hash-1234");
    }

    @Test
    @DisplayName("A new cliente is created active with its Persona and Cliente data")
    void createsValidActiveCliente() {
        Cliente cliente = validCliente();

        assertEquals("jlema", cliente.getClienteId());
        assertEquals("Jose Lema", cliente.getNombre());
        assertEquals("0102030405", cliente.getIdentificacion());
        assertEquals("098254785", cliente.getTelefono());
        assertTrue(cliente.isEstado());
    }

    @Test
    @DisplayName("Cliente inherits from Persona")
    void clienteIsAPersona() {
        assertInstanceOf(Persona.class, validCliente());
    }

    @Test
    @DisplayName("nombre is required")
    void rejectsBlankNombre() {
        assertThrows(DomainValidationException.class, () ->
                Cliente.create("jlema", " ", Gender.MALE, 30, "0102030405", null, null, "hash"));
    }

    @Test
    @DisplayName("identificacion is required")
    void rejectsNullIdentificacion() {
        assertThrows(DomainValidationException.class, () ->
                Cliente.create("jlema", "Jose Lema", Gender.MALE, 30, null, null, null, "hash"));
    }

    @Test
    @DisplayName("contrasena is required")
    void rejectsBlankContrasena() {
        assertThrows(DomainValidationException.class, () ->
                Cliente.create("jlema", "Jose Lema", Gender.MALE, 30, "0102030405", null, null, ""));
    }

    @Test
    @DisplayName("edad cannot be negative")
    void rejectsNegativeEdad() {
        assertThrows(DomainValidationException.class, () ->
                Cliente.create("jlema", "Jose Lema", Gender.MALE, -1, "0102030405", null, null, "hash"));
    }

    @Test
    @DisplayName("Deactivate and activate toggle estado")
    void togglesEstado() {
        Cliente cliente = validCliente();

        cliente.deactivate();
        assertFalse(cliente.isEstado());

        cliente.activate();
        assertTrue(cliente.isEstado());
    }

    @Test
    @DisplayName("Update changes the data but keeps the clienteId")
    void updatesData() {
        Cliente cliente = validCliente();

        cliente.update("Jose L. Lema", Gender.MALE, 31, "0102030405", "Nueva dirección", "099999999", false);

        assertEquals("jlema", cliente.getClienteId());
        assertEquals("Jose L. Lema", cliente.getNombre());
        assertEquals("Nueva dirección", cliente.getDireccion());
        assertFalse(cliente.isEstado());
    }
}
