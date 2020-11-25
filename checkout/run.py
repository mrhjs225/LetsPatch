import os
os.system('export DEFECTS4J_HOME=/home/hjs/dldoldam/defects4j')

project = ['Chart', 'Closure', 'Lang', 'Math', 'Time']
project = ['Chart']
basic_checkout_dir = '/home/hjs/dldoldam/gitclone/LetsPatch/checkout/'
jar_dir = '/home/hjs/dldoldam/gitclone/LetsPatch/LetsPatchProject/target/letspatch.jar'
for project_name in project:
    checkout_dir = basic_checkout_dir + project_name.lower()
    buggy_list = []

    if project_name.lower() == 'chart':
        buggy_list = [1, 11, 24]
        buggy_list = [11]
    elif project_name.lower() == 'closure':
        buggy_list = [1, 11, 38, 92, 109]
    elif project_name.lower() == 'lang':
        buggy_list = [6, 24, 26, 43, 51]
    elif project_name.lower() == 'math':
        buggy_list = [5, 30, 33, 59, 70, 75]
    elif project_name.lower() == 'time':
        buggy_list = [7, 19]

    for temp_buggy_num in buggy_list:
        final_checkout_dir = checkout_dir + '/' + project_name.lower() + '_' + \
            str(temp_buggy_num) + '_buggy'
        os.chdir(final_checkout_dir)
        os.system('java -Xmx4g -cp ../../las.jar:' + jar_dir +
                  ' -Duser.language=en -Duser.timezone=America/Los_Angeles com.github.mrhjs225.letspatch.main.LetsPatch')
