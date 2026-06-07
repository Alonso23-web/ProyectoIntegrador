package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.VehiculoDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.models.enums.EstadoVehiculo;
import proyecto.nuevaases.models.enums.TipoVehiculo;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.services.IVehiculoService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehiculoServiceImpl implements IVehiculoService {

    private final VehiculoRepository vehiculoRepository;

    @Override
    public List<VehiculoDTO> listarTodos() {
        return vehiculoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VehiculoDTO buscarPorId(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));
        return convertToDTO(vehiculo);
    }

    @Override
    public VehiculoDTO guardar(VehiculoDTO dto) {
        Vehiculo vehiculo = convertToEntity(dto);
        return convertToDTO(vehiculoRepository.save(vehiculo));
    }

    @Override
    public void eliminar(Long id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar, ID no encontrado: " + id);
        }
        vehiculoRepository.deleteById(id);
    }

    private VehiculoDTO convertToDTO(Vehiculo entity) {
        return VehiculoDTO.builder()
                .id(entity.getId())
                .placa(entity.getPlaca())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .anio(entity.getAnio())
                .capacidad(entity.getCapacidad())
                .tipo(entity.getTipo().name())
                .estado(entity.getEstado().name())
                .precioPorDia(entity.getPrecioPorDia())
                .imagen(entity.getImagen())
                .descripcion(entity.getDescripcion())
                .build();
    }

    private Vehiculo convertToEntity(VehiculoDTO dto) {
        return Vehiculo.builder()
                .id(dto.getId())
                .placa(dto.getPlaca())
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .anio(dto.getAnio() != null ? dto.getAnio() : 0)
                .capacidad(dto.getCapacidad() != null ? dto.getCapacidad() : 0)
                .tipo(dto.getTipo() != null ? TipoVehiculo.valueOf(dto.getTipo()) : TipoVehiculo.BUS)
                .estado(dto.getEstado() != null ? EstadoVehiculo.valueOf(dto.getEstado()) : EstadoVehiculo.DISPONIBLE)
                .precioPorDia(dto.getPrecioPorDia() != null ? dto.getPrecioPorDia() : 0.0)
                .imagen(dto.getImagen())
                .descripcion(dto.getDescripcion())
                .tipoPropiedad("PROPIO")
                .build();
    }
}
