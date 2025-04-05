const CrearPublicacion = document.getElementById('CrearPublacion') as HTMLElement | null;
const Contenido = document.getElementById('Contenido') as HTMLTextAreaElement | null;
const Titulo = document.getElementById('Titulo') as HTMLInputElement | null;
const alerta = document.getElementById('alerta') as HTMLElement | null;
const felicidades = document.getElementById('Felicidades') as HTMLElement | null;

import type { PublicacionModel } from "../../models/Publicacion";
import type { Usuario } from "../../models/Usuario";
const usu: Usuario = {
    nombre: "",
    nombreUsuario: "",
    email: "",
    contrasena: "",
    fotoPerfil: "",
    fotoFondo: "",
    puesto: "",
    tipoUsuario: ""
}
const publicacion: PublicacionModel = {
    usuario: usu,
    contenido: "",
    titulo: "",
    foto: "",
    fecha: new Date(),
    likes: 0
}

CrearPublicacion?.addEventListener('click',async () => {
    publicacion.contenido = Contenido?.value || ""
    publicacion.titulo = Titulo?.value || "";
    await ObtenerUsuario()
    if(publicacion.contenido === "" || publicacion.titulo === ""){
        if (alerta) {
            alerta.style.display = 'block';

            setTimeout(() => {
                alerta.style.display = 'none'
            },2000);
        }
        return;
    }
    try{
        const response = await fetch('http://localhost:3000/api/Publicacion/CrearPublicacion',{
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(publicacion)
        })
        const data = await response.json();
        if(response.ok){
            if (felicidades) {
                felicidades.style.display = 'block';
    
                setTimeout(() => {
                    felicidades.style.display = 'none'
                    location.reload()
                },2000);
            }
            if (Contenido && Titulo) {
                Contenido.value = "";
                Titulo.value = "";
            }
        }
    }catch(e){
        console.log("Error al verificar autenticacion");
    }
})

async function ObtenerUsuario(){
    try {
        const response = await fetch("http://localhost:3000/api/usuario/getCookie", {
            method: "GET",
            credentials: "include",
        });
        const data = await response.json();
        if (!response.ok) {
            return;
        }
        publicacion.usuario = data.user.row[0];
    } catch (e) {
        console.log("Error al verificar autenticación:");
    }
}