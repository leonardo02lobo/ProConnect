const mainMessage = document.getElementById('mainMessage')
const asideMessage = document.getElementById('asideMessage')

window.addEventListener('load', () => {
    RenderizarComponents()
})

window.addEventListener('resize', () => {
    RenderizarComponents()
})

mainMessage.addEventListener('click', (e) => {
    const data = e.target.closest('#tarjetamensaje')
    mainMessage.style.display = 'none'
    asideMessage.style.display = 'flex'
    mainMessage.classList.add('col-span-3')
    mainMessage.classList.remove('col-span-1')
})

function RenderizarComponents(){
    if(window.screen.width <= 767){
        asideMessage.style.display = 'none'
        mainMessage.classList.remove('col-span-1')
        mainMessage.classList.add('col-span-3')
    }else{
        asideMessage.style.display = 'flex'
        mainMessage.classList.add('col-span-1')
        mainMessage.classList.remove('col-span-3')
    }
}