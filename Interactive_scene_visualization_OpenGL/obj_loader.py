import sys
import math
import os
from OpenGL.GL import *
from OpenGL.GLU import *
from OpenGL.GLUT import *
import numpy as np

class Material:
    def __init__(self):
        self.name = ""
        self.Ka = [0.2, 0.2, 0.2]  # ambient
        self.Kd = [0.8, 0.8, 0.8]  # diffuse
        self.Ks = [0.0, 0.0, 0.0]  # specular
        self.Ns = 10.0  # specular exponent
        self.d = 1.0    # dissolve (transparency)

class Face:
    def __init__(self):
        self.vertices = []
        self.normals = []
        self.texcoords = []
        self.material = None

class OBJLoader:
    def __init__(self):
        self.vertices = []
        self.normals = []
        self.texcoords = []
        self.faces = []
        self.materials = {}
        self.current_material = None
        
    def load_mtl(self, filename):
        """Wczytywanie pliku MTL z materiałami"""
        if not os.path.exists(filename):
            print(f"Plik MTL nie istnieje: {filename}")
            return
            
        current_material = None
        
        with open(filename, 'r') as file:
            for line in file:
                line = line.strip()
                if not line or line.startswith('#'):
                    continue
                    
                parts = line.split()
                if not parts:
                    continue
                    
                if parts[0] == 'newmtl':
                    current_material = Material()
                    current_material.name = parts[1]
                    self.materials[parts[1]] = current_material
                    
                elif current_material:
                    if parts[0] == 'Ka':
                        current_material.Ka = [float(parts[1]), float(parts[2]), float(parts[3])]
                    elif parts[0] == 'Kd':
                        current_material.Kd = [float(parts[1]), float(parts[2]), float(parts[3])]
                    elif parts[0] == 'Ks':
                        current_material.Ks = [float(parts[1]), float(parts[2]), float(parts[3])]
                    elif parts[0] == 'Ns':
                        current_material.Ns = float(parts[1])
                    elif parts[0] == 'd':
                        current_material.d = float(parts[1])
    
    def load_obj(self, filename):
        """Wczytywanie pliku OBJ"""
        if not os.path.exists(filename):
            print(f"Plik OBJ nie istnieje: {filename}")
            return False
            
        base_path = os.path.dirname(filename)
        
        with open(filename, 'r') as file:
            for line in file:
                line = line.strip()
                if not line or line.startswith('#'):
                    continue
                    
                parts = line.split()
                if not parts:
                    continue
                    
                if parts[0] == 'mtllib':
                    mtl_path = os.path.join(base_path, parts[1])
                    self.load_mtl(mtl_path)
                    
                elif parts[0] == 'v':
                    # Vertex position
                    x, y, z = float(parts[1]), float(parts[2]), float(parts[3])
                    self.vertices.append([x, y, z])
                    
                elif parts[0] == 'vn':
                    # Vertex normal
                    nx, ny, nz = float(parts[1]), float(parts[2]), float(parts[3])
                    self.normals.append([nx, ny, nz])
                    
                elif parts[0] == 'vt':
                    # Texture coordinate
                    u, v = float(parts[1]), float(parts[2])
                    self.texcoords.append([u, v])
                    
                elif parts[0] == 'usemtl':
                    self.current_material = parts[1] if parts[1] in self.materials else None
                    
                elif parts[0] == 'f':
                    # Face
                    face = Face()
                    face.material = self.current_material
                    
                    for vertex_data in parts[1:]:
                        indices = vertex_data.split('/')
                        
                        # Vertex index (required)
                        v_idx = int(indices[0]) - 1  # OBJ indices start from 1
                        face.vertices.append(v_idx)
                        
                        # Texture coordinate index (optional)
                        if len(indices) > 1 and indices[1]:
                            vt_idx = int(indices[1]) - 1
                            face.texcoords.append(vt_idx)
                        
                        # Normal index (optional)
                        if len(indices) > 2 and indices[2]:
                            vn_idx = int(indices[2]) - 1
                            face.normals.append(vn_idx)
                    
                    self.faces.append(face)
        
        return True
    
    def get_bounding_box(self):
        """Obliczanie bounding box sceny"""
        if not self.vertices:
            return [-1, -1, -1], [1, 1, 1]
            
        min_coords = [float('inf')] * 3
        max_coords = [float('-inf')] * 3
        
        for vertex in self.vertices:
            for i in range(3):
                min_coords[i] = min(min_coords[i], vertex[i])
                max_coords[i] = max(max_coords[i], vertex[i])
                
        return min_coords, max_coords

class Camera:
    def __init__(self):
        self.position = [0, 0, 5]
        self.target = [0, 0, 0]
        self.up = [0, 1, 0]
        self.fov = 45.0
        
        # Parametry dla różnych trybów ruchu
        self.move_speed = 0.1
        self.rotate_speed = 0.02
        self.zoom_speed = 0.1
        
    def get_forward_vector(self):
        """Wektor kierunku patrzenia"""
        forward = [
            self.target[0] - self.position[0],
            self.target[1] - self.position[1], 
            self.target[2] - self.position[2]
        ]
        length = math.sqrt(sum(x*x for x in forward))
        if length > 0:
            forward = [x/length for x in forward]
        return forward
    
    def get_right_vector(self):
        """Wektor prawy (cross product forward x up)"""
        forward = self.get_forward_vector()
        right = [
            forward[1] * self.up[2] - forward[2] * self.up[1],
            forward[2] * self.up[0] - forward[0] * self.up[2],
            forward[0] * self.up[1] - forward[1] * self.up[0]
        ]
        length = math.sqrt(sum(x*x for x in right))
        if length > 0:
            right = [x/length for x in right]
        return right
    
    def pan(self, dx, dy):
        """Przesuwanie równoległe do płaszczyzny obrazu (panning)"""
        right = self.get_right_vector()
        forward = self.get_forward_vector()
        up_local = [
            right[1] * forward[2] - right[2] * forward[1],
            right[2] * forward[0] - right[0] * forward[2],
            right[0] * forward[1] - right[1] * forward[0]
        ]
        
        # Przesunięcie w prawo
        for i in range(3):
            self.position[i] += right[i] * dx * self.move_speed
            self.target[i] += right[i] * dx * self.move_speed
        
        # Przesunięcie w górę
        for i in range(3):
            self.position[i] += up_local[i] * dy * self.move_speed
            self.target[i] += up_local[i] * dy * self.move_speed
    
    def dolly(self, delta):
        """Spacerowanie - przesuwanie wzdłuż kierunku patrzenia"""
        forward = self.get_forward_vector()
        for i in range(3):
            self.position[i] += forward[i] * delta * self.move_speed
            self.target[i] += forward[i] * delta * self.move_speed
    
    def orbit(self, theta, phi):
        """Orbitowanie - ruch po sferze wokół target"""
        # Wektor od target do position
        vec = [
            self.position[0] - self.target[0],
            self.position[1] - self.target[1],
            self.position[2] - self.target[2]
        ]
        
        # Odległość od target
        radius = math.sqrt(sum(x*x for x in vec))
        
        if radius > 0:
            # Konwersja do współrzędnych sferycznych
            curr_phi = math.asin(vec[1] / radius)
            curr_theta = math.atan2(vec[2], vec[0])
            
            # Aktualizacja kątów
            new_theta = curr_theta + theta * self.rotate_speed
            new_phi = max(-math.pi/2 + 0.1, min(math.pi/2 - 0.1, curr_phi + phi * self.rotate_speed))
            
            # Powrót do współrzędnych kartezjańskich
            self.position[0] = self.target[0] + radius * math.cos(new_phi) * math.cos(new_theta)
            self.position[1] = self.target[1] + radius * math.sin(new_phi)
            self.position[2] = self.target[2] + radius * math.cos(new_phi) * math.sin(new_theta)
    
    def look_around(self, dx, dy):
        """Rozglądanie się - przesuwanie target przy stałej pozycji"""
        right = self.get_right_vector()
        forward = self.get_forward_vector()
        up_local = [
            right[1] * forward[2] - right[2] * forward[1],
            right[2] * forward[0] - right[0] * forward[2],
            right[0] * forward[1] - right[1] * forward[0]
        ]
        
        for i in range(3):
            self.target[i] += right[i] * dx * self.move_speed
            self.target[i] += up_local[i] * dy * self.move_speed

class OBJViewer:
    def __init__(self):
        self.obj_loader = OBJLoader()
        self.camera = Camera()
        self.scene_loaded = False
        
        # Tryby kamery
        self.CAMERA_PAN = 0
        self.CAMERA_DOLLY = 1
        self.CAMERA_ORBIT = 2
        self.CAMERA_LOOK = 3
        self.camera_mode = self.CAMERA_ORBIT
        
        # Stan klawiszy
        self.keys_pressed = set()
        
    def setup_camera_for_scene(self):
        """Automatyczne ustawienie kamery dla sceny"""
        if not self.obj_loader.vertices:
            return
            
        min_coords, max_coords = self.obj_loader.get_bounding_box()
        
        # Środek sceny
        center = [
            (min_coords[0] + max_coords[0]) / 2,
            (min_coords[1] + max_coords[1]) / 2,
            (min_coords[2] + max_coords[2]) / 2
        ]
        
        # Rozmiar sceny
        size = max(max_coords[i] - min_coords[i] for i in range(3))
        
        # Ustaw target na środek sceny
        self.camera.target = center[:]
        
        # Ustaw pozycję kamery na przekątnej prostopadłościanu
        diagonal_offset = size * 1.5
        self.camera.position = [
            center[0] + diagonal_offset * 0.7,
            center[1] + diagonal_offset * 0.7,
            center[2] + diagonal_offset * 0.7
        ]
        
        # Dostosuj FOV aby objąć całą scenę
        distance = math.sqrt(sum((self.camera.position[i] - center[i])**2 for i in range(3)))
        self.camera.fov = 2 * math.degrees(math.atan(size / (2 * distance)))
        self.camera.fov = max(20, min(120, self.camera.fov))  # Ograniczenie FOV
    
    def setup_lighting(self):
        """Automatyczne ustawienie oświetlenia"""
        glEnable(GL_LIGHTING)
        glEnable(GL_LIGHT0)
        glEnable(GL_LIGHT1)
        glEnable(GL_COLOR_MATERIAL)
        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE)
        
        if self.obj_loader.vertices:
            min_coords, max_coords = self.obj_loader.get_bounding_box()
            center = [(min_coords[i] + max_coords[i]) / 2 for i in range(3)]
            size = max(max_coords[i] - min_coords[i] for i in range(3))
            
            # Światło główne - z góry i z przodu
            light0_pos = [center[0] + size, center[1] + size * 2, center[2] + size, 1.0]
            glLightfv(GL_LIGHT0, GL_POSITION, light0_pos)
            glLightfv(GL_LIGHT0, GL_DIFFUSE, [0.8, 0.8, 0.8, 1.0])
            glLightfv(GL_LIGHT0, GL_SPECULAR, [1.0, 1.0, 1.0, 1.0])
            
            # Światło wypełniające - z drugiej strony
            light1_pos = [center[0] - size, center[1] + size, center[2] - size, 1.0]
            glLightfv(GL_LIGHT1, GL_POSITION, light1_pos)
            glLightfv(GL_LIGHT1, GL_DIFFUSE, [0.4, 0.4, 0.4, 1.0])
            glLightfv(GL_LIGHT1, GL_SPECULAR, [0.2, 0.2, 0.2, 1.0])
        else:
            # Domyślne oświetlenie
            glLightfv(GL_LIGHT0, GL_POSITION, [5.0, 10.0, 5.0, 1.0])
            glLightfv(GL_LIGHT0, GL_DIFFUSE, [0.8, 0.8, 0.8, 1.0])
            glLightfv(GL_LIGHT0, GL_SPECULAR, [1.0, 1.0, 1.0, 1.0])
    
    def render_scene(self):
        """Renderowanie sceny OBJ"""
        if not self.scene_loaded:
            return
            
        for face in self.obj_loader.faces:
            # Ustaw materiał
            if face.material and face.material in self.obj_loader.materials:
                material = self.obj_loader.materials[face.material]
                glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, material.Ka + [1.0])
                glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, material.Kd + [material.d])
                glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, material.Ks + [1.0])
                glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, material.Ns)
            else:
                # Domyślny materiał
                glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, [0.2, 0.2, 0.2, 1.0])
                glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, [0.8, 0.8, 0.8, 1.0])
                glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, [0.0, 0.0, 0.0, 1.0])
                glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, 10.0)
            
            # Rysuj wielokąt
            if len(face.vertices) == 3:
                glBegin(GL_TRIANGLES)
            elif len(face.vertices) == 4:
                glBegin(GL_QUADS)
            else:
                glBegin(GL_POLYGON)
            
            for i, v_idx in enumerate(face.vertices):
                # Normal
                if i < len(face.normals) and face.normals[i] < len(self.obj_loader.normals):
                    normal = self.obj_loader.normals[face.normals[i]]
                    glNormal3f(normal[0], normal[1], normal[2])
                elif len(face.vertices) >= 3:
                    # Oblicz normal geometryczną
                    v1 = self.obj_loader.vertices[face.vertices[0]]
                    v2 = self.obj_loader.vertices[face.vertices[1]]
                    v3 = self.obj_loader.vertices[face.vertices[2]]
                    
                    edge1 = [v2[i] - v1[i] for i in range(3)]
                    edge2 = [v3[i] - v1[i] for i in range(3)]
                    
                    normal = [
                        edge1[1] * edge2[2] - edge1[2] * edge2[1],
                        edge1[2] * edge2[0] - edge1[0] * edge2[2],
                        edge1[0] * edge2[1] - edge1[1] * edge2[0]
                    ]
                    
                    length = math.sqrt(sum(x*x for x in normal))
                    if length > 0:
                        normal = [x/length for x in normal]
                        glNormal3f(normal[0], normal[1], normal[2])
                
                # Vertex
                if v_idx < len(self.obj_loader.vertices):
                    vertex = self.obj_loader.vertices[v_idx]
                    glVertex3f(vertex[0], vertex[1], vertex[2])
            
            glEnd()
    
    def load_scene(self, filename):
        """Wczytanie sceny z pliku OBJ"""
        if self.obj_loader.load_obj(filename):
            self.scene_loaded = True
            self.setup_camera_for_scene()
            print(f"Wczytano scenę: {len(self.obj_loader.vertices)} wierzchołków, {len(self.obj_loader.faces)} ścian")
            print(f"Materiały: {list(self.obj_loader.materials.keys())}")
        else:
            print(f"Nie udało się wczytać sceny z pliku: {filename}")
    
    def display(self):
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        glLoadIdentity()
        
        # Ustaw kamerę
        gluLookAt(
            self.camera.position[0], self.camera.position[1], self.camera.position[2],
            self.camera.target[0], self.camera.target[1], self.camera.target[2],
            self.camera.up[0], self.camera.up[1], self.camera.up[2]
        )
        
        # Renderuj scenę
        if self.scene_loaded:
            self.render_scene()
        else:
            # Rysuj kostki testowe jeśli brak sceny
            glColor3f(0.5, 0.5, 0.5)
            glutSolidCube(1.0)
            
            glPushMatrix()
            glTranslatef(2, 0, 0)
            glColor3f(1.0, 0.5, 0.5)
            glutSolidCube(1.0)
            glPopMatrix()
            
            glPushMatrix()
            glTranslatef(0, 2, 0)
            glColor3f(0.5, 1.0, 0.5)
            glutSolidCube(1.0)
            glPopMatrix()
        
        glutSwapBuffers()
    
    def keyboard(self, key, x, y):
        key = key.decode('ascii').lower()
        
        # Zmiana trybu kamery
        if key == '1':
            self.camera_mode = self.CAMERA_PAN
            print("Tryb: Panning (WSAD - ruch w płaszczyźnie)")
        elif key == '2':
            self.camera_mode = self.CAMERA_DOLLY
            print("Tryb: Dolly (WS - przód/tył)")
        elif key == '3':
            self.camera_mode = self.CAMERA_ORBIT
            print("Tryb: Orbit (WSAD - orbitowanie wokół target)")
        elif key == '4':
            self.camera_mode = self.CAMERA_LOOK
            print("Tryb: Look Around (WSAD - rozglądanie się)")
        
        # Ruch kamery w zależności od trybu
        elif key == 'w':
            if self.camera_mode == self.CAMERA_PAN:
                self.camera.pan(0, 1)
            elif self.camera_mode == self.CAMERA_DOLLY:
                self.camera.dolly(1)
            elif self.camera_mode == self.CAMERA_ORBIT:
                self.camera.orbit(0, 1)
            elif self.camera_mode == self.CAMERA_LOOK:
                self.camera.look_around(0, 1)
        
        elif key == 's':
            if self.camera_mode == self.CAMERA_PAN:
                self.camera.pan(0, -1)
            elif self.camera_mode == self.CAMERA_DOLLY:
                self.camera.dolly(-1)
            elif self.camera_mode == self.CAMERA_ORBIT:
                self.camera.orbit(0, -1)
            elif self.camera_mode == self.CAMERA_LOOK:
                self.camera.look_around(0, -1)
        
        elif key == 'a':
            if self.camera_mode == self.CAMERA_PAN:
                self.camera.pan(-1, 0)
            elif self.camera_mode == self.CAMERA_ORBIT:
                self.camera.orbit(-1, 0)
            elif self.camera_mode == self.CAMERA_LOOK:
                self.camera.look_around(-1, 0)
        
        elif key == 'd':
            if self.camera_mode == self.CAMERA_PAN:
                self.camera.pan(1, 0)
            elif self.camera_mode == self.CAMERA_ORBIT:
                self.camera.orbit(1, 0)
            elif self.camera_mode == self.CAMERA_LOOK:
                self.camera.look_around(1, 0)
        
        elif key == 'r':
            # Reset kamery
            self.setup_camera_for_scene()
            print("Kamera zresetowana")
        
        elif key == 'q' or key == '\x1b':  # ESC
            sys.exit(0)
        
        glutPostRedisplay()
    
    def reshape(self, w, h):
        glViewport(0, 0, w, h)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        
        aspect = w / h if h != 0 else 1
        gluPerspective(self.camera.fov, aspect, 0.1, 1000.0)
        glMatrixMode(GL_MODELVIEW)
    
    def print_help(self):
        print("\n=== STEROWANIE KAMERĄ ===")
        print("1 - Tryb Panning (przesuwanie w płaszczyźnie)")
        print("2 - Tryb Dolly (spacerowanie przód/tył)")
        print("3 - Tryb Orbit (orbitowanie wokół target)")
        print("4 - Tryb Look Around (rozglądanie się)")
        print("\nW/A/S/D - ruch według aktywnego trybu")
        print("R - reset kamery")
        print("Q/ESC - wyjście")
        print("========================\n")
    
    def run(self, obj_filename=None):
        glutInit(sys.argv)
        glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH)
        glutInitWindowSize(1024, 768)
        glutCreateWindow(b"3D Scene Viewer - OBJ Loader")
        
        # Ustawienia OpenGL
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_NORMALIZE)
        glShadeModel(GL_SMOOTH)
        glClearColor(0.1, 0.1, 0.1, 1.0)
        
        # Wczytaj scenę jeśli podano plik
        if obj_filename:
            self.load_scene(obj_filename)
        
        # Ustawienia oświetlenia
        self.setup_lighting()
        
        # Wyświetl pomoc
        self.print_help()
        
        # Funkcje callback
        glutDisplayFunc(self.display)
        glutReshapeFunc(self.reshape)
        glutKeyboardFunc(self.keyboard)
        
        glutMainLoop()

if __name__ == "__main__":
    viewer = OBJViewer()
    
    # Sprawdź czy podano plik OBJ jako argument
    if len(sys.argv) > 1:
        obj_file = sys.argv[1]
        viewer.run(obj_file)
    else:
        print("Użycie: python obj_viewer.py [plik.obj]")
        print("Uruchamianie bez pliku - scena testowa")
        viewer.run()