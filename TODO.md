# TODO - Fix runtime cierre por error MySQL columna 'tipo'

## Plan (resumen)

- El build compila OK; el cierre ocurre por runtime.
- Error: `Field 'tipo' doesn't have a default value`.
- Hipótesis: mapeo JPA de `Vehiculo.tipo` no coincide con el nombre real de columna en MySQL.

## Pasos

1. Identificar qué sentencia/tabla falla (probablemente `vehiculos`).
2. Corregir el mapeo en `Vehiculo.java` para que `tipo` apunte a la columna real (probablemente `tipo` en vez de `tipo_propiedad`).
3. Verificar que `DataInitializer` ya inserta `tipo` correctamente.
4. Ejecutar app nuevamente y confirmar que ya no aparece el error.
