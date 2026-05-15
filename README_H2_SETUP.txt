Proyecto: nueva-ases-express

Motivo: La app se cerraba al arrancar por un error de permisos en MySQL (root@localhost) al ejecutar DDL.

Solución aplicada (desarrollo): usar H2 en memoria.

1) Archivo modificado:
   - src/main/resources/application.properties

   Config base esperada:
   - spring.datasource.url=jdbc:h2:mem:nuevaases_db;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE
   - spring.datasource.driver-class-name=org.h2.Driver
   - spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

2) Recomendado al correr luego de cambios:
   - mvn clean spring-boot:run

Notas:
- Aparecen warnings por favicon.ico (no es un error crítico).
- H2 puede mostrar warning de DB_CLOSE_ON_EXIT; si querés, agregá ;DB_CLOSE_ON_EXIT=FALSE a la URL.

