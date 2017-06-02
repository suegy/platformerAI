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

package org.platformer.benchmark.platform.environments;

import org.platformer.agents.Agent;
import org.platformer.benchmark.platform.engine.*;
import org.platformer.benchmark.platform.engine.level.Level;
import org.platformer.benchmark.platform.engine.sprites.Plumber;
import org.platformer.benchmark.platform.engine.sprites.Sprite;
import org.platformer.benchmark.tasks.SystemOfValues;
import org.platformer.tools.EvaluationInfo;
import org.platformer.tools.PlatformerAIOptions;
import org.platformer.tools.punj.PunctualJudge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey@idsia.ch
 * Date: Mar 3, 2010 Time: 10:08:13 PM
 * Package: org.platform.benchmark.platform.environments
 */

public class PlatformEnvironment implements Environment
{
private int[] marioEgoPos = new int[2];
private int receptiveFieldHeight = -1; // to be setup via PlatformerAIOptions
private int receptiveFieldWidth = -1; // to be setup via PlatformerAIOptions
private int prevRFH = -1;
private int prevRFW = -1;

private byte[][] levelSceneZ;     // memory is allocated in reset
private byte[][] enemiesZ;      // memory is allocated in reset
private byte[][] mergedZZ;      // memory is allocated in reset

public List<Sprite> sprites;

private int[] serializedLevelScene;   // memory is allocated in reset
private int[] serializedEnemies;      // memory is allocated in reset
private int[] serializedMergedObservation; // memory is allocated in reset

private final LevelScene levelScene;
//    private int frame = 0;
private MarioVisualComponent marioVisualComponent;
private Agent agent;

private EvaluationInfo evaluationInfo;

private static String marioTraceFile;

private Recorder recorder;

public static SystemOfValues IntermediateRewardsSystemOfValues = new SystemOfValues();

DecimalFormat df = new DecimalFormat("######.#");

public PlatformEnvironment()
{
//        System.out.println("System.getProperty(\"java.awt.headless\") = " + System.getProperty("java.awt.headless"));
//        System.out.println("System.getProperty(\"verbose\") = " + System.getProperty("-verbose"));
//        System.out.println("Java: JA ZDES'!!");
//        System.out.flush();
    System.out.println(GlobalOptions.getBenchmarkName());
    evaluationInfo = new EvaluationInfo();
    levelScene = new LevelScene();
    //levelScene.platform = new Plumber(levelScene);
}

public void resetDefault()
{
    levelScene.resetDefault();
}

public void reset(String args)
{
    PlatformerAIOptions platformerAIOptions = PlatformerAIOptions.getOptionsByString(args);
    this.reset(platformerAIOptions);
//        PlatformerAIOptions opts = new PlatformerAIOptions(setUpOptions);
//        int[] intOpts = opts.toIntArray();
//        this.reset(intOpts);
}

public void reset(PlatformerAIOptions setUpOptions)
{
    /*System.out.println("\nsetUpOptions = " + setUpOptions);
    for (int i = 0; i < setUpOptions.length; ++i)
    {
        System.out.print(" op[" + i +"] = " + setUpOptions[i]);
    }
    System.out.println("");
    System.out.flush();*/
//    if (!setUpOptions.getReplayOptions().equals(""))

    this.setAgent(setUpOptions.getAgent());

    receptiveFieldWidth = setUpOptions.getReceptiveFieldWidth();
    receptiveFieldHeight = setUpOptions.getReceptiveFieldHeight();

    if (receptiveFieldHeight != this.prevRFH || receptiveFieldWidth != this.prevRFW)
    {
        serializedLevelScene = new int[receptiveFieldHeight * receptiveFieldWidth];
        serializedEnemies = new int[receptiveFieldHeight * receptiveFieldWidth];
        serializedMergedObservation = new int[receptiveFieldHeight * receptiveFieldWidth];

        levelSceneZ = new byte[receptiveFieldHeight][receptiveFieldWidth];
        enemiesZ = new byte[receptiveFieldHeight][receptiveFieldWidth];
        mergedZZ = new byte[receptiveFieldHeight][receptiveFieldWidth];
        this.prevRFH = this.receptiveFieldHeight;
        this.prevRFW = this.receptiveFieldWidth;
    }

    marioEgoPos[0] = setUpOptions.getMarioEgoPosRow();
    marioEgoPos[1] = setUpOptions.getMarioEgoPosCol();

    if (marioEgoPos[0] == 9 && getReceptiveFieldWidth() != 19)
        marioEgoPos[0] = getReceptiveFieldWidth() / 2;
    if (marioEgoPos[1] == 9 && getReceptiveFieldHeight() != 19)
        marioEgoPos[1] = getReceptiveFieldHeight() / 2;

    marioTraceFile = setUpOptions.getTraceFileName();

    if (setUpOptions.isVisualization())
    {
        if (marioVisualComponent == null) {
            marioVisualComponent = new MarioVisualComponent(setUpOptions, this);
            marioVisualComponent.CreateMarioComponentFrame(marioVisualComponent);
        }
        levelScene.reset(setUpOptions);
        marioVisualComponent.reset();
        marioVisualComponent.postInitGraphicsAndLevel();
        marioVisualComponent.setAgent(agent);
        marioVisualComponent.setLocation(setUpOptions.getViewLocation());
        marioVisualComponent.setAlwaysOnTop(setUpOptions.isViewAlwaysOnTop());

        if (setUpOptions.isScale2X())
            GlobalOptions.changeScale2x();
    } else
        levelScene.reset(setUpOptions);

    sprites = levelScene.sprites;

    //create recorder
    String recordingFileName = setUpOptions.getRecordingFileName();

    if (!recordingFileName.equals("off"))
    {
        if (recordingFileName.equals("on"))
            recordingFileName = GlobalOptions.getTimeStamp() + ".zip";

        try
        {
            if (recordingFileName.equals("lazy"))
                recorder = new Recorder();
            else
                recorder = new Recorder(recordingFileName);

            recorder.createFile("level.lvl");
            recorder.writeObject(levelScene.level);
            recorder.closeFile();

            recorder.createFile("options");
            recorder.writeObject(setUpOptions.asString());
            recorder.closeFile();

            recorder.createFile("actions.act");
        } catch (FileNotFoundException e)
        {
            System.err.println("[Plumber AI EXCEPTION] : Some of the recording components were not created. Recording failed");
        } catch (IOException e)
        {
            System.err.println("[Plumber AI EXCEPTION] : Some of the recording components were not created. Recording failed");
            e.printStackTrace();
        }
    }
    evaluationInfo.reset();
    PunctualJudge.resetCounter();
}

public void tick()
{
    levelScene.tick();
    if (GlobalOptions.isVisualization && marioVisualComponent != null)
        marioVisualComponent.tick();
}

public float[] getMarioFloatPos()
{
    return levelScene.getMarioFloatPos();
}

public int getMarioMode()
{
    return levelScene.getMarioMode();
}

public byte[][] getLevelSceneObservationZ(int ZLevel)
{
    int mCol = marioEgoPos[1];
    int mRow = marioEgoPos[0];
    for (int y = levelScene.plumber.mapY - mRow, row = 0; y <= levelScene.plumber.mapY + (receptiveFieldHeight - mRow - 1); y++, row++)
    {
        for (int x = levelScene.plumber.mapX - mCol, col = 0; x <= levelScene.plumber.mapX + (receptiveFieldWidth - mCol - 1); x++, col++)
        {
            if (x >= 0 && x < levelScene.level.length && y >= 0 && y < levelScene.level.height)
            {
                mergedZZ[row][col] = levelSceneZ[row][col] = GeneralizerLevelScene.ZLevelGeneralization(levelScene.level.map[x][y], ZLevel);
            } else
            {
                mergedZZ[row][col] = levelSceneZ[row][col] = 0;
            }

        }
    }
    return levelSceneZ;
}

public byte[][] getEnemiesObservationZ(int ZLevel)
{
    int marioEgoCol = marioEgoPos[1];
    int marioEgoRow = marioEgoPos[0];
    for (int w = 0; w < enemiesZ.length; w++)
        for (int h = 0; h < enemiesZ[0].length; h++)
            enemiesZ[w][h] = 0;
    for (Sprite sprite : sprites)
    {
        if (sprite.isDead() || sprite.kind == levelScene.plumber.kind)
            continue;
        if (sprite.mapX >= 0 &&
                sprite.mapX >= levelScene.plumber.mapX - marioEgoCol &&
                sprite.mapX <= levelScene.plumber.mapX + (receptiveFieldWidth - marioEgoCol - 1) &&
                sprite.mapY >= 0 &&
                sprite.mapY >= levelScene.plumber.mapY - marioEgoRow &&
                sprite.mapY <= levelScene.plumber.mapY + (receptiveFieldHeight - marioEgoRow - 1) &&
                sprite.kind != Sprite.KIND_PRINCESS)
        {
            int row = sprite.mapY - levelScene.plumber.mapY + marioEgoRow;
            int col = sprite.mapX - levelScene.plumber.mapX + marioEgoCol;
            // TODO:!H! take care about side effects of line 243 and be sure not to contaminate levelSceneObservation
            mergedZZ[row][col] = enemiesZ[row][col] = GeneralizerEnemies.ZLevelGeneralization(sprite.kind, ZLevel);
        }
    }
    return enemiesZ;
}
// TODO: !H! substitute the content of getMergedObservationZZ by getLevelSceneObservationZ,
// TODO: !H! getEnemiesObservationZ, called one after another!

public byte[][] getMergedObservationZZ(int ZLevelScene, int ZLevelEnemies)
{
//    int MarioXInMap = (int) platform.x / cellSize;
//    int MarioYInMap = (int) platform.y / cellSize;

//    if (MarioXInMap != (int) platform.x / cellSize ||MarioYInMap != (int) platform.y / cellSize )
//        throw new Error("WRONG platform x or y pos");
    int mCol = marioEgoPos[1];
    int mRow = marioEgoPos[0];
    for (int y = levelScene.plumber.mapY - mRow/*receptiveFieldHeight / 2*/, row = 0; y <= levelScene.plumber.mapY + (receptiveFieldHeight - mRow - 1)/*receptiveFieldHeight / 2*/; y++, row++)
    {
        for (int x = levelScene.plumber.mapX - mCol/*receptiveFieldWidth / 2*/, col = 0; x <= levelScene.plumber.mapX + (receptiveFieldWidth - mCol - 1)/*receptiveFieldWidth / 2*/; x++, col++)
        {
            if (x >= 0 && x < levelScene.level.xExit && y >= 0 && y < levelScene.level.height)
            {
                mergedZZ[row][col] = GeneralizerLevelScene.ZLevelGeneralization(levelScene.level.map[x][y], ZLevelScene);
            } else
                mergedZZ[row][col] = 0;
//                if (x == MarioXInMap && y == MarioYInMap)
//                    mergedZZ[row][col] = platform.kind;
        }
    }
//        for (int w = 0; w < mergedZZ.length; w++)
//            for (int h = 0; h < mergedZZ[0].length; h++)
//                mergedZZ[w][h] = -1;
    for (Sprite sprite : sprites)
    {
        if (sprite.isDead() || sprite.kind == levelScene.plumber.kind)
            continue;
        if (sprite.mapX >= 0 &&
                sprite.mapX >= levelScene.plumber.mapX - mCol &&
                sprite.mapX <= levelScene.plumber.mapX + (receptiveFieldWidth - mCol - 1) &&
                sprite.mapY >= 0 &&
                sprite.mapY >= levelScene.plumber.mapY - mRow &&
                sprite.mapY <= levelScene.plumber.mapY + (receptiveFieldHeight - mRow - 1) &&
                sprite.kind != Sprite.KIND_PRINCESS)
        {
            int row = sprite.mapY - levelScene.plumber.mapY + mRow;
            int col = sprite.mapX - levelScene.plumber.mapX + mCol;
            byte tmp = GeneralizerEnemies.ZLevelGeneralization(sprite.kind, ZLevelEnemies);
            if (tmp != Sprite.KIND_NONE)
                mergedZZ[row][col] = tmp;
        }
    }

    return mergedZZ;
}

public List<String> getObservationStrings(boolean Enemies, boolean LevelMap,
                                          boolean mergedObservationFlag,
                                          int ZLevelScene, int ZLevelEnemies)
{
    List<String> ret = new ArrayList<String>();
    if (levelScene.level != null && levelScene.plumber != null)
    {
        ret.add("Total levelScene length = " + levelScene.level.length);
        ret.add("Total levelScene height = " + levelScene.level.height);
        ret.add("Physical Plumber Position (x,y): (" + df.format(levelScene.plumber.x) + "," + df.format(levelScene.plumber.y) + ")");
        ret.add("Plumber Observation (Receptive Field)   Width: " + receptiveFieldWidth + " Height: " + receptiveFieldHeight);
        ret.add("X Exit Position: " + levelScene.level.xExit);
        int MarioXInMap = (int) levelScene.plumber.x / levelScene.cellSize; //TODO: !!H! doublcheck and replace with levelScene.platform.mapX
        int MarioYInMap = (int) levelScene.plumber.y / levelScene.cellSize;  //TODO: !!H! doublcheck and replace with levelScene.platform.mapY
        ret.add("Calibrated Plumber Position (x,y): (" + MarioXInMap + "," + MarioYInMap + ")\n");

        byte[][] levelScene = getLevelSceneObservationZ(ZLevelScene);
        if (LevelMap)
        {
            ret.add("~ZLevel: Z" + ZLevelScene + " map:\n");
            for (int x = 0; x < levelScene.length; ++x)
            {
                String tmpData = "";
                for (int y = 0; y < levelScene[0].length; ++y)
                    tmpData += levelSceneCellToString(levelScene[x][y]);
                ret.add(tmpData);
            }
        }

        byte[][] enemiesObservation = null;
        if (Enemies || mergedObservationFlag)
            enemiesObservation = getEnemiesObservationZ(ZLevelEnemies);

        if (Enemies)
        {
            ret.add("~ZLevel: Z" + ZLevelScene + " Enemies Observation:\n");
            for (int x = 0; x < enemiesObservation.length; x++)
            {
                String tmpData = "";
                for (int y = 0; y < enemiesObservation[0].length; y++)
                {
//                        if (x >=0 && x <= level.xExit)
                    tmpData += enemyToStr(enemiesObservation[x][y]);
                }
                ret.add(tmpData);
            }
        }

        if (mergedObservationFlag)
        {
            byte[][] mergedObs = getMergedObservationZZ(ZLevelScene, ZLevelEnemies);
            ret.add("~ZLevelScene: Z" + ZLevelScene + " ZLevelEnemies: Z" + ZLevelEnemies + " ; Merged observation /* Plumber ~> #M.# */");
            for (int x = 0; x < levelScene.length; ++x)
            {
                String tmpData = "";
                for (int y = 0; y < levelScene[0].length; ++y)
                    tmpData += levelSceneCellToString(mergedObs[x][y]);
                ret.add(tmpData);
            }
        }
    } else
        ret.add("~[PlatformerAI ERROR] level : " + levelScene.level + " platform : " + levelScene.plumber);
    return ret;
}


private String levelSceneCellToString(int el)
{
    String s = "";
    if (el == 0 || el == 1)
        s = "##";
    s += (el == levelScene.plumber.kind) ? "#M.#" : el;
    while (s.length() < 4)
        s += "#";

    return s + " ";
}

private String enemyToStr(int el)
{
    String s = "";
    if (el == 0)
        s = "";
    s += (el == levelScene.plumber.kind) ? "-m" : el;
    while (s.length() < 2)
        s += "#";
    return s + " ";
}


public float[] getEnemiesFloatPos()
{
    return levelScene.getEnemiesFloatPos();
}

public boolean isMarioOnGround()
{
    return levelScene.isMarioOnGround();
}

public boolean isMarioAbleToJump()
{
    return levelScene.isMarioAbleToJump();
}

public boolean isMarioCarrying()
{
    return levelScene.isMarioCarrying();
}

public boolean isMarioAbleToShoot()
{
    return levelScene.isMarioAbleToShoot();
}

public int getReceptiveFieldWidth()
{
    return receptiveFieldWidth;
}

public int getReceptiveFieldHeight()
{
    return receptiveFieldHeight;
}

public int getKillsTotal()
{
    return levelScene.getKillsTotal();
}

public int getKillsByFire()
{
    return levelScene.getKillsByFire();
}

public int getKillsByStomp()
{
    return levelScene.getKillsByStomp();
}

public int getKillsByShell()
{
    return levelScene.getKillsByShell();
}

public int getMarioStatus()
{
    return levelScene.getMarioStatus();
}

public int[] getObservationDetails()
{
    return new int[]{receptiveFieldWidth, receptiveFieldHeight, marioEgoPos[0], marioEgoPos[1]};
}

public List<Sprite> getSprites()
{
    return sprites;
}

public int[] getSerializedFullObservationZZ(int ZLevelScene, int ZLevelEnemies)
{
    int[] obs = new int[receptiveFieldHeight * receptiveFieldWidth * 2 + 11]; // 11 is a size of the MarioState array

    int receptiveFieldSize = receptiveFieldWidth * receptiveFieldHeight;

    System.arraycopy(getSerializedLevelSceneObservationZ(ZLevelScene), 0, obs, 0, receptiveFieldSize);
    System.arraycopy(getSerializedEnemiesObservationZ(ZLevelScene), 0, obs, receptiveFieldSize, receptiveFieldSize);
    System.arraycopy(getMarioState(), 0, obs, receptiveFieldSize * 2, 11);

    return obs;
}

public int[] getSerializedLevelSceneObservationZ(int ZLevelScene)
{
    // serialization into arrays of primitive types to speed up the data transfer.
    byte[][] levelScene = this.getLevelSceneObservationZ(ZLevelScene);
    for (int i = 0; i < serializedLevelScene.length; ++i)
    {
        final int i1 = i / receptiveFieldWidth;
        final int i2 = i % receptiveFieldWidth;
        serializedLevelScene[i] = (int) levelScene[i1][i2];
    }
    return serializedLevelScene;
}

public int[] getSerializedEnemiesObservationZ(int ZLevelEnemies)
{
    // serialization into arrays of primitive types to speed up the data transfer.
    byte[][] enemies = this.getEnemiesObservationZ(ZLevelEnemies);
    for (int i = 0; i < serializedEnemies.length; ++i)
        serializedEnemies[i] = (int) enemies[i / receptiveFieldWidth][i % receptiveFieldWidth];
    return serializedEnemies;
}

public int[] getSerializedMergedObservationZZ(int ZLevelScene, int ZLevelEnemies)
{
    // serialization into arrays of primitive types to speed up the data transfer.
    byte[][] merged = this.getMergedObservationZZ(ZLevelScene, ZLevelEnemies);
    for (int i = 0; i < serializedMergedObservation.length; ++i)
        serializedMergedObservation[i] = (int) merged[i / receptiveFieldWidth][i % receptiveFieldWidth];
    return serializedMergedObservation;
}

public float[] getCreaturesFloatPos()
{
    return levelScene.getCreaturesFloatPos();
}

public int[] getMarioState()
{
    return levelScene.getMarioState();
}

public void performAction(boolean[] action)
{
    try
    {
        if (recorder != null && recorder.canRecord() && action != null)
        {
            recorder.writeAction(action);
            recorder.changeRecordingState(GlobalOptions.isRecording, getTimeSpent());
        }
    } catch (IOException e)
    {
        e.printStackTrace();
    }
    levelScene.performAction(action);
}

public boolean isLevelFinished()
{
    return levelScene.isLevelFinished();
}

public int[] getEvaluationInfoAsInts()
{
    return this.getEvaluationInfo().toIntArray();
}

public String getEvaluationInfoAsString()
{
    return this.getEvaluationInfo().toString();
}

public EvaluationInfo getEvaluationInfo()
{
    computeEvaluationInfo();
    return evaluationInfo;
}

public Plumber getMario()
{
    return levelScene.plumber;
}

public int getTick()
{
    return levelScene.tickCount;
}

public int getLevelDifficulty()
{
    return levelScene.getLevelDifficulty();
}

public long getLevelSeed()
{
    return levelScene.getLevelSeed();
}

public int getLevelType()
{
    return levelScene.getLevelType();
}

public int getKilledCreaturesTotal()
{
    return levelScene.getKillsTotal();
}

public int getLevelLength()
{
    return levelScene.getLevelLength();
}

public int getLevelHeight()
{
    return levelScene.getLevelHeight();
}

public int getKilledCreaturesByFireBall()
{
    return levelScene.getKillsByFire();
}

public int getKilledCreaturesByShell()
{
    return levelScene.getKillsByShell();
}

public int getKilledCreaturesByStomp()
{
    return levelScene.getKillsByStomp();
}

public int getTimeLeft()
{
    return levelScene.getTimeLeft();
}

public Level getLevel()
{
    return levelScene.level;
}

private void computeEvaluationInfo()
{
    if (recorder != null)
        closeRecorder();
//        evaluationInfo.agentType = agent.getClass().getSimpleName();
//        evaluationInfo.agentName = agent.getName();
    evaluationInfo.marioStatus = levelScene.getMarioStatus();
    evaluationInfo.flowersDevoured = levelScene.plumber.flowersDevoured;
    evaluationInfo.distancePassedPhys = (int) levelScene.plumber.x;
    evaluationInfo.distancePassedCells = levelScene.plumber.mapX;
//     evaluationInfo.totalLengthOfLevelCells = levelScene.level.getWidthCells();
//     evaluationInfo.totalLengthOfLevelPhys = levelScene.level.getWidthPhys();
    evaluationInfo.timeSpent = levelScene.getTimeSpent();
    evaluationInfo.timeLeft = levelScene.getTimeLeft();
    evaluationInfo.coinsGained = levelScene.plumber.coins;
    evaluationInfo.totalNumberOfCoins = levelScene.level.counters.coinsCount;
    evaluationInfo.totalNumberOfHiddenBlocks = levelScene.level.counters.hiddenBlocksCount;
    evaluationInfo.totalNumberOfFlowers = levelScene.level.counters.flowers;
    evaluationInfo.totalNumberOfMushrooms = levelScene.level.counters.mushrooms;
    evaluationInfo.totalNumberOfCreatures = levelScene.level.counters.creatures;
    evaluationInfo.marioMode = levelScene.getMarioMode();
    evaluationInfo.mushroomsDevoured = levelScene.plumber.mushroomsDevoured;
    evaluationInfo.killsTotal = levelScene.getKillsTotal();
    evaluationInfo.killsByStomp = levelScene.getKillsByStomp();
    evaluationInfo.killsByFire = levelScene.getKillsByFire();
    evaluationInfo.killsByShell = levelScene.getKillsByShell();
    evaluationInfo.hiddenBlocksFound = levelScene.plumber.hiddenBlocksFound;
    evaluationInfo.collisionsWithCreatures = levelScene.plumber.collisionsWithCreatures;
    evaluationInfo.Memo = levelScene.memo;
    evaluationInfo.levelLength = levelScene.level.length;
    evaluationInfo.marioTraceFileName = marioTraceFile;
    evaluationInfo.marioTrace = levelScene.level.marioTrace;
    evaluationInfo.greenMushroomsDevoured = levelScene.plumber.greenMushroomsDevoured;
    evaluationInfo.bytecodeInstructions = PunctualJudge.getCounter();
}

public void setAgent(Agent agent)
{
    this.agent = agent;
}

public int getIntermediateReward()
{
    // TODO: reward for coins, killed creatures, cleared dead-ends, bypassed gaps, hidden blocks found
    return levelScene.getBonusPoints();
}

public int[] getMarioEgoPos()
{
    return marioEgoPos;
}

public void closeRecorder()
{
    if (recorder != null)
    {
        try
        {
//            recorder.closeFile();
            recorder.closeRecorder(getTimeSpent());
            //recorder = null;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

public int getTimeSpent()
{
    return levelScene.getTimeSpent();
}

public byte[][] getScreenCapture()
{
    return null;
}

public void setReplayer(Replayer replayer)
{
    levelScene.setReplayer(replayer);
}

public void saveLastRun(String filename)
{
    if (recorder != null && recorder.canSave())
    {
        try
        {
            recorder.saveLastRun(filename);
        } catch (IOException ex)
        {
            System.err.println("[Plumber AI EXCEPTION] : Recording could not be saved.");
            ex.printStackTrace();
        }
    }
}

//public void setRecording(boolean isRecording)
//{
//    this.isRecording = isRecording;
//}
}

