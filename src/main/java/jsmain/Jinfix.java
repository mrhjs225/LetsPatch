package jsmain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.github.thwak.confix.coverage.CoverageManager;
import com.github.thwak.confix.patch.PatchUtils;

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
	public static String targetProjectName;
	public static List<String> poolList;
	
	public static void main(String[] args) {
		loadProperties("/home/hjsvm/hjsaprvm/ConFix/samples/confix.properties");
		// JinContextAnalyzer jinContextAnalyzer = new JinContextAnalyzer(maxPoolLoad);
		String path = "/home/hjsvm/hjsaprvm/condatabase/outputs";

		JinGenerateChangePool.setPathEntries("collections");
		JinGenerateChangePool.testChangePool();
		// jinContextAnalyzer.checkingPreviousContext("/home/hjsvm/hjsaprvm/condatabase/pool/poolTest");
		// targetProjectName = "collections";
		// JinGenerateChangePool.setPathEntries(targetProjectName);		
		// JinGenerateChangePool.generateChangePool(path);

		// targetProjectName = "derby";
		// JinGenerateChangePool.setPathEntries(targetProjectName);
		// JinGenerateChangePool.generateChangePool(path);

		// targetProjectName = "groovy";
		// JinGenerateChangePool.setPathEntries(targetProjectName);
		// JinGenerateChangePool.generateChangePool(path);

		// targetProjectName = "hama";
		// JinGenerateChangePool.setPathEntries(targetProjectName);
		// JinGenerateChangePool.generateChangePool(path);

		// targetProjectName = "ivy";
		// JinGenerateChangePool.setPathEntries(targetProjectName);
		// JinGenerateChangePool.generateChangePool(path);

		// targetProjectName = "lucene";
		// JinGenerateChangePool.setPathEntries(targetProjectName);
		// JinGenerateChangePool.generateChangePool(path);

		// targetProjectName = "mahout";
		// JinGenerateChangePool.setPathEntries(targetProjectName);
		// JinGenerateChangePool.generateChangePool(path);

		System.out.println("done");
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
		targetProjectName = PatchUtils.getStringProperty(props, "targetproject", "");
	}
}
