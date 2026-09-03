package com.gestion.alquileres.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Pantalla de acceso y portada de la aplicacion. */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String caducada,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null)    model.addAttribute("mensajeError", "Usuario o contrasena incorrectos.");
        if (caducada != null) model.addAttribute("mensajeError",
                "Su contrasena ha caducado. Se ha enviado un aviso al correo asociado a su usuario.");
        if (logout != null)   model.addAttribute("mensajeInfo", "Sesion cerrada correctamente.");
        return "login";
    }

    @GetMapping({"/", "/inicio"})
    public String inicio() {
        return "inicio";
    }
}
