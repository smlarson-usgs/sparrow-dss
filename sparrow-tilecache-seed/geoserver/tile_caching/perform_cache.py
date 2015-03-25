'''
Created on Jan 30, 2015

@author: ayan
'''
import argparse
from params import USER, PWD
from seed_request import get_ws_layers, get_layer_styles, execute_seed_request


parser = argparse.ArgumentParser()
parser.add_argument('server_name', type=str, help='Name of the server GeoServer lives on.')
parser.add_argument('zoom_start', type=int, help='Starting zoom level')
parser.add_argument('zoom_stop', type=int, help='Ending zoom level')
parser.add_argument('threads', type=int, help='Number of threads used for caching')
parser.add_argument('is_latest_failure', type=str, help='Was the last caching attempt successful?')
parser.add_argument('workspaces', type=str, help='Workspace to cache layers from')
parser.add_argument('--model_number', type=str, help="Model number to be cached")

args = parser.parse_args()
server_name = args.server_name.lower()
zoom_start = args.zoom_start
zoom_stop = args.zoom_stop
threads = args.threads
is_latest_failure = args.is_latest_failure
workspaces = args.workspaces.lower()

if is_latest_failure.lower() == 'true':
    latest_is_failure = True
else:
    latest_is_failure = False


try:
    if args.model_number.lower() == 'none':
        model_number = None
    elif args.model_number:
        model_number = args.model_number
    else:
        model_number = None
except AttributeError:
    model_number = None

if zoom_start > zoom_stop:
    raise Exception('Starting zoom level cannot be larger than the ending zoom level.')

spdss_gs_url = '{server_name}/rest'.format(server_name=server_name)
spdss_gwc_url = '{server_name}/gwc/rest'.format(server_name=server_name)
USER = USER
PWD = PWD

if workspaces == 'all':
    from params import WORKSPACES as WORKSPACES
elif workspaces == 'catchment':
    from params import CATCHMENT_WORKSPACES as WORKSPACES
elif workspaces == 'flowline':
    from params import FLOWLINE_WORKSPACES as WORKSPACES
elif workspaces == 'resusable':
    from params import REUSABLE_WORKSPACES as WORKSPACES
elif workspaces == 'overlays':
    from params import OVERLAY_WORKSPACES as WORKSPACES
else:
    raise Exception('Workspaces were not specified.')

layers = get_ws_layers(spdss_gs_url, USER, PWD, WORKSPACES, model_number, latest_is_failure=latest_is_failure)
lyr_with_styles = get_layer_styles(spdss_gs_url, USER, PWD, layers)
seed_responses = execute_seed_request(spdss_gwc_url, USER, PWD, lyr_with_styles, 
                                      zoom_start=zoom_start, zoom_stop=zoom_stop, 
                                      threads=threads, progress_check=30, 
                                      latest_is_failure=latest_is_failure
                                      )
print(len(seed_responses))