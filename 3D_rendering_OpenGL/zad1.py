import sys
import math
from OpenGL.GL import *
from OpenGL.GLU import *
from OpenGL.GLUT import *

class ChessApp:
    def __init__(self):
        self.camera_angle = 0.0
        self.camera_radius = 12.0
        self.camera_height = 8.0

    def init_lighting(self):
        glEnable(GL_LIGHTING)
        glEnable(GL_LIGHT0)
        glEnable(GL_LIGHT1)
        glEnable(GL_COLOR_MATERIAL)
        glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE)
        
        glLightfv(GL_LIGHT0, GL_POSITION, (0.0, 0.0, 20.0, 1.0))
        glLightfv(GL_LIGHT0, GL_DIFFUSE, (1.0, 1.0, 1.0, 1.0))
        glLightfv(GL_LIGHT0, GL_SPECULAR, (1.0, 1.0, 1.0, 1.0))
        glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, 30.0)
        glLightfv(GL_LIGHT0, GL_SPOT_DIRECTION, (0.0, 0.0, 0.0))
        
        glLightfv(GL_LIGHT1, GL_POSITION, (-5.0, 10.0, -5.0, 1.0))
        glLightfv(GL_LIGHT1, GL_DIFFUSE, (1.0, 0.5, 0.5, 1.0))
        glLightfv(GL_LIGHT1, GL_SPECULAR, (1.0, 0.5, 0.5, 1.0))
        glLightf(GL_LIGHT1, GL_SPOT_CUTOFF, 45.0)
        glLightfv(GL_LIGHT1, GL_SPOT_DIRECTION, (0.0, -1.0, 0.0))

    def draw_chessboard(self):
        for i in range(8):
            for j in range(8):
                glPushMatrix()
                x = i - 3.5
                z = j - 3.5
                glTranslatef(x, 0.0, z)
                if (i + j) % 2 == 0:
                    glColor3f(0.8, 0.8, 0.6)
                else:
                    glColor3f(0.3, 0.3, 0.3)
                glutSolidCube(1.0)
                glPopMatrix()

    def draw_pawn(self):
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

    def draw_pawns(self):
        glColor3f(0.9, 0.9, 0.9)
        for i in [1, 6]:
            for j in range(8):
                glPushMatrix()
                x = i - 3.5
                z = j - 3.5
                glTranslatef(x, 0.5, z)
                self.draw_pawn()
                glPopMatrix()

    def display(self):
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        glLoadIdentity()

        eye_x = self.camera_radius * math.sin(self.camera_angle)
        eye_z = self.camera_radius * math.cos(self.camera_angle)
        gluLookAt(
            eye_x, self.camera_height, eye_z,
            0.0, 0.0, 0.0,
            0.0, 1.0, 0.0
        )

        self.draw_chessboard()
        self.draw_pawns()
        glutSwapBuffers()

    def animate(self, value):
        self.camera_angle += 0.01
        glutPostRedisplay()
        glutTimerFunc(16, self.animate, 0)

    def reshape(self, w, h):
        glViewport(0, 0, w, h)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        gluPerspective(45.0, w/h, 0.1, 100.0)
        glMatrixMode(GL_MODELVIEW)

    def run(self):
        glutInit(sys.argv)
        glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH)
        glutInitWindowSize(800, 600)
        glutCreateWindow(b"Szachownica 3D")
        glEnable(GL_DEPTH_TEST)
        self.init_lighting()

        glutDisplayFunc(self.display)
        glutReshapeFunc(self.reshape)
        glutTimerFunc(0, self.animate, 0)
        glutMainLoop()

if __name__ == "__main__":
    app = ChessApp()
    app.run()
