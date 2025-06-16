import sys
import numpy as np
import math
from OpenGL.GL import *
from OpenGL.GLU import *
from OpenGL.GLUT import *

class Camera:
    def __init__(self):
        self.eye = np.array([5.0, 5.0, 5.0])
        self.target = np.array([0.0, 0.0, 0.0])
        self.up = np.array([0.0, 1.0, 0.0])
        
        self.orbit_center = np.array([0.0, 0.0, 0.0])
        
        self.cam_params = None
        
        self._calculate_initial_angles()

    def _calculate_initial_angles(self):
        view_dir = self.target - self.eye
        view_dir = view_dir / np.linalg.norm(view_dir)
        
        self.yaw = math.atan2(view_dir[2], view_dir[0])
        
        self.pitch = math.asin(np.clip(view_dir[1], -1.0, 1.0))

    def load_cam_file(self, filename):
        """Wczytuje parametry kamery z pliku .cam"""
        with open(filename, 'r') as f:
            lines = [line.strip() for line in f if line.strip() and not line.startswith('#')]
        
        eye_data = list(map(float, lines[0].split()))
        target_data = list(map(float, lines[1].split()))
        resolution_data = list(map(int, lines[2].split()))
        top_left_data = list(map(float, lines[3].split()))
        du_data = list(map(float, lines[4].split()))
        dv_data = list(map(float, lines[5].split()))
        
        self.cam_params = {
            'resolution': (resolution_data[0], resolution_data[1]),
            'top_left': np.array(top_left_data),
            'du': np.array(du_data),
            'dv': np.array(dv_data)
        }
        
        self._convert_cam_to_opengl(
            np.array(eye_data), np.array(target_data),
            np.array(top_left_data), np.array(du_data), np.array(dv_data)
        )

    def _convert_cam_to_opengl(self, eye_pos, target_pos, top_left, du, dv):
        self.eye = eye_pos
        self.target = target_pos
        
        du_norm = du / np.linalg.norm(du)
        dv_norm = dv / np.linalg.norm(dv)
        
        up_candidate = np.cross(dv_norm, du_norm)
        up_candidate = up_candidate / np.linalg.norm(up_candidate)
        
        if up_candidate[1] < 0:
            up_candidate = -up_candidate
        
        self.up = up_candidate
        
        self.orbit_center = self.target.copy()
        self._calculate_initial_angles()
        
        print(f"  du_norm: {du_norm}")
        print(f"  dv_norm: {dv_norm}")
        print(f"  up obliczone: {self.up}")

    def get_cam_info(self):
        if self.cam_params:
            return (
                f"Rozdzielczość: {self.cam_params['resolution'][0]}x{self.cam_params['resolution'][1]}\n"
                f"Górny lewy róg: {self.cam_params['top_left']}\n"
                f"Wektor dU: {self.cam_params['du']}\n"
                f"Wektor dV: {self.cam_params['dv']}"
            )
        return "Brak parametrów"

    def look_at(self):
        gluLookAt(*self.eye, *self.target, *self.up)

    def pan(self, dx, dy):
        view_dir = self.target - self.eye
        view_dir /= np.linalg.norm(view_dir)

        right = np.cross(view_dir, self.up)
        right /= np.linalg.norm(right)

        up = self.up

        translation = dx * right + dy * up
        self.eye += translation
        self.target += translation
        

    def walk(self, dz):
        view_dir = self.target - self.eye
        view_dir /= np.linalg.norm(view_dir)

        translation = dz * view_dir
        self.eye += translation
        self.target += translation
        

    def rotate(self, d_yaw, d_pitch):
        
        # Aktualizuj kąty
        self.yaw += d_yaw
        self.pitch += d_pitch
        
        # Ogranicz pitch żeby nie przewrócić kamery
        max_pitch = math.pi / 2 - 0.1  # 80 stopni
        old_pitch = self.pitch
        self.pitch = max(-max_pitch, min(max_pitch, self.pitch))
        
        # Oblicz nowy kierunek patrzenia
        x = math.cos(self.pitch) * math.cos(self.yaw)
        y = math.sin(self.pitch)
        z = math.cos(self.pitch) * math.sin(self.yaw)
        
        view_dir = np.array([x, y, z])
        length = np.linalg.norm(view_dir)
        
        
        if length > 0:
            view_dir = view_dir / length
        else:
            print("BŁĄD: view_dir ma zerową długość!")
            return
        
        # Zachowaj odległość od target
        current_distance = np.linalg.norm(self.target - self.eye)
        if current_distance < 0.1:
            current_distance = 5.0  # domyślna odległość
        
        self.target = self.eye + current_distance * view_dir

    def orbit(self, d_theta, d_phi):
        """
        Orbituje kamerą wokół punktu orbit_center.
        d_theta - zmiana kąta azymutalnego (poziomo)
        d_phi - zmiana kąta polarnego (pionowo)
        """
        current_view_dir = self.target - self.eye
        current_distance_to_target = np.linalg.norm(current_view_dir)
        current_view_dir = current_view_dir / current_distance_to_target
        
        # Wektor od orbit_center do eye
        to_eye = self.eye - self.orbit_center
        radius = np.linalg.norm(to_eye)
        
        
        if radius < 0.001:  # Unikaj dzielenia przez zero
            print("BŁĄD: Za mała odległość od centrum orbitowania!")
            return

        
        theta = math.atan2(to_eye[2], to_eye[0])
        phi = math.acos(np.clip(to_eye[1] / radius, -1.0, 1.0))
        
        theta -= d_theta 
        phi += d_phi
        
        # Ogranicz phi żeby nie przejść przez bieguny
        phi = np.clip(phi, 0.1, math.pi - 0.1)
        
        x = radius * math.sin(phi) * math.cos(theta)
        y = radius * math.cos(phi)
        z = radius * math.sin(phi) * math.sin(theta)
        
        self.eye = self.orbit_center + np.array([x, y, z])
        
        self.target = self.orbit_center.copy()
        # self.target = self.eye + current_distance_to_target * current_view_dir
        
        self._calculate_initial_angles()

    def reset_camera(self):
        self.eye = np.array([5.0, 5.0, 5.0])
        self.target = np.array([0.0, 0.0, 0.0])
        self.orbit_center = np.array([0.0, 0.0, 0.0])
        self.up = np.array([0.0, 1.0, 0.0])
        self.cam_params = None
        self._calculate_initial_angles()

    def set_orbit_center(self, center=None):
        if center is None:
            self.orbit_center = self.target.copy()
        else:
            self.orbit_center = np.array(center)

        

class Material:
    def __init__(self, name):
        self.name = name
        
        # Podstawowe parametry materiału (już masz)
        self.Ka = np.array([0.2, 0.2, 0.2])  # ambient
        self.Kd = np.array([0.8, 0.8, 0.8])  # diffuse
        self.Ks = np.array([1.0, 1.0, 1.0])  # specular
        self.Ns = 32.0                       # shininess/specular exponent
        self.d = 1.0                         # dissolve (transparency)
        
        # Dodatkowe parametry MTL
        self.Ke = np.array([0.0, 0.0, 0.0])  # emissive
        self.Ni = 1.0                        # optical density (index of refraction)
        self.illum = 2                       # illumination model
        self.Tr = 0.0                        # transparency (alternative to d)
        
        # Mapowania tekstur - ścieżki do plików
        self.map_Ka = None    # ambient texture map
        self.map_Kd = None    # diffuse texture map  
        self.map_Ks = None    # specular texture map
        self.map_Ke = None    # emissive texture map
        self.map_Ns = None    # shininess texture map
        self.map_d = None     # alpha texture map
        self.map_bump = None  # bump map
        self.bump = None      # bump map (alternatywna nazwa)
        self.disp = None      # displacement map
        self.decal = None     # decal map
        self.refl = None      # reflection map
        
        # Ścieżka do katalogu z materiałami (potrzebna do tekstur)
        self.material_dir = ""

    def calculate_average_color_from_texture(self, texture_path):
        """
        Wczytuje obraz tekstury i oblicza jego średni kolor.
        Zwraca kolor jako numpy array [R, G, B] w zakresie 0-1.
        """
        try:
            # Pełna ścieżka do tekstury
            full_path = os.path.join(self.material_dir, texture_path)
            
            # Sprawdź czy plik istnieje
            if not os.path.exists(full_path):
                print(f"Ostrzeżenie: Nie znaleziono tekstury: {full_path}")
                return None
            
            # Wczytaj obraz
            with Image.open(full_path) as img:
                # Konwertuj do RGB jeśli potrzeba
                if img.mode != 'RGB':
                    img = img.convert('RGB')
                
                # Konwertuj do numpy array
                img_array = np.array(img)
                
                # Oblicz średni kolor (średnia po wszystkich pikselach dla każdego kanału)
                avg_color = np.mean(img_array, axis=(0, 1))
                
                # Przekonwertuj z zakresu 0-255 do 0-1
                avg_color = avg_color / 255.0
                
                print(f"Średni kolor tekstury {texture_path}: RGB({avg_color[0]:.3f}, {avg_color[1]:.3f}, {avg_color[2]:.3f})")
                return avg_color
                
        except Exception as e:
            print(f"Błąd przy wczytywaniu tekstury {texture_path}: {e}")
            return None

    def apply_texture_as_color(self):
        """
        Zastępuje parametr Kd średnim kolorem z tekstury diffuse (map_Kd).
        Wywołuje się to po wczytaniu wszystkich parametrów materiału.
        """
        if self.map_Kd:
            avg_color = self.calculate_average_color_from_texture(self.map_Kd)
            if avg_color is not None:
                self.Kd = avg_color
                print(f"Materiał '{self.name}': Zastąpiono Kd średnim kolorem tekstury")
        
        # Opcjonalnie możesz też zastąpić inne parametry średnimi kolorami z odpowiednich tekstur
        if self.map_Ka:
            avg_color = self.calculate_average_color_from_texture(self.map_Ka)
            if avg_color is not None:
                self.Ka = avg_color
                
        if self.map_Ks:
            avg_color = self.calculate_average_color_from_texture(self.map_Ks)
            if avg_color is not None:
                self.Ks = avg_color

class Model:
    def __init__(self):
        self.vertices = []
        self.normals = []
        self.faces = []
        self.materials = {}
        self.current_material = None

    def load_mtl(self, filename):
        """Wczytuje plik materiałów MTL z pełną obsługą wszystkich parametrów"""
        current_mtl = None
        material_dir = os.path.dirname(filename)  # Katalog z plikiem MTL
        
        try:
            with open(filename, 'r') as f:
                for line in f:
                    tokens = line.strip().split()
                    if not tokens or tokens[0].startswith('#'):
                        continue
                    
                    if tokens[0] == 'newmtl':
                        # Jeśli kończymy poprzedni materiał, zastosuj tekstury jako kolory
                        if current_mtl:
                            current_mtl.apply_texture_as_color()
                        
                        # Twórz nowy materiał
                        current_mtl = Material(tokens[1])
                        current_mtl.material_dir = material_dir
                        self.materials[tokens[1]] = current_mtl
                        
                    elif current_mtl:
                        # Podstawowe kolory
                        if tokens[0] == 'Ka':
                            current_mtl.Ka = np.array(list(map(float, tokens[1:4])))
                        elif tokens[0] == 'Kd':
                            current_mtl.Kd = np.array(list(map(float, tokens[1:4])))
                        elif tokens[0] == 'Ks':
                            current_mtl.Ks = np.array(list(map(float, tokens[1:4])))
                        elif tokens[0] == 'Ke':
                            current_mtl.Ke = np.array(list(map(float, tokens[1:4])))
                        
                        # Parametry liczbowe
                        elif tokens[0] == 'Ns':
                            current_mtl.Ns = float(tokens[1])
                        elif tokens[0] == 'd':
                            current_mtl.d = float(tokens[1])
                        elif tokens[0] == 'Tr':
                            current_mtl.Tr = float(tokens[1])
                            # Tr to alternatywa dla d: d = 1 - Tr
                            current_mtl.d = 1.0 - current_mtl.Tr
                        elif tokens[0] == 'Ni':
                            current_mtl.Ni = float(tokens[1])
                        elif tokens[0] == 'illum':
                            current_mtl.illum = int(tokens[1])
                        
                        # Mapowania tekstur
                        elif tokens[0] == 'map_Ka':
                            current_mtl.map_Ka = ' '.join(tokens[1:])  # Może być ścieżka ze spacjami
                        elif tokens[0] == 'map_Kd':
                            current_mtl.map_Kd = ' '.join(tokens[1:])
                        elif tokens[0] == 'map_Ks':
                            current_mtl.map_Ks = ' '.join(tokens[1:])
                        elif tokens[0] == 'map_Ke':
                            current_mtl.map_Ke = ' '.join(tokens[1:])
                        elif tokens[0] == 'map_Ns':
                            current_mtl.map_Ns = ' '.join(tokens[1:])
                        elif tokens[0] == 'map_d':
                            current_mtl.map_d = ' '.join(tokens[1:])
                        elif tokens[0] == 'map_bump' or tokens[0] == 'bump':
                            current_mtl.map_bump = ' '.join(tokens[1:])
                        elif tokens[0] == 'disp':
                            current_mtl.disp = ' '.join(tokens[1:])
                        elif tokens[0] == 'decal':
                            current_mtl.decal = ' '.join(tokens[1:])
                        elif tokens[0] == 'refl':
                            current_mtl.refl = ' '.join(tokens[1:])
            
            # Nie zapomnij zastosować tekstur dla ostatniego materiału
            if current_mtl:
                current_mtl.apply_texture_as_color()
                
        except Exception as e:
            print(f"Błąd wczytywania MTL: {e}")

    def load_obj(self, filename):
        mtl_dir = filename[:filename.rfind('/')+1] if '/' in filename else ""
        try:
            with open(filename, 'r') as f:
                for line in f:
                    tokens = line.strip().split()
                    if not tokens or tokens[0].startswith('#'):
                        continue
                    if tokens[0] == 'v':
                        self.vertices.append(np.array(list(map(float, tokens[1:4]))))
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

    def draw(self):
        last_material = None
        for material, face_verts in self.faces:
            if material != last_material:
                if material:
                    glMaterialfv(GL_FRONT, GL_AMBIENT, material.Ka)
                    glMaterialfv(GL_FRONT, GL_DIFFUSE, material.Kd)
                    glMaterialfv(GL_FRONT, GL_SPECULAR, material.Ks)
                    glMaterialf(GL_FRONT, GL_SHININESS, min(material.Ns, 128.0))
                else:
                    # Ustaw domyślny materiał gdy brak materiału
                    glMaterialfv(GL_FRONT, GL_AMBIENT, [0.2, 0.2, 0.2, 1.0])
                    glMaterialfv(GL_FRONT, GL_DIFFUSE, [0.8, 0.8, 0.8, 1.0])
                    glMaterialfv(GL_FRONT, GL_SPECULAR, [1.0, 1.0, 1.0, 1.0])
                    glMaterialf(GL_FRONT, GL_SHININESS, 32.0)
                last_material = material
            glBegin(GL_POLYGON)
            for v_idx, n_idx in face_verts:
                if n_idx >= 0 and n_idx < len(self.normals):
                    glNormal3fv(self.normals[n_idx])
                glVertex3fv(self.vertices[v_idx])
            glEnd()

class SceneViewer:
    def __init__(self, obj_file, cam_file=None):
        self.obj_file = obj_file
        self.cam_file = cam_file
        self.model = Model()
        self.camera = Camera()
        self.window_width = 800
        self.window_height = 600

    def init_lighting(self):
        # Oświetlenie
        glEnable(GL_LIGHTING)
        glEnable(GL_LIGHT0)
        
        # Pozycja światła - dalej od obiektów
        glLightfv(GL_LIGHT0, GL_POSITION, [10.0, 10.0, 10.0, 1.0])
        glLightfv(GL_LIGHT0, GL_DIFFUSE, [1.0, 1.0, 1.0, 1.0])
        glLightfv(GL_LIGHT0, GL_SPECULAR, [1.0, 1.0, 1.0, 1.0])
        glLightfv(GL_LIGHT0, GL_AMBIENT, [0.3, 0.3, 0.3, 1.0])

        # Inne ustawienia
        glEnable(GL_DEPTH_TEST)
        glShadeModel(GL_SMOOTH)
        glEnable(GL_NORMALIZE)  # Automatyczna normalizacja normalnych
        
        # Ustawienie domyślnego koloru tła
        glClearColor(0.1, 0.1, 0.1, 1.0)

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
        gluPerspective(45.0, w/h if h != 0 else 1, 0.1, 100.0)
        glMatrixMode(GL_MODELVIEW)

    def handle_key(self, key, special=False):
        step = 0.2
        angle_step = math.radians(5)

        if special:
            # Strzałki - przesuwanie
            if key == GLUT_KEY_LEFT:
                self.camera.pan(-step, 0)
            elif key == GLUT_KEY_RIGHT:
                self.camera.pan(step, 0)
            elif key == GLUT_KEY_UP:
                self.camera.walk(step)
            elif key == GLUT_KEY_DOWN:
                self.camera.walk(-step)
        else:
            # WSAD - rozglądanie się
            if key == b'w':  # patrz w górę
                self.camera.rotate(0, angle_step)
            elif key == b's':  # patrz w dół  
                self.camera.rotate(0, -angle_step)
            elif key == b'a':  # patrz w lewo
                self.camera.rotate(-angle_step, 0)
            elif key == b'd':  # patrz w prawo
                self.camera.rotate(angle_step, 0)
            # Zachowaj l/o do pan
            elif key == b'l':  
                self.camera.pan(0, -step)
            elif key == b'o':  
                self.camera.pan(0, step)
            elif key == b'q':  # orbituj w lewo
                self.camera.orbit(-angle_step, 0)
            elif key == b'e':  # orbituj w prawo
                self.camera.orbit(angle_step, 0)
            elif key == b'r':  # orbituj w górę
                self.camera.orbit(0, -angle_step)
            elif key == b'f':  # orbituj w dół
                self.camera.orbit(0, angle_step)
            # C - ustaw centrum orbitowania
            elif key == b'c':  # ustaw centrum orbitowania na obecny target
                self.camera.set_orbit_center()
            # SPACJA - resetuj kamerę
            elif key == b' ':  # resetuj kamerę
                self.camera.reset_camera()
            # I - wyświetl informacje o parametrach RT
            elif key == b'i':
                print("\n=== INFORMACJE O KAMERZE ===")
                print(f"Eye: {self.camera.eye}")
                print(f"Target: {self.camera.target}")
                print(f"Up: {self.camera.up}")
                print(f"Orbit center: {self.camera.orbit_center}")
                print(self.camera.get_cam_info())
                print("============================\n")

        glutPostRedisplay()


    def keyboard(self, key, x, y):
        self.handle_key(key, special=False)

    def special_keys(self, key, x, y):
        self.handle_key(key, special=True)

    def run(self):
        glutInit(sys.argv)
        glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH)
        glutInitWindowSize(self.window_width, self.window_height)
        glutCreateWindow(b"Wizualizacja sceny 3D")
        
        # Ładowanie modelu
        self.model.load_obj(self.obj_file)
        
        # Ładowanie parametrów kamery jeśli podano plik .cam
        if self.cam_file:
            self.camera.load_cam_file(self.cam_file)
            if self.camera.cam_params and 'resolution' in self.camera.cam_params:
                self.window_width, self.window_height = self.camera.cam_params['resolution']
                print(f"Ustawiam rozdzielczość z pliku .cam: {self.window_width}x{self.window_height}")

        self.init_lighting()
        glutDisplayFunc(self.display)
        glutReshapeFunc(self.reshape)
        glutKeyboardFunc(self.keyboard)        # zwykłe klawisze
        glutSpecialFunc(self.special_keys)     # klawisze specjalne
        
        # Wyświetl instrukcje sterowania
        print("\n=== STEROWANIE ===")
        print("WSAD - rozglądanie się")
        print("Strzałki - przesuwanie/chodzenie")
        print("L/O - przesuwanie w górę/dół")
        print("Q/E - orbitowanie poziomo (lewo/prawo)")
        print("R/F - orbitowanie pionowo (góra/dół)")
        print("C - ustaw centrum orbitowania (tam gdzie patrzysz)")
        print("I - wyświetl informacje o kamerze")
        print("SPACJA - resetuj kamerę do pozycji początkowej")
        print("==================\n")
        
        glutMainLoop()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Użycie: python program.py <plik.obj> [plik.cam]")
        sys.exit(1)
    
    obj_file = sys.argv[1]
    cam_file = sys.argv[2] if len(sys.argv) > 2 else None
    
    viewer = SceneViewer(obj_file, cam_file)
    viewer.run()