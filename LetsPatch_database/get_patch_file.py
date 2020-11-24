import os
import subprocess
from shutil import copy

dir_patch_list = '/home/hjs/dldoldam/gitclone/LetsPatch/LetsPatch_database/pool/patch-list'
dir_projects = '/home/hjs/dldoldam/gitclone/LetsPatch/LetsPatch_database/project'
dir_output = '/home/hjs/dldoldam/gitclone/LetsPatch/LetsPatch_database/changedfile'
fatal = open(
    '/home/hjs/dldoldam/gitclone/LetsPatch/LetsPatch_database/jinseoktest.txt', 'w')
# selected_projects = ['lucene']
patch_file_list = ['collections.patches', 'derby.patches', 'groovy.patches', 'hadoop.patches',
                   'hama.patches', 'ivy.patches', 'lucene.patches', 'mahout.patches']

with open(dir_projects + '/error.log', 'w') as log:
    log.write('')

for path, dirs, files in os.walk(dir_patch_list):
    for f in files:
        # if 'collections' not in f:
        #     continue
        # if f.endswith('.patches'):
        num_fatal = 0
        # if f != 'hadoop.patches' and f != 'pdfbox.patches':
        if f in patch_file_list:

            with open(os.path.join(path, f)) as p_file:
                contents = p_file.read()

            patch_id_dict = {}

            patch_cnt = 0
            changed_file_cnt = 0

            lines = contents.split('\n')
            for ln, line in enumerate(lines):
                if len(line) == 0:
                    break

                patch_id, before_commit_id, after_commit_id = line.split(',')
                print('{:4d} {} {} {}'.format(ln + 1, patch_id,
                                              before_commit_id, after_commit_id))

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
                    project_path += '-jbig2'

                # change directory
                # print("before: %s"%os.getcwd())
                os.chdir(os.path.join(dir_projects, project_path))
                # print("after: %s"%os.getcwd())

                # checkout after commit id
                cmd = 'git reset --hard {}'.format(after_commit_id)
                # os.system(cmd)

                test_proc = subprocess.Popen(
                    cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                out, err = test_proc.communicate()
                if err:
                    print('[ERROR] {}'.format(err))
                    if b'fatal' in err:
                        num_fatal += 1
                    if b'borting' in err:
                        raw_input('What?!!!!')
                # print(out.decode('utf-8'))
                # output = test_proc.err.readline()
                # print('jinseok~~~0', output)
                # print('here??????????????1')

                # print('here??????????????2')
                # print('jinseok~~~~~1', out)
                # print('jinseok~~~~~2', err)

                # run git diff
                cmd = 'git diff-tree --no-commit-id --name-only -r {} {}'.format(
                    before_commit_id, after_commit_id)

                proc = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE)
                out, err = proc.communicate()
                if err:
                    print('[ERROR]\n', err.decode('urf-8'))
                    if 'abort' in err.decode('urf-8'):
                        raw_input('Hi?!')

                out = out.decode('utf-8')
                print(out)
                if 'abort' in out:
                    raw_input('Hey?!')

                print('--------------------------------------')
                target_files = []
                _changed_file = 0
                for t_file in out.split('\n'):
                    if not t_file.endswith('.java'):
                        continue
                    elif 'test' in t_file or 'Test' in t_file:
                        continue
                    else:
                        print('\t' + t_file)
                        target_files.append(t_file)
                        _changed_file += 1
                        try:
                            copy(t_file, dir_after)
                        except:
                            # print('[ERROR-COPY]')
                            with open(dir_projects + '/error.log', 'a') as log:
                                log.write('{} Deleted file {}\n'.format(
                                    after_commit_id, t_file))
                changed_file_cnt += _changed_file
                if _changed_file > 0:
                    patch_cnt += 1
                # checkout after commit id
                cmd = 'git reset --hard {}'.format(before_commit_id)
                os.system(cmd)

                for t_file in target_files:
                    try:
                        copy(t_file, dir_before)
                    except:
                        # print('[ERROR-COPY]')
                        with open(dir_projects + '/error.log', 'a') as log:
                            log.write('{} Inserted file {}\n'.format(
                                before_commit_id, t_file))
                print('========================================')

            # checkout master to restore normally
            # print('{} {}'.format(patch_cnt, changed_file_cnt))
            os.system('git reset --hard HEAD')
            fatal.write(str(num_fatal) + '\n')
fatal.close()
