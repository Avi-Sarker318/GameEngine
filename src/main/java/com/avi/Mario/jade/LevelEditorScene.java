package com.avi.Mario.jade;

import com.avi.Mario.renderer.Shader;
import com.avi.Mario.renderer.Texture;
import com.avi.Mario.util.Time;
import components.SpriteRenderer;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL20.*;

public class LevelEditorScene extends Scene {

    private String vertexShaderSrc = "#version 330 core\n" +
            "layout (location =0) in vec3 aPos;\n" +
            "layout(location=1) in vec4 aColor;\n" +
            "\n" +
            "out vec4 fColor;\n" +
            "void main()\n" +
            "{\n" +
            "    fColor = aColor;\n" +
            "    gl_Position = vec4(aPos,1.0);\n" +
            "}";

    private String fragmentShaderSrc = "#version 330 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main() {\n" +
            "    color =fColor;\n" +
            "}";

    private int vertexID, fragmentID, shaderProgram;
    private float[] vertexArray = {
    // position             // color
      100.5f,0.5f,0.0f,     1.0f,0.0f,0.0f,1.0f,        0,1,       //Bottom right
      0.5f,100.5f,0.0f,     0.0f,1.0f,0.0f,1.0f,        0,1,       //Top Left
      100.5f,100.5f,0.0f,     0.0f,0.0f,1.0f,1.0f,      1,1,       //Top Right
      0.5f,0.5f,0.0f,        1.0f,1.0f,0.0f,1.0f,       0,0,       // Bottom Left
    };
    //import counter clockwise order
    private int[] elementArray = {

            /*
                    x         x


                    x         x
             */
            2,1,0,
            0,1,3
    };


    private int vaoID, vboID, eboID;
    private Shader defaultShader;
    private Texture testTexture;

    GameObject testObj;
    private boolean firstTime = false;
    public LevelEditorScene() {

    }
    @Override
    public void init() {
        this.testObj = new GameObject("test object");
        this.testObj.addComponent(new SpriteRenderer());
        this.addGameObjecttoScene(this.testObj);



        this.camera = new Camera(new Vector2f());
        defaultShader = new Shader("assets/shaders/defaultShader.glsl");
        defaultShader.compile();
        this.testTexture = new Texture("assets/images/testImage.jpg");

        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);
        //create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER,vertexBuffer,GL_STATIC_DRAW);

        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);
        //ADD
        int positionsSize = 3;
        int colorSize = 4;
        int uvSize = 2;
        int vertexSizeBytes = (positionsSize + colorSize + uvSize) * Float.BYTES;
        glVertexAttribPointer(0, positionsSize,GL_FLOAT, false, vertexSizeBytes,0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1,colorSize,GL_FLOAT,false, vertexSizeBytes, positionsSize * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2,uvSize, GL_FLOAT, false, vertexSizeBytes,(positionsSize + colorSize) * Float.BYTES);
        glEnableVertexAttribArray(2);



    }
    @Override
    public void update(float dt) {
        //BIND SHADER PROGRAM
        //camera.position.x -= dt *50.0f;
        //camera.position.y -= dt * 20.0f;
        defaultShader.use();

        //upload texture to shader
        defaultShader.uploadTexture("TEX_SAMPLER", 0);
        glActiveTexture(GL_TEXTURE0);
        testTexture.bind();

        defaultShader.updateMat4f("uProjection", camera.getProjectionMatrix());
        defaultShader.updateMat4f("uView",camera.getViewMatrix());
        defaultShader.uploadFloat("uTime", Time.getTime());
        glBindVertexArray(vaoID);
        //ENABLE THE VERTEX ATTRIBUTE POINTER
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);
        //UNBIND EVERYTHING
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);
        defaultShader.detach();

        if(!firstTime) {
            System.out.println("Creating game Object");
            GameObject go = new GameObject("Game test 2");
            go.addComponent(new SpriteRenderer());
            this.addGameObjecttoScene(go);
            firstTime = true;
        }

            for(GameObject go: this.gameObjects) {
                go.update(dt);
            }

        }


}

