package proyecto.nuevaases.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class ScheduledMaintenanceTasks {

    @Scheduled(cron = "0 0 3 * * ?")
    public void limpiarLogsAntiguos() {
        log.info("=== INICIO: Limpieza de logs antiguos (diaria 3:00 AM) ===");

        File logsDir = new File("logs");
        if (!logsDir.exists() || !logsDir.isDirectory()) {
            log.warn("La carpeta 'logs/' no existe. Se omite la limpieza.");
            return;
        }

        File[] logFiles = logsDir.listFiles((dir, name) -> name.endsWith(".log") || name.endsWith(".log.gz"));
        if (logFiles == null || logFiles.length == 0) {
            log.info("No se encontraron archivos de log para limpiar.");
            return;
        }

        Instant cutoff = Instant.now().minus(Duration.ofDays(15));
        int eliminados = 0;
        int errores = 0;

        for (File logFile : logFiles) {
            try {
                Path path = logFile.toPath();
                BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                Instant lastModified = attrs.lastModifiedTime().toInstant();

                if (lastModified.isBefore(cutoff)) {
                    boolean deleted = Files.deleteIfExists(path);
                    if (deleted) {
                        log.info("Log eliminado: {} (ultima modificacion: {})",
                                logFile.getName(), attrs.lastModifiedTime());
                        eliminados++;
                    }
                }
            } catch (IOException e) {
                log.error("Error al eliminar archivo de log: {} - {}", logFile.getName(), e.getMessage());
                errores++;
            }
        }

        log.info("=== FIN: Limpieza de logs. Eliminados: {}, Errores: {} ===", eliminados, errores);
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void heartbeat() {
        log.info("Heartbeat - Scheduler activo. La aplicacion Nueva Ases Express continua en ejecucion.");
    }
}
