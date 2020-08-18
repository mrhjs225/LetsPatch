import os

import pandas as pd

from pyclustering.cluster.xmeans import xmeans
from pyclustering.cluster.center_initializer import kmeans_plusplus_initializer
from pyclustering.utils import read_sample


tables_dict = {}
tables_dir = 'data/contexttable'
cnt = 1
cluster_groups = {}
out = ''
for path, dirs, files in os.walk(tables_dir):
    for f_table in sorted(files):
        if '.data' in f_table:
            continue
        _id = int(f_table.replace('table', '').replace('.txt', ''))
        lines = open(os.path.join(path, f_table)).readlines()
        cols = ['RST'] + lines[0].split(',')[1:]

        data = []
        rows = lines[1:]
        for r in rows:
            row = r.replace('\n', '').split(',')
            rst = int(row[0].replace('statement', ''))
            remains = [int(x) for x in row[1:]]
            row = [rst] + remains
            data.append(row)
        tables_dict[_id] = pd.DataFrame(data, columns=cols)
        
        sum_zeros = 0
        centers = []
        if len(rows) > 1:
            vector_path = os.path.join(path, 'context{}.data'.format(_id))
            with open(vector_path, 'w') as f:
                for c in cols[1:]:
                    vector = list(tables_dict[_id][c])
                    f.write(' '.join([str(x) for x in vector]) + '\n')
            
            sample = read_sample(vector_path)

            amount_initial_centers = 1
            initial_centers = kmeans_plusplus_initializer(sample, amount_initial_centers).initialize()
            # Create instance of X-Means algorithm. The algorithm will start analysis from 2 clusters, the maximum
            # number of clusters that can be allocated is 20.
            xmeans_instance = xmeans(sample, initial_centers, 20)
            xmeans_instance.process()
            # Extract clustering results: clusters and their centers
            clusters = xmeans_instance.get_clusters()
            centers = xmeans_instance.get_centers()
            if len(centers) > 1:
                cnt += 1
                if len(centers) not in cluster_groups.keys():
                    cluster_groups[len(centers)] = 1
                else:
                    cluster_groups[len(centers)] += 1
                # print(clusters)
                # for center in centers:
                #     print(center)
                # Print total sum of metric errors
                # print("Total WCE:", xmeans_instance.get_total_wce())
            # print('===========================\n\n')
        print('{},{},{}'.format(_id, len(centers), sum_zeros))
# print(cnt)
# print(cluster_groups)
