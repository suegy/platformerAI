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
import org.platformer.agents.LearningAgent;
import org.platformer.benchmark.tasks.BasicTask;
import org.platformer.benchmark.tasks.LearningTask;
import org.platformer.tools.EvaluationInfo;
import org.platformer.tools.PlatformerAIOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey at idsia dot ch
 * Date: Mar 17, 2010 Time: 8:34:17 AM
 * Package: org.platform.scenarios
 */

/**
 * Class used for agent evaluation in Learning track
 * http://www.PlatformerAI.org/learning-track
 */

public final class LearningTrack
{
final static long numberOfTrials = 1000;
final static boolean scoring = false;
private static int killsSum = 0;
private static float marioStatusSum = 0;
private static int timeLeftSum = 0;
private static int marioModeSum = 0;
private static boolean detailedStats = false;

final static int populationSize = 100;

private static int evaluateSubmission(PlatformerAIOptions platformerAIOptions, LearningAgent learningAgent)
{
    LearningTask learningTask = new LearningTask(platformerAIOptions); // provides the level
    learningAgent.setEvaluationQuota(LearningTask.getEvaluationQuota());        // limits the number of evaluations per run for LearningAgent
    learningAgent.setLearningTask(learningTask);  // gives LearningAgent access to evaluator via method LearningTask.evaluate(Agent)
    learningAgent.init();
    learningAgent.learn(); // launches the training process. numberOfTrials happen here
    Agent agent = learningAgent.getBestAgent(); // this agent will be evaluated

    // perform the gameplay task on the same level
    platformerAIOptions.setVisualization(true);
    System.out.println("LearningTrack best agent = " + agent);
    platformerAIOptions.setAgent(agent);
    BasicTask basicTask = new BasicTask(platformerAIOptions);
    basicTask.setOptionsAndReset(platformerAIOptions);
    System.out.println("basicTask = " + basicTask);
    System.out.println("agent = " + agent);

    boolean verbose = true;

    if (!basicTask.runSingleEpisode(1))  // make evaluation on the same episode once
    {
        System.out.println("PlatformerAI: out of computational time per action! Agent disqualified!");
    }
    EvaluationInfo evaluationInfo = basicTask.getEvaluationInfo();
    System.out.println(evaluationInfo.toString());

    int f = evaluationInfo.computeWeightedFitness();
    if (verbose)
    {
        System.out.println("Intermediate SCORE = " + f + ";\n Details: " + evaluationInfo.toString());
    }

    return f;
}

private static int oldEval(PlatformerAIOptions platformerAIOptions, LearningAgent learningAgent)
{
    boolean verbose = false;
    float fitness = 0;
    int disqualifications = 0;

    platformerAIOptions.setVisualization(false);
    //final LearningTask learningTask = new LearningTask(platformerAIOptions);
    //learningTask.setAgent(learningAgent);
    LearningTask task = new LearningTask(platformerAIOptions);

    learningAgent.newEpisode();
    learningAgent.setLearningTask(task);
    learningAgent.setEvaluationQuota(numberOfTrials);
    learningAgent.init();

    for (int i = 0; i < numberOfTrials; ++i)
    {
        System.out.println("-------------------------------");
        System.out.println(i + " trial");
        //learningTask.reset(platformerAIOptions);
        task.setOptionsAndReset(platformerAIOptions);
        // inform your agent that new episode is coming, pick up next representative in population.
//            learningAgent.learn();
        task.runSingleEpisode(1);
        /*if (!task.runSingleEpisode())  // make evaluation on an episode once
        {
            System.out.println("PlatformerAI: out of computational time per action!");
            disqualifications++;
            continue;
        }*/

        EvaluationInfo evaluationInfo = task.getEnvironment().getEvaluationInfo();
        float f = evaluationInfo.computeWeightedFitness();
        if (verbose)
        {
            System.out.println("Intermediate SCORE = " + f + "; Details: " + evaluationInfo.toStringSingleLine());
        }
        // learn the reward
        //learningAgent.giveReward(f);
    }
    // do some post processing if you need to. In general: select the Agent with highest score.
    learningAgent.learn();
    // perform the gameplay task on the same level
    platformerAIOptions.setVisualization(true);
    Agent bestAgent = learningAgent.getBestAgent();
    platformerAIOptions.setAgent(bestAgent);
    BasicTask basicTask = new BasicTask(platformerAIOptions);
    basicTask.setOptionsAndReset(platformerAIOptions);
//        basicTask.setAgent(bestAgent);
    if (!basicTask.runSingleEpisode(1))  // make evaluation on the same episode once
    {
        System.out.println("PlatformerAI: out of computational time per action!");
        disqualifications++;
    }
    EvaluationInfo evaluationInfo = basicTask.getEnvironment().getEvaluationInfo();
    int f = evaluationInfo.computeWeightedFitness();
    if (verbose)
    {
        System.out.println("Intermediate SCORE = " + f + "; Details: " + evaluationInfo.toStringSingleLine());
    }
    System.out.println("LearningTrack final score = " + f);
    return f;
}


public static void main(String[] args)
{
    // set up parameters
    PlatformerAIOptions platformerAIOptions = new PlatformerAIOptions(args);
//    LearningAgent learningAgent = new MLPESLearningAgent(); // Learning track competition entry goes here
    LearningAgent learningAgent = (LearningAgent) platformerAIOptions.getAgent();
    System.out.println("main.learningAgent = " + learningAgent);

//        Level 0
//    platformerAIOptions.setArgs("-lf on -lg on");
    float finalScore = LearningTrack.evaluateSubmission(platformerAIOptions, learningAgent);

//        Level 1
//    platformerAIOptions = new PlatformerAIOptions(args);
//    platformerAIOptions.setAgent(learningAgent);
//    platformerAIOptions.setArgs("-lco off -lb on -le off -lhb off -lg on -ltb on -lhs off -lca on -lde on -ld 5 -ls 133829");
//    finalScore += LearningTrack.evaluateSubmission(platformerAIOptions, learningAgent);

//        Level 2
//    platformerAIOptions = new PlatformerAIOptions(args);
//    platformerAIOptions.setArgs("-lde on -i on -ld 30 -ls 133434");
//    finalScore += LearningTrack.evaluateSubmission(platformerAIOptions, learningAgent);
//
////        Level 3
//    platformerAIOptions = new PlatformerAIOptions(args);
//    platformerAIOptions.setArgs("-lde on -i on -ld 30 -ls 133434 -lhb on");
//    finalScore += LearningTrack.evaluateSubmission(platformerAIOptions, learningAgent);
//
////        Level 4
//    platformerAIOptions = new PlatformerAIOptions(args);
//    platformerAIOptions.setArgs("-lla on -le off -lhs on -lde on -ld 5 -ls 1332656");
//    finalScore += LearningTrack.evaluateSubmission(platformerAIOptions, learningAgent);
//
//    // Level 5 (bonus level)
//    platformerAIOptions = new PlatformerAIOptions(args);
//    platformerAIOptions.setArgs("-le off -lhs on -lde on -ld 5 -ls 1332656");
//    finalScore += LearningTrack.evaluateSubmission(platformerAIOptions, learningAgent);

    System.out.println("finalScore = " + finalScore);
    System.exit(0);
}
}
