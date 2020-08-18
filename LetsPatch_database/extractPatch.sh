#! /bin/bash

# do not use this file!!!!!!!!!!!!!!!!!!!!!!!!!
# this file is odd, please use python file for extract changed file !!!!!
project=collections
cd commons-collections
while read line; do
    patchId=$(echo $line | cut -d ',' -f1)
    beforeCommitId=$(echo $line | cut -d ',' -f2)
    afterCommitId=$(echo $line | cut -d ',' -f3)
    echo $afterCommitId
    beforeCommitResult=$(git show $beforeCommitId)
    afterCommitResult=$(git show $afterCommitId)
    beforeCommitLength=$(expr length "${beforeCommitResult}")
    afterCommitLength=$(expr length "${afterCommitResult}")
    if [ $beforeCommitLength -ne 0 -a $afterCommitLength -ne 0 ];then
        if [ ! -d /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId} ];then
            mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}
            mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
            mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
            echo ${patchId} > /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/${patchId}.txt
        fi
        git checkout $afterCommitId
        dir=$(git diff-tree --no-commit-id --name-only -r $afterCommitId)
        for singleDir in $dir; do
            cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
        done

        git checkout $beforeCommitId
        for singleDir in $dir; do
            cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
        done
    fi
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}
git checkout master

project=derby
cd ../derby
while read line; do
    patchId=$(echo $line | cut -d ',' -f1)
    beforeCommitId=$(echo $line | cut -d ',' -f2)
    afterCommitId=$(echo $line | cut -d ',' -f3)
    echo $afterCommitId
    beforeCommitResult=$(git show $beforeCommitId)
    afterCommitResult=$(git show $afterCommitId)
    beforeCommitLength=$(expr length "${beforeCommitResult}")
    afterCommitLength=$(expr length "${afterCommitResult}")
    if [ $beforeCommitLength -ne 0 -a $afterCommitLength -ne 0 ];then
        if [ ! -d /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId} ];then
            mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}
            mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
            mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
            echo ${patchId} > /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/${patchId}.txt
        fi
        git checkout $afterCommitId
        dir=$(git diff-tree --no-commit-id --name-only -r $afterCommitId)
        for singleDir in $dir; do
            cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
        done

        git checkout $beforeCommitId
        for singleDir in $dir; do
            cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
        done
    fi
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}
git checkout master

project=groovy
cd groovy
while read line; do
    patchId=$(echo $line | cut -d ',' -f1)
    beforeCommitId=$(echo $line | cut -d ',' -f2)
    afterCommitId=$(echo $line | cut -d ',' -f3)
    echo $afterCommitId
    beforeCommitResult=$(git show $beforeCommitId)
    afterCommitResult=$(git show $afterCommitId)
    beforeCommitLength=$(expr length "${beforeCommitResult}")
    afterCommitLength=$(expr length "${afterCommitResult}")
    if [ $beforeCommitLength -ne 0 -a $afterCommitLength -ne 0 ];then
        if [ ! -d /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId} ];then
            mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}
            mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
            mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
            echo ${patchId} > /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/${patchId}.txt
        fi
        git checkout $afterCommitId
        dir=$(git diff-tree --no-commit-id --name-only -r $afterCommitId)
        for singleDir in $dir; do
            cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
        done

        git checkout $beforeCommitId
        for singleDir in $dir; do
            cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
        done
    fi
done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
echo ${project}
git checkout master

# project=hadoop
# cd ../hadoop
# while read line; do
#     patchId=$(echo $line | cut -d ',' -f1)
#     beforeCommitId=$(echo $line | cut -d ',' -f2)
#     afterCommitId=$(echo $line | cut -d ',' -f3)
#     echo $afterCommitId
#     beforeCommitResult=$(git show $beforeCommitId)
#     afterCommitResult=$(git show $afterCommitId)
#     beforeCommitLength=$(expr length "${beforeCommitResult}")
#     afterCommitLength=$(expr length "${afterCommitResult}")
#     if [ $beforeCommitLength -ne 0 -a $afterCommitLength -ne 0 ];then
#         if [ ! -d /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId} ];then
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#             echo ${patchId} > /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/${patchId}.txt
#         fi
#         git checkout $afterCommitId
#         dir=$(git diff-tree --no-commit-id --name-only -r $afterCommitId)
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#         done

#         git checkout $beforeCommitId
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#         done
#     fi
# done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
# echo ${project}
# git checkout master

# project=ivy
# cd ../ant-ivy
# while read line; do
#     patchId=$(echo $line | cut -d ',' -f1)
#     beforeCommitId=$(echo $line | cut -d ',' -f2)
#     afterCommitId=$(echo $line | cut -d ',' -f3)
#     echo $afterCommitId
#     beforeCommitResult=$(git show $beforeCommitId)
#     afterCommitResult=$(git show $afterCommitId)
#     beforeCommitLength=$(expr length "${beforeCommitResult}")
#     afterCommitLength=$(expr length "${afterCommitResult}")
#     if [ $beforeCommitLength -ne 0 -a $afterCommitLength -ne 0 ];then
#         if [ ! -d /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId} ];then
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#             echo ${patchId} > /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/${patchId}.txt
#         fi
#         git checkout $afterCommitId
#         dir=$(git diff-tree --no-commit-id --name-only -r $afterCommitId)
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#         done

#         git checkout $beforeCommitId
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#         done
#     fi
# done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
# echo ${project}
# git checkout master

# project=hama
# cd ../hama
# while read line; do
#     patchId=$(echo $line | cut -d ',' -f1)
#     beforeCommitId=$(echo $line | cut -d ',' -f2)
#     afterCommitId=$(echo $line | cut -d ',' -f3)
#     echo $afterCommitId
#     beforeCommitResult=$(git show $beforeCommitId)
#     afterCommitResult=$(git show $afterCommitId)
#     beforeCommitLength=$(expr length "${beforeCommitResult}")
#     afterCommitLength=$(expr length "${afterCommitResult}")
#     if [ $beforeCommitLength -ne 0 -a $afterCommitLength -ne 0 ];then
#         if [ ! -d /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId} ];then
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#             echo ${patchId} > /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/${patchId}.txt
#         fi
#         git checkout $afterCommitId
#         dir=$(git diff-tree --no-commit-id --name-only -r $afterCommitId)
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#         done

#         git checkout $beforeCommitId
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#         done
#     fi
# done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
# echo ${project}
# git checkout master

# project=lucene
# cd ../lucene-solr
# while read line; do
#     patchId=$(echo $line | cut -d ',' -f1)
#     beforeCommitId=$(echo $line | cut -d ',' -f2)
#     afterCommitId=$(echo $line | cut -d ',' -f3)
#     echo $afterCommitId
#     beforeCommitResult=$(git show $beforeCommitId)
#     afterCommitResult=$(git show $afterCommitId)
#     beforeCommitLength=$(expr length "${beforeCommitResult}")
#     afterCommitLength=$(expr length "${afterCommitResult}")
#     if [ $beforeCommitLength -ne 0 -a $afterCommitLength -ne 0 ];then
#         if [ ! -d /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId} ];then
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#             echo ${patchId} > /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/${patchId}.txt
#         fi
#         git checkout $afterCommitId
#         dir=$(git diff-tree --no-commit-id --name-only -r $afterCommitId)
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#         done

#         git checkout $beforeCommitId
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#         done
#     fi
# done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
# echo ${project}
# git checkout master

# project=mahout
# cd ../mahout
# while read line; do
#     patchId=$(echo $line | cut -d ',' -f1)
#     beforeCommitId=$(echo $line | cut -d ',' -f2)
#     afterCommitId=$(echo $line | cut -d ',' -f3)
#     echo $afterCommitId
#     beforeCommitResult=$(git show $beforeCommitId)
#     afterCommitResult=$(git show $afterCommitId)
#     beforeCommitLength=$(expr length "${beforeCommitResult}")
#     afterCommitLength=$(expr length "${afterCommitResult}")
#     if [ $beforeCommitLength -ne 0 -a $afterCommitLength -ne 0 ];then
#         if [ ! -d /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId} ];then
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#             echo ${patchId} > /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/${patchId}.txt
#         fi
#         git checkout $afterCommitId
#         dir=$(git diff-tree --no-commit-id --name-only -r $afterCommitId)
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#         done

#         git checkout $beforeCommitId
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#         done
#     fi
# done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
# echo ${project}
# git checkout master

# project=pdfbox
# cd ../pdfbox
# while read line; do
#     patchId=$(echo $line | cut -d ',' -f1)
#     beforeCommitId=$(echo $line | cut -d ',' -f2)
#     afterCommitId=$(echo $line | cut -d ',' -f3)
#     echo $afterCommitId
#     beforeCommitResult=$(git show $beforeCommitId)
#     afterCommitResult=$(git show $afterCommitId)
#     beforeCommitLength=$(expr length "${beforeCommitResult}")
#     afterCommitLength=$(expr length "${afterCommitResult}")
#     if [ $beforeCommitLength -ne 0 -a $afterCommitLength -ne 0 ];then
#         if [ ! -d /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId} ];then
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#             mkdir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#             echo ${patchId} > /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/${patchId}.txt
#         fi
#         git checkout $afterCommitId
#         dir=$(git diff-tree --no-commit-id --name-only -r $afterCommitId)
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/afterCommit
#         done

#         git checkout $beforeCommitId
#         for singleDir in $dir; do
#             cp $singleDir /home/hjsvm/hjsaprvm/condatabase/commitfile/${project}/${afterCommitId}/beforeCommit
#         done
#     fi
# done < /home/hjsvm/hjsaprvm/confix2019result/pool/patch-list/${project}.txt
# echo ${project}
# git checkout master
