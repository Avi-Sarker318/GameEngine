package com.avi.Mario.renderer;

import com.avi.Mario.jade.Window;
import com.avi.Mario.util.AssetPool;
import com.avi.Mario.util.JMath;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL20C.glDisableVertexAttribArray;


public class DebugDraw {
    private final static int MAX_LINES = 500;

    private static List<Line2D> lines = new ArrayList<>();

    // 6 floats per vertex, 2 vertices per line

    private static float[] vertexArray = new float [MAX_LINES * 6 *2];

    private static Shader shader = AssetPool.getShader("assets/shaders/debugLine2D.glsl");


    private static int vaoID;
    private static int vboID;

    private static boolean started = false;

    public static void start() {
        //Generate the vao
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        //Create the vbo and buffer some memory

        vboID = GL15.glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL_ARRAY_BUFFER, vertexArray.length * Float.BYTES, GL_DYNAMIC_DRAW);

        //ENABLE the vertex array attributes
        glVertexAttribPointer(0,3,GL_FLOAT, false, 6*Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1,3,GL_FLOAT, false, 6*Float.BYTES, 3* Float.BYTES);
        glEnableVertexAttribArray(1);

        glLineWidth(2.0f);
    }

    public static void beginFrame() {
        if(!started) {
            start();
            started = true;
        }

        //remove dead lines
        for(int i= 0;i< lines.size(); i++) {
            if(lines.get(i).beginFrame() < 0) {
                lines.remove(i);
                i--;
            }
        }
    }

    public static void draw() {
        if(lines.size() <= 0 )return;

        int index = 0;
        for(Line2D line: lines) {
            for(int i=0;i< 2; i++) {
                Vector2f position = i == 0 ? line.getFrom() : line.getTo();
                Vector3f color = line.getColor();

                // load position
                vertexArray[index] = position.x;
                vertexArray[index + 1] = position.y;
                vertexArray[index + 2] = -10.0f;

                //load the color
                vertexArray[index + 3] = color.x;
                vertexArray[index + 4] = color.y;
                vertexArray[index + 5] = color.z;
                index += 6;
            }

        }

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, Arrays.copyOfRange(vertexArray, 0, lines.size() * 6 * 2));

        //use our shader
        shader.use();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());


        //bind the vao
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        //draw the batch
        glDrawArrays(GL_LINES, 0, lines.size() * 2);

        //Disable location
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
        
        //unbind shader
        shader.detach();

    }

    public static void addLine2D(Vector2f from, Vector2f to) {
        addLine2D(from, to, new Vector3f(0,1,0),1);
    }

    public static void addLine2D(Vector2f from, Vector2f to, Vector3f color) {
        addLine2D(from, to, color, 1);
    }

    public static void addLine2D(Vector2f from, Vector2f to, Vector3f color, int lifetime) {
        if(lines.size() >= MAX_LINES) return;

        DebugDraw.lines.add(new Line2D(from, to, color, lifetime));



    }
    public static void addBox2D(Vector2f center , Vector2f dimensions, float rotation,Vector3f color, int lifetime) {
        Vector2f min = new Vector2f(center).sub(new Vector2f(dimensions).mul(0.5f));
        Vector2f max = new Vector2f(center).add(new Vector2f(dimensions).mul(0.5f));

        Vector2f [] vertices = {
            new Vector2f(min.x,min.y), new Vector2f(min.x, max.y),
                new Vector2f(max.x,max.y), new Vector2f(max.x, min.y)
        };
        if(rotation != 0.0f) {
            for(Vector2f vert : vertices) {
                JMath.rotate(vert, rotation, center);
            }
        }

        addLine2D(vertices[0], vertices[1],color, lifetime);
        addLine2D(vertices[0], vertices[3],color, lifetime);
        addLine2D(vertices[1], vertices[2],color, lifetime);
        addLine2D(vertices[2], vertices[3],color, lifetime);

    }
}
