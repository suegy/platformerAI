/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  Neither the name of the Mario AI nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

import clojure.lang.Obj;
import org.platformer.benchmark.platform.engine.level.Level;
import org.platformer.benchmark.platform.engine.sprites.SpriteTemplate;
import org.platformer.benchmark.platform.engine.level.LevelGenerator;
import org.platformer.benchmark.platform.engine.sprites.*;
import org.platformer.benchmark.platform.environments.Environment;
import org.platformer.tools.PlatformerAIOptions;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LevelScene implements SpriteContext
{
public static final boolean[] defaultKeys = new boolean[Environment.numberOfKeys];
public static final String[] keysStr = {"<<L ", "R>> ", "\\\\//", "JUMP", " RUN", "^UP^"};

public static final int cellSize = 16;

final public List<Sprite> sprites = new ArrayList<Sprite>();
final private List<Sprite> spritesToAdd = new ArrayList<Sprite>();
final private List<Sprite> spritesToRemove = new ArrayList<Sprite>();

public Level level;
public Plumber plumber;
public float xCam, yCam, xCamO, yCamO;

public int tickCount;

public int startTime = 0;
private int timeLeft;
private int width;
private int height;

private static boolean onLadder = false;

private Random randomGen = new Random(0);

final private List<Float> enemiesFloatsList = new ArrayList<Float>();
final private float[] marioFloatPos = new float[2];
final private int[] marioState = new int[11];
private int numberOfHiddenCoinsGained = 0;

private int greenMushroomMode = 0;

public String memo = "";
private Point marioInitialPos;
private int bonusPoints = -1;

//    public int getTimeLimit() {  return timeLimit; }

public void setTimeLimit(int timeLimit)
{ this.timeLimit = timeLimit; }

private int timeLimit = 200;

private long levelSeed;
private int levelType;
private int levelDifficulty;
private int levelLength;
private int levelHeight;
public int killedCreaturesTotal;
public int killedCreaturesByFireBall;
public int killedCreaturesByStomp;
public int killedCreaturesByShell;

private Replayer replayer;

//    private int[] args; //passed to reset method. ATTENTION: not cloned.

public LevelScene()
{
    try
    {
//            System.out.println("Java::LevelScene: loading tiles.dat...");
//            System.out.println("LS: System.getProperty(\"user.dir()\") = " + System.getProperty("user.dir"));
        Level.loadBehaviors(new DataInputStream(LevelScene.class.getResourceAsStream("/tiles.dat")));
    } catch (IOException e)
    {
        System.err.println("[platformerAI ERROR] : error loading file resources/tiles.dat ; ensure this file exists in ch/idsia/benchmark/platform/engine ");
        e.printStackTrace();
        System.exit(0);
    }
}


// TODO: !H!: Move to PlatformEnvironment !!

public float[] getEnemiesFloatPos()
{
    enemiesFloatsList.clear();
    for (Sprite sprite : sprites)
    {
        // TODO:[M]: add unit tests for getEnemiesFloatPos involving all kinds of creatures
        if (sprite.isDead()) continue;
        switch (sprite.kind)
        {
            case Sprite.KIND_GOOMBA:
            case Sprite.KIND_BULLET_BILL:
            case Sprite.KIND_ENEMY_FLOWER:
            case Sprite.KIND_GOOMBA_WINGED:
            case Sprite.KIND_GREEN_KOOPA:
            case Sprite.KIND_GREEN_KOOPA_WINGED:
            case Sprite.KIND_RED_KOOPA:
            case Sprite.KIND_RED_KOOPA_WINGED:
            case Sprite.KIND_SPIKY:
            case Sprite.KIND_SPIKY_WINGED:
            case Sprite.KIND_SHELL:
            {
                enemiesFloatsList.add((float) sprite.kind);
                enemiesFloatsList.add(sprite.x - plumber.x);
                enemiesFloatsList.add(sprite.y - plumber.y);
            }
        }
    }

    float[] enemiesFloatsPosArray = new float[enemiesFloatsList.size()];

    int i = 0;
    for (Float F : enemiesFloatsList)
        enemiesFloatsPosArray[i++] = F;

    return enemiesFloatsPosArray;
}

public int fireballsOnScreen = 0;

List<Shell> shellsToCheck = new ArrayList<Shell>();

public void checkShellCollide(Shell shell)
{
    shellsToCheck.add(shell);
}

List<Fireball> fireballsToCheck = new ArrayList<Fireball>();

public void checkFireballCollide(Fireball fireball)
{
    fireballsToCheck.add(fireball);
}

public void tick()
{
    if (GlobalOptions.isGameplayStopped)
        return;

    timeLeft--;
    if (timeLeft == 0)
        plumber.die("Time out!");
    xCamO = xCam;
    yCamO = yCam;

    if (startTime > 0)
    {
        startTime++;
    }

    float targetXCam = plumber.x - 160;

    xCam = targetXCam;

    if (xCam < 0) xCam = 0;
    if (xCam > level.length * cellSize - GlobalOptions.VISUAL_COMPONENT_WIDTH)
        xCam = level.length * cellSize - GlobalOptions.VISUAL_COMPONENT_WIDTH;

    fireballsOnScreen = 0;

    for (Sprite sprite : sprites)
    {
        if (sprite != plumber)
        {
            float xd = sprite.x - xCam;
            float yd = sprite.y - yCam;
            if (xd < -64 || xd > GlobalOptions.VISUAL_COMPONENT_WIDTH + 64 || yd < -64 || yd > GlobalOptions.VISUAL_COMPONENT_HEIGHT + 64)
            {
                removeSprite(sprite);
            } else
            {
                if (sprite instanceof Fireball)
                    fireballsOnScreen++;
            }
        }
    }

    tickCount++;
    level.tick();

//            boolean hasShotCannon = false;
//            int xCannon = 0;

    for (int x = (int) xCam / cellSize - 1; x <= (int) (xCam + this.width) / cellSize + 1; x++)
        for (int y = (int) yCam / cellSize - 1; y <= (int) (yCam + this.height) / cellSize + 1; y++)
        {
            int dir = 0;

            if (x * cellSize + 8 > plumber.x + cellSize) dir = -1;
            if (x * cellSize + 8 < plumber.x - cellSize) dir = 1;

            SpriteTemplate st = level.getSpriteTemplate(x, y);

            if (st != null)
            {
//                        if (st.getType() == Sprite.KIND_SPIKY)
//                        {
//                            System.out.println("here");
//                        }

                if (st.lastVisibleTick != tickCount - 1)
                {
                    if (st.sprite == null || !sprites.contains(st.sprite))
                        st.spawn(this, x, y, dir);
                }

                st.lastVisibleTick = tickCount;
            }

            if (dir != 0)
            {
                byte b = level.getBlock(x, y);
                if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0)
                {
                    if ((b % cellSize) / 4 == 3 && b / cellSize == 0)
                    {
                        if ((tickCount - x * 2) % 100 == 0)
                        {
//                                    xCannon = x;
                            for (int i = 0; i < 8; i++)
                            {
                                addSprite(new Sparkle(x * cellSize + 8, y * cellSize + (int) (Math.random() * cellSize), (float) Math.random() * dir, 0, 0, 1, 5));
                            }
                            addSprite(new BulletBill(this, x * cellSize + 8 + dir * 8, y * cellSize + 15, dir));

//                                    hasShotCannon = true;
                        }
                    }
                }
            }
        }

    for (Sprite sprite : sprites)
        sprite.tick();

    byte levelElement = level.getBlock(plumber.mapX, plumber.mapY);
    if (levelElement == (byte) (13 + 3 * 16) || levelElement == (byte) (13 + 5 * 16))
    {
        if (levelElement == (byte) (13 + 5 * 16))
            plumber.setOnTopOfLadder(true);
        else
            plumber.setInLadderZone(true);
    } else if (plumber.isInLadderZone())
    {
        plumber.setInLadderZone(false);
    }


    for (Sprite sprite : sprites)
        sprite.collideCheck();

    for (Shell shell : shellsToCheck)
    {
        for (Sprite sprite : sprites)
        {
            if (sprite != shell && !shell.dead)
            {
                if (sprite.shellCollideCheck(shell))
                {
                    if (plumber.carried == shell && !shell.dead)
                    {
                        plumber.carried = null;
                        plumber.setRacoon(false);
                        //System.out.println("sprite = " + sprite);
                        shell.die();
                        ++this.killedCreaturesTotal;
                    }
                }
            }
        }
    }
    shellsToCheck.clear();

    for (Fireball fireball : fireballsToCheck)
        for (Sprite sprite : sprites)
            if (sprite != fireball && !fireball.dead)
                if (sprite.fireballCollideCheck(fireball))
                    fireball.die();
    fireballsToCheck.clear();


    sprites.addAll(0, spritesToAdd);
    sprites.removeAll(spritesToRemove);
    spritesToAdd.clear();
    spritesToRemove.clear();
}

public void addSprite(Sprite sprite)
{
    spritesToAdd.add(sprite);
    sprite.tick();
}

public void removeSprite(Sprite sprite)
{
    spritesToRemove.add(sprite);
}

public void bump(int x, int y, boolean canBreakBricks)
{
    byte block = level.getBlock(x, y);

    if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0)
    {
        if (block == 1)
            this.plumber.gainHiddenBlock();
        bumpInto(x, y - 1);
        byte blockData = level.getBlockData(x, y);
        if (blockData < 0)
            level.setBlockData(x, y, (byte) (blockData + 1));

        if (blockData == 0)
        {
            level.setBlock(x, y, (byte) 4);
            level.setBlockData(x, y, (byte) 4);
        }

        if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
        {
            if (randomGen.nextInt(5) == 0 && level.difficulty > 4)
            {
                addSprite(new GreenMushroom(this, x * cellSize + 8, y * cellSize + 8));
                ++level.counters.greenMushrooms;
            } else
            {
                if (!this.plumber.large)
                {
                    addSprite(new Mushroom(this, x * cellSize + 8, y * cellSize + 8));
                    ++level.counters.mushrooms;
                } else
                {
                    addSprite(new FireFlower(this, x * cellSize + 8, y * cellSize + 8));
                    ++level.counters.flowers;
                }
            }
        } else
        {
            this.plumber.gainCoin();
            addSprite(new CoinAnim(x, y));
        }
    }

    if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
    {
        bumpInto(x, y - 1);
        if (canBreakBricks)
        {
            level.setBlock(x, y, (byte) 0);
            for (int xx = 0; xx < 2; xx++)
                for (int yy = 0; yy < 2; yy++)
                    addSprite(new Particle(x * cellSize + xx * 8 + 4, y * cellSize + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));
        } else
        {
            level.setBlockData(x, y, (byte) 4);
        }
    }
}

public void bumpInto(int x, int y)
{
    byte block = level.getBlock(x, y);
    if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
    {
        this.plumber.gainCoin();
        level.setBlock(x, y, (byte) 0);
        addSprite(new CoinAnim(x, y + 1));
    }

    for (Sprite sprite : sprites)
    {
        sprite.bumpCheck(x, y);
    }
}

public int getTimeSpent() { return startTime / GlobalOptions.mariosecondMultiplier; }

public int getTimeLeft() { return timeLeft / GlobalOptions.mariosecondMultiplier; }

public int getKillsTotal()
{
    return killedCreaturesTotal;
}

public int getKillsByFire()
{
    return killedCreaturesByFireBall;
}

public int getKillsByStomp()
{
    return killedCreaturesByStomp;
}

public int getKillsByShell()
{
    return killedCreaturesByShell;
}

public int[] getMarioState()
{
    marioState[0] = this.getMarioStatus();
    marioState[1] = this.getMarioMode();
    marioState[2] = this.isMarioOnGround() ? 1 : 0;
    marioState[3] = this.isMarioAbleToJump() ? 1 : 0;
    marioState[4] = this.isMarioAbleToShoot() ? 1 : 0;
    marioState[5] = this.isMarioCarrying() ? 1 : 0;
    marioState[6] = this.getKillsTotal();
    marioState[7] = this.getKillsByFire();
    marioState[8] = this.getKillsByStomp();
    marioState[9] = this.getKillsByShell();
    marioState[10] = this.getTimeLeft();
    return marioState;
}

public void performAction(boolean[] action)
{
    // might look ugly , but arrayCopy is not necessary here:
    this.plumber.keys = action;
}

public boolean isLevelFinished()
{
    return (plumber.getStatus() != Plumber.STATUS_RUNNING);
}

public boolean isMarioAbleToShoot()
{
    return plumber.isAbleToShoot();
}

public int getMarioStatus()
{
    return plumber.getStatus();
}

/**
 * first and second elements of the array are x and y Plumber coordinates correspondingly
 *
 * @return an array of size 2*(number of creatures on screen) including platform
 */
public float[] getCreaturesFloatPos()
{
    float[] enemies = this.getEnemiesFloatPos();
    float ret[] = new float[enemies.length + 2];
    System.arraycopy(this.getMarioFloatPos(), 0, ret, 0, 2);
    System.arraycopy(enemies, 0, ret, 2, enemies.length);
    return ret;
}

public boolean isMarioOnGround()
{ return plumber.isOnGround(); }

public boolean isMarioAbleToJump()
{ return plumber.mayJump(); }

public void resetDefault()
{
    // TODO: set values to defaults
    reset(PlatformerAIOptions.getDefaultOptions());
}

public void reset(PlatformerAIOptions platformerAIOptions) {
//        System.out.println("\nLevelScene RESET!");
//        this.gameViewer = setUpOptions[0] == 1;
//        System.out.println("this.platform.isMarioInvulnerable = " + this.platform.isMarioInvulnerable);
//    this.levelDifficulty = platformerAIOptions.getLevelDifficulty();
//        System.out.println("this.levelDifficulty = " + this.levelDifficulty);
//    this.levelLength = platformerAIOptions.getLevelLength();
//        System.out.println("this.levelLength = " + this.levelLength);
//    this.levelSeed = platformerAIOptions.getLevelRandSeed();
//        System.out.println("levelSeed = " + levelSeed);
//    this.levelType = platformerAIOptions.getLevelType();
//        System.out.println("levelType = " + levelType);


    GlobalOptions.FPS = platformerAIOptions.getFPS();
//        System.out.println("GlobalOptions.FPS = " + GlobalOptions.FPS);
    GlobalOptions.isPowerRestoration = platformerAIOptions.isPowerRestoration();
//        System.out.println("GlobalOptions.isPowerRestoration = " + GlobalOptions.isPowerRestoration);
//    GlobalOptions.isPauseWorld = platformerAIOptions.isPauseWorld();
    GlobalOptions.areFrozenCreatures = platformerAIOptions.isFrozenCreatures();
//        System.out.println("GlobalOptions = " + GlobalOptions.isPauseWorld);
//        GlobalOptions.isTimer = platformerAIOptions.isTimer();
//        System.out.println("GlobalOptions.isTimer = " + GlobalOptions.isTimer);
//        isToolsConfigurator = setUpOptions[11] == 1;
    this.setTimeLimit(platformerAIOptions.getTimeLimit());
//        System.out.println("this.getTimeLimit() = " + this.getTimeLimit());
//        this.isViewAlwaysOnTop() ? 1 : 0, setUpOptions[13]
    GlobalOptions.isVisualization = platformerAIOptions.isVisualization();
//        System.out.println("visualization = " + visualization);

    killedCreaturesTotal = 0;
    killedCreaturesByFireBall = 0;
    killedCreaturesByStomp = 0;
    killedCreaturesByShell = 0;

    marioInitialPos = platformerAIOptions.getMarioInitialPos();
    greenMushroomMode = platformerAIOptions.getGreenMushroomMode();

    if (replayer != null) {
        try {
//            replayer.openNextReplayFile();
            replayer.openFile("level.lvl");
            Object rawLevel = replayer.readString();
            String descr = (String) rawLevel;
            level = Level.fromString(descr);
            level.counters.resetUncountableCounters();
//            replayer.closeFile();
//            replayer.closeRecorder();
        } catch (IOException e) {
            System.err.println("[Plumber AI Exception] : level reading failed");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } else {

        String name = platformerAIOptions.getLevelToLoad();
        String asciiname = platformerAIOptions.getASCIILevelToLoad();
        if (!asciiname.equals("none")) {
            String fileLocation = System.getProperty("user.dir")+File.separator+"rsrc"+File.separator;

            try {
                level = Level.loadASCII(new BufferedReader(new FileReader(fileLocation+asciiname)),platformerAIOptions);
            } catch (IOException e) {
                level = LevelGenerator.createLevel(platformerAIOptions);
            } catch (ClassNotFoundException e) {
                level = LevelGenerator.createLevel(platformerAIOptions);
            }
        } else if (name.equals("none")) {
            level = LevelGenerator.createLevel(platformerAIOptions);
        } else {
            try {
                String fileLocation = System.getProperty("user.dir")+File.separator+"rsrc"+File.separator;

                level = Level.load(new BufferedReader(new FileReader(fileLocation+name)));

                //level = Level.load(new BufferedReader(new FileReader(name)));
            } catch (IOException e) {
                level = LevelGenerator.createLevel(platformerAIOptions);
            } catch (ClassNotFoundException e) {
                level = LevelGenerator.createLevel(platformerAIOptions);
            }
        }

    }
    String fileName = platformerAIOptions.getLevelFileName();
    if (!fileName.equals(""))
    {
        try
        {
            level.save(new OutputStreamWriter(new FileOutputStream(fileName)));
        } catch (IOException e)
        {
            System.err.println("[Plumber AI Exception] : Cannot write to file " + fileName);
            e.printStackTrace();
        }
    }
    this.levelSeed = level.randomSeed;
    this.levelLength = level.length;
    this.levelHeight = level.height;
    this.levelType = level.type;
    this.levelDifficulty = level.difficulty;

    Sprite.spriteContext = this;
    sprites.clear();
    this.width = GlobalOptions.VISUAL_COMPONENT_WIDTH;
    this.height = GlobalOptions.VISUAL_COMPONENT_HEIGHT;

    Sprite.setCreaturesGravity(platformerAIOptions.getCreaturesGravity());
    Sprite.setCreaturesWind(platformerAIOptions.getWind());
    Sprite.setCreaturesIce(platformerAIOptions.getIce());

    bonusPoints = -1;

    this.plumber = new Plumber(this);
    //System.out.println("platform = " + platform);
    memo = "";
    this.plumber.resetStatic(platformerAIOptions);
    sprites.add(this.plumber);
    startTime = 1;
    timeLeft = timeLimit * GlobalOptions.mariosecondMultiplier;

    tickCount = 0;
}

public float[] getMarioFloatPos()
{
    marioFloatPos[0] = this.plumber.x;
    marioFloatPos[1] = this.plumber.y;
    return marioFloatPos;
}

public int getMarioMode()
{ return plumber.getMode(); }

public boolean isMarioCarrying()
{ return plumber.carried != null; }

public int getLevelDifficulty()
{ return levelDifficulty; }

public long getLevelSeed()
{ return levelSeed; }

public int getLevelLength()
{ return levelLength; }

public int getLevelHeight()
{ return levelHeight; }

public int getLevelType()
{ return levelType; }


public void addMemoMessage(final String memoMessage)
{
    memo += memoMessage;
}

public Point getMarioInitialPos() {return marioInitialPos;}

public void setReplayer(Replayer replayer)
{
    this.replayer = replayer;
}

public int getGreenMushroomMode()
{
    return greenMushroomMode;
}

public int getBonusPoints()
{
    return bonusPoints;
}

public void setBonusPoints(final int bonusPoints)
{
    this.bonusPoints = bonusPoints;
}

public void appendBonusPoints(final int superPunti)
{
    bonusPoints += superPunti;
}


}

//    public void update(boolean[] action)
//    {
//        System.arraycopy(action, 0, platform.keys, 0, 6);
//    }
