# TODO - Pasajes: compra (N pasajes, precio fijo, asientos únicos)

## Paso 1: Precio fijo

- [x] Quitar campo editable de precio del formulario de compra (mostrar solo resumen)
- [x] Ajustar JS para no enviar `precio` al backend

## Paso 2: UI para N pasajes

- [ ] En `cliente-buscar.html`: crear UI dinámica para N pasajeros (nombre/dni) y selección de asientos por pasajero
- [ ] En `cliente-buscar.js`: permitir seleccionar múltiples asientos (exactamente N) y validar

## Paso 3: Endpoint backend para reservar múltiples

- [ ] Crear DTO/params en `PasajesApiController` para reservar lista de pasajeros-asientos
- [ ] En `ReservaService` + `ReservaServiceImpl`: crear N reservas con precio fijo S/12
- [ ] Validar asientos ocupados en backend antes de guardar (sin duplicados)

## Paso 4: Límite miniván (max 15)

- [ ] Ajustar render de grilla: si tipoBus == MINIVAN entonces máximo 15

## Paso 5: “Mis viajes” (solo lectura, mostrar origen/destino)

- [ ] Asegurar que no existan acciones de editar/eliminar en vista de cliente
- [ ] Confirmar que se muestra `origen` y `destino`

## Paso 6: Compilar y pruebas

- [ ] Ejecutar `mvn test` / compilar
- [ ] Probar manual: N=1, N=2, N=5 y asientos ocupados
