'''
Created on Jan 27, 2015

@author: ayan
'''
from py_geoserver_rest_requests import GeoWebCacheSetUp
from secure_params import GWC_URL, USER, PWD


if __name__ == '__main__':
    
    """
    Run from the command line to terminate all seeding tasks.
    """
    
    sp_gwc = GeoWebCacheSetUp(GWC_URL, USER, PWD, None, None)
    # abort all seeding tasks
    abort = sp_gwc.abort_seed_request(kill_all=True)
    print(abort.status_code)