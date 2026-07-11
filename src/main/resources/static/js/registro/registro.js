document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("formRegistro");

  const nombreCompleto = document.getElementById("nombreCompleto");
  const dni = document.getElementById("dni");
  const email = document.getElementById("email");
  const telefono = document.getElementById("telefono");
  const password = document.getElementById("password");
  const confirmarPassword = document.getElementById("confirmarPassword");

  if (!form) return;

  const nombreRegex =
    /^[A-Za-zÁÉÍÓÚáéíóúÑñ]+(?:\s+[A-Za-zÁÉÍÓÚáéíóúÑñ]+)+$/;

  const dniRegex = /^\d{8}$/;
  const telefonoRegex = /^9\d{8}$/;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

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

    setEstado(
      nombreCompleto,
      valido,
      "Ingresa nombres y apellidos completos. Ejemplo: Luis Alberto Pérez Gómez."
    );

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

    const valido =
      valor.length >= 8 &&
      /[A-Z]/.test(valor) &&
      /[a-z]/.test(valor) &&
      /\d/.test(valor);

    setEstado(
      password,
      valido,
      "Mínimo 8 caracteres, una mayúscula, una minúscula y un número."
    );

    return valido;
  }

  function validarConfirmarPassword() {
    const valido =
      confirmarPassword.value.length > 0 &&
      confirmarPassword.value === password.value;

    setEstado(confirmarPassword, valido, "Las contraseñas no coinciden.");

    return valido;
  }

  nombreCompleto.addEventListener("input", validarNombre);
  dni.addEventListener("input", validarDni);
  email.addEventListener("input", validarEmail);
  telefono.addEventListener("input", validarTelefono);
  password.addEventListener("input", () => {
    validarPassword();
    validarConfirmarPassword();
  });
  confirmarPassword.addEventListener("input", validarConfirmarPassword);

  form.addEventListener("submit", (e) => {
    const rNombre = validarNombre();
    const rDni = validarDni();
    const rEmail = validarEmail();
    const rTelefono = validarTelefono();
    const rPassword = validarPassword();
    const rConfirmarPassword = validarConfirmarPassword();

    const todoValido =
      rNombre &&
      rDni &&
      rEmail &&
      rTelefono &&
      rPassword &&
      rConfirmarPassword;

    if (!todoValido) {
      console.log("Registro - validaciones fallidas", {
        nombre: rNombre,
        dni: rDni,
        email: rEmail,
        telefono: rTelefono,
        password: rPassword,
        confirmarPassword: rConfirmarPassword
      });

      e.preventDefault();
    }
  });
});
