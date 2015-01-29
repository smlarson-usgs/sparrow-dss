'''
Created on Jan 27, 2015

@author: ayan
'''
import argparse
from py_geoserver_rest_requests import GeoWebCacheSetUp    


if __name__ == '__main__':
    
    """
    Run from the command line to terminate all seeding tasks.
    """
    
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
        
    GWC_URL = param_values['GWC_HOST']
    USER = param_values['USER']
    PWD = param_values['PWD']
    
    sp_gwc = GeoWebCacheSetUp(GWC_URL, USER, PWD, None, None)
    # abort all seeding tasks
    abort = sp_gwc.abort_seed_request(kill_all=True)
    print(abort.status_code)