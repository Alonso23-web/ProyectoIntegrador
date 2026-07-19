/* ============================================================
   cliente-buscar.js — Nueva UI de compra de pasajes
   
   Flujo simplificado:
   1. Ruta y fecha (origen, destino, fecha)
   2. Cantidad de pasajeros (+/- botones)
   3. Datos de cada pasajero (nombre + DNI)
   4. Resumen y confirmación
   
   NO hay mapa de asientos.
   NO hay selector de horarios.
   NO hay medidor de disponibilidad.
   El viaje se asigna automáticamente.
   ============================================================ */

const API_PASAJE = "/api/pasajes";
const PRECIO_UNITARIO = 12.00;

// ─── Utilidades ────────────────────────────────────────────

function escapeHtml(str) {
    return String(str ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function formatearPrecio(valor) {
    return `S/ ${Number(valor ?? 0).toFixed(2)}`;
}

function getTodayStr() {
    const d = new Date();
    return d.getFullYear() + "-" +
        String(d.getMonth() + 1).padStart(2, "0") + "-" +
        String(d.getDate()).padStart(2, "0");
}

// ─── Estado global ─────────────────────────────────────────

const pasajesUI = {
    estado: {
        viajeSeleccionado: null,  // { id, totalAsientos, ocupados, disponibles, precio, horaEstimada }
        origen: "",
        destino: "",
        fecha: "",
        cantidad: 1,
    },

    // ─── Alertas ────────────────────────────────────────────

    setAlerta(containerId, tipo, html) {
        const el = document.getElementById(containerId);
        if (!el) return;
        el.innerHTML = `<div class="alert alert-${tipo} d-flex align-items-center gap-2 mt-2 mb-0 py-2 small" role="alert">
            <i class="bi ${
                tipo === "success" ? "bi-check-circle-fill" :
                tipo === "danger" ? "bi-exclamation-triangle-fill" :
                tipo === "warning" ? "bi-exclamation-circle-fill" :
                "bi-info-circle-fill"
            }"></i><span>${html}</span></div>`;
    },

    clearAlerta(containerId) {
        const el = document.getElementById(containerId);
        if (el) el.innerHTML = "";
    },

    // ─── Cargar rutas al iniciar ────────────────────────────

    async cargarRutas() {
        try {
            const r = await fetch(`${API_PASAJE}/rutas`);
            if (!r.ok) return;
            const rutas = await r.json();

            const origenSelect = document.getElementById("origen");
            const destinoSelect = document.getElementById("destino");
            if (!origenSelect || !destinoSelect) return;

            const destinosPorOrigen = {};
            rutas.forEach(({ origen, destino }) => {
                if (!destinosPorOrigen[origen]) destinosPorOrigen[origen] = new Set();
                destinosPorOrigen[origen].add(destino);
            });

            origenSelect.innerHTML = '<option value="">Seleccionar origen</option>';
            Object.keys(destinosPorOrigen).forEach(o => {
                origenSelect.innerHTML += `<option value="${escapeHtml(o)}">${escapeHtml(o)}</option>`;
            });

            origenSelect.addEventListener("change", () => {
                const selected = origenSelect.value;
                destinoSelect.innerHTML = '<option value="">Seleccionar destino</option>';
                if (selected && destinosPorOrigen[selected]) {
                    destinosPorOrigen[selected].forEach(d => {
                        destinoSelect.innerHTML += `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`;
                    });
                }
                // Reset results when route changes
                this.resetearResultados();
            });

            destinoSelect.addEventListener("change", () => {
                if (origenSelect.value && destinoSelect.value && document.getElementById("fecha").value) {
                    this.resetearResultados();
                }
            });

            if (origenSelect.value) origenSelect.dispatchEvent(new Event("change"));
        } catch (e) {
            console.warn("Error cargando rutas:", e);
        }
    },

    resetearResultados() {
        document.getElementById("resultados-area")?.classList.add("d-none");
        this.estado.viajeSeleccionado = null;
        document.getElementById("pasajerosContainer").innerHTML = "";
        this.clearAlerta("buscar-alert");
        this.clearAlerta("compra-alert");
    },

    // ─── Buscar viaje (submit del formulario) ───────────────

    async buscarViajes(origen, destino, fecha) {
        this.clearAlerta("buscar-alert");
        this.estado.viajeSeleccionado = null;
        document.getElementById("pasajerosContainer").innerHTML = "";
        document.getElementById("compra-alert").innerHTML = "";

        document.getElementById("buscar-loading")?.classList.remove("d-none");

        try {
            const url = `${API_PASAJE}/disponibilidad?origen=${encodeURIComponent(origen)}&destino=${encodeURIComponent(destino)}&fecha=${encodeURIComponent(fecha)}`;
            const r = await fetch(url);
            if (!r.ok) throw new Error(`Error HTTP ${r.status} al consultar disponibilidad`);

            const viaje = await r.json();

            this.estado.origen = origen;
            this.estado.destino = destino;
            this.estado.fecha = fecha;
            this.estado.cantidad = 1;

            document.getElementById("resultados-area")?.classList.remove("d-none");

            if (!viaje || !viaje.viajeId) {
                this.setAlerta("buscar-alert", "warning",
                    `No hay viajes disponibles para <strong>${escapeHtml(origen)} → ${escapeHtml(destino)}</strong> el <strong>${escapeHtml(fecha)}</strong>. Prueba con otra fecha.`);
                document.getElementById("pasajerosContainer").innerHTML =
                    '<div class="text-muted small text-center py-3"><i class="bi bi-info-circle me-1"></i>No hay viajes disponibles.</div>';
                this.actualizarPanelResumen();
                document.getElementById("step4-card")?.classList.add("d-none");
                document.getElementById("step3-card")?.classList.add("d-none");
                document.getElementById("step2-card")?.classList.add("d-none");
                return;
            }

            this.estado.viajeSeleccionado = {
                id: viaje.viajeId,
                totalAsientos: viaje.totalAsientos,
                ocupados: viaje.ocupados,
                disponibles: viaje.disponibles,
                precio: viaje.precio,
                horaEstimada: viaje.horaEstimada,
            };

            // Mostrar las cards de pasos 2-4
            document.getElementById("step2-card")?.classList.remove("d-none");
            document.getElementById("step3-card")?.classList.remove("d-none");
            document.getElementById("step4-card")?.classList.remove("d-none");

            // Actualizar cantidad máxima según disponibilidad
            const disponibles = viaje.disponibles;
            document.getElementById("cantidad").value = 1;
            this.estado.cantidad = 1;
            document.getElementById("cantidadDisplay").textContent = "1";

            // Deshabilitar botón + si no hay suficientes disponibles
            document.getElementById("btn-sumar").disabled = disponibles <= 1;

            this.renderPasajerosInputs(1);
            this.actualizarPanelResumen();

            // Mostrar notificación de éxito
            this.setAlerta("buscar-alert", "success",
                `Viaje encontrado: <strong>${escapeHtml(origen)} → ${escapeHtml(destino)}</strong> — <strong>${disponibles} cupo(s)</strong> disponible(s). Completa los datos para reservar.`);

        } catch (err) {
            console.error("Error en buscarViajes:", err);
            this.setAlerta("buscar-alert", "danger",
                `<i class="bi bi-exclamation-triangle me-1"></i>${escapeHtml(err.message)}`);
            document.getElementById("resultados-area")?.classList.add("d-none");
        } finally {
            document.getElementById("buscar-loading")?.classList.add("d-none");
        }
    },

    // ─── Cantidad de pasajeros (+/-) ───────────────────────

    cambiarCantidad(delta) {
        const v = this.estado.viajeSeleccionado;
        if (!v) return;

        const disponibles = v.disponibles;
        let nueva = this.estado.cantidad + delta;
        nueva = Math.max(1, Math.min(nueva, disponibles));

        if (nueva === this.estado.cantidad) return;

        this.estado.cantidad = nueva;
        document.getElementById("cantidad").value = nueva;
        document.getElementById("cantidadDisplay").textContent = nueva;

        // Actualizar botones
        document.getElementById("btn-restar").disabled = nueva <= 1;
        document.getElementById("btn-sumar").disabled = nueva >= disponibles;

        this.renderPasajerosInputs(nueva);
        this.actualizarPanelResumen();
        this.clearAlerta("compra-alert");
    },

    // ─── Datos de cada pasajero ─────────────────────────────

    renderPasajerosInputs(cantidad) {
        const container = document.getElementById("pasajerosContainer");
        if (!container) return;

        if (!cantidad || cantidad < 1) {
            container.innerHTML = '<div class="text-muted small text-center py-3"><i class="bi bi-info-circle me-1"></i>Selecciona al menos 1 pasajero.</div>';
            return;
        }

        // Obtener datos del usuario autenticado para autocompletar (si existen)
        const currentUserName = document.body.getAttribute("data-current-user-name") || "";
        const currentUserDni = document.body.getAttribute("data-current-user-dni") || "";

        let html = `<div class="small text-muted mb-3">
            <i class="bi bi-info-circle me-1" style="color: #f5a623;"></i>
            Ingresa los datos de cada pasajero. El <strong>primer pasajero</strong> se autocompleta con tus datos.
        </div>`;

        for (let i = 0; i < cantidad; i++) {
            const num = i + 1;
            const nombreVal = i === 0 ? escapeHtml(currentUserName) : "";
            const dniVal = i === 0 ? escapeHtml(currentUserDni) : "";

            html += `
            <div class="pasajero-card card border rounded-3 p-3 mb-3">
                <div class="d-flex align-items-center gap-2 mb-2">
                    <span class="pasajero-numero">${num}</span>
                    <span class="fw-semibold small" style="color: #0d1b3e;">Pasajero ${num}</span>
                </div>
                <div class="row g-2">
                    <div class="col-md-7">
                        <label class="form-label-xs">Nombre completo</label>
                        <input type="text" class="form-control form-control-sm pasajero-nombre"
                               placeholder="Ej: Juan Pérez" value="${nombreVal}" required
                               data-pasajero="${num}" />
                    </div>
                    <div class="col-md-5">
                        <label class="form-label-xs">DNI</label>
                        <input type="text" class="form-control form-control-sm pasajero-dni"
                               placeholder="12345678" maxlength="8" value="${dniVal}" required
                               data-pasajero="${num}" />
                    </div>
                </div>
            </div>`;
        }

        container.innerHTML = html;
    },

    // ─── Actualizar panel de resumen ────────────────────────

    actualizarPanelResumen() {
        const v = this.estado.viajeSeleccionado;
        const cantidad = this.estado.cantidad;
        const origen = this.estado.origen;
        const destino = this.estado.destino;
        const fecha = this.estado.fecha;
        const total = cantidad * PRECIO_UNITARIO;
        const tieneViaje = !!v;

        // Elementos del panel de resumen (columna derecha)
        const setText = (id, text) => {
            const el = document.getElementById(id);
            if (el) el.textContent = text || "—";
        };

        setText("panel-ruta", tieneViaje ? `${origen} → ${destino}` : "—");
        setText("panel-fecha", tieneViaje ? fecha : "—");
        setText("panel-cantidad", tieneViaje ? `${cantidad} pasajero(s)` : "—");
        setText("panel-detalle-precio", tieneViaje ? `${cantidad} × ${formatearPrecio(PRECIO_UNITARIO)}` : "—");
        setText("panel-total", tieneViaje ? formatearPrecio(total) : "—");

        // Resumen final (step 5)
        setText("resumen-ruta-final", tieneViaje ? `${origen} → ${destino}` : "—");
        setText("resumen-fecha-final", tieneViaje ? fecha : "—");
        setText("resumen-cantidad-final", tieneViaje ? `${cantidad} pasaje(s)` : "—");
        setText("resumen-total-final", tieneViaje ? formatearPrecio(total) : "—");

        // Botón confirmar
        const btn = document.getElementById("btn-confirmar-compra");
        if (btn) {
            btn.disabled = !tieneViaje || cantidad < 1;
            if (tieneViaje && cantidad > 0) {
                btn.innerHTML = `<i class="bi bi-cart-check me-2"></i>Comprar pasaje — ${formatearPrecio(total)}`;
            } else {
                btn.innerHTML = `<i class="bi bi-cart-check me-2"></i>Comprar pasaje`;
            }
        }
    },

    // ─── Confirmar compra ───────────────────────────────────

    async confirmarCompra() {
        const v = this.estado.viajeSeleccionado;
        if (!v) {
            this.setAlerta("compra-alert", "danger", "No hay un viaje seleccionado.");
            return;
        }

        // Validar que todos los pasajeros tengan nombre y DNI
        const nombres = document.querySelectorAll(".pasajero-nombre");
        const dnis = document.querySelectorAll(".pasajero-dni");
        const pasajeros = [];

        for (let i = 0; i < nombres.length; i++) {
            const nombre = nombres[i].value.trim();
            const dni = dnis[i].value.trim();

            if (!nombre) {
                this.setAlerta("compra-alert", "danger",
                    `El pasajero ${i + 1} debe tener un nombre.`);
                nombres[i].focus();
                return;
            }
            if (!dni) {
                this.setAlerta("compra-alert", "danger",
                    `El pasajero ${i + 1} debe tener un DNI.`);
                dnis[i].focus();
                return;
            }
            if (!/^\d{8}$/.test(dni)) {
                this.setAlerta("compra-alert", "danger",
                    `El DNI del pasajero ${i + 1} debe tener 8 dígitos.`);
                dnis[i].focus();
                return;
            }

            pasajeros.push({ nombrePasajero: nombre, dniPasajero: dni });
        }

        // Deshabilitar botón para evitar doble envío
        const btn = document.getElementById("btn-confirmar-compra");
        btn.disabled = true;
        btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status"></span>Procesando...`;

        this.clearAlerta("compra-alert");

        try {
            const r = await fetch(`${API_PASAJE}/${v.id}/reservar-multiples`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(pasajeros),
            });

            if (!r.ok) {
                const errMsg = await r.text().catch(() => "Error al procesar la compra");
                throw new Error(errMsg);
            }

            const data = await r.json();

            // Mostrar éxito
            const codigos = data.codigos || [];
            document.getElementById("boleto-area")?.classList.remove("d-none");

            // Generar HTML con los códigos
            let codigosHtml = codigos.map(c =>
                `<span class="badge bg-light text-dark border px-3 py-2 fw-bold fs-6 me-1">${escapeHtml(c)}</span>`
            ).join("");

            document.getElementById("codigos-result").innerHTML = `
                <div class="mt-2">
                    <div class="small text-muted mb-2">Código(s) de boleto:</div>
                    <div>${codigosHtml}</div>
                </div>
            `;

            // Ocultar formulario de pasajeros y botón de pago
            document.getElementById("pasajerosContainer")?.classList.add("d-none");
            document.getElementById("btn-confirmar-compra")?.classList.add("d-none");
            document.getElementById("resumen-detalle")?.classList.add("d-none");

            // Scroll al área de éxito
            document.getElementById("boleto-area")?.scrollIntoView({ behavior: "smooth", block: "center" });

            this.setAlerta("compra-alert", "success",
                `¡Compra exitosa! Se generaron ${codigos.length} boleto(s). Guarda tu(s) código(s) para abordar.`);

        } catch (err) {
            console.error("Error en confirmarCompra:", err);
            this.setAlerta("compra-alert", "danger",
                `<i class="bi bi-exclamation-triangle me-1"></i>${escapeHtml(err.message)}`);
            btn.disabled = false;
            this.actualizarPanelResumen();
        }
    },

    // ─── Mis viajes ─────────────────────────────────────────

    async cargarMisViajes() {
        const container = document.getElementById("mis-viajes-result");
        if (!container) return;

        container.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-amarillo" role="status"></div><div class="mt-2 text-muted small">Cargando tus viajes...</div></div>';

        try {
            const r = await fetch(`${API_PASAJE}/mis`);
            if (!r.ok) throw new Error("Error al cargar tus viajes");

            const viajes = await r.json();

            if (!viajes || viajes.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-4">
                        <i class="bi bi-ticket text-muted display-6 mb-2"></i>
                        <p class="text-muted">No tienes viajes registrados.</p>
                        <a href="/pasajes/cliente" class="btn btn-amarillo btn-sm fw-bold">
                            <i class="bi bi-cart-plus me-1"></i>Comprar pasajes
                        </a>
                    </div>`;
                return;
            }

            let rows = viajes.map(v => `
                <tr>
                    <td><span class="fw-semibold small">${escapeHtml(v.nombrePasajero)}</span></td>
                    <td><span class="small">${escapeHtml(v.dni || "—")}</span></td>
                    <td><span class="small">${escapeHtml(v.origen)} → ${escapeHtml(v.destino)}</span></td>
                    <td><span class="small">${v.fechaViaje || "—"}</span></td>
                    <td><code class="small">${escapeHtml(v.codigoBoleto || "—")}</code></td>
                    <td class="fw-semibold small">${formatearPrecio(v.precio)}</td>
                </tr>
            `).join("");

            container.innerHTML = `
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small">
                            <tr>
                                <th>Pasajero</th>
                                <th>DNI</th>
                                <th>Ruta</th>
                                <th>Fecha</th>
                                <th>Código</th>
                                <th>Total</th>
                            </tr>
                        </thead>
                        <tbody>${rows}</tbody>
                    </table>
                </div>`;
        } catch (err) {
            console.error("Error cargando mis viajes:", err);
            container.innerHTML = `<div class="alert alert-danger small py-2">${escapeHtml(err.message)}</div>`;
        }
    },

    // ─── Estado de viaje por código ─────────────────────────

    async consultarEstadoViaje() {
        const codigo = document.getElementById("estado-boleto-codigo")?.value.trim();
        const container = document.getElementById("estado-viaje-result");
        if (!container) return;

        if (!codigo) {
            container.innerHTML = '<div class="alert alert-warning small py-2">Ingresa un código de boleto.</div>';
            return;
        }

        container.innerHTML = '<div class="text-center py-3"><div class="spinner-border spinner-border-sm text-amarillo" role="status"></div><div class="mt-1 text-muted small">Consultando...</div></div>';

        try {
            const r = await fetch(`${API_PASAJE}/estado/${encodeURIComponent(codigo)}`);
            if (!r.ok) {
                if (r.status === 404) {
                    container.innerHTML = '<div class="alert alert-warning small py-2">No se encontró un boleto con ese código.</div>';
                } else {
                    throw new Error("Error al consultar");
                }
                return;
            }

            const d = await r.json();

            let estadoBadge, estadoColor;
            switch (d.estado) {
                case "RESERVADO": estadoBadge = "bg-warning text-dark"; estadoColor = "#f5a623"; break;
                case "PAGADO": estadoBadge = "bg-success"; estadoColor = "#28a745"; break;
                case "FINALIZADO": estadoBadge = "bg-info text-dark"; estadoColor = "#17a2b8"; break;
                case "CANCELADO": estadoBadge = "bg-secondary"; estadoColor = "#6c757d"; break;
                default: estadoBadge = "bg-secondary"; estadoColor = "#6c757d";
            }

            let estadoViajeHtml = '';
            if (d.estadoViaje) {
                const evBadge = d.estadoViaje === 'EN_VIAJE'
                    ? 'bg-warning text-dark'
                    : d.estadoViaje === 'LLEGO' ? 'bg-success' : 'bg-secondary';
                estadoViajeHtml = `<span class="badge ${evBadge} rounded-pill fs-6 px-3 py-2 ms-2">${escapeHtml(d.estadoViaje)}</span>`;
            }

            container.innerHTML = `
                <div class="card border-0 shadow-sm mt-3" style="border-radius: 16px;">
                    <div class="card-body p-4">
                        <div class="text-center mb-3">
                            <i class="bi bi-ticket-perforating display-6" style="color: #f5a623;"></i>
                            <h5 class="fw-bold mt-2" style="color: #0d1b3e;">${escapeHtml(d.nombrePasajero)}</h5>
                            <span class="badge ${estadoBadge} rounded-pill fs-6 px-3 py-2">${escapeHtml(d.estado)}</span>
                            ${estadoViajeHtml}
                        </div>
                        <div class="row g-2 small">
                            <div class="col-6"><span class="text-muted">Ruta</span><br><span class="fw-semibold">${escapeHtml(d.origen)} → ${escapeHtml(d.destino)}</span></div>
                            <div class="col-6"><span class="text-muted">Fecha</span><br><span class="fw-semibold">${escapeHtml(d.fecha)}</span></div>
                            <div class="col-6"><span class="text-muted">Código</span><br><code class="fw-bold">${escapeHtml(d.codigoBoleto)}</code></div>
                            <div class="col-6"><span class="text-muted">Precio</span><br><span class="fw-semibold">${formatearPrecio(d.precio)}</span></div>
                        </div>
                        <hr class="my-2">
                        <p class="small text-muted mb-0"><i class="bi bi-info-circle me-1" style="color: #f5a623;"></i>${escapeHtml(d.detalles || "Viaje programado.")}</p>
                    </div>
                </div>`;
        } catch (err) {
            console.error("Error consultando estado:", err);
            container.innerHTML = `<div class="alert alert-danger small py-2">${escapeHtml(err.message)}</div>`;
        }
    },
};
