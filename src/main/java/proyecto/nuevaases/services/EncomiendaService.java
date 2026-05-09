package proyecto.nuevaases.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.repositories.EncomiendaRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EncomiendaService {

    private final EncomiendaRepository encomiendaRepository;

    public List<Encomienda> listarTodos() {
        return encomiendaRepository.findAll();
    }

    public Optional<Encomienda> obtenerPorId(Long id) {
        return encomiendaRepository.findById(id);
    }

    public Optional<Encomienda> buscarPorCodigoRastreo(String codigoRastreo) {
        return encomiendaRepository.findByCodigoRastreo(codigoRastreo);
    }

    public Encomienda guardar(Encomienda encomienda) {
    return encomiendaRepository.save(encomienda);
}

    public void eliminar(Long id) {
        encomiendaRepository.deleteById(id);
    }

    public List<Encomienda> buscarPorDni(String dni, String estado) {
        if (estado != null && !estado.isEmpty()) {
            return encomiendaRepository.findByDniRemitenteOrDniDestinatarioAndEstado(dni, dni, estado);
        }
        return encomiendaRepository.findByDniRemitenteOrDniDestinatario(dni, dni);
    }

    public List<Encomienda> buscarPorCreadoPorEmail(String creadoPorEmail) {
        return encomiendaRepository.findByCreadoPorEmail(creadoPorEmail);
    }

    public double calcularPrecio(String origen, String destino, double peso) {
        // Tarifa base según ruta
        double tarifaBase = obtenerTarifaBase(origen, destino);
        // S/ 1.50 por kg adicional sobre el primer kg
        double cargoPeso = peso > 1 ? (peso - 1) * 1.50 : 0;
        return Math.round((tarifaBase + cargoPeso) * 100.0) / 100.0;
    }

    private double obtenerTarifaBase(String origen, String destino) {
        String ruta = origen.toLowerCase() + "-" + destino.toLowerCase();
        return switch (ruta) {
            case "trujillo-chepén", "chepén-trujillo" -> 5.00;
            case "trujillo-pacasmayo", "pacasmayo-trujillo" -> 4.50;
            case "chepén-pacasmayo", "pacasmayo-chepén" -> 3.50;
            default -> 5.00;
        };
    }
}