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

package org.platformer.benchmark.platform.engine.level;

import org.platformer.benchmark.platform.engine.sprites.SpriteTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.platformer.benchmark.platform.engine.sprites.Sprite;
import org.platformer.utils.Configuration;
import org.platformer.tools.PlatformerAIOptions;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class Level implements Serializable {
    private static final long serialVersionUID = -2222762134065697580L;

    static public class objCounters implements Serializable {
        public int deadEndsCount = 0;
        public int cannonsCount = 0;
        public int hillStraightCount = 0;
        public int tubesCount = 0;
        public int blocksCount = 0;
        public int coinsCount = 0;
        public int gapsCount = 0;
        public int hiddenBlocksCount = 0;
        public int totalCannons;
        public int totalGaps;
        public int totalDeadEnds;
        public int totalBlocks;
        public int totalHiddenBlocks;
        public int totalCoins;
        public int totalHillStraight;
        public int totalTubes;
        // TODO:TASK:[M] : include in Evaluation info:
        public int totalPowerUps;

        public int mushrooms = 0;
        public int flowers = 0;
        public int creatures = 0;
        public int greenMushrooms = 0;

        private static final long serialVersionUID = 4505050755444159808L;

        public void reset(final PlatformerAIOptions args) {
            deadEndsCount = 0;
            cannonsCount = 0;
            hillStraightCount = 0;
            tubesCount = 0;
            blocksCount = 0;
            coinsCount = 0;
            gapsCount = 0;
            hiddenBlocksCount = 0;
            mushrooms = 0;
            flowers = 0;
            creatures = 0;
            greenMushrooms = 0;
            totalHillStraight = args.getHillStraightCount() ? Integer.MAX_VALUE : 0;
            totalCannons = args.getCannonsCount() ? Integer.MAX_VALUE : 0;
            totalGaps = args.getGapsCount() ? Integer.MAX_VALUE : 0;
            totalDeadEnds = args.getDeadEndsCount() ? Integer.MAX_VALUE : 0;
            totalBlocks = args.getBlocksCount() ? Integer.MAX_VALUE : 0;
            totalHiddenBlocks = args.getHiddenBlocksCount() ? Integer.MAX_VALUE : 0;
            totalCoins = args.getCoinsCount() ? Integer.MAX_VALUE : 0;
            totalTubes = args.getTubesCount() ? Integer.MAX_VALUE : 0;
            resetUncountableCounters();
        }

        public void resetUncountableCounters() {
            mushrooms = 0;
            flowers = 0;
            greenMushrooms = 0;
        }
    }

    public static final String[] BIT_DESCRIPTIONS = {//
            "BLOCK UPPER", //
            "BLOCK ALL", //
            "BLOCK LOWER", //
            "SPECIAL", //
            "BUMPABLE", //
            "BREAKABLE", //
            "PICKUPABLE", //
            "ANIMATED",//
    };

    public static byte[] TILE_BEHAVIORS = new byte[256];

    public static final int BIT_BLOCK_UPPER = 1 << 0;
    public static final int BIT_BLOCK_ALL = 1 << 1;
    public static final int BIT_BLOCK_LOWER = 1 << 2;
    public static final int BIT_SPECIAL = 1 << 3;
    public static final int BIT_BUMPABLE = 1 << 4;
    public static final int BIT_BREAKABLE = 1 << 5;
    public static final int BIT_PICKUPABLE = 1 << 6;
    public static final int BIT_ANIMATED = 1 << 7;

    public objCounters counters;

    //private final int FILE_HEADER = 0x271c4178;
    public int length;
    public int height;
    public int randomSeed;
    public int type;
    public int difficulty;

    public byte[][] map;
    public byte[][] data;
    // Experimental feature: Plumber TRACE
    public int[][] marioTrace;

    public SpriteTemplate[][] spriteTemplates;

    public int xExit;
    public int yExit;


    public Level(int length, int height) {
//        ints = new Vector();
//        booleans = new Vector();
        this.length = length;
        this.height = height;

        xExit = 50;
        yExit = 10;
//        System.out.println("Java: Level: lots of news here...");
//        System.out.println("length = " + length);
//        System.out.println("height = " + height);
        try {
            map = new byte[length][height];
//        System.out.println("map = " + map);
            data = new byte[length][height];
//        System.out.println("data = " + data);
            spriteTemplates = new SpriteTemplate[length][height];

            marioTrace = new int[length][height + 1];

        } catch (OutOfMemoryError e) {
            System.err.println("Java: PlatformerAI MEMORY EXCEPTION: OutOfMemory exception. Exiting...");
            e.printStackTrace();
            System.exit(-3);
        }
//        System.out.println("spriteTemplates = " + spriteTemplates);
//        observation = new byte[length][height];
//        System.out.println("observation = " + observation);
    }

    public static void loadBehaviors(DataInputStream dis) throws IOException {
        dis.readFully(Level.TILE_BEHAVIORS);
    }

    public static void saveBehaviors(DataOutputStream dos) throws IOException {
        dos.write(Level.TILE_BEHAVIORS);
    }

    public static Level load(Reader reader) throws IOException, ClassNotFoundException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Level level = gson.fromJson(reader, Level.class);
        return level;
    }

    public static ArrayList<char []> generateASCIILevel(int levelLength, int levelHeight,int randSeed){
        Random random = new Random(randSeed);
        ArrayList<char[]> level = new ArrayList<>();

        for (int y=0;y<levelHeight;y++){
            char [] line = new char[levelLength];
            for (int x =0;x<levelLength;x++){
                line[x] = '-'; //prefill with air
                int oracle = random.nextInt(18);
                float probability = random.nextFloat();

                if (probability < .4) {
                    line[x] = '-'; //prefill with air
                } else if (probability < .6) {
                    line[x] = 'X'; //square block or ground tile
                } else {
                    switch (oracle) {
                        case 0: line[x] = '<'; //left pipe opening
                            if (x<levelLength-1) {
                                line[x+1] = '>';
                                continue;
                            }

                            break;
                        case 1: line[x] = '>'; //right pipe opening
                            if (x>0) {
                                line[x-1] = '<';
                                continue;
                            }
                            break;
                        case 2: line[x] = '['; //left pipe
                            if (x<levelLength-1) {
                                line[x+1] = ']';
                                continue;
                            }
                            break;
                        case 3: line[x] = ']'; //right pipe
                            if (x>0) {
                                line[x-1] = '[';
                                continue;
                            }
                            break;
                        case 4: line[x] = 'E'; //gumba or other enemy
                            break;
                        case 5: line[x] = 'X'; //square block or ground tile
                            break;
                        case 6: line[x] = 'S'; //breakable block
                            break;
                        case 7: line[x] = 'Q'; //question block
                            break;
                        case 8: line[x] = 'C'; //coin
                            break;
                        case 9: line[x] = 'P'; //princess
                            break;
                        default:
                            break;
                    }

                }
                level.remove(y);
                level.add(y,line);

            }
        }

        return level;
    }

    public static ArrayList<char[]> mutateASCIILevel(ArrayList<char[]> level, int mutationPoints, int randSeed) {
        Random random = new Random(randSeed);
        int levelLength = level.size();
        int levelHeight = level.get(0).length;

        for (int m=0;m<mutationPoints;m++) {
            int y = random.nextInt(levelLength);
            int x = random.nextInt(levelHeight);
            int  oracle = random.nextInt(18);
            char[] line = level.get(y);

            line[x] = '-'; //prefill with air


            //TODO: apply some meaningful distribution to generate level

            switch (oracle) {
                case 0: line[x] = '<'; //left pipe opening
                    break;
                case 1: line[x] = '>'; //right pipe opening
                    break;
                case 2: line[x] = '['; //left pipe
                    break;
                case 3: line[x] = ']'; //right pipe
                    break;
                case 4: line[x] = 'E'; //gumba or other enemy
                    break;
                case 5: line[x] = 'X'; //square block or ground tile
                    break;
                case 6: line[x] = 'S'; //breakable block
                    break;
                case 7: line[x] = 'Q'; //question block
                    break;
                case 8: line[x] = 'C'; //coin
                    break;
                case 9: line[x] = 'P'; //princess
                    break;
                default:
                    break;
            }
            level.remove(y);
            level.add(y,line);
        }

        return level;
    }

    public static Level loadASCII(ArrayList<char []> levelSlices, PlatformerAIOptions platformerAIOptions) {

        Random random = new Random(platformerAIOptions.getLevelRandSeed());
        char[][] map = null;
        byte[][] shadowMap = null;
        ArrayList<char[]> mapStrings = levelSlices;


        Level level = LevelGenerator.createLevel(platformerAIOptions);

        if (mapStrings.size() < 1)
            return level;

        map = mapStrings.toArray(new char[1][1]);
        shadowMap  = new byte[map[0].length][map.length];
        level.data = new byte[map[0].length][map.length];
        level.spriteTemplates = new SpriteTemplate[map[0].length][map.length];

        //TODO: continue working here to get the ASCII reader in place
        for (int x = 0; x < map[0].length; x++) {
            for (int y = 0; y < map.length; y++) {
                switch (map[y][x]) {
                    case '-': //air
                        shadowMap[x][y] = 0;
                        break;
                    case '<': //left pipe opening
                        shadowMap[x][y] = 10;
                        break;
                    case '>': //right pipe opening
                        shadowMap[x][y] = 11;
                        break;
                    case '[': //left pipe
                    case 'p': //left pipe
                        shadowMap[x][y] = 26;
                        break;
                    case 'P':
                    case ']': //right pipe
                        shadowMap[x][y] = 27;
                        break;
                    case 'c': //canon base
                        shadowMap[x][y] = 14;
                        break;
                    case 'C': //flower pot or canon
                        shadowMap[x][y] = 30; // or 46
                        break;
                    case 'g': //gumba
                        level.setSpriteTemplate(x,y,new SpriteTemplate(Sprite.KIND_GOOMBA));
                        break;
                    case 'G': //gumba
                        level.setSpriteTemplate(x,y,new SpriteTemplate(Sprite.KIND_GOOMBA_WINGED));
                        break;
                    case 't': //spiky
                        level.setSpriteTemplate(x,y,new SpriteTemplate(Sprite.KIND_SPIKY));
                        break;
                    case 'k': //koopa
                        level.setSpriteTemplate(x,y,new SpriteTemplate(Sprite.KIND_GREEN_KOOPA));
                        break;
                    case 'K': //winged koopa
                        level.setSpriteTemplate(x,y,new SpriteTemplate(Sprite.KIND_GREEN_KOOPA_WINGED));
                        break;
                    case 'V': //flower thing
                        level.setSpriteTemplate(x,y,new SpriteTemplate(Sprite.KIND_ENEMY_FLOWER));
                        break;
                    case 'E': //gumba or other enemy
                        level.setSpriteTemplate(x,y,new SpriteTemplate(Sprite.KIND_GOOMBA));
                        break;
                    case 'X': //square block or ground tile
                        shadowMap[x][y] = -119;//also -103,...
                        break;
                    case 'S': //breakable block
                        shadowMap[x][y] = 16;//also 17,22
                        break;
                    case 'M'://shroom
                    case '*'://flower
                        shadowMap[x][y] = (byte) (22);
                        break;
                    case 'O'://multi coin
                        shadowMap[x][y] = (byte) (23);
                        break;
                    case 'Q': //question block
                        int tile = random.nextInt(3);
                        shadowMap[x][y] = (byte) (21 + tile);
                        break;
                    case '?': //hidden block
                        shadowMap[x][y] = (byte) (1);
                        break;
                    case 'o': //coin
                        shadowMap[x][y] = 34;
                        break;
                    case 'H': //princess aka Home
                        level.xExit = x;
                        level.yExit = y;
                        level.setSpriteTemplate(x, y, new SpriteTemplate(Sprite.KIND_PRINCESS));
                        break;
                    default:
                        break;
                }
            }
        }

        level.map = shadowMap;
        level.length = shadowMap.length;
        level.height = shadowMap[0].length;
        //coordinates of the exit
        if (level.xExit > level.length-1)
            level.xExit = level.length-1;

        Configuration.getConfiguration().write();

        return level;
    }

    public static Level loadASCII(Reader reader, PlatformerAIOptions platformerAIOptions) throws IOException, ClassNotFoundException {
        BufferedReader bReader = new BufferedReader(reader);
        ArrayList<char[]> levelSlices = new ArrayList<>();
        String line = bReader.readLine();
        while (line != null) {
            levelSlices.add(line.toCharArray());
            line = bReader.readLine();
        }
        return loadASCII(levelSlices,platformerAIOptions);
    }


    public void save(Writer oos) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        oos.write(gson.toJson(this));
        oos.flush();
        oos.close();
    }

    public String jSON() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static Level fromString(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Level level = gson.fromJson(json, Level.class);
        return level;
    }

    /**
     * Animates the unbreakable brick when smashed from below by Plumber
     */
    public void tick() {
        // TODO:!!H! Optimize this!
        for (int x = 0; x < length; x++)
            for (int y = 0; y < height; y++)
                if (data[x][y] > 0) data[x][y]--;
    }

    public byte getBlockCapped(int x, int y) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x >= length) x = length - 1;
        if (y >= height) y = height - 1;
        return map[x][y];
    }

    public byte getBlock(int x, int y) {
        if (x < 0) x = 0;
        if (y < 0) return 0;
        if (x >= length) x = length - 1;
        if (y >= height) y = height - 1;
        return map[x][y];
    }

    public void setBlock(int x, int y, byte b) {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= length) return;
        if (y >= height) return;
        map[x][y] = b;
    }

    public void setBlockData(int x, int y, byte b) {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= length) return;
        if (y >= height) return;
        data[x][y] = b;
    }

    public byte getBlockData(int x, int y) {
        if (x < 0) return 0;
        if (y < 0) return 0;
        if (x >= length) return 0;
        if (y >= height) return 0;
        return data[x][y];
    }

    public boolean isBlocking(int x, int y, float xa, float ya) {
        byte block = getBlock(x, y);
        boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
        blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
        blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;

        return blocking;
    }

    public SpriteTemplate getSpriteTemplate(int x, int y) {
        if (x < 0) return null;
        if (y < 0) return null;
        if (x >= length) return null;
        if (y >= height) return null;
        return spriteTemplates[x][y];
    }

    public boolean setSpriteTemplate(int x, int y, SpriteTemplate spriteTemplate) {
        if (x < 0) return false;
        if (y < 0) return false;
        if (x >= length) return false;
        if (y >= height) return false;
        spriteTemplates[x][y] = spriteTemplate;
        return true;
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
        counters = (Level.objCounters) aInputStream.readObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
        aOutputStream.writeObject(counters);
    }
}