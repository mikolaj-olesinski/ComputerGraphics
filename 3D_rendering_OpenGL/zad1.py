import sys
import math
from OpenGL.GL import *
from OpenGL.GLU import *
from OpenGL.GLUT import *

camera_angle = 0.0
camera_radius = 12.0
camera_height = 8.0

def ogl_configure():
    glEnable(GL_DEPTH_TEST)
    glEnable(GL_LIGHTING)
    glEnable(GL_LIGHT0)
    glEnable(GL_LIGHT1)

def init_lighting():
    glLightfv(GL_LIGHT0, GL_DIFFUSE, (1.0, 1.0, 1.0, 1.0))
    glLightfv(GL_LIGHT0, GL_SPECULAR, (1.0, 1.0, 1.0, 1.0))
    glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, 30.0)
    
    glLightfv(GL_LIGHT1, GL_DIFFUSE, (1.0, 0.5, 0.5, 1.0))
    glLightfv(GL_LIGHT1, GL_SPECULAR, (1.0, 0.5, 0.5, 1.0))
    glLightf(GL_LIGHT1, GL_SPOT_CUTOFF, 45.0)

def set_lights_position():
    glLightfv(GL_LIGHT0, GL_POSITION, (0.0, 5.0, 5.0, 1.0))
    glLightfv(GL_LIGHT0, GL_SPOT_DIRECTION, (0.0, -1.0, -1.0))  
    glLightfv(GL_LIGHT1, GL_POSITION, (-5.0, 10.0, -5.0, 1.0))
    glLightfv(GL_LIGHT1, GL_SPOT_DIRECTION, (0.0, -1.0, 0.0))  

def set_camera():
    global camera_angle, camera_radius, camera_height
    
    eye_x = camera_radius * math.sin(camera_angle)
    eye_z = camera_radius * math.cos(camera_angle)
    
    gluLookAt(
        eye_x, camera_height, eye_z,
        0.0, 0.0, 0.0, #Center 
        0.0, 1.0, 0.0  # Up vector
    )

def set_material_light_square():
    color = [0.8, 0.8, 0.6, 1.0] 
    specular = [0.0, 0.0, 0.0, 1.0] 
    shininess = 100.0
    
    glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, color)
    glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, color)
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, specular)
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, shininess)

def set_material_dark_square():
    color = [0.3, 0.3, 0.3, 1.0]  
    specular = [0.0, 0.0, 0.0, 1.0] 
    shininess = 0.0 
    
    glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, color)
    glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, color)
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, specular)
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, shininess)

def set_material_light_pawn():
    color = [0.9, 0.9, 0.9, 1.0] 
    specular = [0.0, 0.0, 0.0, 1.0] 
    shininess = 0.0 
    
    glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, color)
    glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, color)
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, specular)
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, shininess)

def set_material_dark_pawn():
    color = [0.2, 0.2, 0.2, 1.0]   
    specular = [0.0, 0.0, 0.0, 1.0]
    shininess = 0.0
    
    glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, color)
    glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, color)
    glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, specular)
    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, shininess)

def draw_chessboard():
    for i in range(8):
        glPushMatrix()
        glTranslatef(i - 3.5, 0.0, 0.0)
        
        for j in range(8):
            glPushMatrix()
            glTranslatef(0.0, 0.0, j - 3.5)
            
            if (i + j) % 2 == 0:
                set_material_light_square()
            else:
                set_material_dark_square()
            
            glutSolidCube(1.0)
            #glutSolidTeapot(1.0)
            glPopMatrix()
        glPopMatrix()


def draw_single_pawn():
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

def draw_pawns():
    for i in [0, 1, 6, 7]:
        for j in range(8):
            glPushMatrix()
            x = i - 3.5
            z = j - 3.5
            glTranslatef(x, 0.5, z)
            
            if i in [0, 1]:  
                set_material_dark_pawn()  
            else:
                set_material_light_pawn() 
            
            draw_single_pawn()
            glPopMatrix()

def draw_scene():
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
    glLoadIdentity()

    set_camera()
    glPushMatrix()

    set_lights_position()
    draw_chessboard()
    draw_pawns()
    
    glPopMatrix()
    glutSwapBuffers()

def animate_scene():
    global camera_angle
    
    camera_angle += 0.01
    if camera_angle >= 2 * math.pi:
        camera_angle -= 2 * math.pi
    
    glutPostRedisplay()

def timer_callback(value):
    animate_scene()
    glutTimerFunc(16, timer_callback, 0) 

def reshape_callback(w, h):
    print(f"Reshape: width={w}, height={h}")
    glViewport(0, 0, w, h)
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    gluPerspective(45.0, w/h, 0.1, 100.0)
    glMatrixMode(GL_MODELVIEW)

def init_glut(argv):
    glutInit(argv)
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH)
    glutInitWindowSize(800, 600)
    glutInitWindowPosition(100, 100)
    glutCreateWindow("Szachownica 3D - Reczne Materialy")
    
    glutDisplayFunc(draw_scene)
    glutReshapeFunc(reshape_callback)
    glutTimerFunc(0, timer_callback, 0)

def main():
    init_glut(sys.argv)
    ogl_configure()
    init_lighting()
    glutMainLoop()

if __name__ == "__main__":
    main()