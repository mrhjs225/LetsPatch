import os

projects = ['Chart', 'Closure', 'Lang', 'Math', 'Time']

for project in projects:
    buggy_list = []

    if project.lower() == 'chart':
        buggy_list = [1, 10, 11, 24]
    elif project.lower() == 'closure':
        buggy_list = [1, 11, 38, 92, 93, 109]
    elif project.lower() == 'lang':
        buggy_list = [6, 24, 26, 43, 51]
    elif project.lower() == 'math':
        buggy_list = [5, 30, 33, 59, 70, 75]
    elif project.lower() == 'time':
        buggy_list = [7, 19]

    for buggy_num in buggy_list:
        copy_dir = '../../' + project.lower() + '/' + project.lower() + '_' + str(buggy_num) + '_buggy/relatedstatement_result.txt'
        os.system('cp ' + copy_dir + ' ./' + project.lower() + str(buggy_num) + '_relatedstatement_result.txt')
