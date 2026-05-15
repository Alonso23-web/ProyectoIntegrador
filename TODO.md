# TODO - Cliente (Pasajes / Encomiendas / Contacto)

- [x] 1. Actualizar navbar con dropdowns profesionales (Pasajes, Encomiendas, Perfil) sin modificar Inicio/Nosotros.
- [x] 2. Encomiendas (frontend): ajustar `encomiendas.html` para incluir tabs requeridos (Registrar envío, Cotizar, Mis envíos, Rastrear) y timeline de estados dentro del módulo.

- [x] 3. Encomiendas (frontend): que el navbar no lleve a `/rastrear` (seguir rastreo dentro de `encomiendas.html`).

- [ ] 4. Encomiendas (backend): asegurar que `/api/encomiendas/{codigo}` devuelve y mapea correctamente estados para timeline.
- [x] 5. Pasajes: modelar entidades `Viaje` y `Reserva` (backend) y ajustar servicios/repositorios.

- [ ] 6. Pasajes (frontend): implementar buscador, listado, selección de asientos, compra, boleto/QR descargable, mis viajes y estado con timeline.

- [ ] 7. Contacto: implementar/ajustar formulario, datos de contacto, Google Maps embebido y FAQ.
- [ ] 8. Validaciones + mensajes success/error + loading (JS) en cada módulo.
- [ ] 9. Pruebas: ejecutar app y verificar flujos cliente end-to-end.
