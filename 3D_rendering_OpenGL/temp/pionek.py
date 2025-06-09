import sys
from OpenGL.GL import *
from OpenGL.GLU import *
from OpenGL.GLUT import *

class PawnRender:
    def __init__(self):
        self.camera_position = (5.0, 6.0, 1.0)

    def init_lighting(self):
        glEnable(GL_LIGHTING)
        glEnable(GL_LIGHT0)
        glEnable(GL_COLOR_MATERIAL)
        glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE)

        glLightfv(GL_LIGHT0, GL_POSITION, (0.0, 10.0, 0.0, 1.0))
        glLightfv(GL_LIGHT0, GL_DIFFUSE, (1.0, 1.0, 1.0, 1.0))
        glLightfv(GL_LIGHT0, GL_SPECULAR, (1.0, 1.0, 1.0, 1.0))
        glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, 45.0)
        glLightfv(GL_LIGHT0, GL_SPOT_DIRECTION, (0.0, -1.0, 0.0))



    def draw_pawn(self):
        
        glColor3f(0.92, 0.92, 0.92)  
        glPushMatrix()
        glScalef(0.8, 0.2, 0.8)
        glutSolidSphere(0.5, 32, 32)
        glPopMatrix()

        glPushMatrix()
        glTranslatef(0, 0.3, 0)
        glScalef(0.5, 0.6, 0.5)
        glutSolidSphere(0.4, 32, 32)
        glPopMatrix()

        glPushMatrix()
        glTranslatef(0, 0.7, 0)
        glutSolidSphere(0.2, 32, 32)
        glPopMatrix()

    def display(self):
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        glLoadIdentity()

        gluLookAt(
            *self.camera_position,  # kamera
            0.0, 0.4, 0.0,          # patrzymy na środek pionka
            0.0, 1.0, 0.0           # "góra"
        )

        self.draw_pawn()
        glutSwapBuffers()

    def reshape(self, w, h):
        glViewport(0, 0, w, h)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        gluPerspective(45.0, w / h, 0.1, 100.0)
        glMatrixMode(GL_MODELVIEW)

    def run(self):
        glutInit(sys.argv)
        glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH)
        glutInitWindowSize(800, 600)
        glutCreateWindow(b"3D pionek")
        glEnable(GL_DEPTH_TEST)
        self.init_lighting()

        glutDisplayFunc(self.display)
        glutReshapeFunc(self.reshape)
        glutMainLoop()

if __name__ == "__main__":
    app = PawnRender()
    app.run()
