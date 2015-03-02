'''
Created on Jan 27, 2015

@author: ayan
'''
import argparse
from py_geoserver_rest_requests import GeoWebCacheSetUp
from params import USER, PWD    


if __name__ == '__main__':
    
    """
    Run from the command line to terminate all seeding tasks.
    """
    
    parser = argparse.ArgumentParser()
    parser.add_argument('server_name', type=str)
    args = parser.parse_args()
    server_name = args.server_name.lower()
    
        
    USER = USER
    PWD = PWD
    gwc_url = '{server_name}/gwc/rest'.format(server_name=server_name)
    
    sp_gwc = GeoWebCacheSetUp(gwc_url, USER, PWD, None, None)
    # abort all seeding tasks
    abort = sp_gwc.abort_seed_request(kill_all=True)
    print(abort.status_code)