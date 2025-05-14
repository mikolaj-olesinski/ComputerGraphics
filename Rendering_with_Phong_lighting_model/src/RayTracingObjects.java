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
        // Założenie: środek kuli jest w (0,0,0), więc vector 'oc' to po prostu ray.origin
        Vector3 oc = ray.origin;

        double a = ray.direction.dot(ray.direction);
        double b = 2.0 * oc.dot(ray.direction);
        double c = oc.dot(oc) - radius * radius; // oc.dot(oc) to kwadrat długości wektora oc

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return false;

        double sqrtDiscriminant = Math.sqrt(discriminant);
        double t0 = (-b - sqrtDiscriminant) / (2 * a);
        double t1 = (-b + sqrtDiscriminant) / (2 * a);

        if (t0 > t1) {
            double temp = t0;
            t0 = t1;
            t1 = temp;
        }

        if (t0 < 0) {
            t0 = t1;
            if (t0 < 0) return false;
        }

        t[0] = t0;
        return true;
    }

    public Vector3 getNormalAt(Vector3 point) {
        return point.subtract(center).normalize();
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
        return this.subtract(normal.multiply(2 * dotProduct));
    }
}
