import os
# run patch test by all of bug in defects4j
# project = ['Chart', 'Closure', 'Lang', 'Math', 'Mockito', 'Time']
project = ['Closure']
basic_checkout_dir = '/home/hjs/dldoldam/checkout/'

for project_name in project:
    checkout_dir = basic_checkout_dir + project_name.lower()
    buggy_num = 0

    if project_name.lower() == 'chart':
        buggy_num = 26
    elif project_name.lower() == 'closure':
        buggy_num = 133
    elif project_name.lower() == 'lang':
        buggy_num = 65
    elif project_name.lower() == 'math':
        buggy_num = 106
    elif project_name.lower() == 'mockito':
        buggy_num = 38
    elif project_name.lower() == 'time':
        buggy_num = 27

    for temp_buggy_num in [25, 132, 133, 96, 97, 98, 99]:
        final_checkout_dir = checkout_dir + '/' + project_name.lower() + '_' + str(temp_buggy_num) + '_buggy'
        os.chdir(final_checkout_dir)
        os.system('/usr/bin/java -Xmx4g -cp ../../las.jar:/home/hjs/dldoldam/checkout/jarfolder/confix-0.0.1-SNAPSHOT-jar-with-dependencies_con_closure.jar -Duser.language=en -Duser.timezone=America/Los_Angeles com.github.thwak.confix.main.ConFix')