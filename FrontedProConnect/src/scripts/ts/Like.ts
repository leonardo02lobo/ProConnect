const likeButton = document.getElementById('like') as HTMLButtonElement | null;
const botonLike = document.getElementById('botonLike') as HTMLElement | null;
import { getCookie } from "../../services/GetCookie";
import { DarLikeId, QuitarLikeId } from "../../services/Likes";
import { darLike } from "../js/DarLike";
import { MostrarMensaje } from "../js/Manejoalerta";
import { ObtenerNumerosLikes } from "./MirarPublicacion";

let nolikeButton = document.getElementById('nolike') as HTMLButtonElement | null;

addEventListener('load', async () => {
    const dataUser = await getCookie();
    const url = new URL(window.location.href);
    const LikesPublicacion = await ObtenerNumerosLikes(parseInt(url.pathname.split('/')[2]));
    LikesPublicacion.forEach((element: any) => {
        if (element['usuario_id'] === dataUser['id']) {
            if (botonLike) {
                botonLike.innerHTML = `
                <button class="bg-red-600 hover:bg-red-700 text-white font-bold py-3.5 px-4 rounded-lg cursor-pointer" id="nolike" > No Like </button>
                `
                nolikeButton = document.getElementById('nolike') as HTMLButtonElement | null;
                nolikeButton?.addEventListener('click', async () => {
                    console.log('b')
                    const url = new URL(window.location.href);
                    await QuitarLikePublicacion(parseInt(url.pathname.split('/')[2]));
                })
            }
        }
    });
})

likeButton?.addEventListener('click', async () => {
    const url = new URL(window.location.href);
    await DarLikePublicacion(parseInt(url.pathname.split('/')[2]));
})

async function DarLikePublicacion(id: number) {
    const response = await DarLikeId(id);
    if (response?.ok) {
        darLike();
    } else {
        MostrarMensaje()
    }
}

async function QuitarLikePublicacion(id: number) {
    const response = await QuitarLikeId(id);
    if (response?.ok) {
        darLike();
    } else {
        MostrarMensaje();
    }
}