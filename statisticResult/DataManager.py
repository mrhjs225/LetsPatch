import os
import pickle
import sys

import pandas as pd

sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))


class DataManager(object):
    auts = [
        'net.fred.feedex',
        'eu.siacs.conversations',
        'at.bitfire.davdroid',
        'org.y20k.transistor',
        'com.fsck.k9',
        'com.manichord.mgit',
        'com.danielme.muspyforandroid',
        'jp.redmine.redmineclient',
        'com.owncloud.android',
        'com.xargsgrep.portknocker',
        'org.proninyaroslav.libretorrent',
        'org.connectbot',
        'com.einmalfel.podlisten',
        'net.sourceforge.servestream',
    ]
    options = [
        ('SWT_1', '-Act-RE-'),
        ('SWT_4', '-Act-RE-'),
        ('SWT_4', '-Act-RE-TI-'),
        ('SWT_1', '-Int-RE-'),
        ('SWT_1', '-Cnd-RE-'),
        ('SWT_4', '-Int-RE-'),
        ('SWT_4', '-Cnd-RE-'),
        ('SWT_4', '-Int-RE-TI-'),
        ('SWT_4', '-Cnd-RE-TI-'),
        ('SWT_4', '-Cnd-RE-TI-CAG-'),
        ('DWT_4', '-Cnd-RE-TI-CAG-')
    ]
    
    def __init__(self, output_path):
        super(DataManager, self).__init__()

        self.columns = None
        self.retrieved_packages = 0
        self.histories = {}
        self.raw_df = self._set_raw_data_frame()
        self._get_raw_data_frame(output_path)

    def get_options(self):
        return self._option_ordering(list(set(self.raw_df['options'])))

    def get_history_options(self):
        return sorted(self.histories.keys())

    def get_history(self, option):
        return self.histories[option]

    def _option_ordering(self, options):
        order = []
        for opt in self.options:
            wait, opts = opt
            for opt_data in options:
                if wait in opt_data and opts in opt_data \
                        and len(opt_data) <= len(wait + '0' + opts) + 4:
                    order.append(opt_data)

            for ord in order:
                if ord in options:
                    options.remove(ord)

        return order

    def get_overview(self, option=None):
        legends = [
            'options',
            'run',

            'avg-steps',
            'secs/step',

            'max-monkey',
            'avg-monkey',
            'min-monkey',

            'max-jhe',
            'avg-jhe',
            'min-jhe',
        ]

        ovv_df = pd.DataFrame(columns=legends)

        ovv_dict = {
            'vs.monkey': {'x': [], 'y': []},
            'vs.JHE': {'x': [], 'y': []}
        }

        options = self._option_ordering(self.get_options())
        for opt in options:
            cov_df, delta_df, _ = self.get_activity_coverage_data(opt)

            monkey_data = delta_df['avg-monkey'].tolist()
            # monkey_data += delta_df['max-monkey'].tolist()
            # monkey_data += delta_df['min-monkey'].tolist()

            for d in monkey_data:
                ovv_dict['vs.monkey']['x'].append(opt)
                ovv_dict['vs.monkey']['y'].append(d)

            jhe_data = delta_df['avg-jhe'].tolist()
            # jhe_data += delta_df['max-jhe'].tolist()
            # jhe_data += delta_df['min-jhe'].tolist()

            for d in jhe_data:
                ovv_dict['vs.JHE']['x'].append(opt)
                ovv_dict['vs.JHE']['y'].append(d)
            row_data = [
                opt,
                round(cov_df['duration_end'].mean(), 3),
                round(cov_df['steps_end'].mean(), 3),
                round((3600. / cov_df['steps_end'].mean()) - int(opt.split('-')[0].split('_')[1]), 3),

                round(delta_df['max-monkey'].max(), 3),
                round(delta_df['avg-monkey'].mean(), 3),
                round(delta_df['min-monkey'].min(), 3),

                round(delta_df['max-jhe'].max(), 3),
                round(delta_df['avg-jhe'].mean(), 3),
                round(delta_df['min-jhe'].min(), 3),
            ]

            row = ovv_df.index.max() + 1
            if len(ovv_df.index) == 0:
                row = 1
            ovv_df.loc[row] = row_data

        return ovv_dict, ovv_df

    def get_activity_coverage_data(self, options):
        cov_df = self._get_base_activity_coverage_data(options)
        
        delta_df = self._get_delta_activity_coverage_data(cov_df)
        delta_df = delta_df.set_index('package_name')
        
        hm_zero_pos = self._get_heatmap_zero_pos(delta_df)

        return cov_df, delta_df, hm_zero_pos

    def _get_base_activity_coverage_data(self, options):
        cov_data = self._get_filtered_cleaned_df(options)

        legends = [
            'package_name',
            'Monkey',
            'JHD',
            'JHE',
            'CoVerCoDy-max',
            'CoVerCoDy-avg',
            'CoVerCoDy-min',
            '#TotalActivity',
            'duration_end',
            'steps_end',
        ]
        cov_df = pd.DataFrame(columns=legends)

        for package in list(set(cov_data['package_name'])):
            pack_table = cov_data[cov_data['package_name'].isin([package])]
            row_data = [
                package,
                pack_table['monkey'].max(),
                pack_table['JHD'].max(),
                pack_table['JHE'].max(),
                pack_table['cov_act'].max(),
                round(pack_table['cov_act'].mean(), 3),
                pack_table['cov_act'].min(),
                pack_table['total_act'].max(),
                round(pack_table['duration_end'].sum() / 3600, 3),
                round(pack_table['step_end'].sum() / len(pack_table.index), 3),
            ]
            
            row = cov_df.index.max() + 1
            if len(cov_df.index) == 0:
                row = 1
            cov_df.loc[row] = row_data

        return self._package_ordering(cov_df)

    def _get_delta_activity_coverage_data(self, cov_df):

        legends = [
            'package_name',
            'max-monkey',
            'avg-monkey',
            'min-monkey',
            'max-jhe',
            'avg-jhe',
            'min-jhe',
        ]
        delta_df = pd.DataFrame(columns=legends)

        for package in list(set(cov_df['package_name'])):
            pack_table = cov_df[cov_df['package_name'].isin([package])]
            
            row = pack_table.index[0]

            monkey = pack_table['Monkey'][row]
            jhe = pack_table['JHE'][row]
            total = pack_table['#TotalActivity'][row]

            _max = pack_table['CoVerCoDy-max'][row]
            _avg = pack_table['CoVerCoDy-avg'][row]
            _min = pack_table['CoVerCoDy-min'][row]
            # get max vs monkey
            row_data = [
                package,
                100. * (_max - monkey) / total,
                100. * (_avg - monkey) / total,
                100. * (_min - monkey) / total,
                100. * (_max - jhe) / total,
                100. * (_avg - jhe) / total,
                100. * (_min - jhe) / total,

            ]
            
            row = delta_df.index.max() + 1
            if len(delta_df.index) == 0:
                row = 1
            delta_df.loc[row] = row_data

        return self._package_ordering(delta_df)

    def _get_heatmap_zero_pos(self, delta_df):
        hm_max = max(list(delta_df.max()))
        hm_min = min(list(delta_df.min()))
        hm_total = abs(hm_max) + abs(hm_min)
        return abs(hm_min) / hm_total

    def _get_filtered_cleaned_df(self, options):
        df = self.raw_df[self.raw_df['options'].isin([options])]
        
        # remove option columns
        opt_cols = [col for col in list(df.columns) if 'opt_' in col]
        return df.drop(opt_cols, axis=1)

    def get_complex_coverage_data(self, options):
        cpx_df = self._get_base_cpx_coverage_data(options)

        return cpx_df

    def _get_base_cpx_coverage_data(self, options):
        cpx_data = self._get_filtered_cleaned_df(options)

        legends = [
            'package_name',
            'covered-cui-max',
            'covered-cui-avg',
            'covered-cui-min',
            'total-cui-max',
            'total-cui-avg',
            'total-cui-min',
            'unique-views-max',
            'unique-views-avg',
            'unique-views-min',
            'total-view-max',
            'total-view-avg',
            'total-view-min',
            '#TotalActivity',
            'acc-duration',
            'secs/step',
            'steps-max',
            'steps-avg',
            'steps-min',
        ]
        cpx_df = pd.DataFrame(columns=legends)


        for package in list(set(cpx_data['package_name'])):
            pack_table = cpx_data[cpx_data['package_name'].isin([package])]

            row_data = [
                package,
                pack_table['cov_cpx'].max(),
                pack_table['cov_cpx'].mean(),
                pack_table['cov_cpx'].min(),

                pack_table['tot_cpx'].max(),
                pack_table['tot_cpx'].mean(),
                pack_table['tot_cpx'].min(),

                pack_table['unq_views'].max(),
                pack_table['unq_views'].mean(),
                pack_table['unq_views'].min(),

                pack_table['tot_views'].max(),
                pack_table['tot_views'].mean(),
                pack_table['tot_views'].min(),
                
                pack_table['total_act'].max(),
                pack_table['duration_end'].sum() / 3600,
                pack_table['duration_end'].sum() / pack_table['step_end'].sum(),

                pack_table['step_end'].max(),
                pack_table['step_end'].mean(),
                pack_table['step_end'].min(),
            ]
            
            row = cpx_df.index.max() + 1
            if len(cpx_df.index) == 0:
                row = 1
            cpx_df.loc[row] = row_data

        return self._package_ordering(cpx_df)

    def _package_ordering(self, df):
        if 'package_name' in df.columns:
            df = df.set_index('package_name')
            df = df.T
        order = []
        for aut in self.auts:
            if aut in df.columns:
                order.append(aut)

        df = df[order].T
        return df.reset_index()

    def _set_raw_data_frame(self):
        self.columns = [
            'package_name',
            'options',
            'opt_is_DCW',
            'opt_waiting_time',
            'opt_view_equality',
            'opt_exploration',
            'opt_is_CAG',
            'opt_is_TI',
            'opt_is_slack',
            'duration_end',
            'opt_time_out',
            'step_end',
            'opt_step_out',
            'index',

            'monkey',
            'JHD',
            'JHE',
            'total_act',
            
            'cov_act',
            'tot_act',
            'cov_int',
            'tot_int',
            'cov_cpx',
            'tot_cpx',
            
            'unq_views',
            'cov_cpx2',
            'tot_views',
            'bnd_views',
            'tot_edges',
            'unq_edges',
        ]

        # set columns
        return pd.DataFrame(columns=self.columns)

    def _get_raw_data_frame(self, output_path):
        print('target output dir: {}'.format(output_path))
        total = 0
        target_pickles = []
        for path, dirs, files in os.walk(output_path):
            for f in sorted(files):
                if 'backup' in path:
                    continue
                if f.startswith('VTG_') and f.endswith('.pickle'):
                    total += 1
                    target_pickles.append(os.path.join(path, f))

        self.retrieved_packages = 0
        for pickle_path in target_pickles:
            output_dir_name = pickle_path.split(os.path.sep)[-1]
            print('{:3d}/{:3d}\t{}'
                  .format(self.retrieved_packages + 1, total, output_dir_name))
            if os.stat(pickle_path).st_size != 0:
                with open(pickle_path, 'rb') as pick:
                    aut_model = pickle.load(pick)

                self._get_aut_infos(aut_model)

            # if self.retrieved_packages == 1:
            #     pd.set_option('max_columns', None)
            #     pd.set_option('max_rows', None)
            #     pd.set_option('max_colwidth', None)
            #     pd.set_option('precision', 2)
            #     print(self.raw_df.head())
            #     return

        print('{}-ea of pickles collected'.format(self.retrieved_packages))

    def _get_aut_infos(self, aut_model):
        
        package = aut_model.package_name
        options = aut_model.options
        
        cov_act, tot_act, cov_int, tot_int, cov_cpx, cov_cpx2, tot_cpx, tot_views, bnd_views, unq_views, tot_edges, unq_edges \
        = self._get_coverage(aut_model)

        opt_acronym = ''
        if options.dynamic_waiting_mode:
            opt_acronym += 'DWT_{}-'.format(options.max_waiting_time)
        else:
            opt_acronym += 'SWT_{}-'.format(options.static_waiting_time)

        if options.view_equality_lv == 0:
            opt_acronym += 'Act-'
        elif options.view_equality_lv == 1:
            opt_acronym += 'Int-'
        elif options.view_equality_lv == 2:
            opt_acronym += 'Cnd-'

        if options.mainAlgorithm == 0:
            opt_acronym += 'RE-'
        elif options.mainAlgorithm == 1:
            opt_acronym += 'GRE-'

        if options.text_input_mode:
            opt_acronym += 'TI-'

        if options.cpx_act_gen:
            opt_acronym += 'CAG-'

        if options.time_out != -1:
            opt_acronym += '{}-'.format(options.time_out)

        opt_acronym = opt_acronym[:-1]

        raw_data = [
            package,
            opt_acronym,
            options.dynamic_waiting_mode,
            options.max_waiting_time if options.dynamic_waiting_mode else options.static_waiting_time ,
            options.view_equality_lv,
            options.mainAlgorithm,
            options.cpx_act_gen,
            options.text_input_mode,
            options.slack_mode,
            aut_model.duration_end,
            options.time_out,
            aut_model.cycles_end,
            options.cycle_out,
            options.packageInfo['outputPath'].split('-')[-1],

            self._get_jugular_data(package)[-1] * self._get_jugular_data(package)[0],
            self._get_jugular_data(package)[-3] * self._get_jugular_data(package)[0],
            self._get_jugular_data(package)[-2] * self._get_jugular_data(package)[0],
            self._get_jugular_data(package)[0],

            cov_act,
            tot_act,
            cov_int,
            tot_int,
            cov_cpx,
            tot_cpx,

            unq_views,
            cov_cpx2,
            tot_views,
            bnd_views,
            tot_edges,
            unq_edges
        ]

        history_id = package + '-' + opt_acronym + '-' + options.packageInfo['outputPath'].split('-')[-1]
        # time_stamp = step, duration, (act_covered, act_total, int_total - int_uncovered, int_total, cpx_covered, cpx_total), respond_time
        self._get_accum_coverage(history_id, zip(aut_model.time_stamp, aut_model.action_history + [None]))

        print('{} / {}'.format(aut_model.duration_end, options.time_out))
        if aut_model.duration_end < options.time_out:
            
            return
        
        row = self.raw_df.index.max() + 1
        if len(self.raw_df.index) == 0:
            row = 1
        self.raw_df.loc[row] = raw_data

        self.retrieved_packages = row

    def _get_accum_coverage(self, option_acronym, data):
        columns = ['step',
                        'delta_t',
                        'duration',
                        'act_cov_pec',
                        'act_cov',
                        'act_tot',
                        'int_cov_pec',
                        'int_cov',
                        'int_tot',
                        'cpx_cov_pec',
                        'cpx_cov',
                        'cpx_tot',
                        'on_view',
                        'widget_id',
                        'action']

        hist = []
        duration = 0
        for idx, item in enumerate(data):
            stat, act = item
            step = stat[0]
            delta_t = stat[1] - duration
            duration = stat[1]
            
            act_cov = stat[2][0]
            act_tot = stat[2][1]
            act_cov_pec = 0.
            if act_tot > 0:
                act_cov_pec = act_cov / act_tot
            
            int_cov = stat[2][2]
            int_tot = stat[2][3]
            int_cov_pec = 0.
            if int_tot > 0:
                int_cov_pec = int_cov / int_tot

            cpx_cov = stat[2][4]
            cpx_tot = stat[2][5]
            cpx_cov_pec = 0.
            if cpx_tot > 0:
                cpx_cov_pec = cpx_cov / cpx_tot

            if act is not None:
                on_view = act[0]
                widget_id = act[1]
                action = act[2]
            else:
                on_view = None
                widget_id = None
                action = None

            raw_data = [step, delta_t, duration,
                        act_cov_pec, act_cov, act_tot,
                        int_cov_pec, int_cov, int_tot,
                        cpx_cov_pec, cpx_cov, cpx_tot,
                        on_view, widget_id, action]
            hist.append(raw_data)

        self.histories[option_acronym] = pd.DataFrame(hist, columns=columns)

    def _get_coverage(self, aut_model):
        cov_act = 0 #
        tot_act = 0 #
        cov_int = 0
        tot_int = 0
        cov_cpx = 0
        cov_cpx2 = 0
        tot_cpx = 0

        tot_views = 0 #
        bnd_views = 0
        unq_views = 0
        tot_edges = 0
        unq_edges = 0


        base_acts = aut_model.package_info['activities']

        # remove 3rd party activity
        # rm_act_list = []
        # for act in base_acts:
        #     if aut_model.package_info['packageName'] not in act:
        #         print('except act: {} on {}'.format(act, aut_model.package_info['packageName']))
        #         rm_act_list.append(act)
        # for act in rm_act_list:
        #     base_acts.remove(act)
        
        # total act from model
        tot_act = len(base_acts)


        vtg = aut_model.vtg
        parent_dict = {}
        child_dict = {}
        for view_data in vtg.nodes.data():
            view = view_data[1]['view']
            if view.get_id() == 0 or view.is_bound:
                bnd_views += 1
                continue
            
            # total views
            tot_views += 1

            # act covereage
            if view.activity_name in base_acts:
                cov_act += 1
                base_acts.remove(view.activity_name)

            # find child views
            if view.get_id() not in parent_dict.keys():
                parent_dict[view.get_id()] = []

            # covered complex UIs from all of views
            if view.variant_covered:
                cov_cpx2 += 1

            if not view.get_id() in child_dict.keys():
                for child_data in vtg.nodes.data():
                    child = child_data[1]['view']
                    if child.get_id() in [0, view.get_id()] or child.is_bound:
                        continue

                    eq = view.get_equality_type(child)
                    if eq & view.MASK_SAME_BEHAVIOR == view.FLAG_SAME_PACKAGE | view.FLAG_SAME_ACTIVITY | view.FLAG_SAME_BEHAVIOR:
                        parent_dict[view.get_id()].append(child.get_id())
                        child_dict[child.get_id()] = view.get_id()

                # total unique views
                if len(parent_dict[view.get_id()]) > 0:
                    unq_views += 1
                    # print('View#{} has childs {}'.format(view.get_id(), parent_dict[view.get_id()]))

            # else:
            #     print('View#{} has parent View#{}'.format(view.get_id(), child_dict[view.get_id()]))

        # for unique views
        for parent_id in sorted(parent_dict.keys()):
            view = vtg.nodes[parent_id]['view']
            if len(parent_dict[parent_id]) > 0:
                # total comlex UIs
                if view.complexity:
                    tot_cpx += 1

                # covered complex UIs from unique views
                if view.variant_covered:
                    cov_cpx += 1

                # covered and total interactions
                interactions = {}
                for v_id in [parent_id] + parent_dict[parent_id]:
                    v = vtg.nodes[v_id]['view']
                    for w in v.interactable_widgets.keys():
                        for action in v.widget_possible_actions[w]:
                            interaction = w + '::' + action
                            if interaction not in interactions.keys():
                                interactions[interaction] = 0
                            if v.widget_possible_actions[w][action] > 0:
                                interactions[interaction] = 1
                tot_int += len(interactions.keys())
                cov_int += sum(interactions.values())
        
        # total edges
        tot_edges = len(list(vtg.edges))

        return cov_act, tot_act, cov_int, tot_int, cov_cpx, cov_cpx2, tot_cpx, \
            tot_views, bnd_views, unq_views, tot_edges, unq_edges
            
    def _get_jugular_data(self, package_name):
        # return #Act, monkey, JHE, JHD
        if package_name == 'net.fred.feedex':
            return 8, .5, .75, .2
        elif package_name == 'eu.siacs.conversations':
            return 20, .3, .5, .3
        elif package_name == 'at.bitfire.davdroid':
            return 10, .4, .6, .4
        elif package_name == 'org.y20k.transistor':
            return 3, 2 / 3, 1., 2 / 3
        elif package_name == 'com.fsck.k9':
            return 27, 2 / 27, 7 / 27, 2 / 27

        elif package_name == 'com.manichord.mgit':
            return 10, .3, .8, .4
        elif package_name == 'com.danielme.muspyforandroid':
            return 10, .1, .5, .1
        elif package_name == 'jp.redmine.redmineclient':
            return 16, .25, 5 / 16, .25
        elif package_name == 'com.owncloud.android':
            return 22, .045, 5 / 22, .045
        elif package_name == 'com.xargsgrep.portknocker':
            return 5, .6, .6, .6

        elif package_name == 'org.proninyaroslav.libretorrent':
            return 9, .1263, 4 / 9, 1 / 3
        elif package_name == 'org.connectbot':
            return 12, 1 / 3, 7 / 12, 1 / 3
        elif package_name == 'com.einmalfel.podlisten':
            return 4, .916, 1., .75
        elif package_name == 'net.sourceforge.servestream':
            return 13, 4 / 13, 7 / 13, 4 / 13


if __name__ == "__main__":
    # target_output_path = '/' + os.path.sep.join(['Volumes', 'Tools', 'CoVerCoDy', 'outputs'])
    target_output_path = os.path.sep.join(['C:', 'Users', 'JeongHohyeon', 'git', 'outputs-gcp'])
    dm = DataManager(target_output_path)
    dm.get_activity_coverage_data('DWT_4-Cnd-RE-CAG-TI')

