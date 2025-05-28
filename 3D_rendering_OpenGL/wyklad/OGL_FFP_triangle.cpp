/*
Warto odwiedzić:
   https://learnopengl.com/
   https://www.glfw.org/

Instalacja:

1. Pobierz bibliotekę GLUT https://www.opengl.org/resources/libraries/glut/glutdlls37beta.zip
2. Rozpakuj pobrany plik zip w wybranej lokalizacji
4. W MSVC wybierz konfigurację x86
3. Skopiuj dliki dll z rozpakowanego folderu do:
     glut.dll oraz glut32.dll do C:\Windows\SysWOW64
     dodatkowo glut32.dll do C:\Windows\System32
3. Dodaj ścieżkę do folderu gdzie rozpakowano pobrany zip do opcji właściwości
   projektu w MSVC:
      Dodatkowe katalogi plików nagłówkowych
      Konsolidator->Ogólne->Dodatkowe katalogi bibliotek
3. Do listy dodatkowych bibliotek w Konsolidator->dane wejściowe->Dodatkowe zależności dodaj
      opengl32.lib
      glu32.lib
      glut32.lib
*/

#include <GL/glut.h>
#include <iostream>

void display()
{
    // Set background color as dark blue. Will be used by glClear(...)
    glClearColor(0.0f, 0.0f, 0.6f, 0.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glBegin(GL_TRIANGLES);
    glColor3f(1.0, 0.0, 0.0);
    glVertex2f(10.0, 10.0);

    glColor3f(0.0, 1.0, 0.0);
    glVertex2f(500.0, 10.0);

    glColor3f(0.0, 0.0, 1.0);
    glVertex2f(250.0, 500.0);
    // glVertex2f(200.0, 100.0);

    glEnd();
    glFlush();
}

void myinit()
{
    glClearColor(1.0, 1.0, 1.0, 1.0);
    glColor3f(1.0, 0.0, 0.0);
    glPointSize(5.0);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluOrtho2D(0.0, 499.0, 0.0, 499.0);
}

void main(int argc, char **argv)
{
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_SINGLE | GLUT_RGB);
    glutInitWindowSize(500, 500);
    glutInitWindowPosition(0, 0);
    glutCreateWindow("Points");
    glutDisplayFunc(display);

    myinit();

    // Pobranie i wyświetlenie wersji OpenGL
    std::cout << "Wersja OpenGL: " << glGetString(GL_VERSION) << std::endl;
    std::cout << "Renderer: " << glGetString(GL_RENDERER) << std::endl;
    std::cout << "Dostawca sterownika: " << glGetString(GL_VENDOR) << std::endl;

    glutMainLoop();
}
