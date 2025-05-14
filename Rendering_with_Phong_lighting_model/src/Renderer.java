// Renderer class
import java.awt.image.BufferedImage;

class Renderer {
    public BufferedImage render(Scene scene) {
        int n = scene.imageWidth;
        BufferedImage image = new BufferedImage(n, n, BufferedImage.TYPE_INT_RGB);

        double radius = scene.sphere.radius;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {

                double world_x = -radius + (2 * radius * (j + 0.5)) / n;
                double world_y = radius - (2 * radius * (i + 0.5)) / n;
                double wolrd_z = -radius;


                Vector3 direction = new Vector3(0, 0, 1);
                Vector3 rayOrigin = new Vector3(world_x, world_y, wolrd_z);

                Ray ray = new Ray(rayOrigin, direction);
                Vector3 color = traceRay(ray, scene);


                color = new Vector3(
                        Math.min(1, Math.max(0, color.x)),
                        Math.min(1, Math.max(0, color.y)),
                        Math.min(1, Math.max(0, color.z))
                );

                int r = (int) (color.x * 255);
                int g = (int) (color.y * 255);
                int b = (int) (color.z * 255);
                int rgb = (r << 16) | (g << 8) | b;

                image.setRGB(j, i, rgb);
            }
        }

        return image;
    }

    private Vector3 traceRay(Ray ray, Scene scene) {
        double[] t = new double[1];

        if (scene.sphere.intersect(ray, t)) {

            Vector3 hitPoint = new Vector3(ray.origin.x, ray.origin.y, ray.origin.z + t[0]);

            Vector3 normal = hitPoint.normalize();

            Material material = scene.sphere.material;

            Vector3 viewDir = new Vector3(0, 0, -1);


            // Oc
            Vector3 color = material.selfLuminance;

            //kaC * Ac
            color = color.add(new Vector3(
                    material.ambientCoeff.x * scene.ambientLight.x,
                    material.ambientCoeff.y * scene.ambientLight.y,
                    material.ambientCoeff.z * scene.ambientLight.z
            ));


            for (PointLight light : scene.lights) {
                Vector3 lightDir = light.position.subtract(hitPoint).normalize();

                //fan(r)
                double distToLight = Math.sqrt(light.position.subtract(hitPoint).dot(
                        light.position.subtract(hitPoint)));

                double c1 = 0.05;
                double c2 = 0.005;
                double attenuation = Math.min(1.0, 1.0 / (1.0 + c1 * distToLight + c2 * distToLight * distToLight));


                //diffuse
                double diffuseFactor = Math.max(0, normal.dot(lightDir));

                color = color.add(new Vector3(
                        material.diffuseCoeff.x * light.intensity.x * diffuseFactor * attenuation,
                        material.diffuseCoeff.y * light.intensity.y * diffuseFactor * attenuation,
                        material.diffuseCoeff.z * light.intensity.z * diffuseFactor * attenuation
                ));

                Vector3 reflectedLight = lightDir.multiply(-1).reflect(normal);
                double specularFactor = Math.pow(Math.max(0, reflectedLight.dot(viewDir)), material.glossiness);

                color = color.add(new Vector3(
                        material.specularCoeff.x * light.intensity.x * specularFactor * attenuation,
                        material.specularCoeff.y * light.intensity.y * specularFactor * attenuation,
                        material.specularCoeff.z * light.intensity.z * specularFactor * attenuation
                ));
            }
            return color;
        }

        return new Vector3(0, 0, 0);
    }

}