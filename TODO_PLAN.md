# Plan - Corrigir perfil desalineado

## Información analizada

- `perfil.html` usa `th:replace="~{layout :: navbar}"` y luego carga su propio `container`.
- `layout.html` incluye el navbar y footer como fragmentos, y en el fragmento `navbar` el logo (imagen) tiene estilos que controlan tamaño/posición.
- En `layout.html` hay CSS en línea para el navbar (`.navbar-custom`) y para `.navbar-brand img`.

## Plan (cambio de código)

1. Normalizar el layout del fragment de navbar para que no incluya un HTML completo (evitar que el navegador o thymeleaf inserten estructura/estilos duplicados que rompen el flujo).
   - En `layout.html`, separar/estandarizar el fragmento `navbar` para que sea solo el `<nav>`.
2. Corregir en `perfil.html` un posible conflicto: no depender de estilos internos que “pisan” layout global.
   - Reemplazar la estructura de `perfil.html` para usar un `main` con `container` y `pt-*` consistente.
3. Asegurar que el logo no se desplace al “quedar arriba en el medio”:
   - Ajustar en `layout.html` `.navbar-custom .navbar-brand img` y agregar `display:block`, `object-fit:contain`, y asegurar alineación vertical con `align-items:center`.
4. Agregar una clase `profile-page` en `perfil.html` con márgenes/padding adecuados y sin `min-height` que choque con footer.
5. Probar visualmente: revisar `perfil` en laptop (responsive) y confirmar que el navbar y el contenido quedan alineados.

## Archivos dependientes

- `src/main/resources/templates/layout.html`
- `src/main/resources/templates/perfil.html`

## Followup

- Recargar la página `/perfil` en el navegador y revisar escritorio/laptop.
