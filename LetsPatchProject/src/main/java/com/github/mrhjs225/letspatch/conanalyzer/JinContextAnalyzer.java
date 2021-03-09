package com.github.mrhjs225.letspatch.conanalyzer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.thwak.confix.pool.Change;
import com.github.thwak.confix.pool.ChangePool;
import com.github.thwak.confix.pool.Context;
import com.github.thwak.confix.pool.ContextInfo;
import com.github.thwak.confix.pool.TestContextIdentifier;

public class JinContextAnalyzer {
    public static ChangePool pool;
    public static ArrayList<String> poolList;
    static int maxPoolLoad = 0;

    public JinContextAnalyzer(final int maxPoolLoad) {
        JinContextAnalyzer.pool = new ChangePool();
        JinContextAnalyzer.poolList = new ArrayList<>();
        JinContextAnalyzer.maxPoolLoad = maxPoolLoad;
    }

    /*
     * // for checking previous ConFix paper's context database public void
     * getStatisticsContext(final String poolPath) throws IOException{
     * loadChangePool(poolPath); final Iterator<Context> keyContext =
     * pool.contexts.keySet().iterator(); Context tempContext; ContextInfo
     * contextInfo; Change tempChange; int i = 0;
     * System.out.println("context size: " + pool.contexts.size()); BufferedWriter
     * bufWriter;
     * 
     * HashMap<String, Integer> contextAndStatement = new HashMap<>();
     * 
     * while (keyContext.hasNext()) { tempContext = keyContext.next(); contextInfo =
     * pool.contexts.get(tempContext); contextAndStatement.clear(); bufWriter = new
     * BufferedWriter(new FileWriter(new
     * File("/home/hjsvm/hjsaprvm/condatabase/statisticResult/contexttable/table" +
     * i + ".txt")));
     * 
     * ArrayList<String> totalRelatedStatement = new ArrayList<>(); int changeNum =
     * 0; HashMap<Integer, ArrayList<Integer>> contextTable = new HashMap<>();
     * 
     * for (int j = 0; j < contextInfo.getChanges().size(); j++) {
     * pool.loadChange(contextInfo.getChanges().get(j)); tempChange =
     * pool.changes.get(contextInfo.getChanges().get(j)); ArrayList<Integer>
     * changeVector = new ArrayList<>(); ArrayList<String> changeRelatedStatement =
     * tempChange.leftRelatedStatement; for (int k = 0; k <
     * tempChange.rightRelatedStatement.size(); k++) { if
     * (!changeRelatedStatement.contains(tempChange.rightRelatedStatement.get(k))) {
     * changeRelatedStatement.add(tempChange.rightRelatedStatement.get(k)); } }
     * 
     * for (int k = 0; k < totalRelatedStatement.size(); k++) { if
     * (changeRelatedStatement.contains(totalRelatedStatement.get(k))) {
     * changeVector.add(1); } else { changeVector.add(0); } }
     * 
     * for (int k = 0; k < changeRelatedStatement.size(); k++) { if
     * (!totalRelatedStatement.contains(changeRelatedStatement.get(k))) {
     * changeVector.add(1);
     * totalRelatedStatement.add(changeRelatedStatement.get(k)); for (int l = 0; l <
     * changeNum; l++) { contextTable.get(l).add(0); } } }
     * 
     * contextTable.put(changeNum, changeVector); changeNum++; }
     * 
     * StringBuilder contentString = new StringBuilder(); contentString.append(",");
     * for (int j = 0; j < changeNum - 1; j++) { contentString.append("change" + j +
     * ","); } contentString.append("change" + (changeNum - 1));
     * bufWriter.write(contentString.toString() + "\n");
     * 
     * for (int j = 0; j < totalRelatedStatement.size(); j++) { contentString = new
     * StringBuilder(); contentString.append("statement" + j + ","); for (int k = 0;
     * k < changeNum - 1; k++) { contentString.append(contextTable.get(k).get(j) +
     * ","); } contentString.append(contextTable.get(changeNum - 1).get(j));
     * bufWriter.write(contentString.toString() + "\n"); }
     * 
     * bufWriter.close(); i++; } }
     */
    public void getStatisticsContext(final String poolPath) throws IOException {
        loadChangePool(poolPath);
        final Iterator<Context> keyContext = pool.contexts.keySet().iterator();
        Context tempContext;
        ContextInfo contextInfo;
        Change tempChange;
        int i = 0;
        System.out.println("context size: " + pool.contexts.size());

        HashMap<String, Integer> contextAndStatement = new HashMap<>();

        while (keyContext.hasNext()) {
            tempContext = keyContext.next();
            contextInfo = pool.contexts.get(tempContext);
            System.out.println(contextInfo.getChanges().size());
            // System.out.println(tempContext.hashString);
        }

        // extract context <---> number of relatedstatement
        while (keyContext.hasNext()) {
            tempContext = keyContext.next();
            contextInfo = pool.contexts.get(tempContext);
            contextAndStatement.clear();
            int changeSize = 0;
            for (int j = 0; j < contextInfo.getChanges().size(); j++) {
                pool.loadChange(contextInfo.getChanges().get(j));
                tempChange = pool.changes.get(contextInfo.getChanges().get(j));
                if (!tempChange.type.equals("insert")) {
                    continue;
                }
                changeSize++;
                ArrayList<String> totalRelatedStatement = tempChange.leftRelatedStatement;
                for (int k = 0; k < tempChange.rightRelatedStatement.size(); k++) {
                    if (!totalRelatedStatement.contains(tempChange.rightRelatedStatement.get(k))) {
                        totalRelatedStatement.add(tempChange.rightRelatedStatement.get(k));
                    }
                }
                for (int k = 0; k < totalRelatedStatement.size(); k++) {
                    String relatedStatement = totalRelatedStatement.get(k);
                    if (contextAndStatement.containsKey(relatedStatement)) {
                        contextAndStatement.replace(relatedStatement, contextAndStatement.get(relatedStatement) + 1);
                    } else {
                        contextAndStatement.put(relatedStatement, 1);
                    }
                }
            }

            List<Map.Entry<String, Integer>> list = new LinkedList<>(contextAndStatement.entrySet());

            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    int comparison = (o1.getValue() - o2.getValue()) * -1;
                    return comparison == 0 ? o1.getKey().compareTo(o2.getKey()) : comparison;
                }
            });

            Iterator<Map.Entry<String, Integer>> iter = list.iterator();
            StringBuilder indexString = new StringBuilder();
            StringBuilder contentString = new StringBuilder();
            // contentString.append(tempContext.toString().replaceAll(",", "/"));
            // indexString.append("changenum,");
            contentString.append(i + ",");
            contentString.append(changeSize + "");
            int j = 0;
            // while (iter.hasNext()) {
            // Map.Entry<String, Integer> entry = iter.next();
            // // indexString.append(j + ",");
            // contentString.append(entry.getValue() + ",");
            // j++;
            // }
            // bufWriter.write(indexString.toString() + "\n");
            // bufWriter.write(contentString.toString() + "\n");

            i++;
        }
        // bufWriter.close();
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

        for (i = 0; i < -1; i++) {
            if (keyContext.hasNext()) {
                tempContext = keyContext.next();
                contextInfo = pool.contexts.get(tempContext);

                System.out.println("----------context----------");
                System.out.println("context: " + tempContext);
                System.out.println("freq: " + contextInfo.getFreq());
                System.out.println("listsize: " + contextInfo.getChanges().size());
                System.out.println("changeFreqsize: " + contextInfo.getChangeFreq().size());
                for (int j = 0; j < contextInfo.getChanges().size(); j++) {
                    System.out.println(" list" + j + ": " + contextInfo.getChanges().get(j)); // this integer using
                    // to find hashmapid in
                    // changepool
                    System.out.println(
                            " freq" + j + ": " + contextInfo.getChangeFreq().get(contextInfo.getChanges().get(j)));
                }
                System.out.println("changes content: " + contextInfo.getChanges().get(0));
                System.out.println("changehashid0: " + pool.hashIdMap.get(contextInfo.getChanges().get(0)));
                for (int j = 0; j < contextInfo.getChanges().size(); j++) {
                    pool.loadChange(contextInfo.getChanges().get(j));
                    tempChange = pool.changes.get(contextInfo.getChanges().get(j));
                    System.out.println("change" + j + ": " + tempChange);
                    for (int k = 0; k < tempChange.leftRelatedStatement.size(); k++) {
                        System.out.println("is left alive?: " + tempChange.leftRelatedStatement.get(k));
                    }
                    for (int k = 0; k < tempChange.rightRelatedStatement.size(); k++) {
                        System.out.println("is right alive?: " + tempChange.rightRelatedStatement.get(k));
                    }
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
        System.out.println("Pool:" + poolPath);
    }
}