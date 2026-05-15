# TODO - Frontend y Backend Cliente (Pasajes/Encomiendas/Contacto)

## Pasajes (Cliente)

- [ ] Implementar compra real en `static/js/pasajes/cliente-buscar.js`: submit de `#compraForm` debe llamar `POST /api/pasajes/{viajeId}/reservar`.
- [ ] Integrar generación de QR en frontend (CDN `qrcode`) y mostrar:
  - [ ] Código de boleto
  - [ ] QR en `<img id="boleto-qr">`
- [ ] Implementar descarga del boleto (mínimo: descarga de imagen PNG del QR).
- [ ] Validaciones de campos (nombre, DNI, asiento seleccionado, precio/viajeId).

## Mis viajes / Estado de viaje (Cliente)

- [ ] Usar `GET /api/pasajes/mis` para obtener historial y próximos.
- [ ] Implementar “Estado de viaje” derivado por fecha/hora del viaje (Programado/En ruta/Finalizado).
- [ ] Conectar en UI desde el menú/Perfil sin crear páginas extra.

## Encomiendas (Cliente)

- [ ] Ajustar `encomiendas.html` para que el cálculo de costo sea 100% en frontend usando fórmula:
  - `Costo = TarifaBase + (peso * PrecioPorKg) + (FactorDistancia)`
- [ ] Mostrar dinámicamente el costo estimado sin recargar.
- [ ] Mostrar mensaje obligatorio: `El costo es referencial y será validado en oficina.`
- [ ] Mantener/armonizar rastreo con timeline.

## Contacto (Cliente)

- [ ] Crear/ajustar template `contacto.html` con formulario + datos + Google Maps embebido + FAQ.
- [ ] Asegurar controlador `ContactoController` para GET/POST y mensajes de éxito/error.
