/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.platformer.benchmark.platform.engine;

import org.platformer.utils.Configuration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;


public class Art {
    public static Image[][] mario;
    public static Image[][] racoonmario;
    public static Image[][] smallMario;
    public static Image[][] fireMario;
    public static Image[][] enemies;
    public static Image[][] items;
    public static Image[][] level;
    public static Image[][] particles;
    public static Image[][] font;
    public static Image[][] bg;
    public static Image[][] bg_gen;
    public static Image[][] princess;

    public enum SpriteSheets {
        BIGPLUMBER, RACOONPLUMBER, SMALLPLUMBER, HOTPLUMBER, ENEMIES, ITEMS, LEVEL,
        PARTICLES, FAR_BACKGROUND, NEAR_BACKGROUND, FONTS, PRINCESS
    }

    ;

    public static void init(GraphicsConfiguration gc) {
        try {
            //TODO: include config parameters
            Map<String,String> assets = Configuration.getConfiguration().getArtConfig();

            mario = cutImage(gc, assets.get(SpriteSheets.BIGPLUMBER.name()), 32, 32);
            racoonmario = cutImage(gc, assets.get(SpriteSheets.RACOONPLUMBER.name()), 32, 32);
            smallMario = cutImage(gc, assets.get(SpriteSheets.SMALLPLUMBER.name()), 16, 16);
            fireMario = cutImage(gc, assets.get(SpriteSheets.HOTPLUMBER.name()), 32, 32);
            enemies = cutImage(gc, assets.get(SpriteSheets.ENEMIES.name()), 16, 32);
            items = cutImage(gc, assets.get(SpriteSheets.ITEMS.name()), 16, 16);
            level = cutImage(gc, assets.get(SpriteSheets.LEVEL.name()), 16, 16);
            particles = cutImage(gc, assets.get(SpriteSheets.PARTICLES.name()), 8, 8);
            bg = cutImage(gc, assets.get(SpriteSheets.NEAR_BACKGROUND.name()), 32, 32);
            bg_gen = cutImage(gc, assets.get(SpriteSheets.FAR_BACKGROUND.name()), 32, 32);
            font = cutImage(gc, assets.get(SpriteSheets.FONTS.name()), 8, 8);
            princess = cutImage(gc, assets.get(SpriteSheets.PRINCESS.name()), 32, 32);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Image getImage(GraphicsConfiguration gc, String imageName) throws IOException {
        BufferedImage source = null;
        try {
            source = ImageIO.read(Art.class.getResourceAsStream(imageName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert source != null;
        Image image = gc.createCompatibleImage(source.getWidth(), source.getHeight(), Transparency.BITMASK);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return image;
    }

    private static Image[][] cutImage(GraphicsConfiguration gc, String imageName, int xSize, int ySize) throws IOException {
        Image source = getImage(gc, imageName);
        Image[][] images = new Image[source.getWidth(null) / xSize][source.getHeight(null) / ySize];
        for (int x = 0; x < source.getWidth(null) / xSize; x++) {
            for (int y = 0; y < source.getHeight(null) / ySize; y++) {
                Image image = gc.createCompatibleImage(xSize, ySize, Transparency.BITMASK);
                Graphics2D g = (Graphics2D) image.getGraphics();
                g.setComposite(AlphaComposite.Src);
                g.drawImage(source, -x * xSize, -y * ySize, null);
                g.dispose();
                images[x][y] = image;
            }
        }

        return images;
    }

}