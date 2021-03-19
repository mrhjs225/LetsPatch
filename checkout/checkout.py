import os

project = ['Chart', 'Closure', 'Lang', 'Math', 'Time']
project = ['Closure', 'Lang', 'Math', 'Time']
basic_checkout_dir = './'
# letspatch needs defects4j 1.1.0
defects4j_dir = '/root/defects4j'
print('test')
for project_name in project:
    checkout_dir = basic_checkout_dir + project_name.lower()
    buggy_list = []

    if project_name.lower() == 'chart':
        buggy_list = [1, 10, 11, 24]
    elif project_name.lower() == 'closure':
        buggy_list = [1, 11, 38, 92, 93, 109]
    elif project_name.lower() == 'lang':
        buggy_list = [6, 24, 26, 43, 51]
    elif project_name.lower() == 'math':
        buggy_list = [5, 30 ,33, 59, 70, 75]
    elif project_name.lower() == 'time':
        buggy_list = [7, 19]

    for temp_buggy_num in buggy_list:
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
