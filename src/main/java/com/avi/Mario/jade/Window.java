package com.avi.Mario.jade;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private int width, height;
    private String title;
    private long glfwWindow;
    private ImGuiLayer imGuiLayer;
    public float r;
    public float g;
    public float b;
    public float a;

    private static Window window= null;

    private static Scene currentScene;
    private Window() {
        this.width = 1920;
        this.height = 1080;
        this.title = "Mario";
        r=0;
        g=0;
        b=0;
        a=1;
    }

    public static void changeScene(int newScene) {
        switch(newScene) {
            case 0:
                currentScene = new LevelEditorScene();
                break;
            case 1:
                currentScene= new LevelScene();
            default:
                assert false: "Unknown scene '" + newScene + "'";
                break;
        }
        currentScene.load();
        currentScene.init();
        currentScene.start();
    }
    public static Window get() {
        if(Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }
    public static Scene getScene() {
        return get().currentScene;
    }
    public void run(){
        System.out.println("Hello LWJGL" + Version.getVersion() + "!");
        init();
        loop();

    }
    public void init() {
        //setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();
        if(!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        //create window
        glfwWindow = glfwCreateWindow(this.width, this.height,this.title, NULL,NULL);
        if(glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create GLFW window");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);
        glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
            Window.setWidth(newWidth);
            Window.setHeight(newHeight);
        });


        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(1);

        //Make visible
        glfwShowWindow(glfwWindow);

        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        this.imGuiLayer = new ImGuiLayer(glfwWindow);
        this.imGuiLayer.initImGui();

        Window.changeScene(0);
    }
    public void loop() {
        float beginTime = (float)glfwGetTime();
        float dt = -1.0f;


        while(!glfwWindowShouldClose(glfwWindow)) {
            glfwPollEvents();


            glClearColor(r, g,b,a);
            glClear(GL_COLOR_BUFFER_BIT);

            if(dt>=0) {
                currentScene.update(dt);
            }
            this.imGuiLayer.update(dt, currentScene);;
            glfwSwapBuffers(glfwWindow);

            float endTime = (float)glfwGetTime();
             dt= endTime-beginTime;
            beginTime = endTime;
        }
        currentScene.saveExit();
    }
    public static int getWidth() {
        return get().width;
    }
    public static int getHeight() {
        return get().height;
    }
    public static void setWidth(int newWidth) {
        get().width = newWidth;
    }
    public static void setHeight(int newHeight) {
        get().height = newHeight;
    }
}
