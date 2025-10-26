const URL_BASE = "http://localhost:8080/api/auth/"

export const AuthApi = {
    Login: (email: string, password: string): Promise<Response> => {
        return fetch(`${URL_BASE}login`, {
                method: "POST",
                headers: {
                    "Content-type": "application/json"
                },
                body: JSON.stringify({
                    "email": email,
                    "password": password
                })
            }).then((response) => {
                return response
            });
    }
}