export const getUserFromCookie = () => {
        const cookie = document.cookie.split("; ")

        const userCookie = cookie.find(row => row.startsWith("user="))
        if (userCookie) {
            try {
                const value = userCookie.split("=")[1]
                return JSON.parse(decodeURIComponent(value))
            } catch (err) {
                console.error("Error al parsear: " + err)
                return null
            }
        }
        return null
    }