/* Pasajes - flujo cliente */

const API_PASAJE = "/api/pasajes"; // Asegúrate de que esta URL sea correcta

function escapeHtml(str) {
    return String(str ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "<")
        .replace(/>/g, ">")
        .replace(/\"/g, "\"")
        .replace(/'/g, "&#039;");
}
// pasajesUI es un objeto global que contendrá toda la lógica de UI para pasajes
const pasajesUI = {
    estado: { resultados: [], viajeSeleccionado: null, ocupacion: [], asientoSeleccionado: null },

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
        this.estado.asientoSeleccionado = null;
        this.estado.ocupacion = [];

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
            })
            .catch(() => {
                this.setAlerta("buscar-alert", "danger", "Error al cargar la ocupación de asientos.");
                this.renderSeatMap();
            })
            .finally(() => this.setLoading(false, "buscar-loading"));
    },

    renderSeatMap() {
        const seatMap = document.getElementById("seat-map");
        const warn = document.getElementById("seat-warning");
        if (!seatMap) return;

        const total = this.estado.viajeSeleccionado?.totalAsientos || 24;
        const ocupadosSet = new Set(this.estado.ocupacion);

        seatMap.innerHTML = "";

        for (let i = 1; i <= total; i++) {
            const occupied = ocupadosSet.has(i);
            const div = document.createElement("div");
            div.className = `seat ${occupied ? "occupied" : "available"}`;
            div.textContent = i;
            div.dataset.asiento = String(i);

            if (!occupied) {
                div.onclick = () => {
                    seatMap.querySelectorAll(".seat.selected").forEach((s) => s.classList.remove("selected"));
                    div.classList.add("selected");
                    this.estado.asientoSeleccionado = i;
                    document.getElementById("asientoSeleccionado").value = String(i);
                    if (warn) warn.classList.add("d-none");
                };
            }

            seatMap.appendChild(div);
        }

        if (warn && this.estado.asientoSeleccionado == null) {
            warn.classList.remove("d-none");
        }
    },

    init() { // Esta función se llama al cargar el DOM
        // Nota: QRCode debe cargarse desde la página (CDN). Evitamos carga dinámica aquí
        // para no introducir dependencias/errores de parseo.
        // Si no está disponible, la compra igual se completa y el QR se mostrará cuando exista la librería.
        if (window.QRCode === undefined) {
            // noop
        }

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

                this.clearAlerta("buscar-alert"); // Limpiar alertas previas
                this.setLoading(true, "buscar-loading");

                fetch(`${API_PASAJE}/buscar?origen=${encodeURIComponent(origen)}&destino=${encodeURIComponent(destino)}&fecha=${encodeURIComponent(fecha)}&cantidadPasajeros=${encodeURIComponent(cantidad)}`)
                    .then((r) => {
                        if (!r.ok) throw new Error("buscar");
                        return r.json();
                    })
                    .then((lista) => {
                        // Guardar resultados para poder seleccionar viaje
                        this.estado.resultados = lista || [];
                        document.getElementById("resultados")?.classList.remove("d-none");
                        this.renderResultados(this.estado.resultados);
                    })
                    .catch(() => {
                        this.setAlerta("buscar-alert", "danger", "No se pudo buscar viajes. Verifica los datos e inténtalo nuevamente.");
                        document.getElementById("resultados")?.classList.add("d-none");
                    })
                    .finally(() => this.setLoading(false, "buscar-loading"));
            });
        }

        const compraForm = document.getElementById("compraForm");
        if (compraForm) {
            compraForm.addEventListener("submit", async (e) => {
                e.preventDefault();

                const viajeId = document.getElementById("viajeId")?.value;
                const precio = document.getElementById("precioSeleccionado")?.value;
                const asiento = document.getElementById("asientoSeleccionado")?.value;
                const nombre = document.getElementById("nombrePasajero")?.value?.trim();
                const dni = document.getElementById("dniPasajero")?.value?.trim();

                if (!viajeId) return this.setAlerta("compra-alert", "warning", "Selecciona un viaje primero.");
                if (!asiento) return this.setAlerta("compra-alert", "warning", "Selecciona un asiento.");
                if (!nombre) return this.setAlerta("compra-alert", "warning", "Ingresa el nombre del pasajero.");
                if (!dni) return this.setAlerta("compra-alert", "warning", "Ingresa el DNI.");

                const precioNum = Number(precio);
                if (!Number.isFinite(precioNum) || precioNum <= 0) {
                    return this.setAlerta("compra-alert", "warning", "Precio inválido. Intenta nuevamente.");
                }

                this.setAlerta(
                    "compra-alert",
                    "info",
                    '<div class="spinner-border spinner-border-sm me-2"></div> Reservando asiento y generando boleto...'
                );

                try {
                    const r = await fetch(`${API_PASAJE}/${encodeURIComponent(viajeId)}/reservar`, {
                        method: "POST",
                        headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" }, // Formato esperado por @RequestParam
                        body: new URLSearchParams({
                            asiento: String(asiento),
                            precio: String(precioNum),
                            nombrePasajero: nombre,
                            dniPasajero: dni,
                            // No se envía viajeId aquí, ya está en la URL
                        }).toString(),
                    });

                    if (!r.ok) throw new Error("reservar");
                    const reserva = await r.json();

                    if (!reserva || !reserva.codigoBoleto) {
                        throw new Error("codigoBoleto");
                    }

                    document.getElementById("boleto-codigo").textContent = reserva.codigoBoleto; // Mostrar código de boleto
                    document.getElementById("boleto-area").classList.remove("d-none");

                    const qrTarget = document.getElementById("boleto-qr");

                    // Si existe librería QR en la página, generamos QR.
                    if (window.QRCode && qrTarget) {
                        await window.QRCode.toDataURL(reserva.codigoBoleto, { margin: 1, width: 220, errorCorrectionLevel: "M" })
                            .then((url) => {
                                qrTarget.src = url; // Mostrar QR
                                qrTarget.dataset.qrDataUrl = url;
                            });
                    } else if (qrTarget) {
                        qrTarget.src = "https://via.placeholder.com/200?text=QR+Generado";
                    }

                    const btnDesc = document.getElementById("boleto-descargar");
                    btnDesc.href = qrTarget.src;
                    btnDesc.download = `boleto-${reserva.codigoBoleto}.png`;
                    btnDesc.onclick = null; // Quitar el preventDefault previo

                    this.setAlerta(
                        "compra-alert",
                        "success",
                        `\n                        <i class="bi bi-check-circle-fill me-2"></i>Reserva confirmada. Boleto listo.\n                    `
                    );
                } catch (err) {
                    this.setAlerta(
                        "compra-alert",
                        "danger", // Mensaje de error
                        "No se pudo completar la compra. Verifica tu sesión e intenta nuevamente."
                    );
                }
            });
        }
    },
};
document.addEventListener("DOMContentLoaded", () => pasajesUI.init()); // Inicializar la UI al cargar el DOM
