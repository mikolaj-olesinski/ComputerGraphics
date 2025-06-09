import sys
import math
import numpy as np
from OpenGL.GL import *
from OpenGL.GLU import *
from OpenGL.GLUT import *

class Vector3:
    def __init__(self, x=0.0, y=0.0, z=0.0):
        self.x = float(x)
        self.y = float(y)
        self.z = float(z)
    
    def __sub__(self, other):
        return Vector3(self.x - other.x, self.y - other.y, self.z - other.z)
    
    def __add__(self, other):
        return Vector3(self.x + other.x, self.y + other.y, self.z + other.z)
    
    def __mul__(self, scalar):
        return Vector3(self.x * scalar, self.y * scalar, self.z * scalar)
    
    def cross(self, other):
        return Vector3(
            self.y * other.z - self.z * other.y,
            self.z * other.x - self.x * other.z,
            self.x * other.y - self.y * other.x
        )
    
    def dot(self, other):
        return self.x * other.x + self.y * other.y + self.z * other.z
    
    def length(self):
        return math.sqrt(self.x**2 + self.y**2 + self.z**2)
    
    def normalize(self):
        l = self.length()
        if l > 0:
            return Vector3(self.x/l, self.y/l, self.z/l)
        return Vector3()
    
    def as_array(self):
        return [self.x, self.y, self.z]
    
    def __repr__(self):
        return f"({self.x:.2f}, {self.y:.2f}, {self.z:.2f})"

class Material:
    def __init__(self):
        self.name = ""
        self.ambient = [0.2, 0.2, 0.2]
        self.diffuse = [0.8, 0.8, 0.8]
        self.specular = [1.0, 1.0, 1.0]
        self.shininess = 100.0

class Face:
    def __init__(self):
        self.vertices = []
        self.normals = []

class Mesh:
    def __init__(self):
        self.material = None
        self.faces = []

class Camera:
    def __init__(self):
        self.eye = Vector3(0, 0, 10)
        self.target = Vector3(0, 0, 0)
        self.up = Vector3(0, 1, 0)
        self.fov = 45.0
        self.near = 0.1
        self.far = 100.0
        self.speed = 1.0  # Zwiększona prędkość
        self.rot_speed = 0.08  # Zwiększona prędkość rotacji
        self.pan_speed = 0.2  # Zwiększona prędkość przesuwania

class OBJLoader:
    @staticmethod
    def load_obj(filename):
        vertices = []
        normals = []
        materials = {}
        meshes = []
        current_mesh = None
        current_material = None
        
        with open(filename, 'r') as file:
            for line in file:
                tokens = line.split()
                if not tokens:
                    continue
                    
                if tokens[0] == 'v':
                    vertices.append(Vector3(*map(float, tokens[1:4])))
                elif tokens[0] == 'vn':
                    normals.append(Vector3(*map(float, tokens[1:4])))
                elif tokens[0] == 'mtllib':
                    materials = OBJLoader.load_mtl(tokens[1])
                elif tokens[0] == 'usemtl':
                    if current_mesh and current_mesh.faces:
                        meshes.append(current_mesh)
                    current_mesh = Mesh()
                    current_mesh.material = materials.get(tokens[1], Material())
                elif tokens[0] == 'f':
                    if current_mesh is None:
                        current_mesh = Mesh()
                        current_mesh.material = Material()
                    
                    face = Face()
                    for token in tokens[1:]:
                        parts = token.split('/')
                        vertex_index = int(parts[0]) - 1
                        normal_index = int(parts[2]) - 1 if len(parts) > 2 and parts[2] else -1
                        face.vertices.append(vertex_index)
                        if normal_index != -1 and normal_index < len(normals):
                            face.normals.append(normal_index)
                    current_mesh.faces.append(face)
        
        if current_mesh and current_mesh.faces:
            meshes.append(current_mesh)
            
        return vertices, normals, meshes

    @staticmethod
    def load_mtl(filename):
        materials = {}
        current_material = None
        
        try:
            with open(filename, 'r') as file:
                for line in file:
                    tokens = line.split()
                    if not tokens:
                        continue
                        
                    if tokens[0] == 'newmtl':
                        current_material = Material()
                        current_material.name = tokens[1]
                        materials[current_material.name] = current_material
                    elif tokens[0] == 'Ka' and current_material:
                        current_material.ambient = list(map(float, tokens[1:4]))
                    elif tokens[0] == 'Kd' and current_material:
                        current_material.diffuse = list(map(float, tokens[1:4]))
                    elif tokens[0] == 'Ks' and current_material:
                        current_material.specular = list(map(float, tokens[1:4]))
                    elif tokens[0] == 'Ns' and current_material:
                        current_material.shininess = float(tokens[1])
        except FileNotFoundError:
            print(f"Material file {filename} not found, using default materials")
        
        return materials

class SceneRenderer:
    def __init__(self, vertices, normals, meshes):
        self.vertices = vertices
        self.normals = normals
        self.meshes = meshes
        self.bbox_min = Vector3(float('inf'), float('inf'), float('inf'))
        self.bbox_max = Vector3(float('-inf'), float('-inf'), float('-inf'))
        self.calculate_bbox()
        # Display list będzie skompilowany później, po inicjalizacji OpenGL
        self.display_list = None
        self.display_list_compiled = False
        
    def calculate_bbox(self):
        for vertex in self.vertices:
            self.bbox_min.x = min(self.bbox_min.x, vertex.x)
            self.bbox_min.y = min(self.bbox_min.y, vertex.y)
            self.bbox_min.z = min(self.bbox_min.z, vertex.z)
            self.bbox_max.x = max(self.bbox_max.x, vertex.x)
            self.bbox_max.y = max(self.bbox_max.y, vertex.y)
            self.bbox_max.z = max(self.bbox_max.z, vertex.z)
    
    def get_center(self):
        return Vector3(
            (self.bbox_min.x + self.bbox_max.x) / 2,
            (self.bbox_min.y + self.bbox_max.y) / 2,
            (self.bbox_min.z + self.bbox_max.z) / 2
        )
    
    def get_size(self):
        return Vector3(
            self.bbox_max.x - self.bbox_min.x,
            self.bbox_max.y - self.bbox_min.y,
            self.bbox_max.z - self.bbox_min.z
        )
    
    def compile_display_list(self):
        """Kompiluje display list dla szybszego renderowania"""
        try:
            self.display_list = glGenLists(1)
            glNewList(self.display_list, GL_COMPILE)
            
            for mesh in self.meshes:
                material = mesh.material
                glMaterialfv(GL_FRONT, GL_AMBIENT, material.ambient)
                glMaterialfv(GL_FRONT, GL_DIFFUSE, material.diffuse)
                glMaterialfv(GL_FRONT, GL_SPECULAR, material.specular)
                glMaterialf(GL_FRONT, GL_SHININESS, material.shininess)
                
                for face in mesh.faces:
                    glBegin(GL_POLYGON)
                    for i, vertex_idx in enumerate(face.vertices):
                        if i < len(face.normals) and face.normals[i] < len(self.normals):
                            normal = self.normals[face.normals[i]]
                            glNormal3f(normal.x, normal.y, normal.z)
                        vertex = self.vertices[vertex_idx]
                        glVertex3f(vertex.x, vertex.y, vertex.z)
                    glEnd()
            
            glEndList()
        except Exception as e:
            print(f"Nie można skompilować display list: {e}")
            self.display_list = None
    
    def render(self):
        # Kompiluj display list przy pierwszym renderowaniu
        if not self.display_list_compiled:
            self.compile_display_list()
            self.display_list_compiled = True
            
        if self.display_list:
            glCallList(self.display_list)
        else:
            # Fallback do bezpośredniego renderowania
            self.render_direct()
    
    def render_direct(self):
        """Bezpośrednie renderowanie bez display list"""
        for mesh in self.meshes:
            material = mesh.material
            glMaterialfv(GL_FRONT, GL_AMBIENT, material.ambient)
            glMaterialfv(GL_FRONT, GL_DIFFUSE, material.diffuse)
            glMaterialfv(GL_FRONT, GL_SPECULAR, material.specular)
            glMaterialf(GL_FRONT, GL_SHININESS, material.shininess)
            
            for face in mesh.faces:
                glBegin(GL_POLYGON)
                for i, vertex_idx in enumerate(face.vertices):
                    if i < len(face.normals) and face.normals[i] < len(self.normals):
                        normal = self.normals[face.normals[i]]
                        glNormal3f(normal.x, normal.y, normal.z)
                    vertex = self.vertices[vertex_idx]
                    glVertex3f(vertex.x, vertex.y, vertex.z)
                glEnd()

class CameraController:
    def __init__(self, camera, scene_center):
        self.camera = camera
        self.scene_center = scene_center
        self.orbit_radius = (camera.eye - scene_center).length()
        self.orbit_angle_x = math.atan2(camera.eye.z - scene_center.z, camera.eye.x - scene_center.x)
        self.orbit_angle_y = math.asin((camera.eye.y - scene_center.y) / self.orbit_radius)
        
        # Kąty dla rozglądania się (look around) - pełna swoboda!
        view_dir = (camera.target - camera.eye).normalize()
        self.look_yaw = math.atan2(view_dir.z, view_dir.x)
        self.look_pitch = math.asin(view_dir.y)
        
        self.update_camera_position()
    
    def update_camera_position(self):
        """Aktualizuje pozycję kamery na podstawie kątów orbity - obsługuje pełne 360°"""
        self.camera.eye = Vector3(
            self.scene_center.x + self.orbit_radius * math.cos(self.orbit_angle_x) * math.cos(self.orbit_angle_y),
            self.scene_center.y + self.orbit_radius * math.sin(self.orbit_angle_y),
            self.scene_center.z + self.orbit_radius * math.sin(self.orbit_angle_x) * math.cos(self.orbit_angle_y)
        )
        self.camera.target = self.scene_center
    
    def pan(self, dx, dy):
        view_dir = (self.camera.target - self.camera.eye).normalize()
        right = view_dir.cross(self.camera.up).normalize()
        up = right.cross(view_dir).normalize()
        
        pan_vector = right * dx * self.camera.pan_speed + up * dy * self.camera.pan_speed
        self.camera.eye = self.camera.eye + pan_vector
        self.camera.target = self.camera.target + pan_vector
        
        # Aktualizuj centrum orbity i kąty look_around
        self.scene_center = self.camera.target
        self.update_look_angles()
    
    def walk(self, direction):
        view_dir = (self.camera.target - self.camera.eye).normalize()
        move_vector = view_dir * direction * self.camera.speed
        self.camera.eye = self.camera.eye + move_vector
        self.camera.target = self.camera.target + move_vector
        
        # Aktualizuj centrum orbity i promień
        self.scene_center = self.camera.target
        self.orbit_radius = (self.camera.eye - self.scene_center).length()
        self.update_look_angles()
    
    def update_look_angles(self):
        """Aktualizuje kąty look_around na podstawie obecnego kierunku kamery"""
        view_dir = (self.camera.target - self.camera.eye).normalize()
        self.look_yaw = math.atan2(view_dir.z, view_dir.x)
        # Usunięto zabezpieczenie - pełna swoboda!
        self.look_pitch = math.asin(view_dir.y)
    
    def zoom(self, factor):
        """Zbliżanie/oddalanie przez zmianę promienia orbity"""
        self.orbit_radius *= factor
        if self.orbit_radius < 0.1:
            self.orbit_radius = 0.1
        elif self.orbit_radius > 1000:
            self.orbit_radius = 1000
        self.update_camera_position()
        self.update_look_angles()  # Synchronizuj kąty look_around
    
    def orbit(self, dx, dy):
        """Orbitowanie - stabilne w pionie, pełna swoboda w poziomie"""
        # Pełna swoboda w poziomie (lewo/prawo)
        self.orbit_angle_x += dx * self.camera.rot_speed
        
        # Stabilne orbitowanie w pionie - z ograniczeniami dla stabilności
        self.orbit_angle_y += dy * self.camera.rot_speed
        
        # Ograniczenia tylko dla orbity pionowej (żeby kamera się nie przewracała)
        max_orbit_pitch = math.pi/2 - 0.01  # ~89 stopni
        self.orbit_angle_y = max(-max_orbit_pitch, min(max_orbit_pitch, self.orbit_angle_y))
        
        self.update_camera_position()
        self.update_look_angles()  # Synchronizuj kąty look_around
    
    def look_around(self, dx, dy):
        """Rozglądanie się - PEŁNA SWOBODA bez ograniczeń!"""
        # Aktualizuj kąty bez żadnych ograniczeń
        self.look_yaw += dx * self.camera.rot_speed
        self.look_pitch += dy * self.camera.rot_speed
        
        # BRAK OGRANICZEŃ! Możesz robić nieskończone koła w każdym kierunku
        
        # Oblicz nowy kierunek patrzenia
        cos_pitch = math.cos(self.look_pitch)
        new_view_dir = Vector3(
            cos_pitch * math.cos(self.look_yaw),
            math.sin(self.look_pitch), 
            cos_pitch * math.sin(self.look_yaw)
        )
        
        # Ustaw nowy target
        self.camera.target = self.camera.eye + new_view_dir

class GraphicsApp:
    def __init__(self, obj_file, mtl_file=None):
        self.vertices, self.normals, self.meshes = OBJLoader.load_obj(obj_file)
        self.scene = SceneRenderer(self.vertices, self.normals, self.meshes)
        self.camera = Camera()
        self.setup_camera()
        self.camera_controller = CameraController(self.camera, self.scene.get_center())
        self.window_width = 800
        self.window_height = 600

    def setup_camera(self):
        center = self.scene.get_center()
        size = self.scene.get_size()
        max_size = max(size.x, size.y, size.z)
        
        # Ustaw kamerę tak, aby widzieć całą scenę
        self.camera.eye = center + Vector3(max_size, max_size, max_size * 2)
        self.camera.target = center
        self.camera.fov = 45.0

    def init_lighting(self):
        glEnable(GL_LIGHTING)
        glEnable(GL_LIGHT0)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_COLOR_MATERIAL)
        glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE)
        glShadeModel(GL_SMOOTH)
        
        # Światło kierunkowe
        glLightfv(GL_LIGHT0, GL_POSITION, [0.0, 1.0, 1.0, 0.0])
        glLightfv(GL_LIGHT0, GL_DIFFUSE, [1.0, 1.0, 1.0, 1.0])
        glLightfv(GL_LIGHT0, GL_SPECULAR, [1.0, 1.0, 1.0, 1.0])
        
        # Światło punktowe
        glEnable(GL_LIGHT1)
        bbox_max = self.scene.bbox_max
        glLightfv(GL_LIGHT1, GL_POSITION, [bbox_max.x, bbox_max.y, bbox_max.z, 1.0])
        glLightfv(GL_LIGHT1, GL_DIFFUSE, [0.8, 0.8, 0.8, 1.0])
        glLightfv(GL_LIGHT1, GL_SPECULAR, [0.8, 0.8, 0.8, 1.0])
        glLightf(GL_LIGHT1, GL_CONSTANT_ATTENUATION, 0.5)
        glLightf(GL_LIGHT1, GL_LINEAR_ATTENUATION, 0.05)

    def display(self):
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        glMatrixMode(GL_MODELVIEW)
        glLoadIdentity()
        
        gluLookAt(
            self.camera.eye.x, self.camera.eye.y, self.camera.eye.z,
            self.camera.target.x, self.camera.target.y, self.camera.target.z,
            self.camera.up.x, self.camera.up.y, self.camera.up.z
        )
        
        self.scene.render()
        glutSwapBuffers()

    def reshape(self, w, h):
        self.window_width = w
        self.window_height = h
        glViewport(0, 0, w, h)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        gluPerspective(self.camera.fov, w/h, self.camera.near, self.camera.far)
        glMatrixMode(GL_MODELVIEW)

    def keyboard(self, key, x, y):
        if key == b'q' or key == b'\x1b':  # ESC też zamyka
            sys.exit(0)
        # Panning
        elif key == b'a':  # Pan left
            self.camera_controller.pan(-1, 0)
        elif key == b'd':  # Pan right
            self.camera_controller.pan(1, 0)
        elif key == b'r':  # Pan up
            self.camera_controller.pan(0, 1)
        elif key == b'f':  # Pan down
            self.camera_controller.pan(0, -1)
        # Walking
        elif key == b'w':  # Walk forward
            self.camera_controller.walk(1)
        elif key == b's':  # Walk backward
            self.camera_controller.walk(-1)
        # Orbiting
        elif key == b'j':  # Orbit left
            self.camera_controller.orbit(-1, 0)
        elif key == b'l':  # Orbit right
            self.camera_controller.orbit(1, 0)
        elif key == b'i':  # Orbit up
            self.camera_controller.orbit(0, 1)
        elif key == b'k':  # Orbit down
            self.camera_controller.orbit(0, -1)
        # Zoom
        elif key == b'z':  # Zoom in
            self.camera_controller.zoom(0.9)
        elif key == b'x':  # Zoom out
            self.camera_controller.zoom(1.1)
        
        glutPostRedisplay()

    def special_keys(self, key, x, y):
        """Obsługa klawiszy specjalnych (strzałki) do rozglądania się"""
        if key == GLUT_KEY_LEFT:
            self.camera_controller.look_around(1, 0)
        elif key == GLUT_KEY_RIGHT:
            self.camera_controller.look_around(-1, 0)
        elif key == GLUT_KEY_UP:
            self.camera_controller.look_around(0, 1)
        elif key == GLUT_KEY_DOWN:
            self.camera_controller.look_around(0, -1)
        
        glutPostRedisplay()

    def print_controls(self):
        print("\n=== STEROWANIE KAMERĄ ===")
        print("WASD + RF - Przesuwanie (pan):")
        print("  W/S - do przodu/tyłu")
        print("  A/D - lewo/prawo") 
        print("  R/F - góra/dół")
        print()
        print("IJKL - Orbitowanie (STABILNE!):")
        print("  I/K - góra/dół (stabilne ~180°)")
        print("  J/L - lewo/prawo (nieskończone koła!)")
        print()
        print("STRZAŁKI - Rozglądanie się (NIESKOŃCZONE KOŁA!):")
        print("  ←→ - lewo/prawo (bez ograniczeń)") 
        print("  ↑↓ - góra/dół (bez ograniczeń)")
        print()
        print("Z/X - Zoom in/out")
        print("Q/ESC - Wyjście")
        print()
        print("💡 BEST OF BOTH: Stabilna orbita + swobodne rozglądanie!")
        print("=======================\n")

    def run(self):
        glutInit(sys.argv)
        glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH)
        glutInitWindowSize(self.window_width, self.window_height)
        glutCreateWindow(b"3D Scene Viewer - Poprawione sterowanie")
        glutDisplayFunc(self.display)
        glutReshapeFunc(self.reshape)
        glutKeyboardFunc(self.keyboard)
        glutSpecialFunc(self.special_keys)  # Dodane dla strzałek
        self.init_lighting()
        self.print_controls()
        glutMainLoop()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python viewer.py <obj_file> [mtl_file]")
        sys.exit(1)
    
    obj_file = sys.argv[1]
    mtl_file = sys.argv[2] if len(sys.argv) > 2 else None
    
    app = GraphicsApp(obj_file, mtl_file)
    app.run()