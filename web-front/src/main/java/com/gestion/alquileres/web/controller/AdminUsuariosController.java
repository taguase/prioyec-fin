package com.gestion.alquileres.web.controller;

import com.gestion.alquileres.core.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Mantenimiento minimo de cuentas: alta y cambio de contrasena. Solo ROLE_ADMIN. */
@Controller
public class AdminUsuariosController {

    private final UsuarioService usuarios;

    public AdminUsuariosController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/admin/usuarios")
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarios.listar());
        return "admin/usuarios";
    }

    @PostMapping("/admin/usuarios/alta")
    public String alta(@RequestParam String username, @RequestParam String password,
                       @RequestParam String email, @RequestParam(required = false) String nombreCompleto,
                       @RequestParam(defaultValue = "ROLE_USER") String rol, Model model) {
        try {
            usuarios.crear(username, password, email, nombreCompleto, rol);
            model.addAttribute("mensajeOk", "Usuario '" + username + "' creado.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", "No se ha podido crear el usuario: " + e.getMessage());
        }
        model.addAttribute("usuarios", usuarios.listar());
        return "admin/usuarios";
    }

    @PostMapping("/admin/usuarios/password")
    public String cambiarPassword(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            usuarios.cambiarPassword(username, password);
            model.addAttribute("mensajeOk", "Contrasena de '" + username + "' actualizada.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", "No se ha podido cambiar la contrasena: " + e.getMessage());
        }
        model.addAttribute("usuarios", usuarios.listar());
        return "admin/usuarios";
    }
}
