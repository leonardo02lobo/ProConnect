async function ObtenerCerrarSesion(){
    const response = await fetch('http://localhost:3000/api/usuario/logout',{
        method: "POST",
        credentials: "include"
    });
    if(response.ok){
        window.location.href = "/"
    }else{
        window.location.href = "/IniciarSesion"
    }
}
ObtenerCerrarSesion()