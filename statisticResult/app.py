import os
import pathlib

import dash
import dash_core_components as dcc
import dash_html_components as html
import dash_table
import plotly.graph_objs as go
from DataManager import DataManager
from plotly.subplots import make_subplots

# get relative data folder
DATA_PATH = pathlib.Path(__file__).parent.joinpath("data").resolve()

external_stylesheets = ['https://codepen.io/chriddyp/pen/bWLwgP.css']

# app = dash.Dash(__name__)
app = dash.Dash(
    __name__, meta_tags=[{"name": "viewport", "content": "width=device-width"}],
    external_stylesheets=external_stylesheets
)

server = app.server
demo_mode = True


# target_output_path = os.path.sep.join(['C:', 'Users', 'JeongHohyeon', 'git', 'outputs'])
# \\115.145.178.154\Tools\CoVerCoDy\outputs
# target_output_path = os.path.sep + os.path.sep + os.path.sep.join(['115.145.178.154', 'Tools', 'CoVerCoDy', 'outputs'])
# target_output_path = '/' + os.path.sep.join(['home', 'selab', 'Tools', 'CoVerCoDy', 'outputs'])
# target_output_path = '/' + os.path.sep.join(['Volumes', 'Tools', 'CoVerCoDy', 'outputs'])
target_output_path = '/' + os.path.sep.join(['Users', 'jeonghohyeon', 'git', 'covercody-data'])

dm = None
dd_options = []


app.layout = html.Div(children=[
    html.H1(children='Hello Horeng! This is CoVerCoDy Dashboard'),

    html.H3(id='dd-output-container', children='Loading...'),

    dcc.Dropdown(
        id='option-dropdown',
        options=dd_options
    ),

    dcc.Graph(id='overview-box-plot'),

    dcc.Graph(id='overview-chart'),

    dash_table.DataTable(id='overview-table',
                         style_table={'overflowX': 'scroll'},
                         style_cell_conditional=[
                             {
                                 'if': {'column_id': 'options'},
                                 'textAlign': 'left'
                             }
                         ],
                         style_data_conditional=[
                             {
                                 'if': {'row_index': 'odd'},
                                 'backgroundColor': 'rgb(248, 248, 248)'
                             }
                         ],
                         ),

    dcc.Graph(id='activity-coverage'),

    dash_table.DataTable(id='activity-coverage-table', style_table={'overflowX': 'scroll'}, ),

    dcc.Graph(id='delta-activity-coverage'),

    dcc.Graph(id='heatmap-activity-coverage'),

    dcc.Graph(id='accum-activity-coverage'),

    dcc.Graph(id='cui-coverage'),
])


@app.callback(
    [
        dash.dependencies.Output('option-dropdown', 'options'),
        dash.dependencies.Output('dd-output-container', 'children'),

        dash.dependencies.Output('overview-box-plot', 'figure'),
        dash.dependencies.Output('overview-chart', 'figure'),
        dash.dependencies.Output('overview-table', 'columns'),
        dash.dependencies.Output('overview-table', 'data'),

        dash.dependencies.Output('activity-coverage', 'figure'),
        dash.dependencies.Output('activity-coverage-table', 'columns'),
        dash.dependencies.Output('activity-coverage-table', 'data'),
        dash.dependencies.Output('delta-activity-coverage', 'figure'),
        dash.dependencies.Output('heatmap-activity-coverage', 'figure'),
        dash.dependencies.Output('accum-activity-coverage', 'figure'),
        dash.dependencies.Output('cui-coverage', 'figure'),
    ],
    [
        dash.dependencies.Input('option-dropdown', 'value')
    ]
)
def update_value(value):
    global dm, dd_options
    if dm is None:
        dm = DataManager(target_output_path)

        # for options
        dd_options = [{'label': opt, 'value': opt} for opt in ['Overview'] + dm.get_options()]
        return dd_options, 'Select options to see!', {}, {}, [], [], {}, [], [], {}, {}, {}, {}

    if value is None:
        return dd_options, 'N/A', {}, {}, [], [], {}, [], [], {}, {}, {}, {}
    elif value == 'Overview':
        ovv_dict, ovv_df = dm.get_overview()

        # Create figure with secondary y-axis
        fig = make_subplots(specs=[[{"secondary_y": True}]])

        # Add traces
        # fig.add_trace(
        #     go.Scatter(x=list(ovv_df['options']), y=list(ovv_df['run']), name="run",
        #                mode='markers+lines+text',
        #                text=list(ovv_df['run'])),
        #     secondary_y=False,
        # )

        fig.add_trace(
            go.Scatter(x=list(ovv_df['options']), y=list(ovv_df['avg-steps']), name="avg-steps",
                       mode='markers+lines+text',
                       text=list(ovv_df['avg-steps'])),
            secondary_y=False,
        )

        fig.add_trace(
            go.Scatter(x=list(ovv_df['options']), y=list(ovv_df['secs/step']), name="secs/step",
                       mode='markers+lines+text',
                       text=list(ovv_df['secs/step'])),
            secondary_y=True,
        )

        fig.update_layout(title='테스트 효율 비교')

        return dd_options, 'Overview {}-ea of results'.format(dm.retrieved_packages), \
               go.Figure(data=[
                       # go.Box(y=ovv_df['vs.monkey'].to_list()[0]),
                       go.Box(x=ovv_dict['vs.monkey']['x'],
                              y=ovv_dict['vs.monkey']['y'],
                              name='CoVerCoDy vs. Monkey',
                              jitter=0.3,
                              pointpos=-1.8,
                              boxpoints='all',
                              marker_color='#FF851B'),
                       go.Box(x=ovv_dict['vs.JHE']['x'],
                              y=ovv_dict['vs.JHE']['y'],
                              name='CoVerCoDy vs. JHE',
                              jitter=0.3,
                              pointpos=-1.8,
                              boxpoints='all',
                              marker_color='#3D9970')
                   ]).update_layout(boxmode='group', title='옵션별 기존연구 대비 액티비티 커버리지 증감율 분포'), \
               fig, \
               [{"name": i, "id": i} for i in ovv_df.columns], \
               ovv_df.to_dict('records'), \
               {}, [], [], {}, {}, {}, {}
    else:
        cov_df, delta_df, hm_zero_pos = dm.get_activity_coverage_data(value)
        x = list(cov_df['package_name'])

        cpx_df = dm.get_complex_coverage_data(value)

        return dd_options, value, {}, {}, [], [],\
            {
                'data': [
                    {'type': 'bar', 'name': 'duration',
                     'mode': 'markers', 'marker': {'color': 'rgba(0, 0, 0, 0.)'},
                     'x': x, 'y': list(cov_df['duration_end'])},
                    {'type': 'scatter', 'name': 'CoVerCoDy-Max',
                     'fill': 'tozeroy', 'fillcolor': 'rgba(112, 173, 71, 1.0)',
                     'mode': 'lines', 'marker': {'color': 'rgba(112, 173, 71, 1.0)'},
                     'x': x, 'y': list(cov_df['CoVerCoDy-max'])},
                    {'type': 'scatter', 'name': 'CoVerCoDy-Avg',
                     'fill': 'tozeroy', 'fillcolor': 'rgba(91, 155, 213, 1.0)',
                     'mode': 'lines', 'marker': {'color': 'rgba(91, 155, 213, 1.0)'},
                     'x': x, 'y': list(cov_df['CoVerCoDy-avg'])},
                    {'type': 'scatter', 'name': 'CoVerCoDy-Min',
                     'fill': 'tozeroy', 'fillcolor': 'rgba(255, 192, 0, 1.0)',
                     'mode': 'lines', 'marker': {'color': 'rgba(255, 192, 0, 1.0)'},
                     'x': x, 'y': list(cov_df['CoVerCoDy-min'])},
                    {'type': 'scatter', 'name': '#TotalActivity',
                     'mode': 'markers', 'marker': {'size': '15', 'symbol': 'circle', 'color': 'black'},
                     'x': x, 'y': list(cov_df['#TotalActivity'])},
                    {'type': 'scatter', 'name': 'Monkey',
                     'mode': 'markers', 'marker': {'size': '12', 'symbol': 'square', 'color': 'rgba(237, 125, 49, 1.0)'},
                     'x': x, 'y': list(cov_df['Monkey'])},
                    {'type': 'scatter', 'name': 'JHE',
                     'mode': 'markers-lines', 'marker': {'size': '9', 'symbol': 'diamond', 'color': '#c55a11'},
                     'x': x, 'y': list(cov_df['JHE'])},
                    {'type': 'scatter', 'name': 'JHD',
                     'mode': 'markers', 'marker': {'size': '9', 'symbol': 'star', 'color': '#843c0c'},
                     'x': x, 'y': list(cov_df['JHD'])},
                ],
                'layout': {
                    'title': 'Activity Coverage ({})'.format(value)
                }
            }, \
            [{"name": i, "id": i} for i in cov_df.columns], \
            cov_df.to_dict('records'), \
            {
                'data': [
                    {'type': 'bar', 'name': 'Max vs. Monkey',
                     'marker': {'color': 'rgb(197, 224, 180)'},
                     'x': x, 'y': delta_df['max-monkey']},
                    {'type': 'bar', 'name': 'Avg vs. Monkey',
                     'marker': {'color': 'rgb(255, 230, 153)'},
                     'x': x, 'y': delta_df['avg-monkey']},
                    {'type': 'bar', 'name': 'Min vs. Monkey',
                     'marker': {'color': 'rgb(248, 203, 173)'},
                     'x': x, 'y': delta_df['min-monkey']},
                    {'type': 'bar', 'name': 'Max vs. JHE',
                     'marker': {'color': 'rgb(0, 176, 80)'},
                     'x': x, 'y': delta_df['max-jhe']},
                    {'type': 'bar', 'name': 'Avg vs. JHE',
                     'marker': {'color': 'rgb(255, 192, 0)'},
                     'x': x, 'y': delta_df['avg-jhe']},
                    {'type': 'bar', 'name': 'Min vs. JHE',
                     'marker': {'color': 'rgb(197, 90, 17)'},
                     'x': x, 'y': delta_df['min-jhe']},
                ],
                'layout': {
                    'title': 'Delta Activity Coverage from CoVerCoDy (%)'
                }
            }, \
            {
                'data': [
                    {'type': 'heatmap', 'name': 'a',
                     'showscale': True,
                     'colorscale': [[0, "rgb(197, 90, 17)"], [hm_zero_pos, "rgb(255, 192, 0)"], [1, "#63be7b"]],
                     'x': x,
                     'y': list(delta_df.columns),
                     'z': delta_df.T.to_numpy()}
                ],
                'layout': {
                    'title': 'Delta Activity Coverage: CoVerCoDy vs. Monkey (%)'
                }
            }, \
            go.Figure(data=[
                go.Bar(name='increased',
                    x=delta_df.columns,
                    y=list(delta_df[delta_df > 0].count()),
                       marker={'color': '#00b050'}),
                go.Bar(name='same',
                    x=delta_df.columns,
                    y=list(delta_df[delta_df == 0].count()),
                       marker={'color': '#ffc000'}),
                go.Bar(name='decreased',
                    x=delta_df.columns,
                    y=list(delta_df[delta_df < 0].count()),
                       marker={'color': '#c55a11'}),
            ]).update_layout(barmode='stack'), \
            {
                'data': [

                     {'type': 'bar', 'name': 'Covered-CUI-Max',
                     'marker': {'color': 'rgba(0, 176, 80, 1.0)'},
                     'x': x, 'y': list(cpx_df['covered-cui-max'])},
                    {'type': 'bar', 'name': 'Covered-CUI-Avg',
                     'marker': {'color': 'rgba(255, 192, 0, 1.0)'},
                     'x': x, 'y': list(cpx_df['covered-cui-avg'])},
                    {'type': 'bar', 'name': 'Covered-CUI-Min',
                     'marker': {'color': 'rgba(197, 90, 17, 1.0)'},
                     'x': x, 'y': list(cpx_df['covered-cui-min'])},

                    {'type': 'bar', 'name': 'Total-CUI-Max',
                     'marker': {'color': 'rgba(0, 176, 80, .8)'},
                     'x': x, 'y': list(cpx_df['total-cui-max'])},
                    {'type': 'bar', 'name': 'Total-CUI-Avg',
                     'marker': {'color': 'rgba(255, 192, 0, .8)'},
                     'x': x, 'y': list(cpx_df['total-cui-avg'])},
                    {'type': 'bar', 'name': 'Total-CUI-Min',
                     'marker': {'color': 'rgba(197, 90, 17, .8)'},
                     'x': x, 'y': list(cpx_df['total-cui-min'])},

                    {'type': 'bar', 'name': 'Unique-views-Max',
                     'marker': {'color': 'rgba(0, 176, 80, 0.6)'},
                     'x': x, 'y': list(cpx_df['unique-views-max'])},
                    {'type': 'bar', 'name': 'Unique-views-Avg',
                     'marker': {'color': 'rgba(255, 192, 0, 0.6)'},
                     'x': x, 'y': list(cpx_df['unique-views-avg'])},
                    {'type': 'bar', 'name': 'Unique-views-Min',
                     'marker': {'color': 'rgba(197, 90, 17, 0.6)'},
                     'x': x, 'y': list(cpx_df['unique-views-min'])},

                    # {'type': 'scatter', 'name': 'Acc-duration',
                    #  'mode': 'markers', 'marker': {'size': '8', 'symbol': 'circle', 'color': 'black'},
                    #  'x': x, 'y': list(cpx_df['acc-duration'])},

                    {'type': 'scatter', 'name': 'Avg-secs/step',
                     'mode': 'markers-+lines', 'marker': {'size': '10', 'symbol': 'circle', 'color': 'black'},
                     'x': x, 'y': list(cpx_df['secs/step'])},

                    {'type': 'scatter', 'name': '#TotalActivity',
                     'mode': 'markers', 'marker': {'size': '12', 'symbol': 'circle', 'color': 'black'},
                     'x': x, 'y': list(cpx_df['#TotalActivity'])},
                ],
                'layout': {
                    'title': 'Complex UI Coverage ({})'.format(value),
                    'yaxis': {'range': [0, 30],}
                }
            }


# Running the server
if __name__ == "__main__":
    app.run_server(debug=True)
