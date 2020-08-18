import os
from subprocess import Popen, PIPE

target_dir = './'

targets = [
    'collections',
    'derby',
    'groovy',
    'hadoop',
    'hama',
    'ivy',
    'lucene',
    'mahout',
    # 'pdfbox'
]

def checker(bfile, afile):
    cmd = 'diff {} {}'.format(bfile, afile)
    # p = Popen(cmd.split(), stdin=PIPE, stdout=PIPE)
    # out, err = p.communicate()
    # if err:
    #     print(err.decode('utf-8'))
    # out = out.decode('utf-8')
    # print(out)

tot_patches = 0
tot_patches_wo_test = 0

for target in targets:
    patches_path = '/home/hjs/dldoldam/confix2019result/pool/patch-list/{}.patches'.format(target)
    with open(patches_path) as f:
        lines = f.readlines()
    print(target, len(lines))
    tot_patches += len(lines)

    dict_ids = {}
    list_ids = []
    for line in lines:
        _id = line.split(',')[0].split('-')[1]
        if _id not in dict_ids.keys():
            dict_ids[_id] = 0
        else:
            dict_ids[_id] += 1

        dir_name = '{}_{}'.format(_id, dict_ids[_id])
        list_ids.append(dir_name)
    # list_ids.reverse()

    noa = 0
    noa_wo_test = 0
    nob = 0
    nob_wo_test = 0
    not_java = 0
    accum_nof = 0
    file_names = []
    file_names_wo_test = []
    patches_cnt = 0
    abs_cnt = 0
    for idx, _id in enumerate(list_ids):
        base_path = target_dir + target + '/' + _id + '/'
        # print(base_path)
        target_path = base_path + 'before/'
        nof = 0
        for path, dirs, files in os.walk(target_path):
            for f in files:
                if f.endswith('.java') and 'test' not in f.lower() :
                    before_f = target_path + f
                    after_f = base_path + 'after/' + f
                    if os.path.exists(after_f):
                        # print('\t' + f)
                        nof += 1
                        
                        file_names.append(f)
                        if 'Abstract' in f:
                            abs_cnt += 1
                    # print('before: ', )
                    # print('after : ', after_f)
                    # print('diff {} {}'.format(before_f, after_f))
                    checker(before_f, after_f)
                    # raw_input('-------------------------------------------------------------------------------------------------')
        accum_nof += nof
        if nof == 0:
            # print(idx + 1, target_path, nof, accum_nof, len(file_names), '<<<<<<<<<<')
            pass
        else:
            patches_cnt += 1
            # print(idx + 1, target_path, nof, accum_nof, len(file_names))
    
    # for f in sorted(list(set(file_names))):
    #     print('{:3d} {}'.format(file_names.count(f), f))
    print(patches_cnt, accum_nof, len(file_names), abs_cnt)
    tot_patches_wo_test += patches_cnt

print(tot_patches_wo_test, tot_patches)

    # for path, dirs, files in os.walk(target_dir + target):
        
    #     for _dir in dirs:
    #         if 'after' in path + '/' + _dir:
    #             nof = 0
    #             for ps, ds, fs in os.walk(path + '/' + _dir):
    #                 for f in fs:
    #                     if f.endswith('.java'):
    #                         nof += 1
    #             accum_nof += nof
    #             if nof == 0:
    #                 print(path + '/' + _dir, nof, accum_nof, '<<<<<<<<<<')
    #             else:
    #                 print(path + '/' + _dir, nof, accum_nof)
            # elif 'before' in path + '/' + _dir:
            #     nof = 0
            #     for ps, ds, fs in os.walk(path + '/' + _dir):
            #         for f in fs:
            #             if f.endswith('.java'):
            #                 nof += 1
            #     if nof == 0:
            #         print(path + '/' + _dir, nof, '<<<<<<<<<<')
            #     else:
            #         print(path + '/' + _dir, nof)
    #     for f in files:
    #         if f.endswith('.java'):
    #             if 'after' in path:
    #                 noa += 1
    #                 if f not in file_names:
    #                     file_names.append(f)

    #                 if 'test' not in f.lower():
    #                     noa_wo_test += 1
    #                     if f not in file_names_wo_test:
    #                         file_names_wo_test.append(f)
    #             elif 'before' in path:
    #                 nob += 1
    #                 if 'test' not in f.lower():
    #                     nob_wo_test += 1
    #         else:
    #             not_java += 1
            
    # print('{}\t{}\t{}\t{}'.format(noa, nob, not_java, len(file_names)))
    # print(noa_wo_test, nob_wo_test, len(file_names_wo_test))
