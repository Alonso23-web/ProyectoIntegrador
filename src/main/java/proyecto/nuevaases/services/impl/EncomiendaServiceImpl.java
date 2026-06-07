package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.EncomiendaDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.services.IEncomiendaService;
import proyecto.nuevaases.services.IHistorialEncomiendaService;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EncomiendaServiceImpl implements IEncomiendaService {

    private final EncomiendaRepository encomiendaRepository;
    private final IHistorialEncomiendaService historialEncomiendaService;

    @Override
    public List<EncomiendaDTO> listarTodos() {
        return encomiendaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EncomiendaDTO buscarPorId(Long id) {
        Encomienda entity = encomiendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encomienda no encontrada con ID: " + id));
        return convertToDTO(entity);
    }

    @Override
    public EncomiendaDTO guardar(EncomiendaDTO dto) {
        // Detectar cambio de estado para registrar historial
        String estadoAnterior = null;
        if (dto.getId() != null) {
            Optional<Encomienda> existente = encomiendaRepository.findById(dto.getId());
            if (existente.isPresent()) {
                estadoAnterior = existente.get().getEstado();
            }
        }

        Encomienda entity = convertToEntity(dto);
        boolean esNuevo = (entity.getCodigoRastreo() == null || entity.getCodigoRastreo().isEmpty());
        if (esNuevo) {
            entity.setCodigoRastreo(generarCodigoRastreo());
        }

        Encomienda guardada = encomiendaRepository.save(entity);

        // Registrar historial si el estado cambió
        if (estadoAnterior != null && !estadoAnterior.equals(guardada.getEstado())) {
            String email = dto.getCreadoPorEmail() != null ? dto.getCreadoPorEmail() : "sistema";
            historialEncomiendaService.registrarCambio(
                    guardada.getId(),
                    estadoAnterior,
                    guardada.getEstado(),
                    email,
                    "Cambio de estado: " + estadoAnterior + " → " + guardada.getEstado()
            );
        }

        return convertToDTO(guardada);
    }

    @Override
    public void eliminar(Long id) {
        if (!encomiendaRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar la encomienda con ID: " + id);
        }
        encomiendaRepository.deleteById(id);
    }

    @Override
    public Optional<EncomiendaDTO> buscarPorCodigoRastreo(String codigoRastreo) {
        return encomiendaRepository.findByCodigoRastreo(codigoRastreo).map(this::convertToDTO);
    }

    @Override
    public List<EncomiendaDTO> buscarPorDni(String dni, String estado) {
        if (estado != null && !estado.isEmpty()) {
            return encomiendaRepository.findByDniRemitenteOrDniDestinatarioAndEstado(dni, dni, estado)
                    .stream().map(this::convertToDTO).collect(Collectors.toList());
        }
        return encomiendaRepository.findByDniRemitenteOrDniDestinatario(dni, dni)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<EncomiendaDTO> buscarPorCreadoPorEmail(String creadoPorEmail) {
        return encomiendaRepository.findByCreadoPorEmail(creadoPorEmail)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private static final double CARGO_MANEJO = 1.50;
    private static final double CARGO_PESO_EXTRA = 1.50;
    private static final double PRECIO_POR_KG = 1.50; // precio unitario por kg (para vista de 'precio por peso')
    private static final double PESO_NORMAL_INCLUIDO = 10.0; // peso normal sin cargo extra

    @Override
    public double calcularPrecio(String origen, String destino, double peso) {
        double tarifaBase = obtenerTarifaBase(origen, destino);
        double cargoPeso = peso > PESO_NORMAL_INCLUIDO ? (peso - PESO_NORMAL_INCLUIDO) * CARGO_PESO_EXTRA : 0;
        double total = tarifaBase + cargoPeso + CARGO_MANEJO;
        return Math.round(total * 100.0) / 100.0;
    }

    @Override
    public double calcularPrecioPorPeso(double peso) {
        double total = peso * PRECIO_POR_KG;
        return Math.round(total * 100.0) / 100.0;
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

    private String generarCodigoRastreo() {
        return "NAE-2024-" + (new Random().nextInt(9000) + 1000);
    }

    private EncomiendaDTO convertToDTO(Encomienda entity) {
        return EncomiendaDTO.builder()
                .id(entity.getId()).codigoRastreo(entity.getCodigoRastreo())
                .remitente(entity.getRemitente()).dniRemitente(entity.getDniRemitente())
                .destinatario(entity.getDestinatario()).dniDestinatario(entity.getDniDestinatario())
                .origen(entity.getOrigen()).destino(entity.getDestino())
                .descripcion(entity.getDescripcion()).peso(entity.getPeso())
                .precio(entity.getPrecio()).fechaEnvio(entity.getFechaEnvio())
                .fechaEstimadaEntrega(entity.getFechaEstimadaEntrega()).estado(entity.getEstado())
                .observaciones(entity.getObservaciones())
                .creadoPorEmail(entity.getCreadoPorEmail()).build();
    }

    private Encomienda convertToEntity(EncomiendaDTO dto) {
        return Encomienda.builder()
                .id(dto.getId()).codigoRastreo(dto.getCodigoRastreo())
                .remitente(dto.getRemitente()).dniRemitente(dto.getDniRemitente())
                .destinatario(dto.getDestinatario()).dniDestinatario(dto.getDniDestinatario())
                .origen(dto.getOrigen()).destino(dto.getDestino())
                .descripcion(dto.getDescripcion()).peso(dto.getPeso())
                .precio(dto.getPrecio()).fechaEnvio(dto.getFechaEnvio())
                .fechaEstimadaEntrega(dto.getFechaEstimadaEntrega()).estado(dto.getEstado())
                .observaciones(dto.getObservaciones())
                .creadoPorEmail(dto.getCreadoPorEmail()).build();
    }
}