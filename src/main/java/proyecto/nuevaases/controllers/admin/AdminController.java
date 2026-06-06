package proyecto.nuevaases.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.services.IUsuarioService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUsuarioService usuarioService;
    private final ReservaRepository reservaRepository;
    private final EncomiendaRepository encomiendaRepository;

    // ==================== USUARIOS ====================

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodosUsuarios());
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/estado/{id}")
    public String cambiarEstadoUsuario(
            @PathVariable Long id,
            @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {
        try {
            var usuario = usuarioService.cambiarEstadoActivo(id, activo);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Estado de " + usuario.getNombreCompleto() + " actualizado a " + (activo ? "ACTIVO" : "BLOQUEADO"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cambiar estado: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    // ==================== REPORTES ====================

    @GetMapping("/reportes")
    public String reportes(Model model) {
        // Métricas clave
        long totalPasajes = reservaRepository.count();
        double ingresosHistoricos = reservaRepository.sumAllPrecio();
        long totalEncomiendas = encomiendaRepository.count();
        long diasConViajes = reservaRepository.countDistinctViajeFecha();
        double promedioDiario = diasConViajes > 0 ? (double) totalPasajes / diasConViajes : 0;

        model.addAttribute("totalPasajes", totalPasajes);
        model.addAttribute("ingresosHistoricos", ingresosHistoricos);
        model.addAttribute("totalEncomiendas", totalEncomiendas);
        model.addAttribute("promedioDiario", Math.round(promedioDiario * 100.0) / 100.0);

        // Pasajes agrupados por fecha
        model.addAttribute("pasajesPorFecha", reservaRepository.countAndSumByFecha());

        // Encomiendas por estado
        model.addAttribute("encomiendasPorEstado", encomiendaRepository.countGroupByEstado());

        return "admin/reportes";
    }

    @GetMapping(value = "/reportes/generar", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> generarReporte() throws IOException {
        long totalPasajes = reservaRepository.count();
        double ingresosHistoricos = reservaRepository.sumAllPrecio();
        long totalEncomiendas = encomiendaRepository.count();
        long diasConViajes = reservaRepository.countDistinctViajeFecha();
        double promedioDiario = diasConViajes > 0 ? (double) totalPasajes / diasConViajes : 0;

        List<Object[]> pasajesPorFecha = reservaRepository.countAndSumByFecha();
        List<Object[]> encomiendasPorEstado = encomiendaRepository.countGroupByEstado();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet resumen = workbook.createSheet("Resumen");
            int r = 0;
            Row header = resumen.createRow(r++);
            header.createCell(0).setCellValue("Métrica");
            header.createCell(1).setCellValue("Valor");

            Row row = resumen.createRow(r++);
            row.createCell(0).setCellValue("Total pasajes");
            row.createCell(1).setCellValue(totalPasajes);

            row = resumen.createRow(r++);
            row.createCell(0).setCellValue("Ingresos históricos");
            row.createCell(1).setCellValue(ingresosHistoricos);

            row = resumen.createRow(r++);
            row.createCell(0).setCellValue("Total encomiendas");
            row.createCell(1).setCellValue(totalEncomiendas);

            row = resumen.createRow(r++);
            row.createCell(0).setCellValue("Promedio diario pasajes");
            row.createCell(1).setCellValue(promedioDiario);

            Sheet s2 = workbook.createSheet("Pasajes por fecha");
            int r2 = 0;
            Row h2 = s2.createRow(r2++);
            h2.createCell(0).setCellValue("Fecha");
            h2.createCell(1).setCellValue("Pasajes vendidos");
            h2.createCell(2).setCellValue("Total recaudado");

            for (Object[] rowData : pasajesPorFecha) {
                Row rr = s2.createRow(r2++);
                rr.createCell(0).setCellValue(rowData[0] != null ? rowData[0].toString() : "");
                rr.createCell(1).setCellValue(rowData[1] != null ? ((Number) rowData[1]).longValue() : 0);
                rr.createCell(2).setCellValue(rowData[2] != null ? ((Number) rowData[2]).doubleValue() : 0.0);
            }

            Sheet s3 = workbook.createSheet("Encomiendas por estado");
            int r3 = 0;
            Row h3 = s3.createRow(r3++);
            h3.createCell(0).setCellValue("Estado");
            h3.createCell(1).setCellValue("Cantidad");

            for (Object[] rowData : encomiendasPorEstado) {
                Row rr = s3.createRow(r3++);
                rr.createCell(0).setCellValue(rowData[0] != null ? rowData[0].toString() : "");
                rr.createCell(1).setCellValue(rowData[1] != null ? ((Number) rowData[1]).longValue() : 0);
            }

            for (Sheet sh : Arrays.asList(resumen, s2, s3)) {
                for (int c = 0; c < 6; c++) {
                    sh.autoSizeColumn(c);
                }
            }

            workbook.write(out);
            byte[] bytes = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "reportes.xlsx");
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok().headers(headers).body(bytes);
        }
    }
}
