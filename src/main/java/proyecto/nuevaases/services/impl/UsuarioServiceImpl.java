package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.UsuarioDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Usuario;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.services.IUsuarioService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UsuarioDTO> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email).map(this::convertToDTO);
    }

    @Override
    public UsuarioDTO registrar(UsuarioDTO dto, String password) {
        Usuario entity = convertToEntity(dto);
        entity.setPassword(passwordEncoder.encode(password != null ? password : ""));
        entity.setActivo(true);
        return convertToDTO(usuarioRepository.save(entity));
    }

    @Override
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public boolean existeDni(String dni) {
        return usuarioRepository.existsByDni(dni);
    }

    @Override
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioDTO convertToDTO(Usuario entity) {
        return UsuarioDTO.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .nombreCompleto(entity.getNombreCompleto())
                .dni(entity.getDni())
                .telefono(entity.getTelefono())
                .rol(entity.getRol())
                .activo(entity.isActivo())
                .numeroLicencia(entity.getNumeroLicencia())
                .aniosExperiencia(entity.getAniosExperiencia())
                .tipoVehiculo(entity.getTipoVehiculo())
                .estadoPostulacion(entity.getEstadoPostulacion())
                .documentoUrl(entity.getDocumentoUrl())
                .build();
    }

    private Usuario convertToEntity(UsuarioDTO dto) {
        return Usuario.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .password("") // La contraseña se asigna en registrar()
                .nombreCompleto(dto.getNombreCompleto())
                .dni(dto.getDni())
                .telefono(dto.getTelefono())
                .rol(dto.getRol())
                .activo(dto.isActivo())
                .numeroLicencia(dto.getNumeroLicencia())
                .aniosExperiencia(dto.getAniosExperiencia())
                .tipoVehiculo(dto.getTipoVehiculo())
                .estadoPostulacion(dto.getEstadoPostulacion())
                .documentoUrl(dto.getDocumentoUrl())
                .build();
    }
}
