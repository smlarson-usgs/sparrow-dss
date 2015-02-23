'''
Created on Jan 30, 2015

@author: ayan
'''
import argparse
from params import WORKSPACES, USER, PWD
from seed_request import get_ws_layers, get_layer_styles, execute_seed_request


parser = argparse.ArgumentParser()
parser.add_argument('server_name', type=str)
parser.add_argument('zoom_start', type=int)
parser.add_argument('zoom_stop', type=int)
parser.add_argument('threads', type=int)

args = parser.parse_args()
server_name = args.server_name.lower()
zoom_start = args.zoom_start
zoom_stop = args.zoom_stop
threads = args.threads

if zoom_start > zoom_stop:
    raise Exception('Starting zoom level cannot be larger than the ending zoom level.')

spdss_gs_url = '{server_name}/rest'.format(server_name=server_name)
spdss_gwc_url = '{server_name}/gwc/rest'.format(server_name=server_name)
USER = USER
PWD = PWD

layers = get_ws_layers(spdss_gs_url, USER, PWD, WORKSPACES)
lyr_with_styles = get_layer_styles(spdss_gs_url, USER, PWD, layers)
seed_responses = execute_seed_request(spdss_gwc_url, USER, PWD, lyr_with_styles, 
                                      zoom_start=zoom_start, zoom_stop=zoom_stop, 
                                      threads=threads
                                      )
print(len(seed_responses))