package proyecto.nuevaases.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CreationHelper helper = workbook.getCreationHelper();

            // Estilo encabezado: solo negrita
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }, null));
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 70, (byte) 90, (byte) 120 }, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Estilo número entero
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(helper.createDataFormat().getFormat("#,##0"));

            // Estilo moneda
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(helper.createDataFormat().getFormat("#,##0.00"));

            // Estilo fecha
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(helper.createDataFormat().getFormat("dd/MM/yyyy"));

            // --- Hoja 1: Resumen ---
            Sheet resumen = workbook.createSheet("Resumen");
            int r = 0;

            // Título hoja Resumen
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }, null));

            XSSFCellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 13, (byte) 27, (byte) 42 }, null));
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row titleRow = resumen.createRow(r++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de metricas del negocio");
            titleCell.setCellStyle(titleStyle);
            resumen.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
            resumen.createRow(r++);

            Row header = resumen.createRow(r++);
            Cell h0 = header.createCell(0);
            h0.setCellValue("Metrica");
            h0.setCellStyle(headerStyle);
            Cell h1 = header.createCell(1);
            h1.setCellValue("Valor");
            h1.setCellStyle(headerStyle);

            Row row;
            row = resumen.createRow(r++);
            row.createCell(0).setCellValue("Total pasajes");
            row.createCell(1).setCellValue(totalPasajes);
            row.getCell(1).setCellStyle(numberStyle);

            row = resumen.createRow(r++);
            row.createCell(0).setCellValue("Ingresos historicos");
            row.createCell(1).setCellValue(ingresosHistoricos);
            row.getCell(1).setCellStyle(currencyStyle);

            row = resumen.createRow(r++);
            row.createCell(0).setCellValue("Total encomiendas");
            row.createCell(1).setCellValue(totalEncomiendas);
            row.getCell(1).setCellStyle(numberStyle);

            row = resumen.createRow(r++);
            row.createCell(0).setCellValue("Promedio diario pasajes");
            row.createCell(1).setCellValue(promedioDiario);
            row.getCell(1).setCellStyle(currencyStyle);

            resumen.setColumnWidth(0, 8000);
            resumen.setColumnWidth(1, 4000);

            // --- Hoja 2: Pasajes por fecha ---
            Sheet s2 = workbook.createSheet("Pasajes por fecha");
            int r2 = 0;

            Row title2 = s2.createRow(r2++);
            Cell tc2 = title2.createCell(0);
            tc2.setCellValue("Ventas de pasajes por fecha");
            tc2.setCellStyle(titleStyle);
            s2.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
            s2.createRow(r2++);

            Row h2 = s2.createRow(r2++);
            Cell h2c0 = h2.createCell(0);
            h2c0.setCellValue("Fecha");
            h2c0.setCellStyle(headerStyle);
            Cell h2c1 = h2.createCell(1);
            h2c1.setCellValue("Pasajes vendidos");
            h2c1.setCellStyle(headerStyle);
            Cell h2c2 = h2.createCell(2);
            h2c2.setCellValue("Total recaudado");
            h2c2.setCellStyle(headerStyle);

            for (Object[] rowData : pasajesPorFecha) {
                Row rr = s2.createRow(r2++);
                Cell dateCell = rr.createCell(0);
                if (rowData[0] instanceof java.time.LocalDate) {
                    dateCell.setCellValue((java.time.LocalDate) rowData[0]);
                    dateCell.setCellStyle(dateStyle);
                } else {
                    dateCell.setCellValue(rowData[0] != null ? rowData[0].toString() : "");
                }
                Cell countCell = rr.createCell(1);
                countCell.setCellValue(rowData[1] != null ? ((Number) rowData[1]).longValue() : 0);
                countCell.setCellStyle(numberStyle);
                Cell totalCell = rr.createCell(2);
                totalCell.setCellValue(rowData[2] != null ? ((Number) rowData[2]).doubleValue() : 0.0);
                totalCell.setCellStyle(currencyStyle);
            }

            s2.autoSizeColumn(0);
            s2.autoSizeColumn(1);
            s2.autoSizeColumn(2);

            // --- Hoja 3: Encomiendas por estado ---
            Sheet s3 = workbook.createSheet("Encomiendas por estado");
            int r3 = 0;

            Row title3 = s3.createRow(r3++);
            Cell tc3 = title3.createCell(0);
            tc3.setCellValue("Resumen de encomiendas");
            tc3.setCellStyle(titleStyle);
            s3.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
            s3.createRow(r3++);

            Row h3 = s3.createRow(r3++);
            Cell h3c0 = h3.createCell(0);
            h3c0.setCellValue("Estado");
            h3c0.setCellStyle(headerStyle);
            Cell h3c1 = h3.createCell(1);
            h3c1.setCellValue("Cantidad");
            h3c1.setCellStyle(headerStyle);

            for (Object[] rowData : encomiendasPorEstado) {
                Row rr = s3.createRow(r3++);
                rr.createCell(0).setCellValue(rowData[0] != null ? rowData[0].toString() : "");
                Cell cantCell = rr.createCell(1);
                cantCell.setCellValue(rowData[1] != null ? ((Number) rowData[1]).longValue() : 0);
                cantCell.setCellStyle(numberStyle);
            }

            s3.autoSizeColumn(0);
            s3.autoSizeColumn(1);

            workbook.write(out);
            byte[] bytes = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "reportes.xlsx");
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok().headers(headers).body(bytes);
        }
    }
}
