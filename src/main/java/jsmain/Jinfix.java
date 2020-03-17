package jsmain;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.github.thwak.confix.patch.PatchUtils;
import com.github.thwak.confix.pool.ChangePoolGenerator;
import com.github.thwak.confix.pool.Context;
import com.github.thwak.confix.pool.ContextInfo;
import com.github.thwak.confix.pool.PLRTContextIdentifier;
import com.github.thwak.confix.coverage.CoverageManager;

import com.github.thwak.confix.pool.Change;
import com.github.thwak.confix.pool.ChangeOrigin;
import com.github.thwak.confix.pool.ChangePool;

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
	public static String targetProjectName;
	public static String classPathString;
	public static String sourcePathString;
	
	public static void main(String[] args) {
		String path = "/home/hjsvm/hjsaprvm/condatabase/commitfile";

		loadProperties("/home/hjsvm/hjsaprvm/ConFix/samples/confix.properties");
		setPathEntries(targetProjectName);
		generateChangePool(path);
		targetProjectName = "derby";
		setPathEntries(targetProjectName);
		generateChangePool(path);
		targetProjectName = "groovy";
		setPathEntries(targetProjectName);
		generateChangePool(path);
		targetProjectName = "hama";
		setPathEntries(targetProjectName);
		generateChangePool(path);
		targetProjectName = "ivy";
		setPathEntries(targetProjectName);
		generateChangePool(path);
		targetProjectName = "lucene";
		setPathEntries(targetProjectName);
		generateChangePool(path);
		targetProjectName = "mahout";
		setPathEntries(targetProjectName);
		generateChangePool(path);
		
		//for check ConFix context database
		// for (String poolPath : poolList) {
		// 	checkingPreviousContext(poolPath);
		// }

		// checkingPreviousContext("/home/hjsvm/hjsaprvm/ConFix/pool");

		System.out.println("done");
	}

	// to generate changePool
	public static void generateChangePool(String path) {
		ChangePoolGenerator cpg = new ChangePoolGenerator(new PLRTContextIdentifier());
		cpg.pool.setPoolDir(new File("/home/hjsvm/hjsaprvm/ConFix/pool"));
		String[] classPathEntries = new String[] {classPathString};
		String[] sourcePathEntries = new String[] {sourcePathString};
		File beforePatchFile;
		File afterPatchFile;
		ArrayList<String> beforeList = new ArrayList<>();
		ArrayList<String> afterList = new ArrayList<>();

		for (File info: new File(path + "/" + targetProjectName).listFiles()) {
			if (info.isDirectory()) {
				int i = 0;
				beforeList.clear();
				afterList.clear();
				beforePatchFile = new File(info.getPath() + "/beforeCommit");
				afterPatchFile = new File(info.getPath() + "/afterCommit");
				beforeList = new ArrayList<>(Arrays.asList(beforePatchFile.list()));
				afterList = new ArrayList<>(Arrays.asList(afterPatchFile.list()));
				try {
					for (i = 0; i < beforeList.size(); i++) {
						if(beforeList.get(i).endsWith(".java") && afterList.contains(beforeList.get(i))) {
							cpg.collect(info.getName() + ":" + info.getPath() + "/afterCommit/" + beforeList.get(i), new File(info.getPath() + "/beforeCommit/" + beforeList.get(i)), new File(info.getPath() + "/afterCommit/"+beforeList.get(i)), classPathEntries, sourcePathEntries);

						}
					}
				} catch(Exception e) {
					System.out.println("i: " + i);
					System.out.println("id:" + info.getName() + ":" + info.getPath() + "/afterCommit/");
				}
			} else {
			}
		}
		cpg.pool.storeTo(new File("/home/hjsvm/hjsaprvm/ConFix/pool"), true);
		pool = cpg.pool;
	}

	// to set classPathEntries and sourcePathEntries
	public static void setPathEntries(String targetProject) {
		String basicPath = "/home/hjsvm/hjsaprvm/condatabase/";
		if(targetProject.equals("collections")) {
			classPathString = basicPath + "commons-collections" + "/target";
			sourcePathString = basicPath + "commons-collections" + "/src";
		} else if (targetProject.equals("derby")) {
			classPathString = basicPath + targetProject + "/bin";
			sourcePathString = basicPath + targetProject + "/java";
		} else if (targetProject.equals("groovy")) {
			classPathString = basicPath + targetProject + "/bin";
			sourcePathString = basicPath + targetProject + "/src";
		} else if (targetProject.equals("hama")) {
			classPathString = "";
			sourcePathString = basicPath + targetProject + "/core/src";
		} else if (targetProject.equals("ivy")) {
			classPathString = basicPath + "ant-ivy" + "/bin/src";
			sourcePathString = basicPath + "ant-ivy" + "/src";
		} else if (targetProject.equals("lucene")) {
			classPathString = basicPath + "lucene-solr" + "/bin";
			sourcePathString = basicPath + "lucene-solr" + "/lucene";
		} else if (targetProject.equals("mahout")) {
			classPathString = "";
			sourcePathString = basicPath + targetProject + "/core/src";
		}
	}

	// jinseok: for checking previous ConFix paper's context database
	public static void checkingPreviousContext(String poolPath) {
		loadChangePool(poolPath);
		System.out.println("--------------------------new pool------------------------");
		Iterator<Context> keyContext = pool.contexts.keySet().iterator();
		Context tempContext;
		ContextInfo contextInfo;
		Change tempChange;

		for (int i = 0; i < 2; i++) {
			tempContext = keyContext.next();
			contextInfo = pool.contexts.get(tempContext);
			
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
			System.out.println("id: " + tempChange.id);
			System.out.println("type: " + tempChange.type);
			System.out.println("node: " + tempChange.node.value);
			System.out.println("node: " + tempChange.node.id);
			System.out.println("node: " + tempChange.node.kind);
			System.out.println("node: " + tempChange.node.parent.value);
			System.out.println("location: " + tempChange.location.value);
			System.out.println("code: " + tempChange.code);
			System.out.println("locationCOde: " + tempChange.locationCode);
			
		}
	}

	//jinseok: for extracting statistics information from previous ConFix context database
	public static void extractContextStatistics() {
		int fileNum = 0;

		for (String poolPath : poolList) {
			loadChangePool(poolPath);
			Iterator<Context> keyContext = pool.contexts.keySet().iterator();
			int number = 0;
			ContextInfo contextInfo;

			try {
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("/home/hjsvm/hjsaprvm/ConFix/pool/statistics" + fileNum + ".txt"));
				while(keyContext.hasNext()) {
					contextInfo = pool.contexts.get(keyContext.next());
					String str = number + ", " + contextInfo.getChanges().size() + "\n";
					bos.write(str.getBytes());
					number++;
				}
				bos.close();
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			fileNum++;
		}
	}

	public static void extractContextCommit() {
		String poolPath = poolList.get(1);
		loadChangePool(poolPath);
		Iterator<Context> keyContext = pool.contexts.keySet().iterator();
		int number = 0;
		ContextInfo contextInfo;
		ArrayList<Integer> checkList = new ArrayList<>();

		try {
			BufferedReader bufReader = new BufferedReader(new FileReader(new File("/home/hjsvm/hjsaprvm/ConFix/pool/plrtCheck.txt")));
			BufferedWriter bufWriter = new BufferedWriter(new FileWriter(new File("/home/hjsvm/hjsaprvm/ConFix/pool/plrtAnalyze.txt")));
			String line = "";

			while((line = bufReader.readLine()) != null) {
				checkList.add(Integer.parseInt(line));
			}
			
			while(keyContext.hasNext()) {
				Context tempContext = keyContext.next();
				System.out.println(tempContext.hashString);
				System.out.println(tempContext.hash);
				if (checkList.contains(number)) {
					contextInfo = pool.contexts.get(tempContext);
					for (int i = 0; i < 15; i += 4) {
						int changeInt = contextInfo.getChanges().get(i);
						pool.loadChange(changeInt);
						bufWriter.write(pool.hashIdMap.get(changeInt) + "\n");
					}
				}
				number++;
			}
			bufReader.close();
			bufWriter.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public static void extractContextInformation() {
		String poolPath = poolList.get(1);
		loadChangePool(poolPath);
		Iterator<Context> keyContext = pool.contexts.keySet().iterator();
		int number = 0;
		ContextInfo contextInfo;
		ArrayList<Integer> checkList = new ArrayList<>();

		try {
			BufferedReader bufReader = new BufferedReader(new FileReader(new File("/home/hjsvm/hjsaprvm/ConFix/pool/plrtCheck.txt")));
			BufferedWriter bufWriter = new BufferedWriter(new FileWriter(new File("/home/hjsvm/hjsaprvm/ConFix/pool/plrtAnalyze.txt")));
			String line = "";

			while((line = bufReader.readLine()) != null) {
				checkList.add(Integer.parseInt(line));
			}
			
			while(keyContext.hasNext()) {
				Context tempContext = keyContext.next();
				if (checkList.contains(number)) {
					contextInfo = pool.contexts.get(tempContext);
					bufWriter.write("-------------------------------------------\n");
					bufWriter.write("Context: " + tempContext + "\n");
					bufWriter.write("Number of change: " + contextInfo.getChanges().size() + "\n");

					for (int i = 0; i < 10; i += 2) {
						int changeInt = contextInfo.getChanges().get(i);
						pool.loadChange(changeInt);
						bufWriter.write("Change's id number: " + changeInt + "\n");
						bufWriter.write(pool.hashIdMap.get(changeInt) + "\n");
					}
				}
				number++;
			}
			bufReader.close();
			bufWriter.close();
		} catch (Exception e) {
			System.out.println(e.toString());
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
		targetProjectName = PatchUtils.getStringProperty(props, "targetproject", "");
	}
}
