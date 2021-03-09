import os

project_names = ['java-design-patterns', 'elasticsearch', 'spring-boot', 'interviews', 'RxJava', 'guava',
                 'okhttp', 'retrofit', 'Java', 'MPAndroidChart', 'zxing', 'leakcanary', 'jadx', 'selenium',
                 'dbeaver', 'libgdx', 'generator-jhipster', 'spring-cloud-alibaba', 'ExoPlayer', 'CNTK', 'jenkins',
                 'flatbuffers', 'redisson', 'flink', 'Sentinel', 'mybatis-3', 'cat', 'ray', 'Android-CleanArchitecture',
                 'HikariCP', 'graal', 'java8-tutorial']
project_names = ['cat']
cmd = 'git log --grep "patch" --grep "repair" --grep "fix" --fix "bug" --pretty=format:"%H" > commitid.txt'
try:
    for project in project_names:
        dir_str = '/home/hjs/dldoldam/gitclone/LetsPatch/LetsPatch_database/temp_project/' + project
        os.chdir(dir_str)
        result = ''

        f = open('commitid.txt', 'r')
        lines = f.readlines()
        for line in lines:
            line = line.rstrip('\n')
            temp = os.popen('git log ' + line +
                            ' -2 --pretty=format:"%H"').read()
            temp_list = temp.split('\n')
            # print(temp1[0])
            result += temp_list[0] + ',' + temp_list[1] + '\n'
        f.close()
        f = open('commitId.txt', 'w')
        f.write(result)
        f.close()
except:
    print("exception")
