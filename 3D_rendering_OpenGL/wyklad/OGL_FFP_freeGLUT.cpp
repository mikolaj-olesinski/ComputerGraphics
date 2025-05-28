#include <GLFW/glfw3.h>
#include <GLUT/freeglut.h>

void set_transforms(
    int width,   /* window x resolution */
    int height); /* window y resolution */

void draw_box(bool local_material = 1);
void draw_strips();

#ifndef WIN32
#define CALLBACK
#define APIENTRY
#endif /* !WIN32 */

// Global parameters for simple animation
GLfloat rotation = 0.0;

void draw_sphere()
{
    glEnable(GL_LIGHTING);

    static float mat3_diff[4] = {0.2, 0.2, 0.3, -1.0};
    static float mat3_amb[4] = {0.0, 0.0, 0.0, -1.0};
    static float mat3_spec[4] = {1.0, 1.0, 1.0, -1.0};
    GLfloat g = 50.0;
    glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, mat3_amb);
    glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, mat3_diff);
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, mat3_spec);
    glMaterialfv(GL_FRONT_AND_BACK, GL_SHININESS, &g);

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();

    glTranslatef(2.0, 0.0, 2.0);
    glutSolidSphere(0.3, 100, 100);
    glPopMatrix();
}

// ==============================================================================
// The complete single frame rendering
// ==============================================================================
void draw_scene(void)
{
    // ============================================================================
    // General settings
    // ============================================================================
    glEnable(GL_DEPTH_TEST);
    glDisable(GL_CULL_FACE);
    glCullFace(GL_FRONT);

    // glShadeModel( GL_FLAT );
    glShadeModel(GL_SMOOTH);

    // Background color
    glClearColor(0.8, 0.8, 0.8, 0.0);

    // Buffer initialization
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // ============================================================================
    // Lighting
    // ============================================================================
    glDisable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
    glEnable(GL_LIGHT1);
    glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, GL_FALSE);

    // Set light position right here if it is defined directly in world coordinates
    static float light_pos[4] = {0.0, 3.0, 0.0, 1.0};
    static float light_diff[4] = {3.0, 3.0, 3.0, 1.0};
    static float light_spec[4] = {5.0, 3.0, 3.0, 1.0};

    // Use more physically-accurate settings and set specular and diffuse equal
    glLightfv(GL_LIGHT0, GL_DIFFUSE, light_diff);
    glLightfv(GL_LIGHT0, GL_POSITION, light_pos);

    // ============================================================================
    // Wiew related transformation
    set_transforms(glutGet(GLUT_WINDOW_WIDTH), glutGet(GLUT_WINDOW_HEIGHT));

    // ============================================================================
    // Drawing
    draw_box();
    draw_strips();

    draw_sphere();
    // ============================================================================
    // Only now we can be sure the data for rendering are already "consumed"
    glutSwapBuffers();
}

// ==============================================================================
// Apdating transformations according to current window aspect ratio
// ==============================================================================
void set_transforms(
    int width,  /* window x resolution */
    int height) /* window y resolution */
{
    glViewport(0, 0, width, height);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    // glFrustum( -0.1, 0.1, -0.1, 0.1, 0.2, 500 );
    gluPerspective(70, width / (double)height, 0.2, 500);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    gluLookAt(2, 4, 10, 0.0, 0.0, 0.0, 0, 1, 0);
}

// ==============================================================================
// Displaying geometry
// ==============================================================================
void draw_box(bool local_material)
{
    // Draws the axis aligned cube centered at (0,0,0)

    glMatrixMode(GL_MODELVIEW);
    glPushMatrix();

    // Light on/off
    glEnable(GL_LIGHTING);

    // Animate it by rotation
    glRotatef(rotation, 0.0f, 0.0f, 1.0f); // around Z - should rotate counterclockwize

    static float mat1_diff[4] = {1.0, 1.0, 1.0, -1.0};
    static float mat1_amb[4] = {0.0, 0.0, 0.0, -1.0};
    static float mat1_spec[4] = {0.0, 0.0, 0.0, -1.0};

#if 1
    if (local_material)
    {
        glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, mat1_amb);
        glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, mat1_diff);
        glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, mat1_spec);
    }

    glColor3f(1.0, 0.0, 0.0);
    glBegin(GL_POLYGON);
    glNormal3f(0.0, 0.0, -1.0);
    glVertex3d(-1.0, 1.0, -1.0);
    glVertex3d(-1.0, -1.0, -1.0);
    glVertex3d(1.0, -1.0, -1.0);
    glEnd();
    glBegin(GL_POLYGON);
    glNormal3f(0.0, 0.0, -1.0);
    glVertex3d(-1.0, 1.0, -1.0);
    glVertex3d(1.0, -1.0, -1.0);
    glVertex3d(1.0, 1.0, -1.0);
    glEnd();
    glBegin(GL_POLYGON);
    glNormal3f(0.0, 0.0, 1.0);
    glVertex3d(-1.0, 1.0, 1.0);
    glVertex3d(-1.0, -1.0, 1.0);
    glVertex3d(1.0, -1.0, 1.0);
    glEnd();
    glBegin(GL_POLYGON);
    glNormal3f(0.0, 0.0, 1.0);
    glVertex3d(-1.0, 1.0, 1.0);
    glVertex3d(1.0, -1.0, 1.0);
    glVertex3d(1.0, 1.0, 1.0);
    glEnd();
#endif

#if 1
    static float mat2_diff[4] = {0.0, 0.0, 1.0, 1.0};
    static float mat2_amb[4] = {0.0, 0.0, 0.0, 1.0};
    static float mat2_spec[4] = {0.0, 0.0, 0.0, 1.0};
    if (local_material)
    {
        glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, mat2_amb);
        glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, mat2_diff);
        glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, mat2_spec);
    }

    glColor3f(0.0, 1.0, 0.0);
    glBegin(GL_POLYGON);
    glNormal3f(0.0, -1.0, 0.0);
    glVertex3d(-1.0, -1.0, -1.0);
    glVertex3d(-1.0, -1.0, 1.0);
    glVertex3d(1.0, -1.0, -1.0);
    glEnd();
    glBegin(GL_POLYGON);
    glNormal3f(0.0, -1.0, 0.0);
    glVertex3d(1.0, -1.0, -1.0);
    glVertex3d(-1.0, -1.0, 1.0);
    glVertex3d(1.0, -1.0, 1.0);
    glEnd();
    glColor3f(0.0, 0.2, 0.0); // dark blue on top
    glBegin(GL_POLYGON);
    glNormal3f(0.0, 1.0, 0.0);
    glVertex3d(-1.0, 1.0, 1.0);
    glVertex3d(-1.0, 1.0, -1.0);
    glVertex3d(1.0, 1.0, -1.0);
    glEnd();
    glBegin(GL_POLYGON);
    glNormal3f(0.0, 1.0, 0.0);
    glVertex3d(-1.0, 1.0, 1.0);
    glVertex3d(1.0, 1.0, -1.0);
    glVertex3d(1.0, 1.0, 1.0);
    glEnd();
#endif

#if 1
    static float mat3_diff[4] = {0.2, 1.0, 0.2, 1.0};
    static float mat3_amb[4] = {0.0, 0.0, 0.0, 1.0};
    static float mat3_spec[4] = {0.0, 0.0, 0.0, 1.0};
    if (local_material)
    {
        glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, mat3_amb);
        glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, mat3_diff);
        glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, mat3_spec);
    }

    glColor3f(0.0, 0.0, 1.0); // blue on left size
    glBegin(GL_POLYGON);
    glNormal3f(-1.0, 0.0, 0.0);
    glVertex3d(-1.0, 1.0, -1.0);
    glVertex3d(-1.0, -1.0, -1.0);
    glVertex3d(-1.0, -1.0, 1.0);
    glEnd();
    glBegin(GL_POLYGON);
    glNormal3f(-1.0, 0.0, 0.0);
    glVertex3d(-1.0, 1.0, 1.0);
    glVertex3d(-1.0, 1.0, -1.0);
    glVertex3d(-1.0, -1.0, 1.0);
    glEnd();
    glColor3f(1.0, 1.0, 1.0); // white on right size
    glBegin(GL_POLYGON);
    glNormal3f(1.0, 0.0, 0.0);
    glVertex3d(1.0, 1.0, -1.0);
    glVertex3d(1.0, -1.0, -1.0);
    glVertex3d(1.0, -1.0, 1.0);
    glEnd();
    glBegin(GL_POLYGON);
    glNormal3f(1.0, 0.0, 0.0);
    glVertex3d(1.0, 1.0, 1.0);
    glVertex3d(1.0, 1.0, -1.0);
    glVertex3d(1.0, -1.0, 1.0);
    glEnd();
#endif

    glPopMatrix();
}

void draw_strips()
{
    // Draws narrow rectangles along XYZ axes to show the orientation of the
    // coordinate systems

    // Axes not subject to illuminate
    glDisable(GL_LIGHTING);
    glMatrixMode(GL_MODELVIEW);

    // X
    glColor3f(1.0, 0.0, 0.0);
    glBegin(GL_POLYGON);
    glNormal3f(0.0, 0.0, 1.0);
    glVertex3d(0.0, 0.0, .0);
    glVertex3d(10.0, 0.0, .0);
    glVertex3d(10.0, 0.1, .0);
    glVertex3d(0.0, 0.1, .0);
    glEnd();
    glPushMatrix();
    glTranslatef(5.0, 0.0, 0.0);
    glRotatef(90, 0.0, 1.0, 0.0);
    glutWireCone(0.3, 1.0, 20, 1);
    glPopMatrix();

    // y
    glColor3f(0.0, 1.0, 0.0);
    glBegin(GL_POLYGON);
    glNormal3f(1.0, 0.0, 0.0);
    glVertex3d(0.0, 0.0, 0.0);
    glVertex3d(0.0, 10.0, 0.0);
    glVertex3d(0.0, 10.0, 0.1);
    glVertex3d(0.0, 0.0, 0.1);
    glEnd();

    glPushMatrix();
    glTranslatef(0.0, 5.0, 0.0);
    glRotatef(-90, 1.0, 0.0, 0.0);
    glutWireCone(0.3, 1.0, 20, 1);
    glPopMatrix();

    // z
    glColor3f(0.0, 0.0, 1.0);
    glBegin(GL_POLYGON);
    glNormal3f(1.0, 0.0, 0.0);

    glVertex3d(0.0, 0.0, 0.0);
    glVertex3d(0.0, 0.0, 10.0);
    glVertex3d(0.0, 0.1, 10.0);
    glVertex3d(0.0, 0.1, 0.0);
    glEnd();

    glPushMatrix();
    glTranslatef(0.0, 0.0, 5.0);
    glutWireCone(0.3, 1.0, 20, 1);
    glPopMatrix();
}

// ============================================================================
// Scene animation
// ============================================================================
void rotate_objects()
{
    rotation += 0.05;
    if (rotation >= 360.0)
        rotation -= 360.0;

    // Redraw is needed
    draw_scene();
}

int main(int argc, char **argv)
{
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_RGB | GLUT_DOUBLE | GLUT_DEPTH);
    glutInitWindowSize(500, 500);
    glutInitWindowPosition(10, 10);
    glutCreateWindow("Box");
    glutDisplayFunc(draw_scene);
    glutIdleFunc(rotate_objects);

    glutMainLoop();
}
