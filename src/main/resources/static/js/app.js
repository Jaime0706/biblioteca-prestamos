'use strict';

// Si se abre index.html directamente desde el disco (file://), se usa la URL completa del backend.
const API = location.protocol === 'file:' ? 'http://localhost:8081/api' : '/api';

const $ = (id) => document.getElementById(id);

/** Escapa texto antes de insertarlo en el HTML (evita inyección de código). */
const esc = (valor) => String(valor ?? '').replace(/[&<>"']/g, (c) =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

const fecha = (iso) => {
    if (!iso) return '—';
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
};

// ---------------------------------------------------------------- comunicación con la API
async function api(ruta, metodo = 'GET', cuerpo = null) {
    const opciones = { method: metodo, headers: {} };
    if (cuerpo !== null) {
        opciones.headers['Content-Type'] = 'application/json';
        opciones.body = JSON.stringify(cuerpo);
    }

    let respuesta;
    try {
        respuesta = await fetch(API + ruta, opciones);
    } catch (e) {
        throw { message: 'No se pudo conectar con el servidor. Verifique que el backend esté iniciado (puerto 8081).', errors: {} };
    }

    const texto = await respuesta.text();
    let datos = null;
    if (texto) {
        try { datos = JSON.parse(texto); } catch (e) { datos = null; }
    }

    if (!respuesta.ok) {
        throw {
            message: (datos && datos.message) || `Error ${respuesta.status} al procesar la solicitud.`,
            errors: (datos && datos.errors) || {}
        };
    }
    return datos;
}

// ---------------------------------------------------------------- mensajes
let temporizadorMensaje = null;

function mostrarMensaje(tipo, texto, errores = {}) {
    const caja = $('mensaje');
    const detalle = Object.entries(errores);
    caja.className = 'mensaje ' + (tipo === 'exito' ? 'exito' : 'error');
    caja.innerHTML = esc(texto) + (detalle.length
        ? '<ul>' + detalle.map(([campo, msg]) => `<li>${esc(msg)}</li>`).join('') + '</ul>'
        : '');
    caja.hidden = false;
    clearTimeout(temporizadorMensaje);
    if (tipo === 'exito') {
        temporizadorMensaje = setTimeout(() => { caja.hidden = true; }, 5000);
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function ocultarMensaje() { $('mensaje').hidden = true; }

const CAMPOS = {
    titulo: 'libro-titulo', autor: 'libro-autor', isbn: 'libro-isbn',
    libroId: 'prestamo-libro', nombreLector: 'prestamo-lector', diasPlazo: 'prestamo-dias'
};

function limpiarErroresCampos() {
    document.querySelectorAll('.field-error').forEach((e) => { e.textContent = ''; });
    document.querySelectorAll('.invalid').forEach((e) => e.classList.remove('invalid'));
}

function marcarErroresCampos(errores) {
    Object.entries(errores || {}).forEach(([campo, msg]) => {
        const aviso = $('err-' + campo);
        if (aviso) aviso.textContent = msg;
        const input = $(CAMPOS[campo]);
        if (input) input.classList.add('invalid');
    });
}

function manejarError(e) {
    marcarErroresCampos(e.errors);
    mostrarMensaje('error', e.message || 'Ocurrió un error inesperado.', e.errors);
}

// ---------------------------------------------------------------- pestañas
function irAPestana(nombre) {
    document.querySelectorAll('.tab').forEach((t) => t.classList.toggle('active', t.dataset.tab === nombre));
    ['resumen', 'libros', 'prestamos'].forEach((p) => { $('tab-' + p).hidden = (p !== nombre); });
    if (nombre === 'resumen') cargarIndicadores();
    if (nombre === 'libros') cargarLibros();
    if (nombre === 'prestamos') { cargarLibrosDisponiblesSelect(); cargarPrestamos(); }
}

document.querySelectorAll('.tab').forEach((t) =>
    t.addEventListener('click', () => { ocultarMensaje(); irAPestana(t.dataset.tab); }));

// ---------------------------------------------------------------- resumen / indicadores
async function cargarIndicadores() {
    try {
        const i = await api('/prestamos/indicadores');
        const tarjetas = [
            ['Libros activos', i.librosActivos],
            ['Libros disponibles', i.librosDisponibles],
            ['Libros prestados', i.librosPrestados],
            ['% de libros prestados', i.porcentajeLibrosPrestados + ' %'],
            ['Préstamos activos', i.prestamosActivos],
            ['Préstamos vencidos', i.prestamosVencidos, i.prestamosVencidos > 0],
            ['Préstamos devueltos', i.prestamosDevueltos],
            ['Total de préstamos', i.totalPrestamos]
        ];
        $('indicadores').innerHTML = tarjetas.map(([etiqueta, valor, alerta]) =>
            `<div class="stat${alerta ? ' alerta' : ''}"><div class="valor">${esc(valor)}</div><div class="etiqueta">${esc(etiqueta)}</div></div>`
        ).join('');
    } catch (e) { manejarError(e); }
}

// ---------------------------------------------------------------- libros
let filtroLibros = { tipo: 'todos', texto: '' };

async function cargarLibros() {
    try {
        let ruta = '/libros';
        if (filtroLibros.tipo === 'disponibles') ruta = '/libros/disponibles';
        if (filtroLibros.tipo === 'buscar') ruta = '/libros/buscar?texto=' + encodeURIComponent(filtroLibros.texto);
        pintarLibros(await api(ruta));
    } catch (e) { manejarError(e); }
}

function estadoLibro(l) {
    if (!l.activo) return '<span class="badge gris">Desactivado</span>';
    return l.disponible ? '<span class="badge ok">Disponible</span>' : '<span class="badge warn">Prestado</span>';
}

function pintarLibros(libros) {
    const tbody = $('tabla-libros');
    if (!libros.length) {
        tbody.innerHTML = '<tr><td class="vacio" colspan="6">No hay libros para mostrar.</td></tr>';
        return;
    }
    tbody.innerHTML = libros.map((l) => `
        <tr>
            <td>${esc(l.id)}</td>
            <td>${esc(l.titulo)}</td>
            <td>${esc(l.autor)}</td>
            <td>${esc(l.isbn)}</td>
            <td>${estadoLibro(l)}</td>
            <td class="acciones">
                <button class="btn small" data-accion="editar-libro" data-id="${l.id}">Editar</button>
                ${l.disponible ? `<button class="btn small ok" data-accion="prestar-libro" data-id="${l.id}">Prestar</button>` : ''}
                ${l.activo
                    ? `<button class="btn small danger" data-accion="desactivar-libro" data-id="${l.id}">Desactivar</button>`
                    : `<button class="btn small ok" data-accion="activar-libro" data-id="${l.id}">Activar</button>`}
            </td>
        </tr>`).join('');
}

function reiniciarFormLibro() {
    $('form-libro').reset();
    $('libro-id').value = '';
    $('libro-form-titulo').textContent = 'Registrar libro';
    $('libro-guardar').textContent = 'Guardar libro';
    $('libro-cancelar').hidden = true;
    limpiarErroresCampos();
}

$('form-libro').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    limpiarErroresCampos();
    ocultarMensaje();
    const id = $('libro-id').value;
    const cuerpo = {
        titulo: $('libro-titulo').value,
        autor: $('libro-autor').value,
        isbn: $('libro-isbn').value
    };
    try {
        if (id) {
            await api('/libros/' + id, 'PUT', cuerpo);
            mostrarMensaje('exito', 'Libro actualizado correctamente.');
        } else {
            await api('/libros', 'POST', cuerpo);
            mostrarMensaje('exito', 'Libro registrado correctamente.');
        }
        reiniciarFormLibro();
        cargarLibros();
    } catch (e) { manejarError(e); }
});

$('libro-cancelar').addEventListener('click', reiniciarFormLibro);

document.querySelectorAll('[data-filtro-libros]').forEach((b) =>
    b.addEventListener('click', () => {
        filtroLibros = { tipo: b.dataset.filtroLibros, texto: '' };
        $('buscar-libro').value = '';
        document.querySelectorAll('[data-filtro-libros]').forEach((x) => x.classList.toggle('active', x === b));
        ocultarMensaje();
        cargarLibros();
    }));

$('form-buscar-libro').addEventListener('submit', (ev) => {
    ev.preventDefault();
    const texto = $('buscar-libro').value.trim();
    document.querySelectorAll('[data-filtro-libros]').forEach((x) => x.classList.remove('active'));
    filtroLibros = texto ? { tipo: 'buscar', texto } : { tipo: 'todos', texto: '' };
    if (!texto) document.querySelector('[data-filtro-libros="todos"]').classList.add('active');
    ocultarMensaje();
    cargarLibros();
});

$('tabla-libros').addEventListener('click', async (ev) => {
    const boton = ev.target.closest('button[data-accion]');
    if (!boton) return;
    const id = boton.dataset.id;
    ocultarMensaje();
    try {
        switch (boton.dataset.accion) {
            case 'editar-libro': {
                const l = await api('/libros/' + id);
                $('libro-id').value = l.id;
                $('libro-titulo').value = l.titulo;
                $('libro-autor').value = l.autor;
                $('libro-isbn').value = l.isbn;
                $('libro-form-titulo').textContent = 'Modificar libro #' + l.id;
                $('libro-guardar').textContent = 'Guardar cambios';
                $('libro-cancelar').hidden = false;
                limpiarErroresCampos();
                $('form-libro').scrollIntoView({ behavior: 'smooth' });
                break;
            }
            case 'desactivar-libro':
                if (!confirm('¿Desactivar este libro? Dejará de estar disponible para préstamos.')) return;
                await api('/libros/' + id, 'DELETE');
                mostrarMensaje('exito', 'Libro desactivado correctamente.');
                cargarLibros();
                break;
            case 'activar-libro':
                await api('/libros/' + id + '/activar', 'PUT');
                mostrarMensaje('exito', 'Libro activado correctamente.');
                cargarLibros();
                break;
            case 'prestar-libro':
                irAPestana('prestamos');
                await cargarLibrosDisponiblesSelect(id);
                $('prestamo-lector').focus();
                break;
        }
    } catch (e) { manejarError(e); }
});

// ---------------------------------------------------------------- préstamos
let filtroPrestamos = { tipo: 'todos', texto: '' };

async function cargarLibrosDisponiblesSelect(seleccionar) {
    try {
        const libros = await api('/libros/disponibles');
        const select = $('prestamo-libro');
        const anterior = seleccionar || select.value;
        select.innerHTML = '<option value="">— Seleccione un libro disponible —</option>' +
            libros.map((l) => `<option value="${l.id}">${esc(l.titulo)} (${esc(l.autor)})</option>`).join('');
        if (anterior && libros.some((l) => String(l.id) === String(anterior))) select.value = anterior;
    } catch (e) { manejarError(e); }
}

async function cargarPrestamos() {
    try {
        let ruta = '/prestamos';
        if (filtroPrestamos.tipo === 'activos') ruta = '/prestamos/activos';
        if (filtroPrestamos.tipo === 'vencidos') ruta = '/prestamos/vencidos';
        if (filtroPrestamos.tipo === 'lector') ruta = '/prestamos/buscar?lector=' + encodeURIComponent(filtroPrestamos.texto);
        pintarPrestamos(await api(ruta));
    } catch (e) { manejarError(e); }
}

function estadoPrestamo(p) {
    if (p.estado === 'DEVUELTO') return '<span class="badge ok">Devuelto</span>';
    return p.vencido ? '<span class="badge danger">Vencido</span>' : '<span class="badge warn">Activo</span>';
}

function pintarPrestamos(prestamos) {
    const tbody = $('tabla-prestamos');
    if (!prestamos.length) {
        tbody.innerHTML = '<tr><td class="vacio" colspan="8">No hay préstamos para mostrar.</td></tr>';
        return;
    }
    tbody.innerHTML = prestamos.map((p) => `
        <tr>
            <td>${esc(p.id)}</td>
            <td>${esc(p.tituloLibro)}<br><small>${esc(p.isbnLibro)}</small></td>
            <td>${esc(p.nombreLector)}</td>
            <td>${fecha(p.fechaPrestamo)}</td>
            <td>${fecha(p.fechaDevolucionPrevista)}</td>
            <td>${fecha(p.fechaDevolucionReal)}</td>
            <td>${estadoPrestamo(p)}</td>
            <td class="acciones">
                ${p.estado === 'ACTIVO'
                    ? `<button class="btn small" data-accion="editar-prestamo" data-id="${p.id}">Editar</button>
                       <button class="btn small ok" data-accion="devolver" data-id="${p.id}">Devolver</button>`
                    : '—'}
            </td>
        </tr>`).join('');
}

function reiniciarFormPrestamo() {
    $('form-prestamo').reset();
    $('prestamo-id').value = '';
    $('prestamo-dias').value = 7;
    $('prestamo-libro').hidden = false;
    $('prestamo-libro-texto').hidden = true;
    $('prestamo-form-titulo').textContent = 'Registrar préstamo';
    $('prestamo-guardar').textContent = 'Registrar préstamo';
    $('prestamo-cancelar').hidden = true;
    limpiarErroresCampos();
}

$('form-prestamo').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    limpiarErroresCampos();
    ocultarMensaje();
    const id = $('prestamo-id').value;
    const dias = $('prestamo-dias').value;
    try {
        if (id) {
            await api('/prestamos/' + id, 'PUT', {
                nombreLector: $('prestamo-lector').value,
                diasPlazo: dias === '' ? null : parseInt(dias, 10)
            });
            mostrarMensaje('exito', 'Préstamo actualizado correctamente.');
        } else {
            const libro = $('prestamo-libro').value;
            await api('/prestamos', 'POST', {
                libroId: libro === '' ? null : parseInt(libro, 10),
                nombreLector: $('prestamo-lector').value,
                diasPlazo: dias === '' ? null : parseInt(dias, 10)
            });
            mostrarMensaje('exito', 'Préstamo registrado correctamente.');
        }
        reiniciarFormPrestamo();
        await cargarLibrosDisponiblesSelect();
        cargarPrestamos();
    } catch (e) {
        manejarError(e);
        // Si el libro dejó de estar disponible, se refresca la lista del selector.
        cargarLibrosDisponiblesSelect();
    }
});

$('prestamo-cancelar').addEventListener('click', reiniciarFormPrestamo);

document.querySelectorAll('[data-filtro-prestamos]').forEach((b) =>
    b.addEventListener('click', () => {
        filtroPrestamos = { tipo: b.dataset.filtroPrestamos, texto: '' };
        $('buscar-lector').value = '';
        document.querySelectorAll('[data-filtro-prestamos]').forEach((x) => x.classList.toggle('active', x === b));
        ocultarMensaje();
        cargarPrestamos();
    }));

$('form-buscar-lector').addEventListener('submit', (ev) => {
    ev.preventDefault();
    const texto = $('buscar-lector').value.trim();
    document.querySelectorAll('[data-filtro-prestamos]').forEach((x) => x.classList.remove('active'));
    filtroPrestamos = texto ? { tipo: 'lector', texto } : { tipo: 'todos', texto: '' };
    if (!texto) document.querySelector('[data-filtro-prestamos="todos"]').classList.add('active');
    ocultarMensaje();
    cargarPrestamos();
});

$('tabla-prestamos').addEventListener('click', async (ev) => {
    const boton = ev.target.closest('button[data-accion]');
    if (!boton) return;
    const id = boton.dataset.id;
    ocultarMensaje();
    try {
        if (boton.dataset.accion === 'devolver') {
            if (!confirm('¿Registrar la devolución de este libro?')) return;
            await api('/prestamos/devolver/' + id, 'PUT');
            mostrarMensaje('exito', 'Devolución registrada correctamente.');
            await cargarLibrosDisponiblesSelect();
            cargarPrestamos();
        } else if (boton.dataset.accion === 'editar-prestamo') {
            const p = await api('/prestamos/' + id);
            const dias = Math.round((new Date(p.fechaDevolucionPrevista) - new Date(p.fechaPrestamo)) / 86400000);
            $('prestamo-id').value = p.id;
            $('prestamo-libro').hidden = true;
            $('prestamo-libro-texto').hidden = false;
            $('prestamo-libro-texto').value = p.tituloLibro;
            $('prestamo-lector').value = p.nombreLector;
            $('prestamo-dias').value = dias;
            $('prestamo-form-titulo').textContent = 'Modificar préstamo #' + p.id;
            $('prestamo-guardar').textContent = 'Guardar cambios';
            $('prestamo-cancelar').hidden = false;
            limpiarErroresCampos();
            $('form-prestamo').scrollIntoView({ behavior: 'smooth' });
        }
    } catch (e) { manejarError(e); }
});

// ---------------------------------------------------------------- inicio
irAPestana('resumen');
