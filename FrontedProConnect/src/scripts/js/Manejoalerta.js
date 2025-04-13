const alerta = document.getElementById('alerta')

addEventListener('load', () => {
    alerta.style.display = 'none';
})

export function MostrarMensaje() {
    alerta.style.display = 'flex'
    setTimeout(()=> {
     alerta.style.display = 'none'   
    },2000)
}