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

package org.platformer;

import org.platformer.benchmark.platform.engine.GlobalOptions;
import org.platformer.tools.PlatformerAIOptions;
import org.platformer.utils.ParameterContainer;
import junit.framework.TestCase;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Aug 28, 2010
 * Time: 8:36:02 PM
 * Package: org.platform.unittests
 */

public class CmdLineOptionsTest extends TestCase
{
PlatformerAIOptions platformerAIOptions;

@BeforeTest
public void setUp()
{
    platformerAIOptions = new PlatformerAIOptions();
}

@AfterTest
public void tearDown()
{
}

@Test
public void testTotalNumberOfOptions() throws Exception
{
    assertEquals(56, platformerAIOptions.getTotalNumberOfOptions());
}

@Test
public void testAllOptionsHaveDefaults()
{
    assertEquals(ParameterContainer.getTotalNumberOfOptions(), ParameterContainer.getNumberOfAllowedOptions());
}

@Test
public void testSetArgs() throws Exception
{
    String args = "-ag org.platformer.agents.controllers.human.HumanKeyboardAgent" +
            //  " -amico off" +
            " -echo off" +
            " -ewf on" +
            " -fc off" +
            " -cgr 1.0" +
            " -mgr 1.0" +
            " -gv off" +
            " -gvc off" +
            " -i off" +
            " -jp 7" +
            " -ld 0" +
            " -ll 320" +
            " -ls 0" +
            " -lt 0" +
            " -fps 24" +
            " -mm 2" +
            " -pw off" +
            " -pr off" +
            " -rfh 19" +
            " -rfw 19" +
            " -srf off" +
            " -tl 200" +
            " -tc off" +
            " -vaot off" +
            " -vlx 0" +
            " -vly 0" +
            " -vis on" +
            " -vw 320" +
            " -vh 240" +
            " -zs 1" +
            " -ze 0" +
            " -lh 15" +
            " -lde off" +
            " -lca on" +
            " -lhs on" +
            " -ltb on" +
            " -lco on" +
            " -lb on" +
            " -lg on" +
            " -lhb off" +
            " -le g,gw,gk,gkw,rk,rkw,s,sw" +
            " -lf off" +
            " -gmm 1";
    platformerAIOptions.setArgs(args);
    assertEquals(platformerAIOptions.getAgentFullLoadName(), "org.platform.agents.controllers.human.HumanKeyboardAgent");
    assertEquals(platformerAIOptions.isEcho(), false);
    assertEquals(platformerAIOptions.isExitProgramWhenFinished(), true);
    assertEquals(platformerAIOptions.isFrozenCreatures(), false);
    assertEquals(platformerAIOptions.getCreaturesGravity(), 1.0f);
    assertEquals(platformerAIOptions.getMarioGravity(), 1.0f);
    assertEquals(platformerAIOptions.isGameViewer(), false);
    assertEquals(platformerAIOptions.isGameViewerContinuousUpdates(), false);
    assertEquals(platformerAIOptions.isMarioInvulnerable(), false);
    assertEquals(platformerAIOptions.getJumpPower(), 7.0f);
    assertEquals(platformerAIOptions.getLevelDifficulty(), 0);
    assertEquals(platformerAIOptions.getLevelLength(), 320);
    assertEquals(platformerAIOptions.getLevelRandSeed(), 0);
    assertEquals(platformerAIOptions.getLevelType(), 0);
    assertEquals(platformerAIOptions.getFPS(), 24);
    assertEquals(platformerAIOptions.getMarioMode(), 2);
    assertEquals(platformerAIOptions.isPowerRestoration(), false);
    assertEquals(platformerAIOptions.getReceptiveFieldHeight(), 19);
    assertEquals(platformerAIOptions.getReceptiveFieldWidth(), 19);
    assertEquals(platformerAIOptions.isReceptiveFieldVisualized(), false);
    assertEquals(platformerAIOptions.getTimeLimit(), 200);
    assertEquals(platformerAIOptions.isToolsConfigurator(), false);
    assertEquals(platformerAIOptions.isViewAlwaysOnTop(), false);
    assertEquals(platformerAIOptions.getViewLocation().x, 0);
    assertEquals(platformerAIOptions.getViewLocation().y, 0);
    assertEquals(platformerAIOptions.isVisualization(), true);
    assertEquals(platformerAIOptions.getViewWidth(), 320);
    assertEquals(platformerAIOptions.getViewHeight(), 240);
    assertEquals(platformerAIOptions.getZLevelScene(), 1);
    assertEquals(platformerAIOptions.getZLevelEnemies(), 0);
    assertEquals(platformerAIOptions.getLevelHeight(), 15);
    assertEquals(platformerAIOptions.getDeadEndsCount(), false);
    assertEquals(platformerAIOptions.getCannonsCount(), true);
    assertEquals(platformerAIOptions.getHillStraightCount(), true);
    assertEquals(platformerAIOptions.getTubesCount(), true);
    assertEquals(platformerAIOptions.getCoinsCount(), true);
    assertEquals(platformerAIOptions.getBlocksCount(), true);
    assertEquals(platformerAIOptions.getGapsCount(), true);
    assertEquals(platformerAIOptions.getHiddenBlocksCount(), false);
    assertEquals(platformerAIOptions.getEnemies(), "g,gw,gk,gkw,rk,rkw,s,sw");
    assertEquals(platformerAIOptions.isFlatLevel(), false);
    assertEquals(platformerAIOptions.getGreenMushroomMode(), 1);
//    TODO:TASK:[M] test all cases
}

@Test
public void testSetLevelEnemies()
{
    platformerAIOptions.setArgs("-le 1111111111");
    // TODO:TASK:[M] test various conditions
}

@Test
public void testSetMarioInvulnerable() throws Exception
{
    platformerAIOptions.setMarioInvulnerable(true);
    assertEquals(platformerAIOptions.isMarioInvulnerable(), true);
    platformerAIOptions.setArgs("-i off");
    assertEquals(platformerAIOptions.isMarioInvulnerable(), false);
}

@Test
public void testDefaultAgent()
{
    assertNotNull(platformerAIOptions.getAgent());
    assertEquals("org.platformer.agents.controllers.human.HumanKeyboardAgent", platformerAIOptions.getAgentFullLoadName());
    assertEquals("HumanKeyboardAgent", platformerAIOptions.getAgent().getName());
}

@Test
public void testStop()
{
    this.platformerAIOptions.setArgs("-stop on");
    assertEquals(true, this.platformerAIOptions.isStopGamePlay());
    assertEquals(GlobalOptions.isGameplayStopped, this.platformerAIOptions.isStopGamePlay());
}

@Test
public void testReset()
{
    platformerAIOptions.setArgs("-echo on -rfw 21 -rfh 17");
    assertTrue(platformerAIOptions.isEcho());
    assertEquals(21, platformerAIOptions.getReceptiveFieldWidth());
    assertEquals(17, platformerAIOptions.getReceptiveFieldHeight());

    platformerAIOptions.reset();
    assertFalse(platformerAIOptions.isEcho());
    assertEquals(19, platformerAIOptions.getReceptiveFieldWidth());
    assertEquals(19, platformerAIOptions.getReceptiveFieldHeight());
}

}
