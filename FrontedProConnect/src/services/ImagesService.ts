export async function getImageUrl(fotoPerfil: string): Promise<string | undefined> {
    try {
        const result = await fetch(`http://localhost:3000${fotoPerfil}`)
        if (!result.ok) {
            throw new Error('Error fetching image')
        }
        return result.url;
    } catch (error) {
        console.log('Error fetching image:', error);
    }
}