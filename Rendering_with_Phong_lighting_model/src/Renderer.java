// Renderer class
import java.awt.image.BufferedImage;

class Renderer {
    public BufferedImage render(Scene scene) {
        BufferedImage image = new BufferedImage(scene.imageWidth, scene.imageHeight, BufferedImage.TYPE_INT_RGB);

        double aspectRatio = (double) scene.imageWidth / scene.imageHeight;
        double viewportHeight = 2.0;
        double viewportWidth = aspectRatio * viewportHeight;

        for (int y = 0; y < scene.imageHeight; y++) {
            for (int x = 0; x < scene.imageWidth; x++) {
                // Mapuj piksel do przestrzeni świata
                double u = (x + 0.5) / scene.imageWidth;
                double v = (y + 0.5) / scene.imageHeight;

                // Konwertuj do współrzędnych widoku
                double worldX = -viewportWidth/2 + u * viewportWidth;
                double worldY = viewportHeight/2 - v * viewportHeight;

                // Równoległe promienie mają ten sam kierunek
                Vector3 direction = new Vector3(0, 0, 1).normalize();
                Vector3 rayOrigin = new Vector3(worldX, worldY, -scene.sphere.radius);

                Ray ray = new Ray(rayOrigin, direction);
                Vector3 color = traceRay(ray, scene);

                // Ogranicz wartości kolorów do [0, 1]
                color = new Vector3(
                        Math.min(1, Math.max(0, color.x)),
                        Math.min(1, Math.max(0, color.y)),
                        Math.min(1, Math.max(0, color.z))
                );

                // Konwertuj kolor do RGB int
                int r = (int) (color.x * 255);
                int g = (int) (color.y * 255);
                int b = (int) (color.z * 255);
                int rgb = (r << 16) | (g << 8) | b;

                image.setRGB(x, y, rgb);
            }
        }

        return image;
    }

    private Vector3 traceRay(Ray ray, Scene scene) {
        double[] t = new double[1];

        if (scene.sphere.intersect(ray, t)) {
            // Oblicz punkt przecięcia
            Vector3 hitPoint = ray.origin.add(ray.direction.multiply(t[0]));

            // Oblicz normalną w punkcie przecięcia
            Vector3 normal = scene.sphere.getNormalAt(hitPoint);

            // Pobierz właściwości materiału
            Material material = scene.sphere.material;

            // Kierunek patrzenia (od punktu przecięcia do kamery)
            Vector3 viewDir = ray.origin.subtract(hitPoint).normalize();

            // Inicjalizacja kolorem własnego świecenia
            Vector3 color = material.selfLuminance;

            // Dodaj składową światła otoczenia
            color = color.add(new Vector3(
                    material.ambientCoeff.x * scene.ambientLight.x,
                    material.ambientCoeff.y * scene.ambientLight.y,
                    material.ambientCoeff.z * scene.ambientLight.z
            ));

            // Dodaj wkład od każdego źródła światła
            for (PointLight light : scene.lights) {
                // Oblicz kierunek światła
                Vector3 lightDir = light.position.subtract(hitPoint).normalize();

                // Odległość do światła
                double distToLight = Math.sqrt(light.position.subtract(hitPoint).dot(
                        light.position.subtract(hitPoint)));

                // Oblicz tłumienie światła (uproszczona wersja)
                double attenuation = Math.min(1.0, 1.0 / (1.0 + 0.1 * distToLight + 0.01 * distToLight * distToLight));

                // Odbicie dyfuzyjne
                double diffuseFactor = Math.max(0, normal.dot(lightDir));

                // Odbicie lustrzane
                Vector3 reflectedLight = lightDir.multiply(-1).reflect(normal);
                double specularFactor = Math.pow(Math.max(0, reflectedLight.dot(viewDir)), material.glossiness);

                // Dodaj składową dyfuzyjną
                color = color.add(new Vector3(
                        material.diffuseCoeff.x * light.intensity.x * diffuseFactor * attenuation,
                        material.diffuseCoeff.y * light.intensity.y * diffuseFactor * attenuation,
                        material.diffuseCoeff.z * light.intensity.z * diffuseFactor * attenuation
                ));

                // Dodaj składową lustrzaną
                color = color.add(new Vector3(
                        material.specularCoeff.x * light.intensity.x * specularFactor * attenuation,
                        material.specularCoeff.y * light.intensity.y * specularFactor * attenuation,
                        material.specularCoeff.z * light.intensity.z * specularFactor * attenuation
                ));
            }

            return color;
        }

        // Brak przecięcia, zwróć czarny (kolor tła)
        return new Vector3(0, 0, 0);
    }
}