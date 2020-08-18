import os

project = ['Closure']
basic_checkout_dir = '/home/hjs/dldoldam/checkout/'

for project_name in project:
    checkout_dir = basic_checkout_dir + project_name.lower()
    buggy_list = []

    if project_name.lower() == 'chart':
        buggy_list = [1, 10, 11, 24]
    elif project_name.lower() == 'closure':
        buggy_list = [1, 11, 14, 38, 73, 92, 93, 109, 123]
    elif project_name.lower() == 'lang':
        buggy_list = [6, 24, 26, 43, 51, 57]
    elif project_name.lower() == 'math':
        buggy_list = [5, 20, 30, 33, 34, 59, 70, 75]
    elif project_name.lower() == 'time':
        buggy_list = [7, 19]

    for temp_buggy_num in buggy_list:
        final_checkout_dir = checkout_dir + '/' + project_name.lower() + '_' + str(temp_buggy_num) + '_buggy'
        os.chdir(final_checkout_dir)
        os.system('/usr/bin/java -Xmx4g -cp ../../las.jar:/home/hjs/dldoldam/checkout/jarfolder/confix-0.0.1-SNAPSHOT-jar-with-dependencies_final_closure.jar -Duser.language=en -Duser.timezone=America/Los_Angeles com.github.thwak.confix.main.ConFix')
