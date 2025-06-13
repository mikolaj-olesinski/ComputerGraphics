import numpy as np
import math
from OpenGL.GL import *
from OpenGL.GLU import *
from temp import Model
from OpenGL.GLUT import *

class Camera:
    def __init__(self):
        self.E = np.array([5.0, 5.0, 5.0])      # pozycja obserwatora
        self.T = np.array([0.0, 0.0, 0.0])      # punkt centralny na rzutni (target)
        self.UL = None                          # współrzędne lewego górnego wierzchołka ekranu
        self.dU = None                          # wektor przemieszczenia między sąsiednimi kolumnami
        self.dV = None                          # wektor przemieszczenia między sąsiednimi wierszami
        
        self.AX = None                          # kierunek osi poziomej ekranu
        self.AY = None                          # kierunek osi pionowej ekranu  
        self.D = None                           # znormalizowany kierunek patrzenia
        
        self.rx = 800                           # rozdzielczość pozioma
        self.ry = 600                           # rozdzielczość pionowa
        self.fov = 45.0                         # kąt widzenia w stopniach
        
        self._initialize_camera_params()
        
    def _initialize_camera_params(self):
        self.D = self.T - self.E
        self.D = self.D / np.linalg.norm(self.D)
        
        world_up = np.array([0.0, 1.0, 0.0])
        
        self.AX = np.cross(self.D, world_up)
        self.AX = self.AX / np.linalg.norm(self.AX)
        
        self.AY = np.cross(self.AX, self.D)
        self.AY = self.AY / np.linalg.norm(self.AY)
        
        distance_to_target = np.linalg.norm(self.T - self.E)
        half_height = distance_to_target * math.tan(math.radians(self.fov / 2))
        half_width = half_height * (self.rx / self.ry)
        
        self.dU = (2 * half_width / self.rx) * self.AX
        self.dV = (2 * half_height / self.ry) * self.AY
        
        self.UL = self.T - half_width * self.AX + half_height * self.AY
        
    def load_cam_file(self, filename):
        with open(filename, 'r') as f:
            lines = [line.strip() for line in f if line.strip() and not line.startswith('#')]
        
        self.E = np.array(list(map(float, lines[0].split())))
        self.T = np.array(list(map(float, lines[1].split())))
        self.rx, self.ry = map(int, lines[2].split())
        self.UL = np.array(list(map(float, lines[3].split())))
        self.dU = np.array(list(map(float, lines[4].split())))
        self.dV = np.array(list(map(float, lines[5].split())))
        
        self.D = self.T - self.E
        self.D = self.D / np.linalg.norm(self.D)
        
        self.AX = self.dU / np.linalg.norm(self.dU)
        self.AY = self.dV / np.linalg.norm(self.dV)
        
    def _rotation_matrix(self, angle, axis):
        axis = axis / np.linalg.norm(axis)
        cos_a = math.cos(angle)
        sin_a = math.sin(angle)
        
        K = np.array([
            [0, -axis[2], axis[1]],
            [axis[2], 0, -axis[0]],
            [-axis[1], axis[0], 0]
        ])
        
        R = np.eye(3) + sin_a * K + (1 - cos_a) * np.dot(K, K)
        return R
        
    def _rotate_point_around_axis(self, point, angle, axis, center):
        translated = point - center
        
        rotation_matrix = self._rotation_matrix(angle, axis)
        rotated = np.dot(rotation_matrix, translated)
        
        return rotated + center
        
    def _rotate_vector(self, vector, angle, axis):
        rotation_matrix = self._rotation_matrix(angle, axis)
        return np.dot(rotation_matrix, vector)
    
    def walk(self, distance):
        dD = distance * self.D
        
        self.E = self.E + dD
        self.UL = self.UL + dD  
        self.T = self.T + dD
            
    def pan_horizontal(self, distance):
        dAx = distance * self.AX
        
        self.E = self.E + dAx
        self.UL = self.UL + dAx
        self.T = self.T + dAx
        
    def pan_vertical(self, distance):
        dAy = distance * self.AY
        
        self.E = self.E + dAy
        self.UL = self.UL + dAy
        self.T = self.T + dAy
        
    def pan(self, dx, dy):
        self.pan_horizontal(dx)
        self.pan_vertical(dy)
        

    def look_up_down(self, angle):
        self.T = self._rotate_point_around_axis(self.T, angle, self.AX, self.E)
        self.UL = self._rotate_point_around_axis(self.UL, angle, self.AX, self.E)
        
        self.AY = self._rotate_vector(self.AY, angle, self.AX)
        self.dV = self._rotate_vector(self.dV, angle, self.AX)
        self.D = self._rotate_vector(self.D, angle, self.AX)
        
        
    def look_left_right(self, angle):
        self.T = self._rotate_point_around_axis(self.T, angle, self.AY, self.E)
        self.UL = self._rotate_point_around_axis(self.UL, angle, self.AY, self.E)
        
        self.AX = self._rotate_vector(self.AX, angle, self.AY)
        self.dU = self._rotate_vector(self.dU, angle, self.AY)  
        self.D = self._rotate_vector(self.D, angle, self.AY)
        

    def orbit_up_down(self, angle):
        self.E = self._rotate_point_around_axis(self.E, angle, self.AX, self.T)
        self.UL = self._rotate_point_around_axis(self.UL, angle, self.AX, self.T)
        
        self.AY = self._rotate_vector(self.AY, angle, self.AX)
        self.dV = self._rotate_vector(self.dV, angle, self.AX)
        self.D = self._rotate_vector(self.D, angle, self.AX)
        
        
    def orbit_left_right(self, angle):
        self.E = self._rotate_point_around_axis(self.E, angle, self.AY, self.T)
        self.UL = self._rotate_point_around_axis(self.UL, angle, self.AY, self.T)
        
        self.AX = self._rotate_vector(self.AX, angle, self.AY)
        self.dU = self._rotate_vector(self.dU, angle, self.AY)
        self.D = self._rotate_vector(self.D, angle, self.AY)
        

    def look_at(self):
        up = self.AY 
        gluLookAt(*self.E, *self.T, *up)
        
    def get_opengl_up_vector(self):
        return self.AY
        
    def reset_camera(self):
        self.E = np.array([5.0, 5.0, 5.0])
        self.T = np.array([0.0, 0.0, 0.0])
        self._initialize_camera_params()
        
    def get_camera_info(self):
        return f"""
=== PARAMETRY KAMERY ===
E (obserwator): {self.E}
T (target): {self.T}
UL (lewy górny): {self.UL}
D (kierunek): {self.D}
AX (oś pozioma): {self.AX}
AY (oś pionowa): {self.AY}
dU: {self.dU}
dV: {self.dV}
Rozdzielczość: {self.rx}x{self.ry}
FOV: {self.fov}°
========================
"""

class CameraController:
    def __init__(self):
        self.camera = Camera()
        self.movement_speed = 0.2
        self.rotation_speed = math.radians(5) 
        
    def handle_movement(self, key):
        if key == 'w':  
            self.camera.walk(self.movement_speed)
        elif key == 's': 
            self.camera.walk(-self.movement_speed)
        elif key == 'a':  
            self.camera.pan_horizontal(-self.movement_speed)
        elif key == 'd': 
            self.camera.pan_horizontal(self.movement_speed)
        elif key == 'q':
            self.camera.pan_vertical(self.movement_speed)
        elif key == 'e': 
            self.camera.pan_vertical(-self.movement_speed)
            
    def handle_looking(self, key):
        if key == 'i':  
            self.camera.look_up_down(-self.rotation_speed)
        elif key == 'k': 
            self.camera.look_up_down(self.rotation_speed)
        elif key == 'j':  
            self.camera.look_left_right(-self.rotation_speed)
        elif key == 'l': 
            self.camera.look_left_right(self.rotation_speed)
            
    def handle_orbiting(self, key):
        if key == 'up': 
            self.camera.orbit_up_down(-self.rotation_speed)
        elif key == 'down': 
            self.camera.orbit_up_down(self.rotation_speed)
        elif key == 'left': 
            self.camera.orbit_left_right(-self.rotation_speed)
        elif key == 'right':
            self.camera.orbit_left_right(self.rotation_speed)

class SceneViewer:
    def __init__(self, obj_file, cam_file=None):
        self.obj_file = obj_file
        self.cam_file = cam_file
        self.model = Model()  
        self.camera_controller = CameraController()
        self.window_width = 800
        self.window_height = 600

    def init_lighting(self):
        glEnable(GL_LIGHTING)
        glEnable(GL_LIGHT0)
        glLightfv(GL_LIGHT0, GL_POSITION, [10.0, 10.0, 10.0, 1.0])
        glLightfv(GL_LIGHT0, GL_DIFFUSE, [1.0, 1.0, 1.0, 1.0])
        glLightfv(GL_LIGHT0, GL_SPECULAR, [1.0, 1.0, 1.0, 1.0])
        glLightfv(GL_LIGHT0, GL_AMBIENT, [0.3, 0.3, 0.3, 1.0])
        glEnable(GL_DEPTH_TEST)
        glShadeModel(GL_SMOOTH)
        glEnable(GL_NORMALIZE)
        glClearColor(0.1, 0.1, 0.1, 1.0)

    def display(self):
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        glLoadIdentity()
        
        self.camera_controller.camera.look_at()
        
        self.model.draw()
        glutSwapBuffers()

    def reshape(self, w, h):
        self.window_width = w
        self.window_height = h
        glViewport(0, 0, w, h)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        gluPerspective(45.0, w/h, 0.1, 100.0)
        glMatrixMode(GL_MODELVIEW)

    def keyboard(self, key, x, y):
        key_char = key.decode('utf-8') if isinstance(key, bytes) else key
        
        if key_char in ['w', 's', 'a', 'd', 'q', 'e']:
            self.camera_controller.handle_movement(key_char)
            
        elif key_char in ['i', 'k', 'j', 'l']:
            self.camera_controller.handle_looking(key_char)
            
        elif key == b' ':
            self.camera_controller.camera.reset_camera()
            
        elif key_char == 'p':
            print(self.camera_controller.camera.get_camera_info())
            
        glutPostRedisplay()

    def special_keys(self, key, x, y):
        if key == GLUT_KEY_UP:
            self.camera_controller.handle_orbiting('up')
        elif key == GLUT_KEY_DOWN:
            self.camera_controller.handle_orbiting('down')
        elif key == GLUT_KEY_LEFT:
            self.camera_controller.handle_orbiting('left')
        elif key == GLUT_KEY_RIGHT:
            self.camera_controller.handle_orbiting('right')
            
        glutPostRedisplay()

    def run(self):
        glutInit(sys.argv)
        glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH)
        glutInitWindowSize(self.window_width, self.window_height)
        glutCreateWindow(b"Wizualizacja sceny 3D ")
        
        self.model.load_obj(self.obj_file)
        
        if self.cam_file:
            self.camera_controller.camera.load_cam_file(self.cam_file)
            if hasattr(self.camera_controller.camera, 'rx'):
                self.window_width = self.camera_controller.camera.rx
                self.window_height = self.camera_controller.camera.ry
                print(f"Ustawiam rozdzielczość z pliku .cam: {self.window_width}x{self.window_height}")

        self.init_lighting()
        glutDisplayFunc(self.display)
        glutReshapeFunc(self.reshape)
        glutKeyboardFunc(self.keyboard)
        glutSpecialFunc(self.special_keys)
        
        print("\n=== NOWE STEROWANIE (MODEL RT) ===")
        print("=== RUCH (spacerowanie i panning) ===")
        print("W/S - spacerowanie do przodu/tyłu (wzdłuż osi kamery)")
        print("A/D - panning lewo/prawo (wzdłuż osi AX)")
        print("Q/E - panning góra/dół (wzdłuż osi AY)")
        print("")
        print("=== ROZGLĄDANIE SIĘ (target się porusza) ===")
        print("I/K - rozglądanie góra/dół (obrót wokół AX przez E)")
        print("J/L - rozglądanie lewo/prawo (obrót wokół AY przez E)")
        print("")
        print("=== ORBITOWANIE (obserwator się porusza) ===")
        print("Strzałki góra/dół - orbitowanie góra/dół (obrót wokół AX przez T)")
        print("Strzałki lewo/prawo - orbitowanie lewo/prawo (obrót wokół AY przez T)")
        print("")
        print("P - wyświetl parametry kamery")
        print("SPACJA - resetuj kamerę")
        print("===============================\n")
        
        glutMainLoop()


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Użycie: python program.py <plik.obj> [plik.cam]")
        sys.exit(1)
    
    obj_file = sys.argv[1]
    cam_file = sys.argv[2] if len(sys.argv) > 2 else None
    
    viewer = SceneViewer(obj_file, cam_file)
    viewer.run()