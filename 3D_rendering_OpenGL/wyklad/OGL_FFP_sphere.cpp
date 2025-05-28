#include <iostream>
#include <GLFW/glfw3.h>

#include <GLFW/glfw3.h>
#include <GLUT/freeglut.h>

#define _USE_MATH_DEFINES
#include <math.h>

void set_transforms(
    int width,   /* window x resolution */
    int height); /* window y resolution */

void draw_box(bool local_material = false);
void draw_strips();

#ifndef WIN32
#define CALLBACK
#define APIENTRY
#endif /* !WIN32 */

GLfloat rotation = 0.0;

#define USE_DYNAMIC_DATA 1

/* =============================================================================
 * Description:
 *    Auxiliary data structure representing 3D point or vector. The constructor
 *    creates the point on the sphere which position is specified by spherical
 *    coordinates.
 * ============================================================================= */
struct Point3d
{
    float x, y, z;
    Point3d(double radius, double phi, double theta)
    {
        double r_xz = radius * cos(theta);
        y = radius * sin(theta);
        x = r_xz * cos(phi);
        z = r_xz * sin(phi);
        // printf ( "Point %6.3lf %6.3lf %6.3lf\n", x, y, z );
    }
    void Normalize()
    {
        double len = sqrt(x * x + y * y + z * z);
        x /= len;
        y /= len;
        z /= len;
        // printf ( "NPoint %6.3lf %6.3lf %6.3lf\n", x, y, z );
    }
    void Copy2Array4(GLfloat *ptr)
    {
        *ptr = x;
        *(ptr + 1) = y;
        *(ptr + 2) = z;
        *(ptr + 3) = 1.0f;
    }
    void Copy2Array3(GLfloat *ptr)
    {
        *ptr = x;
        *(ptr + 1) = y;
        *(ptr + 2) = z;
    }
};

/* =============================================================================
 * Description:
 *    This function creates vertex and normal bufers for the sphere centered at (0,0,0).
 *    The precision  of sphere approximation with traingle mesh is controlled by
 *    wsec and hsec parameters.
 * Parameters:
 *    in:
 *      radius - sphere radius
 *      wsec - number of horizontal secrions in the triangle mesh (along longitude)
 *      hsec - number of vertical sections in the triangle mesh (along latitude)
 *    out:
 *      size = number of vertices created vertices
 *      vertices - reference to the pointer to created vertex buffer
 *      normals  - reference to the pointer to the created normals buffer
 * Returns:
 *    none
 * ============================================================================= */
void MakeSphereTrgsf4(
    int &size, GLfloat *&data, GLfloat *&normals,
    double radius = 1.0, int wsec = 20, int hsec = 20)
{
    printf("Building sphere ...\n");
    size = wsec * hsec *
           2 * // 2 triangles per quad
           3 * // 3 vertices per triangle
           4;  // 4 numbers per vertex
    data = new GLfloat[size];

    size = wsec * hsec *
           2 * // 2 triangles per quad
           3 * // 3 vertices per triangle
           4;  // 3 numbers per vertex
    normals = new GLfloat[size];

    double delta_phi = 2 * M_PI / wsec;
    double delta_theta = M_PI / hsec;
    int index = 0;
    int n_index = 0;
    for (int w = 0; w < wsec; w++)
        for (int h = 0; h < hsec; h++)
        {
            double phi_0 = w * delta_phi;
            double phi_1 = (w + 1) * delta_phi;
            double theta_0 = h * delta_theta - 0.5 * M_PI;
            double theta_1 = (h + 1) * delta_theta - 0.5 * M_PI;
            Point3d p1(radius, phi_0, theta_0);
            Point3d p2(radius, phi_1, theta_0);
            Point3d p3(radius, phi_1, theta_1);
            Point3d p4(radius, phi_0, theta_1);

            p1.Copy2Array4(data + index);
            index += 4;
            p2.Copy2Array4(data + index);
            index += 4;
            p3.Copy2Array4(data + index);
            index += 4;

            p3.Copy2Array4(data + index);
            index += 4;
            p4.Copy2Array4(data + index);
            index += 4;
            p1.Copy2Array4(data + index);
            index += 4;

            // Normals for (0,0,0)-centered sphere
            p1.Normalize();
            p2.Normalize();
            p3.Normalize();
            p4.Normalize();

            p1.Copy2Array4(normals + n_index);
            n_index += 4;
            p2.Copy2Array4(normals + n_index);
            n_index += 4;
            p3.Copy2Array4(normals + n_index);
            n_index += 4;

            p3.Copy2Array4(normals + n_index);
            n_index += 4;
            p4.Copy2Array4(normals + n_index);
            n_index += 4;
            p1.Copy2Array4(normals + n_index);
            n_index += 4;
        }

    size = wsec * hsec *
           2 * // 2 triangles per quad
           3;  // 3 vertices per triangle;
    printf("Sphere model ready\n");
}

void DisplayBuffer(GLfloat *data, GLfloat *normals, int n_trgs)
{
    int nind = 0, vind = 0;
    for (int t = 0; t < n_trgs; t++)
    {
        glBegin(GL_TRIANGLES);
        glNormal3f(normals[nind++], normals[nind++], normals[nind++]);
        glVertex3f(data[vind++], data[vind++], data[vind++]);
        glEnd();
    }
}

/* =============================================================================
 * Description:
 *    This function carries out basic openGL state configuration (sets various modes
 *    and global rendering parameters.
 * Parameters:
 *    none:
 * Returns:
 *    none
 * ============================================================================= */
void OGLConfigure()
{
    // Set background color as dark blue. Will be used by glClear(...)
    glClearColor(0.0f, 0.0f, 0.7f, 0.0f);

    // Disable backface culling if you are not sure of normals orientation in the model
    glDisable(GL_CULL_FACE);

    // Apply the same lighting to both traiangle sided
    glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, GL_FALSE);

    // Consider true observer location for speculat component calculation
    glLightModeli(GL_LIGHT_MODEL_LOCAL_VIEWER, GL_TRUE);

    // Set ambient light
    static GLfloat ambient[4] = {0.0, 0.0, 0.0, 1.0};
    glLightModelfv(GL_LIGHT_MODEL_AMBIENT, ambient);

    // To assure correct normals afetr scaling
    glEnable(GL_NORMALIZE);

    // Enable depth test
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LESS);

    // Use only single light
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
}

// Apdating transformations according to current window aspect ratio
void CALLBACK
reshape_scene(GLsizei width,  /* window x resolution */
              GLsizei height) /* window y resolution */
{
    float hor_view_angle = 70.0;
    glViewport(0, 0, width, height);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(hor_view_angle, width / (double)height, 0.2, 500);

    glMatrixMode(GL_MODELVIEW);
}

// The complete single frame rendering
void CALLBACK
draw_scene(void)
{
    OGLConfigure();

    // Set MODELVIEW marix
    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();
    glLoadIdentity();

    // Observer settings can be done only once if observer is fixed
    gluLookAt(0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);

    // Make the copy of pure World->Eye transformation - not necessary here
    // but it is a good practice
    glPushMatrix();

    // Set light position right here if it is defined directly in world coordinates
    static float light_pos[4] = {5.0, -5.0, 3.0, 1.0};
    static float light_diff[4] = {3.0, 3.0, 3.0, 1.0};
    static float light_spec[4] = {3.0, 3.0, 3.0, 1.0};

    // Use more physically-accurate settings and set specular and diffuse equal
    glLightfv(GL_LIGHT0, GL_DIFFUSE, light_diff);
    glLightfv(GL_LIGHT0, GL_SPECULAR, light_spec);
    glLightfv(GL_LIGHT0, GL_POSITION, light_pos);

    static float mat1_diff[4] = {0.4, 0.2, 0.1, 1.0};
    static float mat1_amb[4] = {0.1, 0.3, 0.1, 1.0};
    static float mat1_spec[4] = {2.0, 2.0, 2.0, 1.0};
    glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, mat1_amb);
    glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, mat1_diff);
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, mat1_spec);
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, 100.0);

    // Sphere transformations - to obtain desired sphere position
    glRotatef(rotation, 0.0f, 0.0f, 1.0f);

    glTranslatef(1.0f, 0.0f, 0.0f);
    glScalef(1.0, 1.0, 1.0);

    // Put all sphere triangles to thge rendering pipeline
    glutSolidSphere(1.1, 30, 30);
    // glutSolidTeacup( 3.0 );
    // glRotatef(45.0f, 90.0f, 45.0f, 1.0f);
    // glutSolidTeapot( 1.0 );

    // Discard modeling transformations
    glPopMatrix();

    // Discard World to Eye transformation
    glPopMatrix();

    glFlush();
    glutSwapBuffers();
    // Only now we can be sure the data for rendering are already "consummed"
}

// Scene animation
void CALLBACK
rotate_objects(void)
{
    rotation += 0.3;
    if (rotation >= 360.0)
        rotation -= 360.0;

    draw_scene();
}

int main(int argc, char **argv)
{
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_RGB | GLUT_DOUBLE | GLUT_DEPTH);
    glutInitWindowSize(500, 500);
    glutInitWindowPosition(10, 10);
    glutCreateWindow("Sphere");
    glutDisplayFunc(draw_scene);
    glutIdleFunc(rotate_objects);
    glutReshapeFunc(reshape_scene);

    // Run the animation
    glutMainLoop();
}
