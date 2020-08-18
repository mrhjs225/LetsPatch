import os

import dash

import dash_core_components as dcc
import dash_html_components as html
import plotly.graph_objs as go
from plotly.subplots import make_subplots

external_stylesheets = ['https://codepen.io/chriddyp/pen/bWLwgP.css']

# app = dash.Dash(__name__)
app = dash.Dash(
    __name__, meta_tags=[{"name": "viewport", "content": "width=device-width"}],
    external_stylesheets=external_stylesheets
)

server = app.server
demo_mode = True

# get data
x_data = [1, 2]
y_data = [1, 2]
d1_file = 'data/csresult_insert.txt'
d2_file = 'data/result1.txt'

raw_dict = {}
with open(d2_file) as f:
    # content = f.raed()
    # lines = content.split('\n')
    lines = f.readlines()
    for lnum, line in enumerate(lines):
        raw_dict[lnum] = [
            tuple(line.split(',')[:-1])
        ]
    print(lnum)

with open(d1_file) as f:
    lines = f.readlines()
    for lnum, line in enumerate(lines):
        statistics = line.split(',')[1:-1]
        raw_dict[lnum].append(int(statistics[0]))
        raw_dict[lnum].append(list(int(x) for x in statistics[1:]))

# output
# raw_dicr[0] = [(Normalized(P), Normalized(L), Normalized(R)), 
#                number of related changes,
#                ranked list of frequency of related statement]

# global variables
avg_changes_per_context = 0.

# data precessing for Fig1
fig1_text = []
fig1_x = []
fig1_y = []
fig7_changes = []

for _id in range(len(raw_dict.keys())):
    context, nrc, ranks = raw_dict[_id]
    fig1_text.append(','.join(list(context)))
    fig1_x.append(_id)
    fig1_y.append(nrc)

avg_changes_per_context = sum(fig1_y) / len(fig1_y)

for _id in range(len(raw_dict.keys())):
    context, nrc, ranks = raw_dict[_id]
    if nrc < avg_changes_per_context:
        continue
    fig7_changes.append(nrc)

# data processing for fig2: sorted
fig2_data = zip(fig1_x, fig1_y, fig1_text)
fig2_data_sorted = sorted(fig2_data, reverse=True, key=lambda item: item[1])
tuples = zip(*fig2_data_sorted)
fig2_x = []
fig2_y = []
fig2_text = []

for line in [list(_tuple) for _tuple in fig2_data_sorted]:
    x, y, text = line
    fig2_x.append(str(x))
    fig2_y.append(y)
    fig2_text.append(text)


# data processing for fig 3-5: Portion of Top 5, 3, 1
fig3_x = []
fig3_y = []
fig3_y_abs = []
fig3_text = []

fig4_x = []
fig4_y = []
fig4_y_abs = []
fig4_text = []

fig5_x = []
fig5_y = []
fig5_y_abs = []
fig5_text = []

fig6_y_len_stmt = []

for _id in range(len(raw_dict.keys())):
    context, nrc, ranks = raw_dict[_id]
    if nrc < avg_changes_per_context:
        continue
    fig3_text.append(','.join(list(context)))
    fig3_x.append(_id)

    fig6_y_len_stmt.append(len(ranks))

    # harmonized average
    # x := total # of related statements
    # y := sum of Top-k related statements
    # n := number of kinds of related statements
    # h_avg = 2y(n-k) / (n-k)x + ny
    def h_avg(x, y, n, k):
        return 2 * y * (n - k) / (((n - k) * x) + (n * y))
    

    # top 5
    portion = -1
    abs_sum = -1
    if len(ranks) < 5:
        print(raw_dict[_id])
    else:
        top_sum = sum(ranks[:5])
        abs_sum = top_sum
        total_sum = sum(ranks)
        # portion = 100. * top_sum / total_sum
        portion = h_avg(total_sum, top_sum, len(ranks), 5) * 100.
    fig3_y.append(portion)
    fig3_y_abs.append(abs_sum)

    # top 3
    portion = -1
    abs_sum = -1
    if len(ranks) < 3:
        print(raw_dict[_id])
    else:
        top_sum = sum(ranks[:3])
        abs_sum = top_sum
        total_sum = sum(ranks)
        # portion = 100. * top_sum / total_sum
        portion = h_avg(total_sum, top_sum, len(ranks), 3) * 100.
    fig4_y.append(portion)
    fig4_y_abs.append(abs_sum)

    # top 1
    portion = -1
    abs_sum = -1
    if len(ranks) < 1:
        print(raw_dict[_id])
    else:
        top_sum = sum(ranks[:1])
        abs_sum = top_sum
        total_sum = sum(ranks)
        # portion = 100. * top_sum / total_sum
        portion = h_avg(total_sum, top_sum, len(ranks), 1) * 100.
    fig5_y.append(portion)
    fig5_y_abs.append(abs_sum)


# prepare graph 7

fig7_data = zip(fig3_y, fig4_y, fig5_y, fig6_y_len_stmt, fig7_changes, fig3_text)
fig7_data_sorted = sorted(fig7_data, reverse=True, key=lambda item: item[4])
tuples = zip(*fig7_data_sorted)
fig7_x = []
fig7_y_top5 = []
fig7_y_top3 = []
fig7_y_top1 = []
fig7_y_len_stmt = []
fig7_y_changes = []
fig7_text = []

for line in [list(_tuple) for _tuple in fig7_data_sorted]:
    top5_y, top3_y, top1_y, len_stmt, changes, text = line
    fig7_text.append(text)
    fig7_y_top5.append(top5_y)
    fig7_y_top3.append(top3_y)
    fig7_y_top1.append(top1_y)
    fig7_y_len_stmt.append(len_stmt)
    fig7_y_changes.append(changes)

fig7 = make_subplots(specs=[[{"secondary_y": True}]])
fig7.add_trace(
    go.Bar(
        name='top5',
        y=fig7_y_top5,
        marker={'color': '#ff0000'},
        text=fig7_text,
    ),
    secondary_y=False,
)
fig7.add_trace(
    go.Bar(
        name='top3',
        y=fig7_y_top3,
        marker={'color': '#00ff00'}),
    secondary_y=False,
)
fig7.add_trace(
    go.Bar(
        name='top1',
        y=fig7_y_top1,
        marker={'color': '#0000ff'}),
    secondary_y=False,
)

fig7.add_trace(
    go.Scatter(
        name='len_stmt',
        y=fig7_y_len_stmt,
        mode='markers+lines+text',
        text=fig7_y_len_stmt
    ),
    secondary_y=True,
)

fig7.add_trace(
    go.Scatter(
        name='#changes',
        y=fig7_y_changes,
        mode='markers+lines+text',
        text=fig7_y_changes
    ),
    secondary_y=False,
)

app.layout = html.Div(
    children = [
        html.H1(children='Jinseok\'s Dashboard'),

        dcc.Graph(
            id='fig1',
            figure=go.Figure(data=[
                go.Bar(
                    x=fig1_x,
                    y=fig1_y,
                    text=fig1_text),
            ]).update_layout(title='Context 별 change 개수')

        ),

        dcc.Graph(
            id='fig2',
            figure=go.Figure(data=[
                go.Bar(
                    y=fig2_y,
                    text=fig2_text),
            ]).update_layout(title='Context 별 change 개수(Desc)')
        ),

        dcc.Graph(
            id='fig3',
            figure=go.Figure(data=[
                go.Bar(
                    y=fig3_y,
                    text=fig3_text),
            ]).update_layout(
                title='Portion of Top 5 related statements',
                yaxis={'range': [-10, 110],},
                )
        ),

        dcc.Graph(
            id='fig4',
            figure=go.Figure(data=[
                go.Bar(
                    y=fig4_y,
                    text=fig3_text),
            ]).update_layout(
                title='Portion of Top 3 related statements',
                yaxis={'range': [-10, 110],},
                )
        ),

        dcc.Graph(
            id='fig5',
            figure=go.Figure(data=[
                go.Bar(
                    y=fig5_y,
                    text=fig3_text),
            ]).update_layout(
                title='Portion of Top 1 related statements',
                yaxis={'range': [-10, 110],},
                )
        ),

        dcc.Graph(
            id='fig6',
            figure=go.Figure(data=[
                go.Bar(
                    name='len_stmt',
                    y=fig6_y_len_stmt,
                    text=fig3_text,
                    marker={'color': '#ff0000'}),
                go.Bar(
                    name='top5',
                    y=fig3_y_abs,
                    marker={'color': '#00ff00'}),
                go.Bar(
                    name='top3',
                    y=fig4_y_abs,
                    marker={'color': '#0000ff'}),
                go.Bar(
                    name='top1',
                    y=fig5_y_abs,
                    marker={'color': '#ff00ff'}),
            ]).update_layout(
                title='Portion of Top 1 related statements',
                yaxis={'range': [-10, 650],},
                barmode='overlay',
                )
        ),

        dcc.Graph(
            id='fig7',
            figure=fig7.update_layout(
                title='Portion of Top 1 related statements',
                yaxis={'range': [-10, 110],},
                barmode='overlay',
                )
        ),
    ]
)

# Running the server
if __name__ == "__main__":
    app.run_server(debug=True)
