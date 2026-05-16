/* Pasajes - flujo cliente */

const API_PASAJE = "/api/pasajes";

function escapeHtml(str) {
    return String(str ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "<")
        .replace(/>/g, ">")
        .replace(/\"/g, "\"")
        .replace(/'/g, "&#039;");
}

const pasajesUI = {
    estado: {
        resultados: [],
        viajeSeleccionado: null,
        ocupacion: [],
        asientosSeleccionados: [],
    },

    setLoading(isLoading, containerId) {
        const el = document.getElementById(containerId);
        if (!el) return;
        el.classList.toggle("d-none", !isLoading);
    },

    setAlerta(containerId, tipo, html) {
        const el = document.getElementById(containerId);
        if (!el) return;
        el.innerHTML = `<div class="alert alert-${tipo} mt-2 mb-0">${html}</div>`;
    },

    clearAlerta(containerId) {
        const el = document.getElementById(containerId);
        if (!el) return;
        el.innerHTML = "";
    },

    getCantidadPasajeros() {
        return Number(document.getElementById("cantidad")?.value || 1);
    },

    getPasajerosForm() {
        const pasajerosContainer = document.getElementById("pasajerosContainer");
        if (!pasajerosContainer) return [];
        return Array.from(
            pasajerosContainer.querySelectorAll("[data-pasajero-index]")
        );
    },

    renderResultados(lista) {
        const wrap = document.getElementById("resultados-cards");
        const meta = document.getElementById("resultados-meta");
        if (!wrap) return;

        const cantidad = lista?.length ?? 0;
        if (meta) meta.textContent = `${cantidad} resultado${cantidad === 1 ? "" : "s"}`;

        if (!lista.length) {
            wrap.innerHTML = `
        <div class="col-12">
          <div class="alert alert-warning mb-0">No se encontraron viajes.</div>
        </div>
      `;
            return;
        }

        wrap.innerHTML = lista
            .map(
                (v) => `
        <div class="col-lg-6">
          <div class="card shadow-sm h-100">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-start gap-3">
                <div>
                  <div class="text-muted small">Ruta</div>
                  <div class="fw-bold text-primario">${escapeHtml(v.origen)} → ${escapeHtml(v.destino)}</div>
                  <div class="mt-2 text-muted small">
                    <i class="bi bi-clock me-1"></i>${escapeHtml(v.horaSalida)}
                    <span class="mx-1">•</span> Arr. ${escapeHtml(v.horaLlegada || '---')}
                    <i class="bi bi-calendar me-1"></i>${escapeHtml(v.fecha)}
                  </div>
                  <div class="mt-2">
                    <span class="badge bg-primario">${escapeHtml(v.tipoBus)}</span>
                    <span class="badge bg-light text-primario border ms-1">Total asientos: ${v.totalAsientos}</span>
                  </div>
                </div>
                <div class="text-end">
                  <div class="text-muted small">Precio</div>
                  <div class="fs-4 fw-bold">S/ ${Number(v.precio).toFixed(2)}</div>
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
        document.getElementById("precioResumen").textContent = `S/ ${Number(viaje.precio).toFixed(2)}`;
        document.getElementById("sel-ruta").textContent = `${viaje.origen} → ${viaje.destino}`;
        document.getElementById("sel-hora").textContent = viaje.horaSalida;

        this.setAlerta("buscar-alert", "info", "Cargando asientos ocupados...");
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
            })
            .catch(() => {
                this.setAlerta("buscar-alert", "danger", "Error al cargar la ocupación de asientos.");
                this.renderSeatMap();
                this.renderPasajerosInputs();
            })
            .finally(() => this.setLoading(false, "buscar-loading"));
    },

    renderPasajerosInputs() {
        const container = document.getElementById("pasajerosContainer");
        if (!container) return;

        const cantidad = this.getCantidadPasajeros();
        this.estado.asientosSeleccionados = [];

        container.innerHTML = "";

        for (let i = 0; i < cantidad; i++) {
            container.innerHTML += `
        <div class="col-md-6">
          <label class="form-label fw-semibold">Nombre del pasajero ${i + 1}</label>
          <input
            class="form-control"
            required
            placeholder="Nombre completo"
            name="nombrePasajero-${i}"
            data-pasajero-index="${i}"
          />
        </div>
        <div class="col-md-6">
          <label class="form-label fw-semibold">DNI ${i + 1}</label>
          <input
            class="form-control"
            required
            placeholder="12345678"
            name="dniPasajero-${i}"
            data-pasajero-index="${i}"
          />
        </div>
        `;

            // El seat-map usa asientosSeleccionados[i], por eso el índice debe ser estable.
        }

        // Luego de renderizar, aseguramos que haya campos para mapear.
        // (El renderSeatMap controla el click y luego aquí mismo se puede reflejar).
    },

    // UI selección N asientos distintos
    renderSeatMap() {
        const seatMap = document.getElementById("seat-map");
        const warn = document.getElementById("seat-warning");
        if (!seatMap) return;

        const total = this.estado.viajeSeleccionado?.totalAsientos || 24;
        const cantidadPasajeros = this.getCantidadPasajeros();

        // minivan max 15 asientos (en UI lo limitamos)
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

                    // Toggle: si ya estaba seleccionado, desmarcar
                    if (exists) {
                        this.estado.asientosSeleccionados = this.estado.asientosSeleccionados.filter((x) => x !== seatNum);
                    } else {
                        // No permitir más de N
                        if (this.estado.asientosSeleccionados.length >= cantidadPasajeros) {
                            this.setAlerta(
                                "compra-alert",
                                "warning",
                                `Solo puedes seleccionar hasta ${cantidadPasajeros} asiento(s).`
                            );
                            return;
                        }
                        // No permitir duplicados (ya evitamos con includes)
                        this.estado.asientosSeleccionados.push(seatNum);
                    }

                    // Reordenar para que el índice [0..N-1] sea estable
                    this.estado.asientosSeleccionados.sort((a, b) => a - b);

                    // Refrescar colores selección
                    seatMap.querySelectorAll(".seat.selected").forEach((s) => s.classList.remove("selected"));
                    this.estado.asientosSeleccionados.forEach((s) => {
                        const el = seatMap.querySelector(`.seat[data-asiento='${s}']`);
                        if (el) el.classList.add("selected");
                    });

                    if (warn) {
                        warn.classList.toggle("d-none", this.estado.asientosSeleccionados.length === 0);
                    }

                    // Actualiza inputs placeholder (opcional) - aquí no es necesario
                };
            }

            seatMap.appendChild(div);
        }

        if (warn) warn.classList.remove("d-none");
    },

    collectPasajerosSeleccionados() {
        const cantidad = this.getCantidadPasajeros();
        const formEls = document.getElementById("pasajerosContainer");
        if (!formEls) return [];

        const asientos = this.estado.asientosSeleccionados;
        const pasajeros = [];

        // importante: los inputs dinámicos se renderizan en pares y se guardan como:
        // nombrePasajero-i / dniPasajero-i
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
                `Debes seleccionar exactamente ${pasajerosCantidad} asiento(s).`
            );
            return;
        }

        const pasajeros = this.collectPasajerosSeleccionados();

        for (const p of pasajeros) {
            if (!p.nombrePasajero) {
                this.setAlerta("compra-alert", "warning", "Ingresa nombre de cada pasajero.");
                return;
            }
            if (!p.dniPasajero) {
                this.setAlerta("compra-alert", "warning", "Ingresa DNI de cada pasajero.");
                return;
            }
            if (!p.asiento) {
                this.setAlerta("compra-alert", "warning", "Selecciona asientos suficientes para cada pasajero.");
                return;
            }
        }

        this.setAlerta(
            "compra-alert",
            "info",
            '<div class="spinner-border spinner-border-sm me-2"></div> Reservando...'
        );

        const payload = {
            // El backend (según el endpoint que mencionaste) espera una lista.
            // Aquí lo enviamos como JSON.
            pasajeros,
        };

        try {
            const r = await fetch(`${API_PASAJE}/${encodeURIComponent(viajeId)}/reservar-multiples`, {
                method: "POST",
                headers: { "Content-Type": "application/json;charset=UTF-8" },
                body: JSON.stringify(payload),
            });

            if (!r.ok) throw new Error("reservar-multiples");

            const data = await r.json();

            // data puede venir como {codigos: []} o lista; intentamos flexible.
            const codigos = Array.isArray(data?.codigos)
                ? data.codigos
                : Array.isArray(data)
                    ? data.map((x) => x.codigoBoleto).filter(Boolean)
                    : data?.reservas?.map((x) => x.codigoBoleto).filter(Boolean);

            const boletoArea = document.getElementById("boleto-area");
            const listaEl = document.getElementById("boletosLista");

            boletoArea?.classList.remove("d-none");
            if (listaEl) {
                listaEl.innerHTML = "";
                if (codigos?.length) {
                    listaEl.innerHTML = `<ul class="mb-0">${codigos.map((c) => `<li><code>${c}</code></li>`).join("")}</ul>`;
                } else {
                    listaEl.innerHTML = `<div class="text-muted small">Compra realizada. (No se pudo obtener códigos en la respuesta)</div>`;
                }
            }

            this.setAlerta(
                "compra-alert",
                "success",
                `<i class="bi bi-check-circle-fill me-2"></i>Reserva(s) confirmada(s).`
            );

            // Si quieren QR por cada código, se tendría que generar múltiples QRs.
            // Por ahora, la validación requerida era mostrar los códigos.

        } catch (err) {
            this.setAlerta(
                "compra-alert",
                "danger",
                "No se pudo completar la compra. Verifica tu sesión e intenta nuevamente."
            );
        }
    },

    init() {
        // Buscar viajes
        const form = document.getElementById("buscarForm");
        if (form) {
            form.addEventListener("submit", (e) => {
                e.preventDefault();

                const origen = document.getElementById("origen")?.value?.trim();
                const destino = document.getElementById("destino")?.value?.trim();
                const fecha = document.getElementById("fecha")?.value;
                const cantidad = Number(document.getElementById("cantidad")?.value || 1);

                if (!origen || !destino || !fecha) {
                    this.setAlerta("buscar-alert", "warning", "Completa origen, destino y fecha.");
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

        // Confirmar compra
        const compraForm = document.getElementById("compraForm");
        if (compraForm) {
            compraForm.addEventListener("submit", async (e) => {
                e.preventDefault();
                await this.confirmarCompra();
            });
        }
    },
};

document.addEventListener("DOMContentLoaded", () => pasajesUI.init());

