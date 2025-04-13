const seguir = document.querySelectorAll("#seguir");

seguir?.forEach((element) => {
    element?.addEventListener("click", () => {
        if (element) {
            element.innerHTML = "Siguiendo✓";
        }
        element?.classList.remove("bg-blue-500");
        element?.classList.add("bg-green-500");
    });
});