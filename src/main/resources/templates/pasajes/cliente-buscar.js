document.getElementById('buscarForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const loading = document.getElementById('buscar-loading');
    const resultados = document.getElementById('resultados');
    const container = document.getElementById('resultados-cards');

    loading.classList.remove('d-none');
    resultados.classList.add('d-none');
    container.innerHTML = '';

    const params = new URLSearchParams(new FormData(e.target));
    try {
        const res = await fetch(`/api/pasajes/buscar?${params}`);
        const viajes = await res.json();

        if (viajes.length === 0) {
            document.getElementById('buscar-alert').innerHTML = `<div class="alert alert-warning">No se encontraron viajes para esta fecha.</div>`;
        } else {
            viajes.forEach(v => {
                container.innerHTML += `
                    <div class="col-md-6 col-lg-4">
                        <div class="card h-100 border-0 shadow-sm">
                            <div class="card-body">
                                <div class="d-flex justify-content-between mb-2">
                                    <span class="badge bg-light text-primario">${v.tipoBus}</span>
                                    <span class="fw-bold text-success">S/ ${v.precio.toFixed(2)}</span>
                                </div>
                                <h6 class="fw-bold mb-1">${v.origen} → ${v.destino}</h6>
                                <p class="small text-muted mb-3"><i class="bi bi-clock me-1"></i> ${v.hora} - Arr. ${v.horaLlegada || '...'}</p>
                                <button class="btn btn-outline-primario btn-sm w-100 fw-bold" onclick="seleccionarViaje(${v.id}, '${v.origen} - ${v.destino}', '${v.hora}', ${v.precio})">
                                    Seleccionar
                                </button>
                            </div>
                        </div>
                    </div>`;
            });
            resultados.classList.remove('d-none');
        }
    } finally {
        loading.classList.add('d-none');
    }
});

async function seleccionarViaje(id, ruta, hora, precio) {
    document.getElementById('viajeId').value = id;
    document.getElementById('sel-ruta').innerText = ruta;
    document.getElementById('sel-hora').innerText = hora;
    document.getElementById('precioSeleccionado').value = precio;
    document.getElementById('precioResumen').innerText = `S/ ${precio.toFixed(2)}`;

    document.getElementById('resultados').classList.add('d-none');
    document.getElementById('paso-seleccion').classList.remove('d-none');

    renderMap(id);
}

async function renderMap(viajeId) {
    const mapDiv = document.getElementById('seat-map');
    mapDiv.innerHTML = '<div class="spinner-border text-primary m-auto"></div>';

    const res = await fetch(`/api/pasajes/asientos-ocupados/${viajeId}`);
    const ocupados = await res.json();

    mapDiv.innerHTML = '';
    for (let i = 1; i <= 30; i++) {
        const isOccupied = ocupados.includes(i);
        const seat = document.createElement('div');
        seat.className = `seat ${isOccupied ? 'occupied' : 'available'}`;
        seat.innerText = i;
        if (!isOccupied) {
            seat.onclick = () => {
                document.querySelectorAll('.seat.selected').forEach(s => s.classList.remove('selected'));
                seat.classList.add('selected');
                document.getElementById('asientoSeleccionado').value = i;
            };
        }
        mapDiv.appendChild(seat);
    }
}

document.getElementById('compraForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const asiento = document.getElementById('asientoSeleccionado').value;
    if (!asiento) return alert("Por favor, selecciona un asiento.");

    const data = {
        asiento: parseInt(asiento),
        nombrePasajero: document.getElementById('nombrePasajero').value,
        dniPasajero: document.getElementById('dniPasajero').value
    };

    const viajeId = document.getElementById('viajeId').value;

    try {
        const res = await fetch(`/api/pasajes/${viajeId}/reservar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const reserva = await res.json();

        document.getElementById('compraForm').classList.add('d-none');
        document.getElementById('boleto-area').classList.remove('d-none');
        document.getElementById('boleto-codigo').innerText = reserva.codigoBoleto;

        QRCode.toDataURL(reserva.codigoBoleto, { width: 200 }, (err, url) => {
            if (!err) {
                document.getElementById('boleto-qr').src = url;
                const dl = document.getElementById('boleto-descargar');
                dl.href = url;
                dl.download = `Boleto-${reserva.codigoBoleto}.png`;
                dl.onclick = null;
            }
        });
    } catch (e) {
        alert("Error al procesar la reserva.");
    }
});