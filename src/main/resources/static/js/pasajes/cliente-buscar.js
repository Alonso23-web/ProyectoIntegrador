/* Pasajes - flujo cliente (mejorado) */

const API_PASAJE = "/api/pasajes";

function escapeHtml(str) {
    return String(str ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

const pasajesUI = {
    estado: {
        resultados: [],
        viajeSeleccionado: null,
        ocupacion: [],
        asientosSeleccionados: [],
    },

    // ─── Utilidades ────────────────────────────────────────────

    setLoading(isLoading, containerId) {
        const el = document.getElementById(containerId);
        if (!el) return;
        el.classList.toggle("d-none", !isLoading);
    },

    setAlerta(containerId, tipo, html) {
        const el = document.getElementById(containerId);
        if (!el) return;
        el.innerHTML = `<div class="alert alert-${tipo} d-flex align-items-center gap-2 mt-2 mb-0" role="alert"><i class="bi ${
            tipo === "success" ? "bi-check-circle-fill" :
            tipo === "danger" ? "bi-exclamation-triangle-fill" :
            tipo === "warning" ? "bi-exclamation-circle-fill" :
            "bi-info-circle-fill"
        }"></i><span>${html}</span></div>`;
    },

    clearAlerta(containerId) {
        const el = document.getElementById(containerId);
        if (!el) return;
        el.innerHTML = "";
    },

    getCantidadPasajeros() {
        return Number(document.getElementById("cantidad")?.value || 1);
    },

    getPrecioUnitario() {
        return this.estado.viajeSeleccionado?.precio || 12.00;
    },

    formatearPrecio(valor) {
        return `S/ ${Number(valor ?? 0).toFixed(2)}`;
    },

    // ─── Cargar rutas disponibles ──────────────────────────────

    async cargarRutas() {
        try {
            const r = await fetch(`${API_PASAJE}/rutas`);
            if (!r.ok) return;
            const rutas = await r.json();

            const origenSelect = document.getElementById("origen");
            const destinoSelect = document.getElementById("destino");
            if (!origenSelect || !destinoSelect) return;

            // Agrupar destinos por origen
            const destinosPorOrigen = {};
            rutas.forEach(({ origen, destino }) => {
                if (!destinosPorOrigen[origen]) destinosPorOrigen[origen] = new Set();
                destinosPorOrigen[origen].add(destino);
            });

            // Llenar select de origen
            const origenes = Object.keys(destinosPorOrigen);
            origenSelect.innerHTML = `<option value="">Seleccionar origen</option>`;
            origenes.forEach(o => {
                origenSelect.innerHTML += `<option value="${escapeHtml(o)}">${escapeHtml(o)}</option>`;
            });

            // Al cambiar origen, filtrar destinos
            origenSelect.addEventListener("change", () => {
                const selected = origenSelect.value;
                destinoSelect.innerHTML = `<option value="">Seleccionar destino</option>`;
                if (selected && destinosPorOrigen[selected]) {
                    destinosPorOrigen[selected].forEach(d => {
                        destinoSelect.innerHTML += `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`;
                    });
                }
            });

            // Si ya hay valores preseleccionados, trigger
            if (origenSelect.value) {
                origenSelect.dispatchEvent(new Event("change"));
            }
        } catch (e) {
            console.warn("No se pudieron cargar rutas:", e);
        }
    },

    // ─── Búsqueda de viajes ────────────────────────────────────

    renderResultados(lista) {
        const wrap = document.getElementById("resultados-cards");
        const meta = document.getElementById("resultados-meta");
        if (!wrap) return;

        const cantidad = lista?.length ?? 0;
        if (meta) meta.textContent = `${cantidad} resultado${cantidad === 1 ? "" : "s"}`;

        if (!lista.length) {
            wrap.innerHTML = `
        <div class="col-12">
          <div class="alert alert-warning mb-0">
            <i class="bi bi-search me-2"></i>No se encontraron viajes para esta ruta y fecha.
          </div>
        </div>
      `;
            return;
        }

        wrap.innerHTML = lista
            .map(
                (v) => `
        <div class="col-lg-6">
          <div class="card shadow-sm h-100 border-0" style="border-radius: 16px;">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-start gap-3">
                <div>
                  <div class="text-muted small text-uppercase fw-semibold">Ruta</div>
                  <div class="fw-bold text-primario fs-5">${escapeHtml(v.origen)} <span class="text-amarillo">→</span> ${escapeHtml(v.destino)}</div>
                  <div class="mt-2 text-muted small">
                    <i class="bi bi-clock me-1"></i> ${escapeHtml(v.horaSalida)}
                    <span class="mx-1">•</span> ${escapeHtml(v.fecha)}
                  </div>
                  <div class="mt-2 d-flex gap-2 flex-wrap">
                    <span class="badge bg-primario">${escapeHtml(v.tipoBus)}</span>
                    <span class="badge bg-light text-primario border">
                      <i class="bi bi-seat me-1"></i>${v.totalAsientos} asientos
                    </span>
                  </div>
                </div>
                <div class="text-end">
                  <div class="text-muted small">Precio por pasaje</div>
                  <div class="fs-4 fw-bold text-primario">${this.formatearPrecio(v.precio)}</div>
                </div>
              </div>
              <div class="mt-3">
                <button class="btn btn-amarillo w-100 fw-bold" type="button" onclick="pasajesUI.seleccionarViaje(${v.id})">
                  <i class="bi bi-ticket-perforated me-2"></i>Seleccionar
                </button>
              </div>
            </div>
          </div>
        </div>
      `
            )
            .join("");
    },

    // ─── Selección de viaje ────────────────────────────────────

    seleccionarViaje(viajeId) {
        const viaje = this.estado.resultados.find((x) => String(x.id) === String(viajeId));
        if (!viaje) {
            this.setAlerta("buscar-alert", "danger", "No se pudo seleccionar el viaje.");
            return;
        }

        this.estado.viajeSeleccionado = viaje;
        this.estado.ocupacion = [];
        this.estado.asientosSeleccionados = [];

        document.getElementById("paso-seleccion")?.classList.remove("d-none");
        document.getElementById("resultados")?.classList.add("d-none");

        document.getElementById("viajeId").value = viaje.id;
        document.getElementById("precioSeleccionado").value = viaje.precio;
        this.actualizarPrecioDetalle();
        this.actualizarResumenViaje(viaje);

        this.setAlerta("buscar-alert", "info", "<i class='bi bi-hourglass-split me-2'></i>Cargando asientos ocupados...");
        this.setLoading(true, "buscar-loading");

        fetch(`${API_PASAJE}/${viaje.id}/ocupacion`)
            .then((r) => {
                if (!r.ok) throw new Error("ocupacion");
                return r.json();
            })
            .then((ocupados) => {
                this.estado.ocupacion = (ocupados || []).map(Number);
                this.renderSeatMap();
                this.renderPasajerosInputs();
                this.validarDisponibilidadAsientos();
            })
            .catch(() => {
                this.setAlerta("buscar-alert", "danger", "Error al cargar la ocupación de asientos.");
                this.renderSeatMap();
                this.renderPasajerosInputs();
            })
            .finally(() => this.setLoading(false, "buscar-loading"));
    },

    // ─── Resumen visual del viaje ──────────────────────────────

    actualizarResumenViaje(viaje) {
        if (!viaje) return;

        const rutaEl = document.getElementById("resumen-ruta");
        const fechaEl = document.getElementById("resumen-fecha");
        const horaEl = document.getElementById("resumen-hora");
        const busEl = document.getElementById("resumen-bus");
        const precioEl = document.getElementById("sel-precio-unitario");

        if (rutaEl) rutaEl.textContent = `${viaje.origen} → ${viaje.destino}`;
        if (fechaEl) fechaEl.textContent = viaje.fecha;
        if (horaEl) horaEl.textContent = viaje.horaSalida;
        if (busEl) busEl.textContent = `${viaje.tipoBus} • ${viaje.totalAsientos} asientos`;
        if (precioEl) precioEl.textContent = this.formatearPrecio(viaje.precio);
    },

    // ─── Validación de disponibilidad ──────────────────────────

    validarDisponibilidadAsientos() {
        const viaje = this.estado.viajeSeleccionado;
        if (!viaje) return;

        const total = viaje.totalAsientos;
        const ocupados = this.estado.ocupacion.length;
        const disponibles = total - ocupados;
        const solicitados = this.getCantidadPasajeros();

        const warningEl = document.getElementById("disp-warning");
        const infoEl = document.getElementById("disp-info");

        if (infoEl) {
            infoEl.textContent = `${disponibles} de ${total} asientos disponibles`;
            infoEl.className = disponibles > 0
                ? "badge bg-success bg-opacity-10 text-success border border-success"
                : "badge bg-danger bg-opacity-10 text-danger border border-danger";
        }

        if (warningEl) {
            if (solicitados > disponibles) {
                warningEl.classList.remove("d-none");
                warningEl.innerHTML = `
                  <i class="bi bi-exclamation-triangle-fill me-2"></i>
                  Solo hay <strong>${disponibles}</strong> asiento${disponibles === 1 ? "" : "s"} disponible${disponibles === 1 ? "" : "s"},
                  pero solicitaste <strong>${solicitados}</strong>. Reduce la cantidad de pasajes.
                `;
            } else {
                warningEl.classList.add("d-none");
            }
        }
    },

    // ─── Inputs de pasajeros ───────────────────────────────────

    renderPasajerosInputs() {
        const container = document.getElementById("pasajerosContainer");
        if (!container) return;

        const cantidad = this.getCantidadPasajeros();

        container.innerHTML = "";

        for (let i = 0; i < cantidad; i++) {
            container.innerHTML += `
        <div class="border rounded-3 p-3 mb-2 bg-white">
          <div class="fw-semibold text-primario mb-2 small">
            <i class="bi bi-person me-1"></i>Pasajero ${i + 1}
          </div>
          <div class="row g-2">
            <div class="col-md-6">
              <input
                class="form-control form-control-sm"
                required
                placeholder="Nombre completo"
                name="nombrePasajero-${i}"
                data-pasajero-index="${i}"
              />
            </div>
            <div class="col-md-6">
              <input
                class="form-control form-control-sm"
                required
                placeholder="DNI"
                name="dniPasajero-${i}"
                data-pasajero-index="${i}"
              />
            </div>
          </div>
        </div>
        `;
        }

        this.actualizarPrecioDetalle();
    },

    // ─── Mapa de asientos ──────────────────────────────────────

    renderSeatMap() {
        const seatMap = document.getElementById("seat-map");
        const warn = document.getElementById("seat-warning");
        if (!seatMap) return;

        const total = this.estado.viajeSeleccionado?.totalAsientos || 24;
        const cantidadPasajeros = this.getCantidadPasajeros();

        const tipo = String(this.estado.viajeSeleccionado?.tipoBus || "").toUpperCase();
        const maxUI = tipo === "MINIVAN" ? Math.min(15, total) : total;

        const ocupadosSet = new Set(this.estado.ocupacion);

        seatMap.innerHTML = "";
        this.estado.asientosSeleccionados = [];

        for (let i = 1; i <= maxUI; i++) {
            const occupied = ocupadosSet.has(i);
            const div = document.createElement("div");
            div.className = `seat ${occupied ? "occupied" : "available"}`;
            div.textContent = i;
            div.dataset.asiento = String(i);

            if (!occupied) {
                div.onclick = () => {
                    const seatNum = i;
                    const exists = this.estado.asientosSeleccionados.includes(seatNum);

                    if (exists) {
                        this.estado.asientosSeleccionados = this.estado.asientosSeleccionados.filter((x) => x !== seatNum);
                    } else {
                        if (this.estado.asientosSeleccionados.length >= cantidadPasajeros) {
                            this.setAlerta(
                                "compra-alert",
                                "warning",
                                `Solo puedes seleccionar hasta ${cantidadPasajeros} asiento(s).`
                            );
                            return;
                        }
                        this.estado.asientosSeleccionados.push(seatNum);
                    }

                    this.estado.asientosSeleccionados.sort((a, b) => a - b);

                    seatMap.querySelectorAll(".seat.selected").forEach((s) => s.classList.remove("selected"));
                    this.estado.asientosSeleccionados.forEach((s) => {
                        const el = seatMap.querySelector(`.seat[data-asiento='${s}']`);
                        if (el) el.classList.add("selected");
                    });

                    if (warn) {
                        warn.classList.toggle("d-none", this.estado.asientosSeleccionados.length === cantidadPasajeros);
                    }

                    this.clearAlerta("compra-alert");
                };
            }

            seatMap.appendChild(div);
        }

        if (warn) warn.classList.remove("d-none");
    },

    // ─── Recoger datos del formulario ──────────────────────────

    collectPasajerosSeleccionados() {
        const cantidad = this.getCantidadPasajeros();
        const formEls = document.getElementById("pasajerosContainer");
        if (!formEls) return [];

        const asientos = this.estado.asientosSeleccionados;
        const pasajeros = [];

        for (let i = 0; i < cantidad; i++) {
            const nombreInput = formEls.querySelector(`input[name='nombrePasajero-${i}']`);
            const dniInput = formEls.querySelector(`input[name='dniPasajero-${i}']`);

            pasajeros.push({
                nombrePasajero: nombreInput?.value?.trim(),
                dniPasajero: dniInput?.value?.trim(),
                asiento: asientos[i],
            });
        }

        return pasajeros;
    },

    // ─── Confirmar compra (con modal y protección doble clic) ──

    mostrarModalConfirmacion() {
        const viaje = this.estado.viajeSeleccionado;
        const cantidad = this.getCantidadPasajeros();
        const precioUnitario = this.getPrecioUnitario();
        const total = cantidad * precioUnitario;

        // Llenar datos del modal
        document.getElementById("modal-ruta").textContent = `${viaje.origen} → ${viaje.destino}`;
        document.getElementById("modal-fecha").textContent = viaje.fecha;
        document.getElementById("modal-hora").textContent = viaje.horaSalida;
        document.getElementById("modal-cantidad").textContent = cantidad;
        document.getElementById("modal-total").textContent = this.formatearPrecio(total);
        document.getElementById("modal-precio-unitario").textContent = this.formatearPrecio(precioUnitario);

        // Mostrar modal
        const modal = new bootstrap.Modal(document.getElementById("confirmModal"));
        modal.show();
    },

    async confirmarCompra() {
        const viajeId = document.getElementById("viajeId")?.value;
        if (!viajeId) {
            this.setAlerta("compra-alert", "warning", "Selecciona un viaje primero.");
            return;
        }

        const pasajerosCantidad = this.getCantidadPasajeros();
        if (this.estado.asientosSeleccionados.length !== pasajerosCantidad) {
            this.setAlerta(
                "compra-alert",
                "warning",
                `<i class="bi bi-info-circle me-1"></i>Debes seleccionar exactamente <strong>${pasajerosCantidad}</strong> asiento(s).`
            );
            return;
        }

        const pasajeros = this.collectPasajerosSeleccionados();

        for (const p of pasajeros) {
            if (!p.nombrePasajero) {
                this.setAlerta("compra-alert", "warning", "Completa el nombre de cada pasajero.");
                return;
            }
            if (!p.dniPasajero) {
                this.setAlerta("compra-alert", "warning", "Completa el DNI de cada pasajero.");
                return;
            }
            if (!p.asiento) {
                this.setAlerta("compra-alert", "warning", "Selecciona asientos suficientes para cada pasajero.");
                return;
            }
        }

        // Mostrar modal de confirmación en lugar de enviar directo
        this.mostrarModalConfirmacion();
    },

    async ejecutarReserva() {
        const viajeId = document.getElementById("viajeId")?.value;
        const pasajeros = this.collectPasajerosSeleccionados();

        // Cerrar modal
        const modal = bootstrap.Modal.getInstance(document.getElementById("confirmModal"));
        if (modal) modal.hide();

        this.setAlerta(
            "compra-alert",
            "info",
            '<div class="spinner-border spinner-border-sm me-2" role="status"></div> Reservando...'
        );

        // Deshabilitar botón de confirmar (protección doble clic)
        const btnConfirmar = document.querySelector("#compraForm button[type='submit']");
        if (btnConfirmar) {
            btnConfirmar.disabled = true;
            btnConfirmar.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Procesando...';
        }

        try {
            const r = await fetch(`${API_PASAJE}/${encodeURIComponent(viajeId)}/reservar-multiples`, {
                method: "POST",
                headers: { "Content-Type": "application/json;charset=UTF-8" },
                body: JSON.stringify(pasajeros),
            });

            if (!r.ok) {
                // Intentar extraer mensaje de error del backend
                let errorMsg = "No se pudo completar la compra.";
                try {
                    const errData = await r.text();
                    if (errData) errorMsg = errData;
                } catch (e) {}
                throw new Error(errorMsg);
            }

            const data = await r.json();

            const reservas = Array.isArray(data) ? data : (data?.reservas || []);
            const codigos = reservas.map((x) => x.codigoBoleto).filter(Boolean);

            const boletoArea = document.getElementById("boleto-area");
            const listaEl = document.getElementById("boletosLista");

            boletoArea?.classList.remove("d-none");
            if (listaEl) {
                listaEl.innerHTML = "";
                if (codigos?.length) {
                    listaEl.innerHTML = `
                        <div class="list-group">
                          ${codigos.map((c) => `
                            <div class="list-group-item d-flex justify-content-between align-items-center border-0 bg-success bg-opacity-10">
                              <span><i class="bi bi-ticket-perforated text-success me-2"></i>Boleto</span>
                              <code class="fw-bold text-success">${c}</code>
                            </div>
                          `).join("")}
                        </div>
                        <div class="mt-2 text-muted small text-center">
                          <i class="bi bi-info-circle me-1"></i>Guarda estos códigos para consultar el estado de tu viaje.
                        </div>
                      `;
                } else {
                    listaEl.innerHTML = `<div class="text-muted small">Compra realizada.</div>`;
                }
            }

            // Hacer scroll hacia el área de boletos
            boletoArea?.scrollIntoView({ behavior: "smooth", block: "center" });

            this.setAlerta(
                "compra-alert",
                "success",
                `<i class="bi bi-check-circle-fill me-2"></i>¡Reserva(s) confirmada(s)!`
            );
        } catch (err) {
            this.setAlerta(
                "compra-alert",
                "danger",
                `<i class="bi bi-exclamation-triangle-fill me-2"></i>${escapeHtml(err.message)}`
            );
        } finally {
            // Re-habilitar botón
            if (btnConfirmar) {
                btnConfirmar.disabled = false;
                btnConfirmar.innerHTML = '<i class="bi bi-credit-card me-2"></i>Confirmar compra';
            }
        }
    },

    // ─── Cantidad de pasajes ───────────────────────────────────

    cambiarCantidad(delta) {
        const input = document.getElementById("cantidad");
        if (!input) return;

        const actual = parseInt(input.value) || 1;
        const nuevo = Math.max(1, actual + delta);
        if (nuevo === actual) return;

        input.value = nuevo;

        const display = document.getElementById("cantidadDisplay");
        if (display) display.textContent = nuevo;

        this.estado.asientosSeleccionados = [];
        this.clearAlerta("compra-alert");

        this.renderSeatMap();
        this.renderPasajerosInputs();
        this.actualizarPrecioDetalle();
        this.validarDisponibilidadAsientos();
    },

    // ─── Precio ────────────────────────────────────────────────

    actualizarPrecioDetalle() {
        const cantidad = this.getCantidadPasajeros();
        const precioUnitario = this.getPrecioUnitario();
        const total = cantidad * precioUnitario;
        const detalleEl = document.getElementById("precioDetalle");
        const resumenEl = document.getElementById("precioResumen");
        const displayEl = document.getElementById("cantidadDisplay");
        const totalModalEl = document.getElementById("modal-total");
        const precioUnitLabel = document.getElementById("precio-unitario-label");

        if (detalleEl) {
            detalleEl.textContent = `${this.formatearPrecio(precioUnitario)} x ${cantidad} pasajero${cantidad === 1 ? "" : "s"} = ${this.formatearPrecio(total)}`;
        }
        if (resumenEl) {
            resumenEl.textContent = this.formatearPrecio(total);
        }
        if (displayEl) {
            displayEl.textContent = cantidad;
        }
        if (totalModalEl) {
            totalModalEl.textContent = this.formatearPrecio(total);
        }
        if (precioUnitLabel) {
            precioUnitLabel.textContent = `${this.formatearPrecio(precioUnitario)} c/u`;
        }
    },

    // ─── Mis Viajes (timeline visual) ──────────────────────────

    async cargarMisViajes() {

    // ─── Mis Viajes (timeline visual) ──────────────────────────

    async cargarMisViajes() {
        this.setAlerta(
            "mis-viajes-result",
            "info",
            '<div class="spinner-border spinner-border-sm me-2" role="status"></div> Cargando tus viajes...'
        );
        try {
            const response = await fetch(`${API_PASAJE}/mis`);
            if (!response.ok) throw new Error("Failed to load mis viajes");
            const misViajes = await response.json();

            if (misViajes.length === 0) {
                this.setAlerta("mis-viajes-result", "warning",
                    '<i class="bi bi-inbox me-2"></i>No tienes viajes registrados aún.');
                return;
            }

            let htmlContent = `
              <div class="row g-3">
          `;

            misViajes.forEach(reserva => {
                const v = reserva.viaje;
                const estado = reserva.estado;
                const paso = estado === "FINALIZADO" ? 3 : estado === "PAGADO" ? 2 : 1;

                const estadoBadge = estado === "RESERVADO" ? "bg-info" :
                    estado === "PAGADO" ? "bg-primary" :
                    estado === "FINALIZADO" ? "bg-success" : "bg-secondary";

                const estadoIcono = estado === "RESERVADO" ? "bi-ticket" :
                    estado === "PAGADO" ? "bi-credit-card" :
                    estado === "FINALIZADO" ? "bi-check-circle" : "bi-question";

                const pasos = [
                    { label: "Reservado", icono: "bi-ticket", desc: "Asiento #" + reserva.asiento },
                    { label: "Pagado", icono: "bi-credit-card", desc: "S/ " + reserva.precio.toFixed(2) },
                    { label: "Finalizado", icono: "bi-check-circle", desc: "Viaje completado" },
                ];

                htmlContent += `
                  <div class="col-md-6">
                    <div class="card border-0 shadow-sm h-100" style="border-radius: 16px;">
                      <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                          <div>
                            <span class="badge ${estadoBadge} text-white mb-1">
                              <i class="bi ${estadoIcono} me-1"></i>${estado}
                            </span>
                            <h6 class="fw-bold text-primario mt-1 mb-0">
                              ${v.origen} → ${v.destino}
                            </h6>
                          </div>
                          <code class="small">${reserva.codigoBoleto}</code>
                        </div>

                        <div class="d-flex gap-3 text-muted small mb-3">
                          <span><i class="bi bi-calendar me-1"></i>${v.fecha}</span>
                          <span><i class="bi bi-clock me-1"></i>${v.horaSalida}</span>
                          <span><i class="bi bi-person me-1"></i>${reserva.nombrePasajero}</span>
                        </div>

                        <!-- Timeline -->
                        <div class="timeline mt-3">
                          ${pasos.map((p, idx) => `
                            <div class="timeline-item ${idx + 1 <= paso ? "active" : ""}">
                              <div class="timeline-marker ${idx + 1 <= paso ? "bg-success" : "bg-secondary"}">
                                <i class="bi ${p.icono}"></i>
                              </div>
                              <div class="timeline-content">
                                <div class="fw-semibold small">${p.label}</div>
                                <div class="text-muted" style="font-size: 11px;">${p.desc}</div>
                              </div>
                            </div>
                          `).join("")}
                        </div>

                        <button class="btn btn-sm btn-outline-primario mt-2 w-100"
                          onclick="pasajesUI.verDetallesViaje('${reserva.codigoBoleto}')">
                          <i class="bi bi-geo-alt me-1"></i>Ver estado
                        </button>
                      </div>
                    </div>
                  </div>
                `;
            });

            htmlContent += `</div>`;
            document.getElementById("mis-viajes-result").innerHTML = htmlContent;

        } catch (error) {
            this.setAlerta("mis-viajes-result", "danger",
                '<i class="bi bi-exclamation-triangle me-2"></i>Error al cargar tus viajes.');
        }
    },

    async consultarEstadoViaje() {
        const codigoBoleto = document.getElementById("estado-boleto-codigo").value.trim();
        if (!codigoBoleto) {
            this.setAlerta("estado-viaje-result", "warning", "Ingresa un código de boleto.");
            return;
        }

        this.setAlerta(
            "estado-viaje-result",
            "info",
            '<div class="spinner-border spinner-border-sm me-2" role="status"></div> Consultando estado...'
        );

        try {
            const response = await fetch(`${API_PASAJE}/estado/${encodeURIComponent(codigoBoleto)}`);
            if (!response.ok) throw new Error("Boleto no encontrado");
            const info = await response.json();

            // Mapear estado a colores e iconos
            const estadosMap = {
                'RESERVADO': { color: 'bg-info', icono: 'bi-calendar-check', texto: 'Reservado' },
                'PAGADO': { color: 'bg-primary', icono: 'bi-credit-card', texto: 'Pagado' },
                'FINALIZADO': { color: 'bg-success', icono: 'bi-check-all', texto: 'Finalizado' },
                'EN_RUTA': { color: 'bg-warning text-dark', icono: 'bi-bus-front', texto: 'En ruta' },
                'CANCELADO': { color: 'bg-danger', icono: 'bi-x-circle', texto: 'Cancelado' },
            };
            const estado = estadosMap[info.estado] || { color: 'bg-secondary', icono: 'bi-question-circle', texto: info.estado };

            const pasos = [
                { label: 'Reservado', icono: 'bi-ticket', desc: 'Asiento #' + info.asiento, active: ['RESERVADO','PAGADO','FINALIZADO','EN_RUTA'].includes(info.estado) },
                { label: 'Pagado', icono: 'bi-credit-card', desc: this.formatearPrecio(info.precio), active: ['PAGADO','FINALIZADO','EN_RUTA'].includes(info.estado) },
                { label: 'Finalizado', icono: 'bi-check-circle', desc: info.estado === 'EN_RUTA' ? 'En curso' : info.estado === 'FINALIZADO' ? 'Completado' : 'Pendiente', active: ['FINALIZADO'].includes(info.estado) },
            ];

            document.getElementById("estado-viaje-result").innerHTML = `
              <div class="card border-0 shadow-sm mt-3" style="border-radius: 16px; overflow: hidden;">
                <!-- Cabecera degradada -->
                <div class="p-4 text-white" style="background: linear-gradient(135deg, #0d1b4c, #1a2d6b);">
                  <div class="text-center mb-3">
                    <i class="bi ${estado.icono} display-5 mb-2"></i>
                    <div class="mt-2">
                      <span class="badge ${estado.color} fs-6 px-3 py-2 text-white">${estado.texto}</span>
                    </div>
                  </div>
                  <!-- Ruta completa -->
                  <div class="text-center">
                    <div class="fs-4 fw-bold">${escapeHtml(info.origen)} <span class="text-amarillo">→</span> ${escapeHtml(info.destino)}</div>
                    <div class="d-flex justify-content-center gap-3 mt-2 text-white-50 small">
                      <span><i class="bi bi-calendar me-1"></i>${info.fecha}</span>
                      <span><i class="bi bi-clock me-1"></i>${info.horaSalida}</span>
                      <span><i class="bi bi-bus-front me-1"></i>${escapeHtml(info.tipoBus)}</span>
                    </div>
                  </div>
                </div>

                <div class="card-body p-4">
                  <!-- Timeline de estados -->
                  <div class="timeline mb-4">
                    ${pasos.map((p, idx) => `
                      <div class="timeline-item ${p.active ? 'active' : ''}">
                        <div class="timeline-marker ${p.active ? 'bg-success' : 'bg-secondary'}">
                          <i class="bi ${p.icono}"></i>
                        </div>
                        <div class="timeline-content">
                          <div class="fw-semibold small">${p.label}</div>
                          <div class="text-muted" style="font-size: 11px;">${p.desc}</div>
                        </div>
                      </div>
                    `).join('')}
                  </div>

                  <hr />

                  <!-- Detalles del boleto -->
                  <div class="row g-3">
                    <div class="col-6">
                      <div class="text-muted small text-uppercase fw-semibold">Boleto</div>
                      <code class="fw-bold">${escapeHtml(info.codigoBoleto)}</code>
                    </div>
                    <div class="col-6">
                      <div class="text-muted small text-uppercase fw-semibold">Asiento</div>
                      <div class="fw-bold">#${info.asiento}</div>
                    </div>
                    <div class="col-6">
                      <div class="text-muted small text-uppercase fw-semibold">Pasajero</div>
                      <div class="fw-semibold">${escapeHtml(info.nombrePasajero)}</div>
                    </div>
                    <div class="col-6">
                      <div class="text-muted small text-uppercase fw-semibold">DNI</div>
                      <div>${escapeHtml(info.dniPasajero)}</div>
                    </div>
                    <div class="col-6">
                      <div class="text-muted small text-uppercase fw-semibold">Precio</div>
                      <div class="fw-bold text-primario">${this.formatearPrecio(info.precio)}</div>
                    </div>
                    <div class="col-6">
                      <div class="text-muted small text-uppercase fw-semibold">Detalle</div>
                      <div class="text-muted small">${escapeHtml(info.detalles)}</div>
                    </div>
                  </div>
                </div>
              </div>
            `;
        } catch (error) {
            this.setAlerta("estado-viaje-result", "danger",
                '<i class="bi bi-exclamation-triangle me-2"></i>No se encontró el boleto. Verifica el código e intenta nuevamente.');
        }
    },

    verDetallesViaje(codigoBoleto) {
        const tabBtn = document.querySelector('.btn-tab[data-tab="estado-viaje"]');
        if (tabBtn) tabBtn.click();
        document.getElementById("estado-boleto-codigo").value = codigoBoleto;
        this.consultarEstadoViaje();
    },

    // ─── Inicialización ────────────────────────────────────────

    init() {
        // Cargar rutas disponibles
        this.cargarRutas();

        // Actualizar precio al cambiar cantidad
        const cantidadInput = document.getElementById("cantidad");
        if (cantidadInput) {
            cantidadInput.addEventListener("input", () => {
                this.actualizarPrecioDetalle();
                this.validarDisponibilidadAsientos();
            });
        }

        // Buscar viajes
        const form = document.getElementById("buscarForm");
        if (form) {
            form.addEventListener("submit", (e) => {
                e.preventDefault();

                const origen = document.getElementById("origen")?.value;
                const destino = document.getElementById("destino")?.value;
                const fecha = document.getElementById("fecha")?.value;
                const cantidad = Number(document.getElementById("cantidad")?.value || 1);

                if (!origen || !destino || !fecha) {
                    this.setAlerta("buscar-alert", "warning", "Completa origen, destino y fecha.");
                    return;
                }

                if (origen === destino) {
                    this.setAlerta("buscar-alert", "warning", "Origen y destino no pueden ser iguales.");
                    return;
                }

                this.clearAlerta("buscar-alert");
                this.setLoading(true, "buscar-loading");

                fetch(`${API_PASAJE}/buscar?origen=${encodeURIComponent(origen)}&destino=${encodeURIComponent(destino)}&fecha=${encodeURIComponent(fecha)}&cantidadPasajeros=${encodeURIComponent(cantidad)}`)
                    .then((r) => {
                        if (!r.ok) throw new Error("buscar");
                        return r.json();
                    })
                    .then((lista) => {
                        this.estado.resultados = lista || [];
                        document.getElementById("resultados")?.classList.remove("d-none");
                        this.renderResultados(this.estado.resultados);
                    })
                    .catch(() => {
                        this.setAlerta(
                            "buscar-alert",
                            "danger",
                            "No se pudo buscar viajes. Verifica los datos e inténtalo nuevamente."
                        );
                        document.getElementById("resultados")?.classList.add("d-none");
                    })
                    .finally(() => this.setLoading(false, "buscar-loading"));
            });
        }

        // Confirmar compra (abre modal)
        const compraForm = document.getElementById("compraForm");
        if (compraForm) {
            compraForm.addEventListener("submit", async (e) => {
                e.preventDefault();
                await this.confirmarCompra();
            });
        }

        // Botón de confirmar en modal
        const confirmBtn = document.getElementById("confirmarReservaBtn");
        if (confirmBtn) {
            confirmBtn.addEventListener("click", () => this.ejecutarReserva());
        }
    },
};

document.addEventListener("DOMContentLoaded", () => pasajesUI.init());
