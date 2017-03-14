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

package org.platformer.scenarios.champ;

import org.platformer.agents.Agent;
import org.platformer.benchmark.tasks.GamePlayTask;
import org.platformer.benchmark.tasks.Task;
import org.platformer.tools.PlatformerAIOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey@idsia.ch
 * Date: Mar 17, 2010
 * Time: 8:33:43 AM
 * Package: org.platform.scenarios
 */

/**
 * Class used for agent evaluation in GamePlay track
 * http://www.PlatformerAI.org/gameplay-track
 */
public final class GamePlayTrack
{
final static int numberOfLevels = 512;
private static int killsSum = 0;
private static float marioStatusSum = 0;
private static int timeLeftSum = 0;
private static int marioModeSum = 0;
private static boolean detailedStats = false;
private static PlatformerAIOptions platformerAIOptions = new PlatformerAIOptions();

public static void evaluateAgent(final Agent agent)
{
    final Task task = new GamePlayTask(platformerAIOptions);
    //platformerAIOptions.setAgent(agent);
    task.setOptionsAndReset(platformerAIOptions);
    System.out.println("Evaluating agent " + agent.getName() + " with seed " + platformerAIOptions.getLevelRandSeed());
    task.doEpisodes(numberOfLevels, false, 1);
    task.printStatistics();
}

public static void evaluateSubmissionZip(final String zipFileName)
{

}


public static void main(String[] args)
{
    platformerAIOptions.setArgs(args);
    evaluateAgent(platformerAIOptions.getAgent());
    System.exit(0);
}
}


