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

package competition.gic2010.turing.Gaudl;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.SimpleLayout;
import org.jgap.Chromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.gp.CommandGene;
import org.jgap.gp.GPFitnessFunction;
import org.jgap.gp.GPProblem;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.function.ADF;
import org.jgap.gp.impl.BranchTypingCross;
import org.jgap.gp.impl.DeltaGPFitnessEvaluator;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.impl.GPProgram;
import org.jgap.gp.impl.TournamentSelector;
import org.jgap.gp.terminal.False;
import org.jgap.gp.terminal.Terminal;
import org.jgap.gp.terminal.True;
import org.jgap.gp.terminal.Variable;

import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;
import competition.gic2010.turing.Gaudl.Genes.And;
import competition.gic2010.turing.Gaudl.Genes.CanJump;
import competition.gic2010.turing.Gaudl.Genes.CanShoot;
import competition.gic2010.turing.Gaudl.Genes.Down;
import competition.gic2010.turing.Gaudl.Genes.IfElse;
import competition.gic2010.turing.Gaudl.Genes.IsAirAt;
import competition.gic2010.turing.Gaudl.Genes.IsBreakableAt;
import competition.gic2010.turing.Gaudl.Genes.IsCoinAt;
import competition.gic2010.turing.Gaudl.Genes.IsEnemyAt;
import competition.gic2010.turing.Gaudl.Genes.IsFireFlowerAt;
import competition.gic2010.turing.Gaudl.Genes.IsMushroomAt;
import competition.gic2010.turing.Gaudl.Genes.IsPrincessAt;
import competition.gic2010.turing.Gaudl.Genes.IsTall;
import competition.gic2010.turing.Gaudl.Genes.IsWalkableAt;
import competition.gic2010.turing.Gaudl.Genes.Jump;
import competition.gic2010.turing.Gaudl.Genes.JumpLeft;
import competition.gic2010.turing.Gaudl.Genes.JumpRight;
import competition.gic2010.turing.Gaudl.Genes.LastActionWas;
import competition.gic2010.turing.Gaudl.Genes.Left;
import competition.gic2010.turing.Gaudl.Genes.LongJump;
import competition.gic2010.turing.Gaudl.Genes.LongJumpLeft;
import competition.gic2010.turing.Gaudl.Genes.LongJumpRight;
import competition.gic2010.turing.Gaudl.Genes.Not;
import competition.gic2010.turing.Gaudl.Genes.ObjectAtXYIs;
import competition.gic2010.turing.Gaudl.Genes.Right;
import competition.gic2010.turing.Gaudl.Genes.Run;
import competition.gic2010.turing.Gaudl.Genes.Shoot;
import competition.gic2010.turing.Gaudl.Genes.SubProgram;
import competition.gic2010.turing.Gaudl.Genes.Wait;
import competition.gic2010.turing.Gaudl.gp.WeightedGPRouletteSelector;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey at idsia dot ch Date: Mar 17, 2010 Time: 8:28:00 AM
 * Package: ch.idsia.scenarios
 */
public final class GPSystemStandAlone extends GPProblem
{
protected static Variable vx;
private GPGenotype Geno;
public Thread gpThread;
public static final int popSize = 100;
private transient Logger LOGGER;

public GPSystemStandAlone(GPFitnessFunction metric) {
	GPConfiguration config;
	
	// configuring the Logger for JGAP
	LOGGER = Logger.getLogger(this.getClass());
	//Thread gpThread = null;
	try {
        config = new GPConfiguration();
        //config.setGPFitnessEvaluator(new DeltaGPFitnessEvaluator());
        config.setProgramCreationMaxTries(-1);
        //config.setStrictProgramCreation(true);
        //config.setMinimumPopSizePercent(popSize);
        config.setMinInitDepth(1);
        config.setMaxInitDepth(7);
        config.setPopulationSize(popSize);
        //Taken from anttrail. WORTH INVESTIGATING.
        config.setCrossoverProb(0.6f);//orig: 0.9f
        config.setReproductionProb(0.4f); //orig: 0.1f
        config.setNewChromsPercent(0.2f); //orig: 0.3f
        config.setMutationProb(0.01f);
        config.setFunctionProb(0.6f);
        //config.setUseProgramCache(true);
        config.setCrossoverMethod(new BranchTypingCross(config,false));
        config.setSelectionMethod(new TournamentSelector(3));
        //config.setSelectionMethod(new WeightedGPRouletteSelector(config));
        config.setPreservFittestIndividual(true);
        config.setFitnessFunction(metric);
        setGPConfiguration(config);
        Geno = create();
        Geno.setVerboseOutput(true);
        gpThread = new Thread(Geno);
    }
    catch (InvalidConfigurationException e){
        System.err.println("wrong config: \n"+e);
    }
	if (gpThread instanceof Thread)
		gpThread.start();
}

@SuppressWarnings("rawtypes")
@Override
public GPGenotype create() throws InvalidConfigurationException {
	Class [] types = { CommandGene.VoidClass, 
		//	CommandGene.BooleanClass
			};
	GPConfiguration conf  = getGPConfiguration();
	Class [][] argTypes = {{},
		//	{CommandGene.BooleanClass,CommandGene.BooleanClass}
			};
	CommandGene [][] nodes = {
			{
				//vx = Variable.create(conf,"X", CommandGene.IntegerClass),
				new Terminal(conf, CommandGene.IntegerClass,-5,5,true),
				new Terminal(conf, CommandGene.IntegerClass,-5,5,true),
				//new Terminal(conf, CommandGene.IntegerClass,-4,4,true),
				new SubProgram(conf,new Class[] {CommandGene.VoidClass,CommandGene.VoidClass}),
				//new SubProgram(conf),
				//new Add(conf, CommandGene.IntegerClass),
				//new GpGreaterThan(conf, CommandGene.IntegerClass),
				//new Equals(conf, CommandGene.IntegerClass),
				new IfElse(conf, CommandGene.BooleanClass),
				//new Equals(conf, CommandGene.BooleanClass),
				//new Equals(conf, CommandGene.IntegerClass),
				//new Or(conf),
				new And(conf),
				new Not(conf),
				new CanJump(conf),
				new CanShoot(conf),
				new LastActionWas(conf),
				new IsTall(conf),
				//new ObjectAtXYIs(conf),
				//new True(conf),
				new IsBreakableAt(conf),
				new IsCoinAt(conf),
				new IsAirAt(conf),
				new IsEnemyAt(conf),
				new IsFireFlowerAt(conf),
				new IsMushroomAt(conf),
				//new IsPrincessAt(conf),
				new IsWalkableAt(conf),
				//new SubProgram(conf,new Class[] {CommandGene.VoidClass,CommandGene.VoidClass,CommandGene.VoidClass}),
				new Down(conf),
				new Wait(conf),
				new Left(conf),
				new Right(conf),
				new Shoot(conf),
				new Jump(conf),
				new LongJump(conf),
				//new JumpLeft(conf),
				//new JumpRight(conf),
				//new LongJumpLeft(conf),
				//new LongJumpRight(conf),
				new Run(conf),
		//		new ADF(conf, 1, 2)
			},
			/*{
				new And(conf),
			}*/
	};
	GPGenotype geno = GPGenotype.randomInitialGenotype(conf, types, argTypes, nodes,
			250, true);
	geno.switchParsimonyPressure(false);
	return geno;
}

public static void main(String[] args) throws InterruptedException
{
//        final String argsString = "-vis on";
	try {
		Logger.getRoot().setLevel(Level.INFO);
		RollingFileAppender logger = new RollingFileAppender(new SimpleLayout(), "genotype.log");
		logger.setMaxBackupIndex(10);
		logger.setMaxFileSize("100MB");
		Logger.getRootLogger().addAppender(logger);
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
    final BasicTask basicTask = new BasicTask(marioAIOptions);
    //GameplayMetricFitness metric = new GameplayMetricFitness(basicTask,marioAIOptions);
    //GamalyzerFitness metric = new GamalyzerFitness(basicTask,marioAIOptions);
    TraceFitness metric = new TraceFitness(basicTask,marioAIOptions);
    GPSystemStandAlone marioGP = new GPSystemStandAlone(metric);
    
    while (marioGP.gpThread.isAlive()) {
    	Thread.sleep(10);
    }
    System.exit(0);
}

}
