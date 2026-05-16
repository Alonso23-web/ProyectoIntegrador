# Refactor: limpiar Java dentro de templates (pasajes)

## Estado: CONFIRMADO PARA ELIMINACIÓN

- **LISTO PARA BORRADO FÍSICO**: Se identificaron 9 archivos `.java` incorrectamente ubicados en:
  - `src/main/resources/templates/pasajes/`
- Esos archivos deben eliminarse porque `templates/` solo debe contener `.html` (y estáticos).

## Objetivo

- La lógica ya ha sido integrada en `src/main/java/proyecto/nuevaases/`.

## Archivos a eliminar (lista detectada por búsqueda)

- [x] `Viaje.java`
- [x] `Reserva.java`
- [x] `Encomienda.java`
- [x] `ViajeRepository.java`
- [x] `ReservaRepository.java`
- [x] `EncomiendaRepository.java`
- [x] `PasajesController.java`
- [x] `PasajesRestController.java`
- [x] `EncomiendasRestController.java`

## Criterio de seguridad

- **VERIFICACIÓN**: Se ha confirmado que `Reserva.java`, `Viaje.java`, `PasajesApiController`, etc., en `src/main/java` contienen la lógica necesaria (incluyendo campos de pasajero y estado de viaje).
- **ACCIÓN**: Puedes borrar manualmente estos archivos en IntelliJ sin miedo; la aplicación no los usa desde esa ubicación.
