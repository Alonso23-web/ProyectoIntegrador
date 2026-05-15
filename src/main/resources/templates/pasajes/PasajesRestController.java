package proyecto.nuevaases.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.model.Reserva;
import proyecto.nuevaases.model.Viaje;
import proyecto.nuevaases.repository.ReservaRepository;
import proyecto.nuevaases.repository.ViajeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/pasajes")
public class PasajesRestController {

    @Autowired private ViajeRepository viajeRepository;
    @Autowired private ReservaRepository reservaRepository;

    @GetMapping("/buscar")
    public List<Viaje> buscar(@RequestParam String origen, @RequestParam String destino, @RequestParam String fecha, @RequestParam(defaultValue = "1") int cantidadPasajeros) {
        return viajeRepository.findByOrigenAndDestinoAndFecha(origen, destino, LocalDate.parse(fecha));
    }

    @GetMapping("/{viajeId}/ocupacion")
    public List<Integer> getAsientosOcupados(@PathVariable Long viajeId) {
        return reservaRepository.findAsientosByViajeId(viajeId);
    }

    @PostMapping("/{viajeId}/reservar")
    public Reserva reservar(
            @PathVariable Long viajeId,
            @RequestParam Integer asiento,
            @RequestParam String nombrePasajero,
            @RequestParam String dniPasajero,
            @RequestParam Double precio) {
        
        Viaje v = viajeRepository.findById(viajeId).orElseThrow();
        
        // Opcional: Validar que el precio recibido coincida con el del viaje

        Reserva reserva = new Reserva();
        reserva.setViaje(v);
        reserva.setAsiento(asiento);
        reserva.setNombrePasajero(nombrePasajero);
        reserva.setDniPasajero(dniPasajero);
        reserva.setEstado("RESERVADO");
        reserva.setCodigoBoleto("NAE-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        
        return reservaRepository.save(reserva);
    }

    @GetMapping("/mis")
    public List<Reserva> getMisViajes() {
        // En un sistema real usaríamos el usuario autenticado. 
        // Aquí devolvemos datos para el DNI por defecto del cliente de prueba.
        return reservaRepository.findByDniPasajero("12345678");
    }

    @GetMapping("/estado/{codigoBoleto}")
    public ResponseEntity<Map<String, String>> getEstadoViaje(@PathVariable String codigoBoleto) {
        Optional<Reserva> reservaOpt = reservaRepository.findByCodigoBoleto(codigoBoleto);
        if (reservaOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        Reserva r = reservaOpt.get();
        Viaje v = r.getViaje();
        String estado = "Programado";
        String detalles = "Su viaje está programado.";

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if (v.getFecha().isBefore(hoy)) {
            estado = "Finalizado";
            detalles = "El viaje ha concluido.";
        } else if (v.getFecha().isEqual(hoy)) {
            if (ahora.isAfter(v.getHoraSalida())) {
                estado = "En ruta";
                detalles = "El bus se encuentra actualmente en trayecto.";
            } else {
                detalles = "El bus sale hoy a las " + v.getHoraSalida();
            }
        } else {
            detalles = "Viaje programado para el " + v.getFecha();
        }

        return ResponseEntity.ok(Map.of("estado", estado, "detalles", detalles));
    }
}