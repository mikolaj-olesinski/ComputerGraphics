import java.awt.*;

// Ray class
class Ray {
    Vector3 origin;
    Vector3 direction;

    public Ray(Vector3 origin, Vector3 direction) {
        this.origin = origin;
        this.direction = direction.normalize();
    }
}

// Material class
class Material {
    Vector3 diffuseCoeff;  // kd (RGB)
    Vector3 specularCoeff; // ks (RGB)
    Vector3 ambientCoeff;  // ka (RGB)
    Vector3 selfLuminance; // S (RGB)
    double glossiness;     // g

    public Material(Vector3 diffuseCoeff, Vector3 specularCoeff, Vector3 ambientCoeff,
                    Vector3 selfLuminance, double glossiness) {
        this.diffuseCoeff = diffuseCoeff;
        this.specularCoeff = specularCoeff;
        this.ambientCoeff = ambientCoeff;
        this.selfLuminance = selfLuminance;
        this.glossiness = glossiness;
    }
}

// Sphere class
class Sphere {
    Vector3 center;
    double radius;
    Material material;

    public Sphere(double radius, Material material) {
        this.center = new Vector3( 0, 0, 0);
        this.radius = radius;
        this.material = material;
    }

    public boolean intersect(Ray ray, double[] t) {
        double x = ray.origin.x;
        double y = ray.origin.y;
        // x^2 + y^2 < r^2
        if (x*x + y*y >= radius*radius) {
            return false;
        }
        // t = r - sqrt(r^2 - x^2 - y^2)
        // distance
        t[0] = -Math.sqrt(radius*radius - x*x - y*y);
        return true;
    }

}

// PointLight class
class PointLight {
    Vector3 position;
    Vector3 intensity; // RGB

    public PointLight(Vector3 position, Vector3 intensity) {
        this.position = position;
        this.intensity = intensity;
    }
}


// Vector3 class
class Vector3 {
    double x, y, z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double dot(Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        return new Vector3(x / length, y / length, z / length);
    }

    public Vector3 subtract(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Vector3 multiply(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }

    public Vector3 reflect(Vector3 normal) {
        double dotProduct = this.dot(normal);
        // Reflection formula: R = I - 2 * (I . N) * N
        return this.subtract(normal.multiply(2 * dotProduct));
    }
}
