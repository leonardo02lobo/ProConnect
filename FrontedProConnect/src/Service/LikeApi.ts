const URL_BASE = "http://localhost:8080/api/like"

export const apis = () => {
    async function AddLike(userId: number, postId: number) {
        try {
            const response = await fetch(`${URL_BASE}/create`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    userId,
                    postId
                })
            })

            if (!response.ok) {
                const errorText = await response.text();
                console.error("Error del servidor:", errorText);
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.text()
            console.log(data)
        } catch (error) {
            console.log((error as Error).message)
        }
    }

    async function RemoveLike(userId: number, postId: number) {
        try {
            const response = await fetch(`${URL_BASE}/delete`, { 
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ 
                    userId: userId,
                    postId: postId
                })
            })

            if (!response.ok) {
                const errorText = await response.text();
                console.error("Error del servidor:", errorText);
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.text();
            console.log("Like eliminado:", data);
            return data;
        } catch (error) {
            console.log((error as Error).message);
            throw error;
        }
    }

    return { AddLike,RemoveLike }
}