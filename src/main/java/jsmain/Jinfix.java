package jsmain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import com.github.thwak.confix.patch.PatchUtils;
import com.github.thwak.confix.pool.ChangePoolGenerator;
import com.github.thwak.confix.pool.Context;
import com.github.thwak.confix.pool.ContextIdentifier;
import com.github.thwak.confix.pool.ContextInfo;
import com.github.thwak.confix.pool.PLRTContextIdentifier;
import com.github.thwak.confix.coverage.CoverageManager;
import com.github.thwak.confix.coverage.TestResult;
import com.github.thwak.confix.coverage.Tester;
import com.github.thwak.confix.patch.PatchInfo;
import com.github.thwak.confix.patch.PatchStrategy;
import com.github.thwak.confix.patch.Patcher;
import com.github.thwak.confix.patch.StrategyFactory;
import com.github.thwak.confix.patch.TargetLocation;
import com.github.thwak.confix.pool.Change;
import com.github.thwak.confix.pool.ChangeOrigin;
import com.github.thwak.confix.pool.ChangePool;
import com.github.thwak.confix.tree.compiler.Compiler;
import com.github.thwak.confix.util.IOUtils;

public class Jinfix {
	public static String testClassPath;
	public static String compileClassPath;
	public static String[] testClassPathEntries;
	public static String[] compileClassPathEntries;
	public static String libClassPath;
	public static List<String> modifiedClasses = new ArrayList<>();
	public static CoverageManager coverage;
	public static String sourceDir;
	public static String targetDir;
	public static String tempDir;
	public static List<String> poolList;
	public static ChangePool pool;
	public static int patchCount = 20;
	public static int maxTrials = 10;
	public static String candidateDir = "candidates";
	public static String patchDir = "patches";
	public static String jvm;
	public static String version;
	public static long timeout;
	public static List<String> triggerTests = new ArrayList<>();
	public static List<String> relTests = new ArrayList<>();
	public static List<String> allTests = new ArrayList<>();
	public static Set<String> brokenTests = new HashSet<>();
	public static long seed;
	public static int numOfTriggers;
	public static long timeBudget;
	public static String pStrategyKey;
	public static String cStrategyKey;
	public static String flMetric;
	public static int maxPoolLoad;
	public static int maxChangeCount;
	
	public static void main(String[] args) {
		loadProperties("/home/hjsvm/hjsaprvm/ConFix/samples/confix.properties");
		ChangePoolGenerator cpg = new ChangePoolGenerator(new PLRTContextIdentifier());
		cpg.pool.setPoolDir(new File("/home/hjsvm/hjsaprvm/ConFix/pool"));
		String[] classPathEntries = new String[] {"/home/hjsvm/hjsaprvm/jfreechart/target/classes"};
		String[] sourcePathEntries = new String[] {"/home/hjsvm/hjsaprvm/jfreechart/src"};
		Random r = new Random(seed);

		String path = "/home/hjsvm/hjsaprvm/condatabase/pool";
		File beforePatchFile = new File(path+"/beforepatch");
		File afterPatchFile = new File(path+"/afterpatch");
		String[] beforeList = beforePatchFile.list();
		String[] afterList = afterPatchFile.list();
		for (int i = 0; i < beforeList.length; i++) {
			cpg.collect("test"+Integer.toString(i), new File(path+"/beforepatch/"+beforeList[i]), new File(path+"/afterpatch/"+afterList[i]), classPathEntries, sourcePathEntries);
		}
		ChangePool changePool = cpg.pool;

		// for context understanding
//		Iterator<Context> setIter = changePool.getContexts().iterator();
//		while(setIter.hasNext()) {
//			Context keyContext = setIter.next();
//			ContextInfo info = changePool.contexts.get(keyContext);
//			System.out.println("context: "+ keyContext.hashString);
//			System.out.println("changeid: " + changePool.getChangeIds(keyContext));
//			for(int i = 0; i < changePool.getChangeIds(keyContext).size(); i++) {
//				System.out.println("change"+i+": " + changePool.getChange(changePool.getChangeIds(keyContext).get(i)));
//			}
//		}

		checkingPreviousContext();

		System.out.println("done");
	}

	// for checking previous ConFix paper's context database
	public static void checkingPreviousContext() {
		for (String poolPath : poolList) {
			loadChangePool(poolPath);
			System.out.println("--------------------------new pool------------------------");
			Iterator<Context> keyContext = pool.contexts.keySet().iterator();
			Context tempContext;
			ContextInfo contextInfo;
			Iterator<Integer> keyInteger;
			Integer tempInteger;
			Change tempChange;
			ChangeOrigin changeOrigin = new ChangeOrigin();

			for (int i = 0; i < 2; i++) {
				tempContext = keyContext.next();
				contextInfo = pool.contexts.get(tempContext);
				keyInteger = contextInfo.getChangeFreq().keySet().iterator();
				tempInteger = keyInteger.next();
				

				System.out.println("----------context----------");
				System.out.println("context: " + tempContext);
				System.out.println("freq: " + contextInfo.getFreq());
				System.out.println("listsize: " + contextInfo.getChanges().size());
				System.out.println("changeFreqsize: " + contextInfo.getChangeFreq().size());
				for (int j = 0; j < contextInfo.getChanges().size(); j++) {
					System.out.println("	list" + j + ": " + contextInfo.getChanges().get(j));	// this integer using to find hashmapid in changepool
					System.out.println("	freq" + j + ": " + contextInfo.getChangeFreq().get(contextInfo.getChanges().get(j)));
				}
				System.out.println("changes content: " + contextInfo.getChanges().get(0));
				System.out.println("change: " + pool.hashIdMap.get(contextInfo.getChanges().get(0)));
				pool.loadChange(contextInfo.getChanges().get(0));
				tempChange = pool.changes.get(contextInfo.getChanges().get(0));
				System.out.println(tempChange);
			}
		}
	}
	private static void loadChangePool(String poolPath) {
		pool = new ChangePool();
		pool.loadFrom(new File(poolPath));
		pool.maxLoadCount = maxPoolLoad;
		System.out.println("Pool:"+poolPath);
	}

	private static void loadProperties(String fileName) {
		Properties props = new Properties();
		File f = new File(fileName);
		try {
			FileInputStream fis = new FileInputStream(f);
			props.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		libClassPath = PatchUtils.getStringProperty(props, "cp.lib", "");
		testClassPath = PatchUtils.getStringProperty(props, "cp.test", "");
		compileClassPath = PatchUtils.getStringProperty(props, "cp.compile", "");
		String priority = PatchUtils.getStringProperty(props, "cp.test.priority", "local");
		if(libClassPath.length() > 0) {
			if("cfix".equals(priority)) {
				testClassPath = libClassPath + File.pathSeparatorChar + testClassPath;
			} else {
				testClassPath = testClassPath + File.pathSeparatorChar + libClassPath;
			}
		}
		testClassPathEntries = testClassPath.split(File.pathSeparator);
		compileClassPathEntries = compileClassPath.split(File.pathSeparator);
		sourceDir = PatchUtils.getStringProperty(props, "src.dir", "src/main/java");
		targetDir = PatchUtils.getStringProperty(props, "target.dir", "target/classes");
		modifiedClasses = PatchUtils.getListProperty(props, "classes.modified", ",");
		poolList = PatchUtils.getListProperty(props, "pool.path", ",");
		jvm = PatchUtils.getStringProperty(props, "jvm", "/usr/bin/java");
		version = PatchUtils.getStringProperty(props, "version", "1.7");
		timeout = Long.parseLong(PatchUtils.getStringProperty(props, "timeout", "10"))*1000;
		patchCount = Integer.parseInt(PatchUtils.getStringProperty(props, "patch.count", "20"));
		maxTrials = Integer.parseInt(PatchUtils.getStringProperty(props, "max.trials", "10"));
		maxChangeCount = Integer.parseInt(PatchUtils.getStringProperty(props, "max.change.count", "25"));
		maxPoolLoad = Integer.parseInt(PatchUtils.getStringProperty(props, "max.pool.load", "1000"));
		seed = Long.parseLong(PatchUtils.getStringProperty(props, "seed", "-1"));
		tempDir = new File("tmp").getAbsolutePath();
		timeBudget = Long.parseLong(PatchUtils.getStringProperty(props, "time.budget", "-1"));
		pStrategyKey = PatchUtils.getStringProperty(props, "patch.strategy", "flfreq");
		cStrategyKey = PatchUtils.getStringProperty(props, "concretize.strategy", "tc");
		flMetric = PatchUtils.getStringProperty(props, "fl.metric", "ochiai");
	}
	
	private static void loadTests() {
		String trigger = IOUtils.readFile("tests.trigger");
		String relevant = IOUtils.readFile("tests.relevant");
		String all = IOUtils.readFile("tests.all");
		Set<String> testSet = new HashSet<>();
		String[] tests = trigger.split("\n");
		numOfTriggers = tests.length;
		for(String test : tests){
			//Get the class name only for trigger tests.
			if(!test.startsWith("#"))
				testSet.add(test.split("::")[0]);
		}
		triggerTests.addAll(testSet);
		relTests.addAll(Arrays.asList(relevant.split("\n")));
		allTests.addAll(Arrays.asList(all.split("\n")));
		File f = new File("tests.broken");
		if(f.exists()) {
			String broken = IOUtils.readFile("tests.broken");
			tests = broken.split("\n");
			for(String t : tests)
				brokenTests.add(t.replace("::", "#"));
		}
	}
}
