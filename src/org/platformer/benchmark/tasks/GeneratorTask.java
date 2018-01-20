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

package org.platformer.benchmark.tasks;

import org.platformer.agents.Agent;
import org.platformer.agents.controllers.ReplayAgent;
import org.platformer.benchmark.platform.engine.GlobalOptions;
import org.platformer.benchmark.platform.engine.Replayer;
import org.platformer.benchmark.platform.environments.Environment;
import org.platformer.benchmark.platform.environments.PlatformEnvironment;
import org.platformer.tools.PlatformerAIOptions;
import org.platformer.tools.ReplayerOptions;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Oct 9, 2010
 * Time: 7:17:49 PM
 * Package: org.platform.benchmark.tasks
 */
public class GeneratorTask implements Task
{
protected Environment environment;
private ReplayAgent agent;
private String name = getClass().getSimpleName();
private Replayer replayer;
private boolean finished = true;
private ArrayList<Integer [][]> visionFieldperFrame = new ArrayList<>();
private ArrayList<Integer []> actionsPerFrame = new ArrayList<>();

public GeneratorTask()
{
    environment = new PlatformEnvironment();

}

public void playOneFile(final PlatformerAIOptions options)
{
    finished = false;
    ReplayerOptions.Interval interval = replayer.getNextIntervalInMarioseconds();
    if (interval == null)
    {
        interval = new ReplayerOptions.Interval(0, replayer.actionsFileSize());
    }
    ArrayList<Integer [][]> visionFieldperFrame = new ArrayList<>();
    ArrayList<Integer []> actionsPerFrame = new ArrayList<>();
    int frameCounter = 0;
    while (!environment.isLevelFinished())
    {
        if (environment.getTimeSpent() == interval.from) //TODO: Comment this piece
            GlobalOptions.isVisualization = true;
        else if (environment.getTimeSpent() == interval.to)
        {
            GlobalOptions.isVisualization = false;
            interval = replayer.getNextIntervalInMarioseconds();
        }
        environment.tick();


        if (!GlobalOptions.isGameplayStopped)
        {
            boolean[] action = agent.getAction();
            //record and memorize environment and actions to be taken
            Integer [] actions = new Integer[action.length];
            for (int i=0;i<action.length;i++)
                actions[i] = action[i] ? 1 : 0;
            actionsPerFrame.add(actions);
            byte[][] rawVision = environment.getMergedObservationZZ(1,0);
            Integer [][] vision = new Integer[rawVision.length][rawVision[0].length];
            for (int y=0;y<rawVision.length;y++)
                for (int x=0;x<rawVision[y].length;x++){
                    vision[y][x]=new Integer(rawVision[y][x]);
                }
            visionFieldperFrame.add(vision);

            environment.performAction(action);
            frameCounter++;
        }

        if (interval == null)
            break;
    }
    this.visionFieldperFrame = visionFieldperFrame;
    this.actionsPerFrame = actionsPerFrame;

}

public Integer[][][] getInputData(){
    return this.visionFieldperFrame.toArray(new Integer[0][0][0]);
}

public Integer[][] getOutputData(){
    return this.actionsPerFrame.toArray(new Integer[0][0]);
}

public int evaluate(final Agent controller)
{
    return 0;
}

public void setOptionsAndReset(final PlatformerAIOptions options)
{}

public void setOptionsAndReset(final String options)
{
    //To change body of implemented methods use File | Settings | File Templates.
}

public void doEpisodes(final int amount, final boolean verbose, final int repetitionsOfSingleEpisode)
{}

public void startReplay(int fps)
{
    try
    {
        agent = new ReplayAgent("Replay agent");
        PlatformerAIOptions options = new PlatformerAIOptions();
        while (replayer.openNextReplayFile())
        {
            replayer.openFile("options");
            String strOptions = (String) replayer.readObject();
            options.setArgs(strOptions);
            //TODO: reset; resetAndSetArgs;
            options.setVisualization(true);
            options.setRecordFile("off");
            agent.setName(options.getAgent().getName());
            options.setAgent(agent);
            options.setFPS(fps);
            environment.reset(options);
            agent.reset();
            agent.setReplayer(replayer);

            environment.setReplayer(replayer);
            environment.reset(options);
            GlobalOptions.isVisualization = false;

            replayer.openFile("actions.act");

            playOneFile(options);

            GlobalOptions.isVisualization = true;
//            replayer.closeFile();
            replayer.closeReplayFile();
            finished = true;
        }
    } catch (IOException e)
    {
        e.printStackTrace();
    } catch (Exception e)
    {
        e.printStackTrace();
    }
}

public boolean isFinished()
{
    return finished;
}

public void reset(String replayOptions)
{
    replayer = new Replayer(replayOptions);
    GlobalOptions.isReplaying = true;
}

public void reset()
{}

public String getName()
{
    return name;
}

public void printStatistics()
{
    //To change body of implemented methods use File | Settings | File Templates.
}

public Environment getEnvironment()
{
    return environment;
}
}
