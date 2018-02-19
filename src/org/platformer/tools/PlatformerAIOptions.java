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

package org.platformer.tools;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;
import org.platformer.benchmark.platform.engine.GlobalOptions;
import org.platformer.benchmark.platform.simulation.SimulationOptions;

import java.awt.*;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 9:05:20 AM
 * Package: org.platform.tools
 */

/**
 * The <code>PlatformerAIOptions</code> class handles the command-line options
 * It sets up parameters from command line if there are any.
 * Defaults are used otherwise.
 *
 * @author Sergey Karakovskiy
 * @version 1.0, Apr 25, 2009
 * @see org.platformer.utils.ParameterContainer
 * @see org.platformer.benchmark.platform.simulation.SimulationOptions
 * @since PlatformerAI0.1
 */

public final class PlatformerAIOptions extends SimulationOptions {
    private static final HashMap<String, PlatformerAIOptions> CmdLineOptionsMapString = new HashMap<String, PlatformerAIOptions>();
    private transient Genson jsonSerialiser;
    final private Point marioInitialPos = new Point();

    public PlatformerAIOptions(String[] args) {
        this();
        this.setArgs(args);
    }

//    @Deprecated

    public PlatformerAIOptions(String args) {
        this();
        this.setArgs(args);
    }

    public PlatformerAIOptions() {
        super();
        jsonSerialiser = new GensonBuilder()
                .useClassMetadata(true)
                .useMethods(false)
                .setSkipNull(true)
                .useFields(true, new VisibilityFilter(Modifier.TRANSIENT, Modifier.STATIC))
                .useClassMetadataWithStaticType(false)
                .create();
        this.setArgs("");
    }

    public void setArgs(String argString) {
        if (!"".equals(argString))
            this.setArgs(argString.trim().split("\\s+"));
        else
            this.setArgs((String[]) null);
    }

    public void deserialize(String json) {
        this.optionsHashMap = (HashMap<String, String>) jsonSerialiser.deserialize(json, Map.class);
    }

    public String asJSONString() {
        String jsonRepresentation = jsonSerialiser.serialize(this.optionsHashMap);

        return jsonRepresentation;
    }

    public void setArgs(String[] args) {

        this.setUpOptions(args);

        if (isEcho()) {
            this.printOptions(false);
        }
        GlobalOptions.receptiveFieldWidth = getReceptiveFieldWidth();
        GlobalOptions.receptiveFieldHeight = getReceptiveFieldHeight();
        if (getMarioEgoPosCol() == 9 && GlobalOptions.receptiveFieldWidth != 19)
            GlobalOptions.marioEgoCol = GlobalOptions.receptiveFieldWidth / 2;
        else
            GlobalOptions.marioEgoCol = getMarioEgoPosCol();
        if (getMarioEgoPosRow() == 9 && GlobalOptions.receptiveFieldHeight != 19)
            GlobalOptions.marioEgoRow = GlobalOptions.receptiveFieldHeight / 2;
        else
            GlobalOptions.marioEgoRow = getMarioEgoPosRow();

        GlobalOptions.VISUAL_COMPONENT_HEIGHT = getViewHeight();
        GlobalOptions.VISUAL_COMPONENT_WIDTH = getViewWidth();
//        Environment.ObsWidth = GlobalOptions.receptiveFieldWidth/2;
//        Environment.ObsHeight = GlobalOptions.receptiveFieldHeight/2;
        GlobalOptions.isShowReceptiveField = isReceptiveFieldVisualized();
        GlobalOptions.isGameplayStopped = isStopGamePlay();
    }

    public float getMarioGravity() {
        // TODO: getMarioGravity, doublecheck if unit test is present and remove if fixed
        return f(getParameterValue("-mgr"));
    }

    public void setMarioGravity(float mgr) {
        setParameterValue("-mgr", s(mgr));
    }

    public float getWind() {
        return f(getParameterValue("-w"));
    }

    public void setWind(float wind) {
        setParameterValue("-w", s(wind));
    }

    public float getIce() {
        return f(getParameterValue("-ice"));
    }

    public void setIce(float ice) {
        setParameterValue("-ice", s(ice));
    }

    public float getCreaturesGravity() {
        // TODO: getCreaturesGravity, same as for mgr
        return f(getParameterValue("-cgr"));
    }

    public int getViewWidth() {
        return i(getParameterValue("-vw"));
    }

    public void setViewWidth(int width) {
        setParameterValue("-vw", s(width));
    }

    public int getViewHeight() {
        return i(getParameterValue("-vh"));
    }

    public void setViewHeight(int height) {
        setParameterValue("-vh", s(height));
    }

    public void printOptions(boolean singleLine) {
        System.out.println("\n[PlatformerAI] : Options have been set to:");
        for (Map.Entry<String, String> el : optionsHashMap.entrySet())
            if (singleLine)
                System.out.print(el.getKey() + " " + el.getValue() + " ");
            else
                System.out.println(el.getKey() + " " + el.getValue() + " ");
    }

    public static PlatformerAIOptions getOptionsByString(String argString) {
        // TODO: verify validity of this method, add unit tests
        if (CmdLineOptionsMapString.get(argString) == null) {
            final PlatformerAIOptions value = new PlatformerAIOptions(argString.trim().split("\\s+"));
            CmdLineOptionsMapString.put(argString, value);
            return value;
        }
        return CmdLineOptionsMapString.get(argString);
    }

    public static PlatformerAIOptions getDefaultOptions() {
        return getOptionsByString("");
    }

    public boolean isToolsConfigurator() {
        return b(getParameterValue("-tc"));
    }

    public boolean isGameViewer() {
        return b(getParameterValue("-gv"));
    }

    public void setGameViewer(boolean gv) {
        setParameterValue("-gv", s(gv));
    }

    public boolean isGameViewerContinuousUpdates() {
        return b(getParameterValue("-gvc"));
    }

    public void setGameViewerContinuousUpdates(boolean gvc) {
        setParameterValue("-gvc", s(gvc));
    }

    public boolean isEcho() {
        return b(getParameterValue("-echo"));
    }

    public void setEcho(boolean echo) {
        setParameterValue("-echo", s(echo));
    }

    public boolean isStopGamePlay() {
        return b(getParameterValue("-stop"));
    }

    public void setStopGamePlay(boolean stop) {
        setParameterValue("-stop", s(stop));
    }

    public float getJumpPower() {
        return f(getParameterValue("-jp"));
    }

    public void setJumpPower(float jp) {
        setParameterValue("-jp", s(jp));
    }

    public int getReceptiveFieldWidth() {
        int ret = i(getParameterValue("-rfw"));
//
//    if (ret % 2 == 0)
//    {
//        System.err.println("\n[PlatformerAI WARNING] : Wrong value for receptive field width: " + ret++ +
//                " ; receptive field width set to " + ret);
//        setParameterValue("-rfw", s(ret));
//    }
        return ret;
    }

    public void setReceptiveFieldWidth(int rfw) {
        setParameterValue("-rfw", s(rfw));
    }

    public int getReceptiveFieldHeight() {
        int ret = i(getParameterValue("-rfh"));
//    if (ret % 2 == 0)
//    {
//        System.err.println("\n[PlatformerAI WARNING] : Wrong value for receptive field height: " + ret++ +
//                " ; receptive field height set to " + ret);
//        setParameterValue("-rfh", s(ret));
//    }
        return ret;
    }

    public void setReceptiveFieldHeight(int rfh) {
        setParameterValue("-rfh", s(rfh));
    }

    public boolean isReceptiveFieldVisualized() {
        return b(getParameterValue("-srf"));
    }

    public void setReceptiveFieldVisualized(boolean srf) {
        setParameterValue("-srf", s(srf));
    }

    public Point getMarioInitialPos() {
        marioInitialPos.x = i(getParameterValue("-mix"));
        marioInitialPos.y = i(getParameterValue("-miy"));
        return marioInitialPos;
    }

    public void reset() {
        optionsHashMap.clear();
    }

    public int getMarioEgoPosRow() {
        return i(getParameterValue("-mer"));
    }

    public int getMarioEgoPosCol() {
        return i(getParameterValue("-mec"));
    }

    public int getExitX() {
        return i(getParameterValue("-ex"));
    }

    public int getExitY() {
        return i(getParameterValue("-ey"));
    }

    public void setExitX(int x) {
        setParameterValue("-ex", s(x));
    }

    public void setExitY(int y) {
        setParameterValue("-ey", s(y));
    }
}