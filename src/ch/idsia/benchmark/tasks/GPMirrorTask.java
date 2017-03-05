
package ch.idsia.benchmark.tasks;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.ReplayAgent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.Replayer;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.tools.MarioAIOptions;
import ch.idsia.tools.ReplayerOptions;

import java.io.IOException;
import java.util.List;

/**
 * User: Swen Gaudl
 */
public class GPMirrorTask implements Task
{
protected Environment environment;
private ReplayAgent agent;
private List<Agent> testAgents;
private String name = getClass().getSimpleName();
private Replayer replayer;

public GPMirrorTask(List<Agent> ag){
	environment = new MarioEnvironment();
    testAgents = ag;
}

public void playOneFile(final MarioAIOptions options)
{
    ReplayerOptions.Interval interval = replayer.getNextIntervalInMarioseconds();
    if (interval == null)
    {
        interval = new ReplayerOptions.Interval(0, replayer.actionsFileSize());
    }

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
        	for (Agent ag : testAgents){
        		ag.integrateObservation(environment);
                ag.giveIntermediateReward(environment.getIntermediateReward());
                ag.getAction();
        	}
        	
        	boolean[] action = agent.getAction();
        	if (action != null) {
                environment.performAction(action);
            } else {
                environment.performAction(new boolean[Environment.numberOfKeys]);
        	}
        }

        if (interval == null)
            break;
    }
}

public int evaluate(final Agent controller)
{
    return 0;
}

public void setOptionsAndReset(final MarioAIOptions options)
{}

public void setOptionsAndReset(final String options)
{
    //To change body of implemented methods use File | Settings | File Templates.
}

public void doEpisodes(final int amount, final boolean verbose, final int repetitionsOfSingleEpisode)
{}

public boolean startReplay(int fps,boolean visu)
{
    try
    {
        agent = new ReplayAgent("Replay agent");
        MarioAIOptions options = new MarioAIOptions();
        while (replayer.openNextReplayFile())
        {
            replayer.openFile("options");
            String strOptions = (String) replayer.readObject();
            options.setArgs(strOptions);
            //TODO: reset; resetAndSetArgs;
            options.setVisualization(visu);
            options.setRecordFile("off");
            agent.setName(options.getAgent().getName());
            options.setAgent(agent);
            options.setFPS(fps);
            agent.reset();
            agent.setReplayer(replayer);

            environment.setReplayer(replayer);
            environment.reset(options);
            //GlobalOptions.isVisualization = false;

            replayer.openFile("actions.act");

            playOneFile(options);
            
            //GlobalOptions.isVisualization = true;
//            replayer.closeFile();
            replayer.closeReplayFile();
            
        }
        environment.setReplayer(null);
    } catch (IOException e)
    {
    	e.printStackTrace();
    	return false;
    } catch (Exception e)
    {
    	e.printStackTrace();
    	return false;
    }
    return true;
}

public boolean isFinished()
{
    return false;
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
