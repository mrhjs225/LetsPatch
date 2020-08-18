import os

project = ['Chart', 'Closure', 'Lang', 'Math', 'Time']
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
    elif project_name.lower() == 'time':
        buggy_num = 27

    for temp_buggy_num in range(1, buggy_num + 1):
        final_checkout_dir = checkout_dir + '/' + project_name.lower() + '_' + str(temp_buggy_num) + '_buggy'
        if not os.path.exists(final_checkout_dir):
            os.mkdir(final_checkout_dir)
        cmd = 'defects4j checkout -p ' + project_name + ' -v ' + str(temp_buggy_num) + 'b -w ' + final_checkout_dir
        os.system(cmd)
        os.chdir(final_checkout_dir)
        os.system('defects4j compile')

        cmd = '../../config.sh . ' + project_name.lower() + ' ' + str(temp_buggy_num) + ' ' + basic_checkout_dir + 'result/' + project_name.lower() + 'Result.txt'
        os.system(cmd)
        cmd = 'cp /home/hjs/dldoldam/confix2019result/coverage/' + project_name.lower() + '/' + project_name.lower() + str(temp_buggy_num) + 'b/coverage-info.obj ' + final_checkout_dir
        os.system(cmd)

    os.chdir('/home/hjs/dldoldam/checkout')
