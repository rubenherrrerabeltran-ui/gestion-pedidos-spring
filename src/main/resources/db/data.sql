-- ============================================================================
-- Datos de prueba (entorno local). INSERT IGNORE no duplica filas cuando la
-- aplicación se reinicia sobre una base MySQL que ya tiene los registros.
-- Contraseñas guardadas como hash PBKDF2 (co.sena.gestionpedidos.util.ClaveUtil):
--   admin@gestionpedidos.local            / Admin2026*     (ADMINISTRADOR)
--   laura.vendedora@gestionpedidos.local  / Vendedor2026*  (VENDEDOR)
--   carlos.cliente@gestionpedidos.local   / Cliente2026*   (CLIENTE)
-- ============================================================================
INSERT IGNORE INTO usuario (id_usuario, nombre, documento, correo, telefono, password_hash, rol) VALUES
  (1, 'Administrador del sistema', '1032456789', 'admin@gestionpedidos.local', '3001234567',
   'pbkdf2$120000$1ddwVi2xLeTUNIqPdHnQAQ==$n/KPxngLX5nteR/5s/4psSG21ooP5bB7GGi3OhemcBE=', 'ADMINISTRADOR'),
  (2, 'Laura Gómez', '52987654', 'laura.vendedora@gestionpedidos.local', '3109876543',
   'pbkdf2$120000$e9TTWoIhWVUwlK3t0J3oGQ==$MBhjVH8tFR3K2K7OhFz2pIZYT+dpLHZxjUFh2VbGyUM=', 'VENDEDOR'),
  (3, 'Carlos Ramírez', '80123456', 'carlos.cliente@gestionpedidos.local', '3155550101',
   'pbkdf2$120000$Wlkk7tVVliaixkqvmSqJkg==$g3DTYFZtN77CiWzRjjAYIDguj9nxqc/FfUcSd7rTiYQ=', 'CLIENTE');

INSERT IGNORE INTO producto (id_producto, nombre_producto, precio, stock, activo) VALUES
  (1, 'Café de origen Huila 500 g', 28000.00, 40, TRUE),
  (2, 'Panela orgánica 1 kg', 6500.00, 60, TRUE),
  (3, 'Chocolate de mesa 250 g', 9800.00, 25, TRUE),
  (4, 'Arepas de maíz x 10', 7200.00, 35, TRUE),
  (5, 'Queso campesino 500 g', 14500.00, 8, TRUE);

INSERT IGNORE INTO pedido (id_pedido, id_usuario, fecha_pedido, estado) VALUES
  (1, 3, '2026-11-10 09:30:00', 'pagado'),
  (2, 3, '2026-11-12 16:05:00', 'pendiente');

INSERT IGNORE INTO detalle_pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES
  (1, 1, 1, 2, 28000.00),
  (2, 1, 2, 3, 6500.00),
  (3, 2, 3, 1, 9800.00),
  (4, 2, 5, 2, 14500.00);
