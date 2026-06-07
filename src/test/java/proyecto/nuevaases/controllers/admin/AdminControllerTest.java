package proyecto.nuevaases.controllers.admin;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.context.support.WithMockUser;

import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.services.IUsuarioService;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ReservaRepository reservaRepository;

    @MockBean
    EncomiendaRepository encomiendaRepository;

    @MockBean
    IUsuarioService usuarioService;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void generarReporte_returnsXlsx() throws Exception {
        when(reservaRepository.count()).thenReturn(10L);
        when(reservaRepository.sumAllPrecio()).thenReturn(1234.56);
        when(reservaRepository.countDistinctViajeFecha()).thenReturn(5L);
        java.util.List<Object[]> pasajes = new java.util.ArrayList<>();
        pasajes.add(new Object[] { LocalDate.of(2026, 1, 1), 2L, 100.0 });
        when(reservaRepository.countAndSumByFecha()).thenReturn(pasajes);

        when(encomiendaRepository.count()).thenReturn(3L);
        java.util.List<Object[]> encomiendas = new java.util.ArrayList<>();
        encomiendas.add(new Object[] { "ENTREGADA", 3L });
        when(encomiendaRepository.countGroupByEstado()).thenReturn(encomiendas);

        MvcResult result = mockMvc.perform(get("/admin/reportes/generar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 10, "El cuerpo de la respuesta debe contener bytes del xlsx");

        try (ByteArrayInputStream in = new ByteArrayInputStream(content);
                XSSFWorkbook wb = new XSSFWorkbook(in)) {
            assertTrue(wb.getNumberOfSheets() >= 1);
            assertNotNull(wb.getSheet("Resumen"));
        }
    }
}
