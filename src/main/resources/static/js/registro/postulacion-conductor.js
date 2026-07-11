document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("formPostulacion");

  const nombreCompleto = document.getElementById("nombreCompleto");
  const dni = document.getElementById("dni");
  const email = document.getElementById("email");
  const telefono = document.getElementById("telefono");
  const password = document.getElementById("password");
  const confirmarPassword = document.getElementById("confirmarPassword");
  const numeroLicencia = document.getElementById("numeroLicencia");
  const aniosExperiencia = document.getElementById("aniosExperiencia");
  const tipoVehiculo = document.getElementById("tipoVehiculo");
  const documento = document.getElementById("documento");

  if (!form) return;

  const nombreRegex = /^[A-Za-zÁÉÍÓÚáéíóúÑñ]+(?:\s+[A-Za-zÁÉÍÓÚáéíóúÑñ]+)+$/;
  const dniRegex = /^\d{8}$/;
  const telefonoRegex = /^9\d{8}$/;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const licenciaPeruRegex = /^T\d{8}$/;

  function setEstado(input, valido, mensaje) {
    if (!input) return;

    const error = document.getElementById(input.id + "Error");
    input.classList.remove("is-valid", "is-invalid");

    if (valido) {
      input.classList.add("is-valid");
      if (error) error.textContent = "";
    } else {
      input.classList.add("is-invalid");
      if (error) error.textContent = mensaje;
    }
  }

  function validarNombre() {
    const valor = nombreCompleto.value.trim();
    const valido = nombreRegex.test(valor);
    setEstado(nombreCompleto, valido, "Ingresa nombres y apellidos completos.");
    return valido;
  }

  function validarDni() {
    dni.value = dni.value.replace(/\D/g, "").slice(0, 8);
    const valido = dniRegex.test(dni.value);
    setEstado(dni, valido, "El DNI debe tener exactamente 8 dígitos.");
    return valido;
  }

  function validarEmail() {
    const valido = emailRegex.test(email.value.trim());
    setEstado(email, valido, "Ingresa un correo válido. Ejemplo: usuario@gmail.com.");
    return valido;
  }

  function validarTelefono() {
    telefono.value = telefono.value.replace(/\D/g, "").slice(0, 9);
    const valido = telefonoRegex.test(telefono.value);
    setEstado(telefono, valido, "El teléfono debe tener 9 dígitos y empezar con 9.");
    return valido;
  }

  function validarPassword() {
    const valor = password.value;
    const valido = valor.length >= 8 && /[A-Z]/.test(valor) && /[a-z]/.test(valor) && /\d/.test(valor);
    setEstado(password, valido, "Mínimo 8 caracteres, una mayúscula, una minúscula y un número.");
    return valido;
  }

  function validarConfirmarPassword() {
    const valido = confirmarPassword.value.length > 0 && confirmarPassword.value === password.value;
    setEstado(confirmarPassword, valido, "Las contraseñas no coinciden.");
    return valido;
  }

  function validarLicencia() {
    if (!numeroLicencia) return true;
    numeroLicencia.value = numeroLicencia.value.toUpperCase().replace(/\s/g, "");
    const valido = licenciaPeruRegex.test(numeroLicencia.value);
    setEstado(numeroLicencia, valido, "Formato inválido. Usa T + 8 dígitos. Ej: T12345678.");
    return valido;
  }

  function validarTipoVehiculo() {
    if (!tipoVehiculo) return true;
    const valido = tipoVehiculo.value !== "";
    setEstado(tipoVehiculo, valido, "Selecciona el tipo de vehículo que manejas.");
    return valido;
  }

  function validarDocumento() {
    if (!documento) return true;
    const archivo = documento.files[0];
    if (!archivo) {
      setEstado(documento, false, "Debes subir tu documento o licencia.");
      return false;
    }
    const tiposPermitidos = ["application/pdf", "image/jpeg", "image/png"];
    const maxSize = 5 * 1024 * 1024;
    if (!tiposPermitidos.includes(archivo.type)) {
      setEstado(documento, false, "Solo se permite PDF, JPG o PNG.");
      return false;
    }
    if (archivo.size > maxSize) {
      setEstado(documento, false, "El archivo no debe superar los 5MB.");
      return false;
    }
    setEstado(documento, true, "");
    return true;
  }

  function validarExperiencia() {
    if (!aniosExperiencia) return true;
    const valor = parseInt(aniosExperiencia.value);
    const valido = !isNaN(valor) && valor >= 1 && valor <= 60;
    setEstado(aniosExperiencia, valido, "Ingresa años de experiencia entre 1 y 60.");
    return valido;
  }

  // ==================== EVENT LISTENERS ====================

  nombreCompleto.addEventListener("input", validarNombre);
  dni.addEventListener("input", validarDni);
  email.addEventListener("input", validarEmail);
  telefono.addEventListener("input", validarTelefono);
  password.addEventListener("input", () => {
    validarPassword();
    validarConfirmarPassword();
  });
  confirmarPassword.addEventListener("input", validarConfirmarPassword);
  tipoVehiculo.addEventListener("change", validarTipoVehiculo);
  aniosExperiencia.addEventListener("input", validarExperiencia);
  documento.addEventListener("change", validarDocumento);

  if (numeroLicencia) {
    numeroLicencia.addEventListener("input", validarLicencia);
  }

  form.addEventListener("submit", (e) => {
    const rNombre = validarNombre();
    const rDni = validarDni();
    const rEmail = validarEmail();
    const rTelefono = validarTelefono();
    const rPassword = validarPassword();
    const rConfirmarPassword = validarConfirmarPassword();
    const rLicencia = validarLicencia();
    const rExperiencia = validarExperiencia();
    const rTipoVehiculo = validarTipoVehiculo();
    const rDocumento = validarDocumento();

    const todoValido = rNombre && rDni && rEmail && rTelefono && rPassword &&
      rConfirmarPassword && rLicencia && rExperiencia && rTipoVehiculo && rDocumento;

    if (!todoValido) {
      console.log("Postulación - validaciones fallidas", {
        nombre: rNombre,
        dni: rDni,
        email: rEmail,
        telefono: rTelefono,
        password: rPassword,
        confirmarPassword: rConfirmarPassword,
        licencia: rLicencia,
        experiencia: rExperiencia,
        tipoVehiculo: rTipoVehiculo,
        documento: rDocumento
      });
      e.preventDefault();
    }
  });
});
