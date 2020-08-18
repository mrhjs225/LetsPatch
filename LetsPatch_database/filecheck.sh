#! /bin/bash
project=ivy
cd ant-ivy
while read commitId; do
    git show ${commitId} > /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}/${commitId}.txt
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}

project=collections
cd ../commons-collections
while read commitId; do
    git show ${commitId} > /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}/${commitId}.txt
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}

project=derby
cd ../derby
while read commitId; do
    git show ${commitId} > /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}/${commitId}.txt
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}

project=groovy
cd ../groovy
while read commitId; do
    git show ${commitId} > /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}/${commitId}.txt
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}

project=hadoop
cd ../hadoop
while read commitId; do
    git show ${commitId} > /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}/${commitId}.txt
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}

project=hama
cd ../hama
while read commitId; do
    git show ${commitId} > /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}/${commitId}.txt
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}

project=lucene
cd ../lucene-solr
while read commitId; do
    git show ${commitId} > /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}/${commitId}.txt
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}

project=mahout
cd ../mahout
while read commitId; do
    git show ${commitId} > /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}/${commitId}.txt
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}

project=pdfbox
cd ../pdfbox
while read commitId; do
    git show ${commitId} > /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}/${commitId}.txt
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}