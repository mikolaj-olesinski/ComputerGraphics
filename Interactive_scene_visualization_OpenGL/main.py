import sys
import math
import numpy as np
from PIL import Image
from OpenGL.GL import *
from OpenGL.GLU import *
from OpenGL.GLUT import *

class Camera:
    def __init__(self):
        self.eye = np.array([0.0, 0.0, 5.0])
        self.target = np.array([0.0, 0.0, 0.0], dtype=np.float64)
        self.up = np.array([0.0, 1.0, 0.0])
        self.speed = 0.1
        self.orbit_speed = 0.01
        self.pan_speed = 0.05
        self.radius = np.linalg.norm(self.eye - self.target)
        
    def look_at(self):
        gluLookAt(
            self.eye[0], self.eye[1], self.eye[2],
            self.target[0], self.target[1], self.target[2],
            self.up[0], self.up[1], self.up[2]
        )
    
    def move_forward(self):
        direction = (self.target - self.eye) * self.speed
        self.eye += direction
        self.target += direction
    
    def move_backward(self):
        direction = (self.target - self.eye) * self.speed
        self.eye -= direction
        self.target -= direction
    
    def pan_left(self):
        right = np.cross(self.target - self.eye, self.up)
        right = right / np.linalg.norm(right)
        self.eye -= right * self.pan_speed
        self.target -= right * self.pan_speed
    
    def pan_right(self):
        right = np.cross(self.target - self.eye, self.up)
        right = right / np.linalg.norm(right)
        self.eye += right * self.pan_speed
        self.target += right * self.pan_speed
    
    def pan_up(self):
        self.eye += self.up * self.pan_speed
        self.target += self.up * self.pan_speed
    
    def pan_down(self):
        self.eye -= self.up * self.pan_speed
        self.target -= self.up * self.pan_speed
    
    def orbit_left(self):
        direction = self.eye - self.target
        cos_theta = math.cos(self.orbit_speed)
        sin_theta = math.sin(self.orbit_speed)
        
        new_x = direction[0] * cos_theta + direction[2] * sin_theta
        new_z = -direction[0] * sin_theta + direction[2] * cos_theta
        
        direction = np.array([new_x, direction[1], new_z])
        self.eye = self.target + direction
    
    def orbit_right(self):
        direction = self.eye - self.target
        cos_theta = math.cos(-self.orbit_speed)
        sin_theta = math.sin(-self.orbit_speed)
        
        new_x = direction[0] * cos_theta + direction[2] * sin_theta
        new_z = -direction[0] * sin_theta + direction[2] * cos_theta
        
        direction = np.array([new_x, direction[1], new_z])
        self.eye = self.target + direction
    
    def orbit_up(self):
        direction = self.eye - self.target
        right = np.cross(direction, self.up)
        right = right / np.linalg.norm(right)
        
        cos_theta = math.cos(self.orbit_speed)
        sin_theta = math.sin(self.orbit_speed)
        
        new_y = direction[1] * cos_theta - direction[2] * sin_theta
        new_z = direction[1] * sin_theta + direction[2] * cos_theta
        
        direction = np.array([direction[0], new_y, new_z])
        self.eye = self.target + direction
    
    def orbit_down(self):
        direction = self.eye - self.target
        right = np.cross(direction, self.up)
        right = right / np.linalg.norm(right)
        
        cos_theta = math.cos(-self.orbit_speed)
        sin_theta = math.sin(-self.orbit_speed)
        
        new_y = direction[1] * cos_theta - direction[2] * sin_theta
        new_z = direction[1] * sin_theta + direction[2] * cos_theta
        
        direction = np.array([direction[0], new_y, new_z])
        self.eye = self.target + direction
    
    def look_left(self):
        direction = self.target - self.eye
        cos_theta = math.cos(self.orbit_speed)
        sin_theta = math.sin(self.orbit_speed)
        
        new_x = direction[0] * cos_theta - direction[2] * sin_theta
        new_z = direction[0] * sin_theta + direction[2] * cos_theta
        
        self.target = self.eye + np.array([new_x, direction[1], new_z])
    
    def look_right(self):
        direction = self.target - self.eye
        cos_theta = math.cos(-self.orbit_speed)
        sin_theta = math.sin(-self.orbit_speed)
        
        new_x = direction[0] * cos_theta - direction[2] * sin_theta
        new_z = direction[0] * sin_theta + direction[2] * cos_theta
        
        self.target = self.eye + np.array([new_x, direction[1], new_z])
    
    def look_up(self):
        direction = self.target - self.eye
        right = np.cross(direction, self.up)
        right = right / np.linalg.norm(right)
        
        cos_theta = math.cos(self.orbit_speed)
        sin_theta = math.sin(self.orbit_speed)
        
        new_y = direction[1] * cos_theta + direction[2] * sin_theta
        new_z = -direction[1] * sin_theta + direction[2] * cos_theta
        
        self.target = self.eye + np.array([direction[0], new_y, new_z])
    
    def look_down(self):
        direction = self.target - self.eye
        right = np.cross(direction, self.up)
        right = right / np.linalg.norm(right)
        
        cos_theta = math.cos(-self.orbit_speed)
        sin_theta = math.sin(-self.orbit_speed)
        
        new_y = direction[1] * cos_theta + direction[2] * sin_theta
        new_z = -direction[1] * sin_theta + direction[2] * cos_theta
        
        self.target = self.eye + np.array([direction[0], new_y, new_z])

class Material:
    def __init__(self, name):
        self.name = name
        self.Ka = np.array([0.2, 0.2, 0.2])  # Ambient
        self.Kd = np.array([0.8, 0.8, 0.8])  # Diffuse
        self.Ks = np.array([1.0, 1.0, 1.0])  # Specular
        self.Ns = 32.0  # Specular exponent
        self.d = 1.0    # Transparency

class Model:
    def __init__(self):
        self.vertices = []
        self.normals = []
        self.faces = []
        self.materials = {}
        self.current_material = None
        self.bbox_min = np.array([float('inf')]*3)
        self.bbox_max = np.array([-float('inf')]*3)
    
    def load_mtl(self, filename):
        current_mtl = None
        mtl_path = filename
        
        try:
            with open(mtl_path, 'r') as f:
                for line in f:
                    line = line.strip()
                    if not line or line.startswith('#'):
                        continue
                    
                    tokens = line.split()
                    if not tokens:
                        continue
                    
                    if tokens[0] == 'newmtl':
                        current_mtl = Material(tokens[1])
                        self.materials[tokens[1]] = current_mtl
                    
                    elif current_mtl:
                        if tokens[0] == 'Ka':
                            current_mtl.Ka = np.array(list(map(float, tokens[1:4])))
                        elif tokens[0] == 'Kd':
                            current_mtl.Kd = np.array(list(map(float, tokens[1:4])))
                        elif tokens[0] == 'Ks':
                            current_mtl.Ks = np.array(list(map(float, tokens[1:4])))
                        elif tokens[0] == 'Ns':
                            current_mtl.Ns = float(tokens[1])
                        elif tokens[0] == 'd':
                            current_mtl.d = float(tokens[1])
                        elif tokens[0] == 'map_Kd':
                            try:
                                img = Image.open(tokens[1])
                                img = img.convert('RGB')
                                pixels = list(img.getdata())
                                avg_color = np.sum(pixels, axis=0) / len(pixels) / 255.0
                                current_mtl.Kd = avg_color
                            except Exception as e:
                                print(f"Błąd wczytywania tekstury: {e}")
        
        except Exception as e:
            print(f"Błąd wczytywania MTL: {e}")
    
    def load_obj(self, filename):
        obj_path = filename
        mtl_dir = obj_path[:obj_path.rfind('/')+1] if '/' in obj_path else ""
        
        try:
            with open(obj_path, 'r') as f:
                for line in f:
                    line = line.strip()
                    if not line or line.startswith('#'):
                        continue
                    
                    tokens = line.split()
                    if not tokens:
                        continue
                    
                    if tokens[0] == 'v':
                        vertex = np.array(list(map(float, tokens[1:4])))
                        self.vertices.append(vertex)
                        self.update_bbox(vertex)
                    
                    elif tokens[0] == 'vn':
                        self.normals.append(np.array(list(map(float, tokens[1:4]))))
                    
                    elif tokens[0] == 'f':
                        face_verts = []
                        for token in tokens[1:]:
                            parts = token.split('/')
                            v_idx = int(parts[0]) - 1
                            n_idx = int(parts[2]) - 1 if len(parts) > 2 and parts[2] else -1
                            face_verts.append((v_idx, n_idx))
                        self.faces.append((self.current_material, face_verts))
                    
                    elif tokens[0] == 'mtllib':
                        self.load_mtl(mtl_dir + tokens[1])
                    
                    elif tokens[0] == 'usemtl':
                        self.current_material = self.materials.get(tokens[1])
        
        except Exception as e:
            print(f"Błąd wczytywania OBJ: {e}")
    
    def update_bbox(self, vertex):
        self.bbox_min = np.minimum(self.bbox_min, vertex)
        self.bbox_max = np.maximum(self.bbox_max, vertex)
    
    def center_scene(self):
        center = (self.bbox_min + self.bbox_max) / 2.0
        self.vertices = [v - center for v in self.vertices]
        self.bbox_min -= center
        self.bbox_max -= center
    
    def draw(self):
        last_material = None
        
        for material, face_verts in self.faces:
            if material != last_material:
                if material:
                    glMaterialfv(GL_FRONT, GL_AMBIENT, material.Ka)
                    glMaterialfv(GL_FRONT, GL_DIFFUSE, material.Kd)
                    glMaterialfv(GL_FRONT, GL_SPECULAR, material.Ks)
                    glMaterialf(GL_FRONT, GL_SHININESS, min(material.Ns, 128.0))
                last_material = material
            
            glBegin(GL_POLYGON)
            for v_idx, n_idx in face_verts:
                if n_idx >= 0 and n_idx < len(self.normals):
                    glNormal3fv(self.normals[n_idx])
                else:
                    # Oblicz normalną geometryczną jeśli brak
                    if len(face_verts) >= 3:
                        v0 = self.vertices[face_verts[0][0]]
                        v1 = self.vertices[face_verts[1][0]]
                        v2 = self.vertices[face_verts[2][0]]
                        normal = np.cross(v1 - v0, v2 - v0)
                        normal = normal / np.linalg.norm(normal)
                        glNormal3fv(normal)
                glVertex3fv(self.vertices[v_idx])
            glEnd()

class SceneViewer:
    def __init__(self, obj_file):
        self.obj_file = obj_file
        self.model = Model()
        self.camera = Camera()
        self.window_width = 800
        self.window_height = 600
        self.init_camera = True
    
    def init_lighting(self):
        glEnable(GL_LIGHTING)
        glEnable(GL_LIGHT0)
        glLightfv(GL_LIGHT0, GL_POSITION, [0, 10, 10, 1])
        glLightfv(GL_LIGHT0, GL_DIFFUSE, [1, 1, 1, 1])
        glLightfv(GL_LIGHT0, GL_SPECULAR, [1, 1, 1, 1])
        
        glEnable(GL_COLOR_MATERIAL)
        glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE)
        
        glEnable(GL_DEPTH_TEST)
        glShadeModel(GL_SMOOTH)
    
    def setup_camera(self):
        if self.init_camera:
            self.init_camera = False
            bbox_size = self.model.bbox_max - self.model.bbox_min
            max_dim = max(bbox_size)
            distance = max_dim * 2.0
            
            self.camera.eye = np.array([0, max_dim/2, distance])
            self.camera.target = np.array([0, 0, 0])
            self.camera.radius = distance
    
    def display(self):
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        glLoadIdentity()
        
        self.camera.look_at()
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
        key = key.decode('utf-8')
        
        if key == 'w': self.camera.move_forward()
        elif key == 's': self.camera.move_backward()
        elif key == 'a': self.camera.pan_left()
        elif key == 'd': self.camera.pan_right()
        elif key == 'q': self.camera.pan_down()
        elif key == 'e': self.camera.pan_up()
        elif key == 'i': self.camera.orbit_up()
        elif key == 'k': self.camera.orbit_down()
        elif key == 'j': self.camera.orbit_left()
        elif key == 'l': self.camera.orbit_right()
        elif key == 'f': self.camera.look_left()
        elif key == 'h': self.camera.look_right()
        elif key == 't': self.camera.look_up()
        elif key == 'g': self.camera.look_down()
        elif key == 'r':  # Reset kamery
            self.init_camera = True
            self.setup_camera()
        
        glutPostRedisplay()
    
    def run(self):
        glutInit(sys.argv)
        glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH)
        glutInitWindowSize(self.window_width, self.window_height)
        glutCreateWindow(b"Wizualizacja sceny 3D")
        
        self.model.load_obj(self.obj_file)
        self.model.center_scene()
        self.setup_camera()
        self.init_lighting()
        
        glutDisplayFunc(self.display)
        glutReshapeFunc(self.reshape)
        glutKeyboardFunc(self.keyboard)
        glutMainLoop()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Użycie: python program.py <plik.obj>")
        sys.exit(1)
    
    viewer = SceneViewer(sys.argv[1])
    viewer.run()