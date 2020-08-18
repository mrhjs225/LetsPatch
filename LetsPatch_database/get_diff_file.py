import os
import subprocess
from shutil import copy

dir_patch_list = '/home/hjs/dldoldam/confix2019result/pool/patch-list'
dir_projects = '/home/hjs/dldoldam/jinfix_database/project'
dir_output = '/home/hjs/dldoldam/jinfix_database/changedfile'

# selected_projects = ['lucene']

with open(dir_projects + '/error.log', 'w') as log:
    log.write('')

for path, dirs, files in os.walk(dir_patch_list):
    for f in files:
        # if f.endswith('.patches'):
        if f != 'hadoop.patches' and f != 'pdfbox.patches':
            print(path, f)

            with open(os.path.join(path, f)) as p_file:
                contents = p_file.read()

            patch_id_dict = {}

            lines = contents.split('\n')
            for ln, line in enumerate(lines):
                if len(line) == 0:
                    break

                patch_id, before_commit_id, after_commit_id = line.split(',')
                # print('{:4d} {} {} {}'.format(ln, patch_id, before_commit_id, after_commit_id))

                if patch_id not in patch_id_dict.keys():
                    patch_id_dict[patch_id] = 0
                else:
                    patch_id_dict[patch_id] += 1

                output_path = os.path.join(
                    dir_output, patch_id.split('-')[0].lower())
                if not os.path.exists(output_path):
                    # print('mkdir', output_path)
                    os.mkdir(output_path)

                output_path_with_id = os.path.join(output_path,
                                                   '{}_{}'.format(patch_id.split('-')[1],
                                                                  patch_id_dict[patch_id]))
                if not os.path.exists(output_path_with_id):
                    # print('mkdir', output_path_with_id)
                    os.mkdir(output_path_with_id)

                dir_before = os.path.join(output_path_with_id, 'before')
                dir_after = os.path.join(output_path_with_id, 'after')
                if not os.path.exists(dir_before):
                    os.mkdir(dir_before)
                if not os.path.exists(dir_after):
                    os.mkdir(dir_after)

                # handling project path exception
                project_path = patch_id.split('-')[0].lower()
                if 'LUCENE' in patch_id:
                    project_path += '-solr'
                if 'IVY' in patch_id:
                    project_path = 'ant-' + project_path
                if 'COLLECTIONS' in patch_id:
                    project_path = 'commons-' + project_path
                if 'HADOOP' in patch_id:
                    project_path += '-common'
                if 'PDFBOX' in patch_id:
                    project_path = 'fontbox'

                # change directory
                # print("before: %s"%os.getcwd())
                os.chdir(os.path.join(dir_projects, project_path))
                # print("after: %s"%os.getcwd())

                # checkout after commit id
                cmd = 'git checkout {}'.format(after_commit_id)
                os.system(cmd)

                # run git diff
                cmd = 'git diff-tree --no-commit-id --name-only -r {}'.format(
                    after_commit_id)
                # print(cmd)
                proc = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE)
                out, err = proc.communicate()
                if err:
                    print('[ERROR]\n', err.decode('urf-8'))

                out = out.decode('utf-8')
                target_files = []
                for t_file in out.split('\n')[:-1]:
                    # print(t_file)
                    target_files.append(t_file)
                    try:
                        copy(t_file, dir_after)
                    except:
                        # print('[ERROR-COPY]')
                        with open(dir_projects + '/error.log', 'a') as log:
                            log.write('{} Deleted file {}\n'.format(
                                after_commit_id, t_file))

                # checkout after commit id
                cmd = 'git checkout {}'.format(before_commit_id)
                os.system(cmd)

                for t_file in target_files:
                    try:
                        copy(t_file, dir_before)
                    except:
                        # print('[ERROR-COPY]')
                        with open(dir_projects + '/error.log', 'a') as log:
                            log.write('{} Inserted file {}\n'.format(
                                before_commit_id, t_file))

            # checkout master to restore normally
            os.system('git checkout master')
