export async function getImageUrl(fotoPerfil: string): Promise<string> {
    try {
        const result = await fetch(`http://localhost:3000${fotoPerfil}`)
        if (!result.ok) {
            console.log('Error fetching image:', result.statusText);
            return "";
        }
        return result.url;
    } catch (error) {
        console.log('Error fetching image:', error);
        return "";
    }
}