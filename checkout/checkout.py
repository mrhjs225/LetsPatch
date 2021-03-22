import os

project = ['Chart', 'Closure', 'Lang', 'Math', 'Time']
project = ['Chart']
basic_checkout_dir = './'
# letspatch needs defects4j 1.1.0
for project_name in project:
    checkout_dir = basic_checkout_dir + project_name.lower()
    buggy_num = []

    if project_name.lower() == 'chart':
        buggy_num = [1, 10, 11, 24]
        buggy_num = [1, 10]
    elif project_name.lower() == 'closure':
        buggy_num = [133]
    elif project_name.lower() == 'lang':
        buggy_num = [65]
    elif project_name.lower() == 'math':
        buggy_num = [106]
    elif project_name.lower() == 'time':
        buggy_num = [27]

    for temp_buggy_num in buggy_num:
        final_checkout_dir = checkout_dir + '/' + project_name.lower() + '_' + \
            str(temp_buggy_num) + '_buggy'
        if not os.path.exists(final_checkout_dir):
            os.mkdir(final_checkout_dir)
        cmd = 'defects4j checkout -p ' + project_name + ' -v ' + \
            str(temp_buggy_num) + 'b -w ' + final_checkout_dir
        os.system(cmd)
        os.chdir(final_checkout_dir)
        os.system('defects4j compile')
        cmd = '../../config.sh . ' + project_name.lower() + ' ' + str(temp_buggy_num) + \
            ' ../../result/' + project_name.lower() + 'Result.txt'
        os.system(cmd)
        cmd = 'cp ../../coverage/' + project_name.lower() + '/' + \
            project_name.lower() + str(temp_buggy_num) + \
            'b/coverage-info.obj .'
        os.system(cmd)
        os.chdir('../../')
