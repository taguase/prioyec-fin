-- ============================================================================
--  gestion-alquileres :: 03 - Usuarios iniciales (identico a la migracion Flyway V2)
--  Las contrasenas estan cifradas con BCrypt (fuerza 10).
--    system    / system      -> cuenta tecnica, NO CADUCA NUNCA
--    admin     / Admin2024   -> administrador, caduca a los 90 dias
--    gestor    / Gestor2024  -> usuario de gestion, caduca a los 90 dias
--  La aplicacion vuelve a garantizar el usuario 'system' en cada arranque
--  (ver UsuarioBootstrap en el modulo core).
-- ============================================================================

INSERT INTO usuario (username, password, email, nombre_completo, rol, activo,
                     password_nunca_caduca, fecha_caducidad_password)
VALUES ('system',
        '$2y$10$LFadDZ8YnN.3ZdzzHpsyAu1rylXLbuxqvwl7.hENEj4ZKZI2jDFB.',
        'system@gestion-alquileres.local',
        'Usuario tecnico del sistema',
        'ROLE_ADMIN', TRUE, TRUE, NULL)
ON CONFLICT (username) DO NOTHING;

INSERT INTO usuario (username, password, email, nombre_completo, rol, activo,
                     password_nunca_caduca, fecha_caducidad_password)
VALUES ('admin',
        '$2y$10$BVMle0mRR1fn3c1sUD0l6OynXV4Kl4zPiU5FthHk581TJqdY9pbaW',
        'admin@gestion-alquileres.local',
        'Administrador',
        'ROLE_ADMIN', TRUE, FALSE, CURRENT_DATE + INTERVAL '90 day')
ON CONFLICT (username) DO NOTHING;

INSERT INTO usuario (username, password, email, nombre_completo, rol, activo,
                     password_nunca_caduca, fecha_caducidad_password)
VALUES ('gestor',
        '$2y$10$mPsjyUhgGHFYus1I0WrlBOBKKCi51QT59g0ZAigETFnlHQQxFDMLi',
        'gestor@gestion-alquileres.local',
        'Gestor de alquileres',
        'ROLE_USER', TRUE, FALSE, CURRENT_DATE + INTERVAL '90 day')
ON CONFLICT (username) DO NOTHING;
