'''
Created on Jan 30, 2015

@author: ayan
'''
import argparse
from params import WORKSPACES
from seed_request import get_ws_layers, get_layer_styles, execute_seed_request


parser = argparse.ArgumentParser()
parser.add_argument('tier', type=str)
args = parser.parse_args()
tier_name = args.tier.lower()

if tier_name == 'dev':
    from params import DEV as param_values
elif tier_name == 'qa':
    from params import QA as param_values
elif tier_name == 'prod':
    from params import PROD as param_values
else:
    raise Exception('Tier name not recognized')

SPDSS_GS_URL = param_values['GS_HOST']
GWC_URL = param_values['GWC_HOST']
USER = param_values['USER']
PWD = param_values['PWD']

layers = get_ws_layers(SPDSS_GS_URL, USER, PWD, WORKSPACES)
lyr_with_styles = get_layer_styles(SPDSS_GS_URL, USER, PWD, layers)
seed_responses = execute_seed_request(GWC_URL, USER, PWD, lyr_with_styles, 
                                      zoom_stop=10, threads=2
                                      )
print(len(seed_responses))