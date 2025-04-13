const likeButton = document.getElementById('like') as HTMLButtonElement | null;
const nolikeButton = document.getElementById('nolike') as HTMLButtonElement | null;
import { DarLikeId, QuitarLikeId } from "../../services/Likes";
import { darLike } from "../js/DarLike";
import { MostrarMensaje } from "../js/Manejoalerta";

likeButton?.addEventListener('click', async () => {
    const url = new URL(window.location.href);
    await DarLikePublicacion(parseInt(url.pathname.split('/')[2]));
})

nolikeButton?.addEventListener('click', async () => {
    const url = new URL(window.location.href);
    await QuitarLikePublicacion(parseInt(url.pathname.split('/')[2]));
})


async function DarLikePublicacion(id: number){
    const response = await DarLikeId(id);
    if(response?.ok){
        darLike();
    }else{
        MostrarMensaje()
    }
}

async function QuitarLikePublicacion(id:number){
    const response = await QuitarLikeId(id);
    if(response?.ok){
        darLike();
    }else{
        MostrarMensaje();
    }
}