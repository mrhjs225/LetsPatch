package com.github.mrhjs225.LetsPatch.conAnalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.github.mrhjs225.LetsPatch.pool.ChangePool;
import com.github.mrhjs225.LetsPatch.pool.ChangePoolGenerator;
import com.github.mrhjs225.LetsPatch.pool.PLRTContextIdentifier;
import com.github.mrhjs225.LetsPatch.pool.TestContextIdentifier;

public class JinGenerateChangePool implements Serializable{
	public static String classPathString;
    public static String sourcePathString;
    public static String targetProjectName;
    public static ChangePool pool;

    public JinGenerateChangePool() {
        JinGenerateChangePool.classPathString = "";
        JinGenerateChangePool.sourcePathString = "";
        JinGenerateChangePool.targetProjectName = "";
        JinGenerateChangePool.pool = null;
    }

    // to generate merge commit file like github
    public static void generateMergeCommit(String projectName) {
        try {
            String databasePath = "/home/hjs/dldoldam/jinfix_database/changedfile/" + projectName;
            String folderPath = "";
            String beforeCommitFile = "";
            String afterCommitFile = "";
            ArrayList<String> beforeCommitFileList = new ArrayList<>();
            ArrayList<String> afterCommitFileList = new ArrayList<>();
            List<String> beforeCommitFileData;
            List<String> afterCommitFileData;
            BufferedWriter bufWriter;
            DiffRowGenerator generator = DiffRowGenerator.create().showInlineDiffs(true).inlineDiffByWord(true)
                    .oldTag(f -> "").newTag(f -> "").build();
            for (File info : new File(databasePath).listFiles()) {
                if (info.isDirectory()) {
                    beforeCommitFileList.clear();
                    afterCommitFileList.clear();
                    beforeCommitFileList = new ArrayList<>(Arrays.asList(new File(info.getPath() + "/before").list()));
                    afterCommitFileList = new ArrayList<>(Arrays.asList(new File(info.getPath() + "/after").list()));
                    
                    for (int i = 0; i < afterCommitFileList.size(); i++) {
                        if (beforeCommitFileList.contains(afterCommitFileList.get(i))
                                && afterCommitFileList.get(i).endsWith(".java")) {
                            beforeCommitFile = info.getPath() + "/before/" + afterCommitFileList.get(i);
                            afterCommitFile = info.getPath() + "/after/" + afterCommitFileList.get(i);
                            beforeCommitFileData = Files.readAllLines(new File(beforeCommitFile).toPath());
                            afterCommitFileData = Files.readAllLines(new File(afterCommitFile).toPath());
                            folderPath = "/home/hjs/dldoldam/jinfix_database/commitfileMerge/" + projectName + "/"
                                    + info.getName();
                            File folder = new File(folderPath);
                            if (!folder.exists()) {
                                folder.mkdir();
                            }
                            bufWriter = new BufferedWriter(new FileWriter(new File(folderPath + "/"
                                    + afterCommitFileList.get(i).substring(0, afterCommitFileList.get(i).length() - 5)
                                    + ".txt")));
                            List<DiffRow> rows = generator.generateDiffRows(beforeCommitFileData, afterCommitFileData);
                            
                            for (int j = 0; j < rows.size(); j++) {
                                bufWriter.write(rows.get(j).getTag() + "	" + rows.get(j).getOldLine() + "	"
                                        + rows.get(j).getNewLine() + "\n");
                            }
                            bufWriter.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // to test contextidentifier
    public static void testChangePool() {
        ChangePoolGenerator cpg = new ChangePoolGenerator(new TestContextIdentifier());
        cpg.pool.setPoolDir(new File("/home/hjs/dldoldam/jinfix_database/pool/poolTest"));
        String[] classPathEntries = new String[] { "" };
        String[] sourcePathEntries = new String[] { "" };
        File beforePatchFile;
        File afterPatchFile;
        ArrayList<String> beforeList = new ArrayList<>();
        ArrayList<String> afterList = new ArrayList<>();

        for (File info : new File("/home/hjs/dldoldam/jinfix_database/changedfile/test").listFiles()) {
            if (info.isDirectory()) {
                int i = 0;
                beforeList.clear();
                afterList.clear();
                beforePatchFile = new File(info.getPath() + "/before");
                afterPatchFile = new File(info.getPath() + "/after");
                beforeList = new ArrayList<>(Arrays.asList(beforePatchFile.list()));
                afterList = new ArrayList<>(Arrays.asList(afterPatchFile.list()));
                
                try {
                    for (i = 0; i < afterList.size(); i++) {
                        if (afterList.get(i).endsWith(".java") && beforeList.contains(afterList.get(i))) {
                            File beforeFileName = new File(info.getPath() + "/before/" + afterList.get(i));
                            File afterFileName = new File(info.getPath() + "/after/" + afterList.get(i));
                            cpg.collect("test", beforeFileName, afterFileName, classPathEntries, sourcePathEntries);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        cpg.pool.storeTo(new File("/home/hjs/dldoldam/jinfix_database/pool/poolTest"));
        pool = cpg.pool;
    }

    // to generate changePool
    public static void generateChangePool(String path) {
		setPathEntries("collections");		
		generateProjectChangePool(path);

		setPathEntries("derby");
		generateProjectChangePool(path);

		setPathEntries("groovy");
		generateProjectChangePool(path);

		setPathEntries("hama");
		generateProjectChangePool(path);

		setPathEntries("ivy");
		generateProjectChangePool(path);

		setPathEntries("lucene");
		generateProjectChangePool(path);

		setPathEntries("mahout");
		generateProjectChangePool(path);
    }

    // to generate changePool by project
    public static void generateProjectChangePool(String path) {
        ChangePoolGenerator cpg = new ChangePoolGenerator(new PLRTContextIdentifier());
        cpg.pool.setPoolDir(new File("/home/hjs/dldoldam/jinfix_database/pool/poolPLRT"));
        String[] classPathEntries = new String[] { classPathString };
        String[] sourcePathEntries = new String[] { sourcePathString };
        File beforePatchFile;
        File afterPatchFile;
        ArrayList<String> beforeList = new ArrayList<>();
        ArrayList<String> afterList = new ArrayList<>();

        for (File info : new File(path + "/" + targetProjectName).listFiles()) {
            if (info.isDirectory()) {
                int i = 0;
                beforeList.clear();
                afterList.clear();
                beforePatchFile = new File(info.getPath() + "/before");
                afterPatchFile = new File(info.getPath() + "/after");
                beforeList = new ArrayList<>(Arrays.asList(beforePatchFile.list()));
                afterList = new ArrayList<>(Arrays.asList(afterPatchFile.list()));
                try {
                    for (i = 0; i < afterList.size(); i++) {
                        if (afterList.get(i).endsWith(".java") && beforeList.contains(afterList.get(i))) {
                            File beforeFileName = new File(info.getPath() + "/before/" + afterList.get(i));
                            File afterFileName = new File(info.getPath() + "/after/" + afterList.get(i));
                            cpg.collect(info.getName() + ":" + info.getPath() + "/after/" + afterList.get(i),
                                    beforeFileName, afterFileName, classPathEntries, sourcePathEntries);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
            }
        }
        cpg.pool.storeTo(new File("/home/hjs/dldoldam/jinfix_database/pool/poolPLRT"), true);
        pool = cpg.pool;
        System.out.println(targetProjectName + " done");
    }

    // to set classPathEntries and sourcePathEntries
    public static void setPathEntries(String targetProject) {
        JinGenerateChangePool.targetProjectName = targetProject;
        String basicPath = "/home/hjs/dldoldam/jinfix_database/project";
        if (targetProject.equals("collections")) {
            classPathString = basicPath + "/commons-collections" + "/target";
            sourcePathString = basicPath + "/commons-collections" + "/src";
        } else if (targetProject.equals("derby")) {
            classPathString = basicPath + targetProject + "/bin";
            sourcePathString = basicPath + targetProject + "/java";
        } else if (targetProject.equals("groovy")) {
            classPathString = basicPath + targetProject + "/bin";
            sourcePathString = basicPath + targetProject + "/src";
        } else if (targetProject.equals("hadoop")) {
            classPathString = "";
            sourcePathString = basicPath + "/hadoop-common";
        } else if (targetProject.equals("hama")) {
            classPathString = basicPath + targetProject + "/core/target";
            sourcePathString = basicPath + targetProject + "/core/src";
        } else if (targetProject.equals("ivy")) {
            classPathString = basicPath + "/ant-ivy" + "/bin/src";
            sourcePathString = basicPath + "/ant-ivy" + "/src";
        } else if (targetProject.equals("lucene")) {
            classPathString = basicPath + "/lucene-solr" + "/bin";
            sourcePathString = basicPath + "/lucene-solr" + "/lucene";
        } else if (targetProject.equals("mahout")) {
            classPathString = basicPath + targetProject + "core/target";
            sourcePathString = basicPath + targetProject + "core/src";
        } else if (targetProject.equals("pdfbox")) {
            classPathString = "";
            sourcePathString = basicPath + "/fontbox" + "/src";
        }
    }

    // to test generate changePool
    public static void testgenerateChangePool(String path) {
        long ctime = System.currentTimeMillis();
        // testSetPathEntries("collections");		
        // testgenerateProjectChangePool(path);
        long ntime = System.currentTimeMillis();
        // System.out.println("time for done: " + (ntime - ctime)/1000 + "s");

        // ctime = System.currentTimeMillis();
		// testSetPathEntries("derby");
        // testgenerateProjectChangePool(path);
        // ntime = System.currentTimeMillis();
        // System.out.println("time for done: " + (ntime - ctime)/1000 + "s");

        ctime = System.currentTimeMillis();
		testSetPathEntries("groovy");
		testgenerateProjectChangePool(path);
        ntime = System.currentTimeMillis();
        System.out.println("time for done: " + (ntime - ctime)/1000 + "s");

        ctime = System.currentTimeMillis();
        testSetPathEntries("hama");
		testgenerateProjectChangePool(path);
        ntime = System.currentTimeMillis();
        System.out.println("time for done: " + (ntime - ctime)/1000 + "s");

        ctime = System.currentTimeMillis();
        testSetPathEntries("ivy");
		testgenerateProjectChangePool(path);
        ntime = System.currentTimeMillis();
        System.out.println("time for done: " + (ntime - ctime)/1000 + "s");

        ctime = System.currentTimeMillis();
        testSetPathEntries("lucene");
		testgenerateProjectChangePool(path);
        ntime = System.currentTimeMillis();
        System.out.println("time for done: " + (ntime - ctime)/1000 + "s");

        ctime = System.currentTimeMillis();
        testSetPathEntries("mahout");
        testgenerateProjectChangePool(path);
        ntime = System.currentTimeMillis();
        System.out.println("time for done: " + (ntime - ctime)/1000 + "s");

        ctime = System.currentTimeMillis();
        testSetPathEntries("hadoop");
		testgenerateProjectChangePool(path);
        ntime = System.currentTimeMillis();
        System.out.println("time for done: " + (ntime - ctime)/1000 + "s");

        ctime = System.currentTimeMillis();
        testSetPathEntries("pdfbox");
		testgenerateProjectChangePool(path);
        ntime = System.currentTimeMillis();
        System.out.println("time for done: " + (ntime - ctime)/1000 + "s");
    }

    // to test generate changePool by project
    public static void testgenerateProjectChangePool(String path) {
        ChangePoolGenerator cpg = new ChangePoolGenerator(new PLRTContextIdentifier());
        cpg.pool.setPoolDir(new File("/home/hjs/dldoldam/jinfix_database/pool/poolTest"));
        String[] classPathEntries = new String[] { classPathString };
        String[] sourcePathEntries = new String[] { sourcePathString };
        File beforePatchFile;
        File afterPatchFile;
        ArrayList<String> beforeList = new ArrayList<>();
        ArrayList<String> afterList = new ArrayList<>();

        for (File info : new File(path + "/" + targetProjectName).listFiles()) {
            if (info.isDirectory()) {
                int i = 0;
                beforeList.clear();
                afterList.clear();
                beforePatchFile = new File(info.getPath() + "/before");
                afterPatchFile = new File(info.getPath() + "/after");
                beforeList = new ArrayList<>(Arrays.asList(beforePatchFile.list()));
                afterList = new ArrayList<>(Arrays.asList(afterPatchFile.list()));
                try {
                    for (i = 0; i < afterList.size(); i++) {
                        if (afterList.get(i).endsWith(".java") && beforeList.contains(afterList.get(i))) {
                            File beforeFileName = new File(info.getPath() + "/before/" + afterList.get(i));
                            File afterFileName = new File(info.getPath() + "/after/" + afterList.get(i));
                            cpg.collect(info.getName() + ":" + info.getPath() + "/after/" + afterList.get(i),
                                    beforeFileName, afterFileName, classPathEntries, sourcePathEntries);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
            }
        }
        cpg.pool.storeTo(new File("/home/hjs/dldoldam/jinfix_database/pool/poolTest"), true);
        pool = cpg.pool;
        System.out.println(targetProjectName + " done");
    }

    public static void testSetPathEntries(String targetProject) {
        JinGenerateChangePool.targetProjectName = targetProject;
        String basicPath = "/home/hjs/dldoldam/jinfix_database/project/";
        if (targetProject.equals("collections")) {
            classPathString = basicPath + "/commons-collections" + "/target";
            sourcePathString = basicPath + "/commons-collections" + "/src";
        } else if (targetProject.equals("derby")) {
            classPathString = basicPath + targetProject + "/bin";
            sourcePathString = basicPath + targetProject + "/java";
        } else if (targetProject.equals("groovy")) {
            classPathString = basicPath + targetProject + "/bin";
            sourcePathString = basicPath + targetProject + "/src";
        } else if (targetProject.equals("hadoop")) {
            classPathString = "";
            sourcePathString = basicPath + "/hadoop-common";
        } else if (targetProject.equals("hama")) {
            classPathString = basicPath + targetProject + "/core/target";
            sourcePathString = basicPath + targetProject + "/core/src";
        } else if (targetProject.equals("ivy")) {
            classPathString = basicPath + "/ant-ivy" + "/bin/src";
            sourcePathString = basicPath + "/ant-ivy" + "/src";
        } else if (targetProject.equals("lucene")) {
            classPathString = basicPath + "/lucene-solr" + "/bin";
            sourcePathString = basicPath + "/lucene-solr" + "/lucene";
        } else if (targetProject.equals("mahout")) {
            classPathString = basicPath + targetProject + "core/target";
            sourcePathString = basicPath + targetProject + "core/src";
        } else if (targetProject.equals("pdfbox")) {
            classPathString = "";
            sourcePathString = basicPath + "/fontbox" + "/src";
        }
    }
}