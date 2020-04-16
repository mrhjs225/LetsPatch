package jsmain;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import com.github.thwak.confix.pool.Change;
import com.github.thwak.confix.pool.ChangePool;
import com.github.thwak.confix.pool.Context;
import com.github.thwak.confix.pool.ContextInfo;


public class JinContextAnalyzer {
    public static ChangePool pool;
    public static ArrayList<String> poolList;
    static int maxPoolLoad = 0;

    public JinContextAnalyzer(final int maxPoolLoad) {
        JinContextAnalyzer.pool = new ChangePool();
        JinContextAnalyzer.poolList = new ArrayList<>();
        JinContextAnalyzer.maxPoolLoad = maxPoolLoad;
    }

    // for checking previous ConFix paper's context database
    public void checkingPreviousContext(final String poolPath) {
        loadChangePool(poolPath);
        System.out.println("--------------------------new pool------------------------");
        System.out.println("poolPath: " + poolPath);
        final Iterator<Context> keyContext = pool.contexts.keySet().iterator();
        Context tempContext;
        ContextInfo contextInfo;
        Change tempChange;
        int i = 0;
        System.out.println("context size: " + pool.contexts.size());
        for (i = 0; i < 5; i++) {
            if (keyContext.hasNext()) {
                tempContext = keyContext.next();
                contextInfo = pool.contexts.get(tempContext);

                System.out.println("----------context----------");
                System.out.println("context: " + tempContext);
                System.out.println("freq: " + contextInfo.getFreq());
                System.out.println("listsize: " + contextInfo.getChanges().size());
                System.out.println("changeFreqsize: " + contextInfo.getChangeFreq().size());
                for (int j = 0; j < contextInfo.getChanges().size(); j++) {
                    System.out.println("	list" + j + ": " + contextInfo.getChanges().get(j)); // this integer using
                                                                                                 // to find hashmapid in
                                                                                                 // changepool
                    System.out.println("	freq" + j + ": " + contextInfo.getChangeFreq().get(contextInfo.getChanges().get(j)));
                }
                System.out.println("changes content: " + contextInfo.getChanges().get(0));
                System.out.println("changehashid0: " + pool.hashIdMap.get(contextInfo.getChanges().get(0)));
                for (int j = 0; j < contextInfo.getChanges().size(); j++) {
                    pool.loadChange(contextInfo.getChanges().get(j));
                    tempChange = pool.changes.get(contextInfo.getChanges().get(j));
                    System.out.println("change" + j + ": " + tempChange);
                }
            } else {
                break;
            }
        }
    }

    // for extracting statistics information from previous ConFix context database
    public static void extractContextStatistics() {
        int fileNum = 0;

        for (final String poolPath : poolList) {
            loadChangePool(poolPath);
            final Iterator<Context> keyContext = pool.contexts.keySet().iterator();
            int number = 0;
            ContextInfo contextInfo;

            try {
                final BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream("/home/hjsvm/hjsaprvm/ConFix/pool/statistics" + fileNum + ".txt"));
                while (keyContext.hasNext()) {
                    contextInfo = pool.contexts.get(keyContext.next());
                    final String str = number + ", " + contextInfo.getChanges().size() + "\n";
                    bos.write(str.getBytes());
                    number++;
                }
                bos.close();
            } catch (final Exception e) {
                System.out.println(e.toString());
            }
            fileNum++;
        }
    }

    public static void extractContextCommit() {
        final String poolPath = poolList.get(1);
        loadChangePool(poolPath);
        final Iterator<Context> keyContext = pool.contexts.keySet().iterator();
        int number = 0;
        ContextInfo contextInfo;
        final ArrayList<Integer> checkList = new ArrayList<>();

        try {
            final BufferedReader bufReader = new BufferedReader(
                    new FileReader(new File("/home/hjsvm/hjsaprvm/ConFix/pool/plrtCheck.txt")));
            final BufferedWriter bufWriter = new BufferedWriter(
                    new FileWriter(new File("/home/hjsvm/hjsaprvm/ConFix/pool/plrtAnalyze.txt")));
            String line = "";

            while ((line = bufReader.readLine()) != null) {
                checkList.add(Integer.parseInt(line));
            }

            while (keyContext.hasNext()) {
                final Context tempContext = keyContext.next();
                System.out.println(tempContext.hashString);
                System.out.println(tempContext.hash);
                if (checkList.contains(number)) {
                    contextInfo = pool.contexts.get(tempContext);
                    for (int i = 0; i < 15; i += 4) {
                        final int changeInt = contextInfo.getChanges().get(i);
                        pool.loadChange(changeInt);
                        bufWriter.write(pool.hashIdMap.get(changeInt) + "\n");
                    }
                }
                number++;
            }
            bufReader.close();
            bufWriter.close();
        } catch (final Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void extractContextInformation() {
        final String poolPath = poolList.get(1);
        loadChangePool(poolPath);
        final Iterator<Context> keyContext = pool.contexts.keySet().iterator();
        int number = 0;
        ContextInfo contextInfo;
        final ArrayList<Integer> checkList = new ArrayList<>();

        try {
            final BufferedReader bufReader = new BufferedReader(
                    new FileReader(new File("/home/hjsvm/hjsaprvm/ConFix/pool/plrtCheck.txt")));
            final BufferedWriter bufWriter = new BufferedWriter(
                    new FileWriter(new File("/home/hjsvm/hjsaprvm/ConFix/pool/plrtAnalyze.txt")));
            String line = "";

            while ((line = bufReader.readLine()) != null) {
                checkList.add(Integer.parseInt(line));
            }

            while (keyContext.hasNext()) {
                final Context tempContext = keyContext.next();
                if (checkList.contains(number)) {
                    contextInfo = pool.contexts.get(tempContext);
                    bufWriter.write("-------------------------------------------\n");
                    bufWriter.write("Context: " + tempContext + "\n");
                    bufWriter.write("Number of change: " + contextInfo.getChanges().size() + "\n");

                    for (int i = 0; i < 10; i += 2) {
                        final int changeInt = contextInfo.getChanges().get(i);
                        pool.loadChange(changeInt);
                        bufWriter.write("Change's id number: " + changeInt + "\n");
                        bufWriter.write(pool.hashIdMap.get(changeInt) + "\n");
                    }
                }
                number++;
            }
            bufReader.close();
            bufWriter.close();
        } catch (final Exception e) {
            System.out.println(e.toString());
        }
    }

    private static void loadChangePool(final String poolPath) {
        pool = new ChangePool();
        pool.loadFrom(new File(poolPath));
        pool.maxLoadCount = maxPoolLoad;
        System.out.println("Pool:"+poolPath);
    }
}