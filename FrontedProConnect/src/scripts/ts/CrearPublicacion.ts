const CrearPublicacion = document.getElementById('CrearPublacion') as HTMLElement | null;
const Contenido = document.getElementById('Contenido') as HTMLTextAreaElement | null;
const Titulo = document.getElementById('Titulo') as HTMLInputElement | null;
const alerta = document.getElementById('alerta') as HTMLElement | null;
const felicidades = document.getElementById('Felicidades') as HTMLElement | null;
const input = document.getElementById('file-upload') as HTMLInputElement | null;

import type { PublicacionModel } from "../../models/Publicacion";
import type { Usuario } from "../../models/Usuario";
import { setPublicacion, SubirImagenFotoPublicacion } from "../../services/PublicacionesService";
import { getCookie } from "../../services/UsuarioService";
const usu: Usuario = {
    nombre: "",
    nombreUsuario: "",
    email: "",
    contrasena: "",
    fotoPerfil: "",
    fotoFondo: "",
    puesto: "",
    tipoUsuario: "",
    id: 0
}
const publicacion: PublicacionModel = {
    usuario: usu,
    contenido: "",
    titulo: "",
    foto: "",
    fecha: new Date(),
    likes: 0,
    id: 0
}

CrearPublicacion?.addEventListener('click', async () => {
    publicacion.contenido = Contenido?.value || ""
    publicacion.titulo = Titulo?.value || "";
    await ObtenerUsuario()
    if (publicacion.contenido === "" || publicacion.titulo === "") {
        if (alerta) {
            alerta.style.display = 'block';

            setTimeout(() => {
                alerta.style.display = 'none'
            }, 2000);
        }
        return;
    }
    const response = await setPublicacion(publicacion);
    const result = await SubirImagenFotoPublicacion(input, response.Id);

    if (response !== null) {
        if (felicidades) {
            felicidades.style.display = 'block';

            setTimeout(() => {
                felicidades.style.display = 'none'
                location.reload()
            }, 2000);
        }
        if (Contenido && Titulo) {
            Contenido.value = "";
            Titulo.value = "";
        }
    }
})

async function ObtenerUsuario() {
    const usuario = await getCookie()
    if (usuario == null) {
        return;
    }
    publicacion.usuario = usuario;
}