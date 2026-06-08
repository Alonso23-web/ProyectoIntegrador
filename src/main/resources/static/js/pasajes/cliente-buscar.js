/* ============================================================
   cliente-buscar.js — Flujo de compra de pasajes
   Buscar viaje → Mapa de asientos → Selector horario → Datos pasajeros → Confirmar
   ============================================================ */

const API_PASAJE = "/api/pasajes";
const PRECIO_UNITARIO = 12.00;
const HORAS = ["08:00", "10:00", "13:00", "16:00"];

function escapeHtml(str) {
    return String(str ?? "").replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}

function formatearPrecio(valor) {
    return `S/ ${Number(valor ?? 0).toFixed(2)}`;
}

const pasajesUI = {
    estado: {
        viajeSeleccionado: null,     // { id, hora, totalAsientos, tipoBus }
        ocupacion: [],               // occupied seat numbers
        asientosSeleccionados: [],   // selected seat numbers
        horariosDisponibles: [],     // [{ hora, viajeId, totalAsientos, ocupados, disponibles }]
        origen: "",
        destino: "",
        fecha: "",
        cantidad: 1,
    },

    currentUser: { nombre: "", dni: "" },

    // ─── Utilidades ────────────────────────────────────────

    setAlerta(containerId, tipo, html) {
        const el = document.getElementById(containerId);
        if (!el) return;
        el.innerHTML = `<div class="alert alert-${tipo} d-flex align-items-center gap-2 mt-2 mb-0 py-2 small" role="alert"><i class="bi ${
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

    getCantidad() { return this.estado.cantidad; },

    // ─── Cargar rutas (al iniciar) ─────────────────────────

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
            });

            if (origenSelect.value) origenSelect.dispatchEvent(new Event("change"));
        } catch (e) {
            console.warn("Error cargando rutas:", e);
        }
    },

    // ─── Buscar viajes (submit del formulario) ─────────────

    async buscarViajes(origen, destino, fecha, cantidad) {
        console.log("=== buscarViajes ===", { origen, destino, fecha, cantidad });

        // Ocultar resultados previos
        document.getElementById("resultados-area")?.classList.add("d-none");
        this.clearAlerta("buscar-alert");
        this.estado.viajeSeleccionado = null;
        this.estado.ocupacion = [];
        this.estado.asientosSeleccionados = [];
        this.estado.horariosDisponibles = [];
        document.getElementById("pasajerosContainer").innerHTML = "";
        document.getElementById("compra-alert").innerHTML = "";

        // Mostrar loading
        document.getElementById("buscar-loading")?.classList.remove("d-none");

        try {
            // Llamar al endpoint de disponibilidad horaria
            const url = `${API_PASAJE}/disponibilidad-horaria?origen=${encodeURIComponent(origen)}&destino=${encodeURIComponent(destino)}&fecha=${encodeURIComponent(fecha)}`;
            console.log("Fetching URL:", url);
            const r = await fetch(url);
            if (!r.ok) throw new Error(`Error HTTP ${r.status} al consultar disponibilidad`);

            const data = await r.json();
            console.log("API Response data:", JSON.stringify(data));

            this.estado.origen = origen;
            this.estado.destino = destino;
            this.estado.fecha = fecha;
            this.estado.cantidad = cantidad;
            this.estado.horariosDisponibles = data || [];

            console.log("horariosDisponibles:", this.estado.horariosDisponibles);
            console.log("Cantidad de horarios:", this.estado.horariosDisponibles.length);

            // Mostrar el área de resultados
            document.getElementById("resultados-area")?.classList.remove("d-none");

            // Mostrar badge con cantidad de viajes encontrados
            const foundBadgeContainer = document.getElementById("found-badge");
            const foundCountSpan = document.getElementById("found-count");
            if (foundBadgeContainer && foundCountSpan) {
                foundCountSpan.textContent = this.estado.horariosDisponibles.length;
                foundBadgeContainer.classList.remove("d-none");
            }

            // Renderizar selector horario
            this.renderTimeSelector();

            // Auto-seleccionar el primer horario disponible
            const primerHorario = this.estado.horariosDisponibles.find(h => h.viajeId);
            console.log("Primer horario encontrado:", primerHorario);

            if (primerHorario) {
                console.log("Auto-seleccionando horario:", primerHorario.hora);
                await this.seleccionarHorario(primerHorario.hora);
                console.log("Después de seleccionarHorario, viajeSeleccionado:", this.estado.viajeSeleccionado);
                this.actualizarPanelResumen();
            } else {
                // No hay viajes — mostrar mapa neutral con mensaje
                console.log("No se encontraron viajes para:", origen, destino, fecha);
                this.renderSeatMap([]);
                document.getElementById("pasajerosContainer").innerHTML =
                    '<div class="text-muted small text-center py-3"><i class="bi bi-exclamation-circle me-1"></i>No hay viajes disponibles para esta ruta y fecha. Prueba con otra fecha.</div>';
                this.setAlerta("buscar-alert", "warning",
                    `<i class="bi bi-info-circle me-1"></i>No hay viajes programados para <strong>${escapeHtml(origen)} → ${escapeHtml(destino)}</strong> el <strong>${escapeHtml(fecha)}</strong>. Prueba con otra fecha.`);
                this.actualizarPanelResumen();
            }

            this.clearAlerta("buscar-alert");
        } catch (err) {
            console.error("Error en buscarViajes:", err);
            console.error("Stack:", err.stack);
            this.setAlerta("buscar-alert", "danger", `<i class="bi bi-exclamation-triangle me-1"></i>${escapeHtml(err.message)}`);
            document.getElementById("resultados-area")?.classList.add("d-none");
        } finally {
            document.getElementById("buscar-loading")?.classList.add("d-none");
        }
    },

    // ─── Selector de horario (4 botones) ───────────────────

    renderTimeSelector() {
        const container = document.getElementById("time-selector");
        if (!container) return;

        const horasConInfo = HORAS.map(h => {
            const info = this.estado.horariosDisponibles.find(d => d.hora === h);
            return {
                hora: h,
                disponibles: info ? info.disponibles : 0,
                total: info ? info.totalAsientos : 15,
                viajeId: info ? info.viajeId : null,
                existe: !!info,
            };
        });

        const selectedHora = this.estado.viajeSeleccionado?.hora;

        container.innerHTML = horasConInfo.map(h => {
            const isSelected = h.hora === selectedHora;
            const label = h.existe
                ? `${h.hora} <span class="time-avail">${h.disponibles} libres</span>`
                : `${h.hora} <span class="time-avail text-danger">Sin viaje</span>`;
            return `
                <button class="btn time-btn ${isSelected ? 'active' : ''} ${!h.existe ? 'disabled' : ''}"
                    data-hora="${h.hora}"
                    data-viaje-id="${h.viajeId || ''}"
                    onclick="pasajesUI.seleccionarHorario('${h.hora}')">
                    ${label}
                </button>
            `;
        }).join("");
    },

    async seleccionarHorario(hora) {
        console.log("=== seleccionarHorario ===", { hora });

        const info = this.estado.horariosDisponibles.find(d => d.hora === hora);
        console.log("Info encontrada:", info);

        if (!info || !info.viajeId) {
            console.log("No se encontró info o viajeId para hora:", hora);
            this.setAlerta("compra-alert", "warning", "No hay viaje disponible para este horario.");
            return;
        }

        // Si ya está seleccionado, no hacer nada
        if (this.estado.viajeSeleccionado?.hora === hora) {
            console.log("Ya seleccionado, ignorando");
            return;
        }

        // Limpiar selecciones previas
        this.estado.viajeSeleccionado = { id: info.viajeId, hora: info.hora, totalAsientos: info.totalAsientos, tipoBus: info.tipoBus };
        console.log("viajeSeleccionado AHORA:", this.estado.viajeSeleccionado);
        console.log("hasTime ahora es:", !!this.estado.viajeSeleccionado);

        this.estado.ocupacion = [];
        this.estado.asientosSeleccionados = [];
        document.getElementById("pasajerosContainer").innerHTML = "";
        this.clearAlerta("compra-alert");

        // Actualizar visual del selector
        document.querySelectorAll(".time-btn").forEach(b => b.classList.remove("active"));
        document.querySelector(`.time-btn[data-hora="${hora}"]`)?.classList.add("active");

        // Mostrar resumen parcial
        this.actualizarResumenViaje();
        this.actualizarPanelResumen();

        // Cargar asientos ocupados
        let ocupados = [];
        try {
            const r = await fetch(`${API_PASAJE}/${info.viajeId}/ocupacion`);
            console.log("Respuesta ocupación:", r.status);
            if (r.ok) {
                ocupados = await r.json();
                console.log("Asientos ocupados:", ocupados);
                this.estado.ocupacion = (ocupados || []).map(Number);
            } else {
                console.warn("Error en ocupación:", r.status);
            }
        } catch (e) {
            console.warn("Error al cargar ocupación:", e);
        }

        // Renderizar mapa con ocupación
        console.log("Renderizando seatMap con ocupados:", this.estado.ocupacion, "viajeSeleccionado:", this.estado.viajeSeleccionado);
        this.renderSeatMap(this.estado.ocupacion);
        this.renderPasajerosInputs();
        this.actualizarPanelResumen();
    },

    // ─── Mapa de asientos MINIVAN ──────────────────────────

    renderSeatMap(ocupados) {
        console.log("=== renderSeatMap ===", { ocupados, viajeSeleccionado: this.estado.viajeSeleccionado });
        const seatMap = document.getElementById("seat-map");
        if (!seatMap) {
            console.log("seat-map element not found!");
            return;
        }

        const ocupadosSet = new Set(ocupados);

        // Layout de la minivan:
        // Row 1: [CONDUCTOR(col1-2)] [SEAT1(col4)]
        // Row 2: [SEAT2(col1)] [SEAT3(col2)]    [SEAT4(col4)]
        // Row 3: [SEAT5(col1)] [SEAT6(col2)]    [SEAT7(col4)]
        // Row 4: [SEAT8(col1)] [SEAT9(col2)]    [SEAT10(col4)]
        // Row 5: [SEAT11(col1)] [SEAT12(col2)] [SEAT13(col3)]
        // Row 6: [SEAT14(col1)] [SEAT15(col2)]

        const layout = [
            { row: 1, seatNum: null, col: 1, colSpan: 2, isDriver: true },
            { row: 1, seatNum: 1, col: 4 },
            { row: 2, seatNum: 2, col: 1 },
            { row: 2, seatNum: 3, col: 2 },
            { row: 2, seatNum: 4, col: 4 },
            { row: 3, seatNum: 5, col: 1 },
            { row: 3, seatNum: 6, col: 2 },
            { row: 3, seatNum: 7, col: 4 },
            { row: 4, seatNum: 8, col: 1 },
            { row: 4, seatNum: 9, col: 2 },
            { row: 4, seatNum: 10, col: 4 },
            { row: 5, seatNum: 11, col: 1 },
            { row: 5, seatNum: 12, col: 2 },
            { row: 5, seatNum: 13, col: 3 },
            { row: 6, seatNum: 14, col: 1 },
            { row: 6, seatNum: 15, col: 2 },
        ];

        // Limpiar selecciones al re-renderizar
        this.estado.asientosSeleccionados = [];

        let html = '';
        html += `<div class="minivan-front"><i class="bi bi-bus-front-fill me-1"></i> FRENTE</div>`;
        html += `<div class="minivan-grid">`;

        const hasTime = !!this.estado.viajeSeleccionado;
        console.log("renderSeatMap: hasTime =", hasTime, "viajeSeleccionado =", this.estado.viajeSeleccionado);

        for (let r = 1; r <= 6; r++) {
            const rowClass = r === 5 ? 'minivan-seats-row row-5' : r === 6 ? 'minivan-seats-row row-6' : 'minivan-seats-row';
            html += `<div class="minivan-row"><div class="${rowClass}">`;

            const maxCol = r === 5 ? 3 : 4;
            for (let c = 1; c <= maxCol; c++) {
                const seat = layout.find(s => s.row === r && s.col === c);
                if (seat) {
                    if (seat.isDriver) {
                        html += `<div class="minivan-seat driver-seat" style="grid-column: span 2;" title="Conductor">
                            <i class="bi bi-person-fill"></i>
                            <span class="seat-label">COND</span>
                        </div>`;
                    } else {
                        const num = seat.seatNum;
                        const occupied = ocupadosSet.has(num);
                        const cls = occupied ? "occupied" : (hasTime ? "available" : "neutral");
                        const clickAttr = (!occupied && hasTime) ? `onclick="pasajesUI.toggleSeat(${num})"` : '';
                        html += `<div class="minivan-seat ${cls}" data-asiento="${num}" ${clickAttr} title="Asiento ${num}${occupied ? ' — Ocupado' : ''}">
                            <span class="seat-label">${num}</span>
                        </div>`;
                    }
                } else if (c === 3) {
                    html += `<div class="minivan-aisle" title="Pasillo"></div>`;
                } else {
                    html += `<div class="minivan-seat empty-cell"></div>`;
                }
            }

            html += `</div></div>`;
        }

        html += `</div>`; // .minivan-grid

        // Leyenda
        html += `<div class="minivan-legend d-flex align-items-center gap-3 justify-content-center mt-2">
            ${hasTime ? `<span class="d-flex align-items-center gap-1 small"><span class="legend-dot" style="background: #2ecc71;"></span> Libre</span>` : ''}
            <span class="d-flex align-items-center gap-1 small"><span class="legend-dot" style="background: #adb5bd;"></span> Ocupado</span>
            <span class="d-flex align-items-center gap-1 small"><span class="legend-dot" style="background: #f5a623;"></span> 1er asiento</span>
            <span class="d-flex align-items-center gap-1 small"><span class="legend-dot" style="background: #fce4a8;"></span> Siguientes</span>
        </div>`;

        // Mensaje si no hay horario seleccionado
        if (!hasTime) {
            html += `<div class="text-center text-muted small mt-2"><i class="bi bi-hand-index-thumb me-1"></i>Selecciona un horario debajo para ver disponibilidad</div>`;
        }

        seatMap.innerHTML = html;
        console.log("SeatMap rendered, hasTime =", hasTime);
    },

    // ─── Selección de asientos ─────────────────────────────

    toggleSeat(seatNum) {
        if (this.estado.ocupacion.includes(seatNum)) return;

        const cantidad = this.getCantidad();
        const idx = this.estado.asientosSeleccionados.indexOf(seatNum);

        if (idx !== -1) {
            // Ya seleccionado → deseleccionar
            this.estado.asientosSeleccionados.splice(idx, 1);
        } else {
            // Nuevo asiento
            if (this.estado.asientosSeleccionados.length >= cantidad) {
                // Máximo alcanzado: deseleccionar el más antiguo (primero) y agregar el nuevo
                this.estado.asientosSeleccionados.shift();
            }
            this.estado.asientosSeleccionados.push(seatNum);
        }

        this.estado.asientosSeleccionados.sort((a, b) => a - b);
        this.updateSeatVisuals();
        this.renderPasajerosInputs();
        this.actualizarPanelResumen();
        this.clearAlerta("compra-alert");
    },

    updateSeatVisuals() {
        const selected = this.estado.asientosSeleccionados;
        document.querySelectorAll(".minivan-seat[data-asiento]").forEach(el => {
            const num = parseInt(el.dataset.asiento);
            const selIdx = selected.indexOf(num);

            el.classList.remove("selected", "selected-first", "selected-other");
            el.style.background = "";
            el.style.boxShadow = "";
            el.style.color = "";
            const badge = el.querySelector(".passenger-badge");
            if (badge) badge.remove();

            if (selIdx !== -1) {
                el.classList.add("selected");
                const isFirst = selIdx === 0;
                el.classList.add(isFirst ? "selected-first" : "selected-other");
                el.style.background = isFirst ? "#f5a623" : "#fce4a8";
                el.style.boxShadow = isFirst
                    ? "0 0 0 3px rgba(245, 166, 35, 0.5), 0 4px 12px rgba(245, 166, 35, 0.25)"
                    : "0 0 0 3px rgba(252, 228, 168, 0.5)";
                el.style.color = isFirst ? "#fff" : "#8b6914";

                // Badge con número de pasajero
                const bdg = document.createElement("span");
                bdg.className = "passenger-badge";
                bdg.textContent = `P${selIdx + 1}`;
                el.appendChild(bdg);
            } else {
                // Restaurar color según estado (usando clases, no inline)
                el.className = el.className
                    .replace(/ selected/g, '')
                    .replace(/ selected-first/g, '')
                    .replace(/ selected-other/g, '');
                // Mantener la clase original: 'minivan-seat available' o 'minivan-seat occupied'
                if (this.estado.ocupacion.includes(num)) {
                    el.className = 'minivan-seat occupied';
                } else if (this.estado.viajeSeleccionado) {
                    el.className = 'minivan-seat available';
                } else {
                    el.className = 'minivan-seat neutral';
                }
            }
        });
    },

    // ─── Inputs de pasajeros ───────────────────────────────

    renderPasajerosInputs() {
        const container = document.getElementById("pasajerosContainer");
        if (!container) return;

        const cantidad = this.getCantidad();
        const seats = this.estado.asientosSeleccionados;

        if (seats.length === 0) {
            container.innerHTML = `<div class="text-muted small text-center py-3"><i class="bi bi-hand-index-thumb me-1"></i>Selecciona asientos en el mapa para registrar pasajeros</div>`;
            return;
        }

        let html = "";
        for (let i = 0; i < cantidad; i++) {
            const seatNum = seats[i];
            if (!seatNum) {
                html += `
                    <div class="pasajero-card border rounded-3 p-3 mb-2" data-pasajero-index="${i}">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <div class="fw-semibold small" style="color: #adb5c0;">
                                <i class="bi bi-person-circle me-1"></i>Pasajero ${i + 1}
                            </div>
                            <span class="text-muted small"><i class="bi bi-hand-index-thumb me-1"></i>Selecciona asiento</span>
                        </div>
                        <div class="row g-2">
                            <div class="col-md-6">
                                <label class="form-label-xs text-muted">Nombre completo</label>
                                <input class="form-control form-control-sm" placeholder="Nombre completo" name="nombrePasajero-${i}" disabled />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label-xs text-muted">DNI</label>
                                <input class="form-control form-control-sm" placeholder="DNI" name="dniPasajero-${i}" disabled />
                            </div>
                        </div>
                    </div>
                `;
                continue;
            }

            const isFirst = i === 0;
            const autoFillName = isFirst && this.currentUser.nombre ? this.currentUser.nombre : "";
            const autoFillDni = isFirst && this.currentUser.dni ? this.currentUser.dni : "";

            html += `
                <div class="pasajero-card border rounded-3 p-3 mb-2" data-pasajero-index="${i}">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <div class="fw-semibold small" style="color: ${isFirst ? '#f5a623' : '#b8860b'};">
                            <i class="bi bi-person-circle me-1"></i>Pasajero ${i + 1}
                        </div>
                        <span class="badge seat-badge" style="background: ${isFirst ? '#f5a623' : '#fce4a8'}; color: ${isFirst ? '#fff' : '#8b6914'};">
                            <i class="bi bi-seat me-1"></i>Asiento ${seatNum}
                        </span>
                    </div>
                    <div class="row g-2">
                        <div class="col-md-6">
                            <label class="form-label-xs text-muted">Nombre completo</label>
                            <input class="form-control form-control-sm" required
                                placeholder="Nombre completo"
                                name="nombrePasajero-${i}"
                                data-pasajero-index="${i}"
                                value="${escapeHtml(autoFillName)}" />
                        </div>
                        <div class="col-md-6">
                            <label class="form-label-xs text-muted">DNI</label>
                            <input class="form-control form-control-sm" required
                                placeholder="DNI"
                                name="dniPasajero-${i}"
                                data-pasajero-index="${i}"
                                value="${escapeHtml(autoFillDni)}" />
                        </div>
                    </div>
                </div>
            `;
        }

        container.innerHTML = html;

        const seatWarning = document.getElementById("seat-warning");
        if (seatWarning) {
            const pending = cantidad - seats.filter(s => s).length;
            if (pending > 0) {
                seatWarning.classList.remove("d-none");
                seatWarning.innerHTML = `<i class="bi bi-hand-index-thumb me-1"></i>Faltan <strong>${pending}</strong> asiento${pending === 1 ? "" : "s"} por seleccionar.`;
            } else {
                seatWarning.classList.add("d-none");
            }
        }
    },

    // ─── Cantidad de pasajeros ─────────────────────────────

    cambiarCantidad(delta) {
        const nuevo = Math.max(1, Math.min(13, this.estado.cantidad + delta));
        if (nuevo === this.estado.cantidad) return;

        this.estado.cantidad = nuevo;
        document.getElementById("cantidad").value = nuevo;

        // Si se reduce, eliminar asientos sobrantes
        if (this.estado.asientosSeleccionados.length > nuevo) {
            this.estado.asientosSeleccionados = this.estado.asientosSeleccionados.slice(0, nuevo);
        }

        document.getElementById("cantidadDisplay").textContent = nuevo;
        this.clearAlerta("compra-alert");
        this.updateSeatVisuals();
        this.renderPasajerosInputs();
        this.actualizarPanelResumen();
    },

    // ─── Panel de resumen sticky ───────────────────────────

    actualizarResumenViaje() {
        const v = this.estado.viajeSeleccionado;
        const rutaEl = document.getElementById("resumen-ruta");
        const horaEl = document.getElementById("resumen-hora");
        if (rutaEl) rutaEl.textContent = v ? `${this.estado.origen} → ${this.estado.destino}` : "—";
        if (horaEl) horaEl.textContent = v ? v.hora : "—";
        const fechaEl = document.getElementById("resumen-fecha");
        if (fechaEl) fechaEl.textContent = this.estado.fecha || "—";
        const precioEl = document.getElementById("sel-precio-unitario");
        if (precioEl) precioEl.textContent = formatearPrecio(PRECIO_UNITARIO);
    },

    actualizarPanelResumen() {
        const v = this.estado.viajeSeleccionado;
        const seats = this.estado.asientosSeleccionados;
        const cantidad = this.getCantidad();

        // Ruta
        document.getElementById("panel-ruta").textContent = this.estado.origen
            ? `${this.estado.origen} → ${this.estado.destino}` : "—";
        document.getElementById("panel-fecha").textContent = this.estado.fecha || "—";
        document.getElementById("panel-hora").textContent = v ? v.hora : "—";

        // Asientos seleccionados
        const seatsContainer = document.getElementById("panel-asientos");
        if (!v || seats.length === 0) {
            seatsContainer.innerHTML = `<span class="text-muted small">Ninguno</span>`;
        } else {
            seatsContainer.innerHTML = seats.map((s, i) =>
                `<span class="badge seat-badge me-1 mb-1" style="background: ${i === 0 ? '#f5a623' : '#fce4a8'}; color: ${i === 0 ? '#fff' : '#8b6914'};">Asiento ${s}</span>`
            ).join("");
        }

        // Cantidad y precio
        const total = cantidad * PRECIO_UNITARIO;
        document.getElementById("panel-cantidad").textContent = `${cantidad} pasajero${cantidad === 1 ? "" : "s"}`;
        document.getElementById("panel-total").textContent = formatearPrecio(total);
        document.getElementById("panel-detalle-precio").textContent =
            `${formatearPrecio(PRECIO_UNITARIO)} x ${cantidad} = ${formatearPrecio(total)}`;

        // Botón confirmar
        const btn = document.getElementById("btn-confirmar-compra");
        if (btn) {
            const allSelected = v && seats.length === cantidad;
            btn.disabled = !allSelected;
        }

        // Actualizar resumen del viaje (sección 2)
        this.actualizarResumenViaje();

        // Marcar la sección activa en el timeline visual
        document.querySelectorAll(".step-indicator").forEach(el => el.classList.remove("active"));
        if (v) document.querySelector(".step-indicator[data-step='2']")?.classList.add("active");
        if (seats.length > 0) document.querySelector(".step-indicator[data-step='3']")?.classList.add("active");
    },

    // ─── Validación y envío ────────────────────────────────

    async confirmarCompra() {
        const v = this.estado.viajeSeleccionado;
        if (!v) {
            this.setAlerta("compra-alert", "warning", "Selecciona un horario primero.");
            return;
        }

        const cantidad = this.getCantidad();
        const seats = this.estado.asientosSeleccionados;

        if (seats.length !== cantidad) {
            this.setAlerta("compra-alert", "warning",
                `<i class="bi bi-info-circle me-1"></i>Selecciona exactamente <strong>${cantidad}</strong> asiento${cantidad === 1 ? "" : "s"} en el mapa.`);
            return;
        }

        const pasajeros = this.collectPasajeros();
        for (let i = 0; i < pasajeros.length; i++) {
            const p = pasajeros[i];
            if (!p.nombrePasajero || p.nombrePasajero.trim() === "") {
                this.setAlerta("compra-alert", "warning", `Completa el nombre del pasajero ${i + 1} (asiento ${seats[i]}).`);
                const card = document.querySelector(`.pasajero-card[data-pasajero-index="${i}"]`);
                if (card) card.scrollIntoView({ behavior: "smooth", block: "center" });
                return;
            }
            if (!p.dniPasajero || p.dniPasajero.trim() === "") {
                this.setAlerta("compra-alert", "warning", `Completa el DNI del pasajero ${i + 1} (asiento ${seats[i]}).`);
                const card = document.querySelector(`.pasajero-card[data-pasajero-index="${i}"]`);
                if (card) card.scrollIntoView({ behavior: "smooth", block: "center" });
                return;
            }
        }

        // Mostrar modal de confirmación
        this.mostrarModalConfirmacion();
    },

    mostrarModalConfirmacion() {
        const cantidad = this.getCantidad();
        const total = cantidad * PRECIO_UNITARIO;
        const seats = this.estado.asientosSeleccionados;

        document.getElementById("modal-ruta").textContent = `${this.estado.origen} → ${this.estado.destino}`;
        document.getElementById("modal-fecha").textContent = this.estado.fecha;
        document.getElementById("modal-hora").textContent = this.estado.viajeSeleccionado?.hora || "—";
        document.getElementById("modal-cantidad").textContent = cantidad;
        document.getElementById("modal-total").textContent = formatearPrecio(total);
        document.getElementById("modal-precio-unitario").textContent = formatearPrecio(PRECIO_UNITARIO);
        document.getElementById("modal-asientos").textContent = seats.join(", ");

        const modal = new bootstrap.Modal(document.getElementById("confirmModal"));
        modal.show();
    },

    collectPasajeros() {
        const cantidad = this.getCantidad();
        const container = document.getElementById("pasajerosContainer");
        if (!container) return [];

        const seats = this.estado.asientosSeleccionados;
        const pasajeros = [];

        for (let i = 0; i < cantidad; i++) {
            const nombreInput = container.querySelector(`input[name='nombrePasajero-${i}']`);
            const dniInput = container.querySelector(`input[name='dniPasajero-${i}']`);
            pasajeros.push({
                nombrePasajero: nombreInput?.value?.trim() || "",
                dniPasajero: dniInput?.value?.trim() || "",
                asiento: seats[i] || 0,
            });
        }
        return pasajeros;
    },

    async ejecutarReserva() {
        const viajeId = this.estado.viajeSeleccionado?.id;
        if (!viajeId) return;

        const pasajeros = this.collectPasajeros();

        const modal = bootstrap.Modal.getInstance(document.getElementById("confirmModal"));
        if (modal) modal.hide();

        this.setAlerta("compra-alert", "info",
            '<div class="spinner-border spinner-border-sm me-2" role="status"></div> Reservando...');

        const btnConfirmar = document.getElementById("btn-confirmar-compra");
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
                let errorMsg = "No se pudo completar la compra.";
                try {
                    const errData = await r.text();
                    if (errData) errorMsg = errData;
                } catch (e) { }
                throw new Error(errorMsg);
            }

            // Éxito — redirigir al historial
            window.location.href = "/pasajes/cliente?tab=mis-viajes";

        } catch (err) {
            this.setAlerta("compra-alert", "danger",
                `<i class="bi bi-exclamation-triangle-fill me-2"></i>${escapeHtml(err.message)}`);
        } finally {
            if (btnConfirmar) {
                btnConfirmar.disabled = false;
                btnConfirmar.innerHTML = '<i class="bi bi-credit-card me-2"></i>Confirmar compra';
            }
        }
    },

    // ─── Mis Viajes ────────────────────────────────────────

    async cargarMisViajes() {
        const resultEl = document.getElementById("mis-viajes-result");
        if (!resultEl) return;

        resultEl.innerHTML = `<div class="alert alert-info d-flex align-items-center gap-2 mb-0 py-2 small" role="alert">
            <div class="spinner-border spinner-border-sm" role="status"></div>
            <span>Cargando tus viajes...</span>
        </div>`;

        try {
            const response = await fetch(`${API_PASAJE}/mis`);
            if (!response.ok) throw new Error("Error");
            const misViajes = await response.json();

            if (misViajes.length === 0) {
                resultEl.innerHTML = `<div class="alert alert-warning mb-0 py-2 small"><i class="bi bi-inbox me-2"></i>No tienes viajes registrados aún.</div>`;
                return;
            }

            let html = `<div class="row g-3">`;
            misViajes.forEach(reserva => {
                const estado = reserva.estado;
                const e = {
                    'RESERVADO': { color: 'bg-info', icono: 'bi-ticket', texto: 'Reservado' },
                    'PAGADO': { color: 'bg-primary', icono: 'bi-credit-card', texto: 'Pagado' },
                    'FINALIZADO': { color: 'bg-success', icono: 'bi-check-circle', texto: 'Finalizado' },
                }[estado] || { color: 'bg-secondary', icono: 'bi-question', texto: estado };

                html += `
                    <div class="col-md-6">
                        <div class="card border-0 shadow-sm h-100" style="border-radius: 16px;">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-start mb-3">
                                    <div>
                                        <span class="badge ${e.color} text-white mb-1"><i class="bi ${e.icono} me-1"></i>${e.texto}</span>
                                        <h6 class="fw-bold mt-1 mb-0" style="color: #0d1b3e;">${reserva.origen} → ${reserva.destino}</h6>
                                    </div>
                                    <code class="small">${reserva.codigoBoleto}</code>
                                </div>
                                <div class="d-flex gap-3 text-muted small mb-3 flex-wrap">
                                    <span><i class="bi bi-calendar me-1"></i>${reserva.fechaViaje}</span>
                                    <span><i class="bi bi-clock me-1"></i>${reserva.horaViaje}</span>
                                    <span><i class="bi bi-person me-1"></i>${reserva.nombrePasajero}</span>
                                    <span><i class="bi bi-seat me-1"></i>Asiento ${reserva.asiento}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
            });
            html += `</div>`;
            resultEl.innerHTML = html;
        } catch (error) {
            resultEl.innerHTML = `<div class="alert alert-danger mb-0 py-2 small"><i class="bi bi-exclamation-triangle me-2"></i>Error al cargar tus viajes.</div>`;
        }
    },

    // ─── Estado de Viaje ───────────────────────────────────

    async consultarEstadoViaje() {
        const codigo = document.getElementById("estado-boleto-codigo")?.value?.trim();
        if (!codigo) {
            this.setAlerta("estado-viaje-result", "warning", "Ingresa un código de boleto.");
            return;
        }

        this.setAlerta("estado-viaje-result", "info",
            '<div class="spinner-border spinner-border-sm me-2" role="status"></div> Consultando...');

        try {
            const response = await fetch(`${API_PASAJE}/estado/${encodeURIComponent(codigo)}`);
            if (!response.ok) throw new Error("No encontrado");
            const info = await response.json();

            const estadosMap = {
                'RESERVADO': { color: 'bg-info', icono: 'bi-calendar-check', texto: 'Reservado' },
                'PAGADO': { color: 'bg-primary', icono: 'bi-credit-card', texto: 'Pagado' },
                'FINALIZADO': { color: 'bg-success', icono: 'bi-check-all', texto: 'Finalizado' },
                'EN_RUTA': { color: 'bg-warning text-dark', icono: 'bi-bus-front', texto: 'En ruta' },
                'CANCELADO': { color: 'bg-danger', icono: 'bi-x-circle', texto: 'Cancelado' },
            };
            const e = estadosMap[info.estado] || { color: 'bg-secondary', icono: 'bi-question-circle', texto: info.estado };

            document.getElementById("estado-viaje-result").innerHTML = `
                <div class="card border-0 shadow-sm mt-3" style="border-radius: 16px; overflow: hidden;">
                    <div class="p-4 text-white" style="background: linear-gradient(135deg, #0d1b3e, #1a2d6b);">
                        <div class="text-center mb-3">
                            <i class="bi ${e.icono} display-5 mb-2"></i>
                            <div class="mt-2"><span class="badge ${e.color} fs-6 px-3 py-2 text-white">${e.texto}</span></div>
                        </div>
                        <div class="text-center">
                            <div class="fs-4 fw-bold">${escapeHtml(info.origen)} <span class="text-amarillo">→</span> ${escapeHtml(info.destino)}</div>
                            <div class="d-flex justify-content-center gap-3 mt-2 text-white-50 small">
                                <span><i class="bi bi-calendar me-1"></i>${info.fecha}</span>
                                <span><i class="bi bi-clock me-1"></i>${info.horaSalida}</span>
                            </div>
                        </div>
                    </div>
                    <div class="card-body p-4">
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
                                <div class="fw-bold" style="color: #0d1b3e;">${formatearPrecio(info.precio)}</div>
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
                '<i class="bi bi-exclamation-triangle me-2"></i>No se encontró el boleto.');
        }
    },

    verDetallesViaje(codigoBoleto) {
        const tabBtn = document.querySelector('.btn-tab[data-tab="estado-viaje"]');
        if (tabBtn) tabBtn.click();
        const input = document.getElementById("estado-boleto-codigo");
        if (input) input.value = codigoBoleto;
        this.consultarEstadoViaje();
    },

    // ─── Inicialización ────────────────────────────────────

    init() {
        const body = document.body;
        this.currentUser.nombre = body.dataset.currentUserName || "";
        this.currentUser.dni = body.dataset.currentUserDni || "";

        this.cargarRutas();

        // Buscar viajes
        const form = document.getElementById("buscarForm");
        if (form) {
            form.addEventListener("submit", (e) => {
                e.preventDefault();
                const origen = document.getElementById("origen")?.value;
                const destino = document.getElementById("destino")?.value;
                const fecha = document.getElementById("fecha")?.value;
                const cantidad = parseInt(document.getElementById("cantidad")?.value) || 1;

                if (!origen || !destino || !fecha) {
                    this.setAlerta("buscar-alert", "warning", "Completa origen, destino y fecha.");
                    return;
                }
                if (origen === destino) {
                    this.setAlerta("buscar-alert", "warning", "Origen y destino no pueden ser iguales.");
                    return;
                }
                if (cantidad > 13) {
                    this.setAlerta("buscar-alert", "warning", "Máximo 13 pasajeros por compra.");
                    return;
                }

                this.buscarViajes(origen, destino, fecha, cantidad);
            });
        }

        // Cambiar cantidad (botones + y -)
        const cantidadInput = document.getElementById("cantidad");
        if (cantidadInput) {
            cantidadInput.addEventListener("change", () => {
                const val = parseInt(cantidadInput.value) || 1;
                const oldCant = this.estado.cantidad;
                this.estado.cantidad = Math.max(1, Math.min(13, val));
                cantidadInput.value = this.estado.cantidad;
                // Si se redujo, eliminar asientos sobrantes
                if (this.estado.asientosSeleccionados.length > this.estado.cantidad) {
                    this.estado.asientosSeleccionados = this.estado.asientosSeleccionados.slice(0, this.estado.cantidad);
                }
                // Si cambió, actualizar visual
                if (this.estado.cantidad !== oldCant) {
                    document.getElementById("cantidadDisplay").textContent = this.estado.cantidad;
                    this.updateSeatVisuals();
                    this.renderPasajerosInputs();
                    this.actualizarPanelResumen();
                }
            });
        }

        // Confirmar compra
        const btnConfirmar = document.getElementById("btn-confirmar-compra");
        if (btnConfirmar) {
            btnConfirmar.addEventListener("click", (e) => {
                e.preventDefault();
                this.confirmarCompra();
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
