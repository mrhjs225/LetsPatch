package jsmain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.github.thwak.confix.patch.PatchUtils;
import com.github.thwak.confix.pool.ChangePoolGenerator;
import com.github.thwak.confix.pool.ContextIdentifier;

public class jinfix {
	public static void main(String[] args) {
		loadProperties("confix.properties");
		ChangePoolGenerator cpg = new ChangePoolGenerator(new ContextIdentifier());
		String before = "/home/hjsvm/hjsaprvm/before1.java";
		String after = "/home/hjsvm/hjsaprvm/after1.java";
		
		String[] classPathEntries = new String[] {""};
		String[] sourcePathEntries = new String[] {"/home/hjsvm/hjsaprvm/jfreechart/src"};
		
		File beforeFile = new File(before);
		File afterFile = new File(after);
		
		cpg.collect("test", beforeFile, afterFile, classPathEntries, sourcePathEntries);
		
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
	}
}
