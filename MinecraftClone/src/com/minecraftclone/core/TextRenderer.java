/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.minecraftclone.core;

import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImageWrite.*;

public class TextRenderer {

    private int textureId;
    private int width;
    private int height;
    private Font font;
    private Color color;
    private String currentText;

    public TextRenderer(String text, Font font, Color color) {
        this.font = font;
        this.color = color;
        updateText(text);
    }

    public void updateText(String newText) {
        if (newText.equals(currentText)) {
            return; // Evita regenerar si no cambia
        }
        if (textureId != 0) {
            GL11.glDeleteTextures(textureId);
        }

        BufferedImage img = createTextImage(newText, font, color);
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.textureId = loadTextureFromBufferedImage(img);
        this.currentText = newText;
    }

    private BufferedImage createTextImage(String text, Font font, Color color) {
        BufferedImage img = new BufferedImage(256, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setFont(font);
        g.setColor(new Color(255, 255, 255, 255)); // blanco opaco
        g.setBackground(new Color(0, 0, 0, 0)); // fondo completamente transparente
        g.clearRect(0, 0, img.getWidth(), img.getHeight());

        g.drawString(text, 10, 40);
        g.dispose();
        return img;
    }

    public static int loadTextureFromBufferedImage(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                buffer.put((byte) (pixel & 0xFF));         // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
            }
        }
        buffer.flip();

        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Preservar el canal alfa
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glBindTexture(GL_TEXTURE_2D, 0);
        return textureID;
    }

    public void render(float x, float y) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glColor4f(1, 1, 1, 1);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x, y);
        glTexCoord2f(1, 0);
        glVertex2f(x + width, y);
        glTexCoord2f(1, 1);
        glVertex2f(x + width, y + height);
        glTexCoord2f(0, 1);
        glVertex2f(x, y + height);
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }

    public void cleanup() {
        if (textureId != 0) {
            glDeleteTextures(textureId);
        }
    }
}
