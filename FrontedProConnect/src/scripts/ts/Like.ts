const likeButton = document.getElementById('like') as HTMLButtonElement | null;
const nolikeButton = document.getElementById('nolike') as HTMLButtonElement | null;
import { darLike } from "../js/DarLike";

likeButton?.addEventListener('click', async () => {
    const url = new URL(window.location.href);
    await DarLikePublicacion(parseInt(url.pathname.split('/')[2]));
})

nolikeButton?.addEventListener('click', async () => {
    const url = new URL(window.location.href);
    await QuitarLikePublicacion(parseInt(url.pathname.split('/')[2]));
})


async function DarLikePublicacion(id: number){
    const response = await fetch(`http://localhost:3000/api/Publicacion/Publicacion/DarLike/${id}`,{
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
    })
    if(response.ok){
        darLike();
    }
}

async function QuitarLikePublicacion(id:number){
    const response = await fetch(`http://localhost:3000/api/Publicacion/Publicacion/EliminarLike/${id}`,{
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
    })
    if(response.ok){
        darLike();
    }
}