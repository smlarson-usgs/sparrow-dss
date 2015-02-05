'''
Created on Jan 27, 2015

@author: ayan
'''
import time
import datetime
import logging
import argparse
from py_geoserver_rest_requests import GeoServerWorkspace, GeoWebCacheSetUp, GeoServerLayers


def get_ws_layers(gs_url, gs_user, gs_pwd, workspaces):
    """
    Get the layers belonging to a workspace.
    
    :param str gs_url: geoserver rest url
    :param str gs_user: geoserver username
    :param str gs_pwd: geoserver password
    :param list workspaces: list of geoserver workspaces
    :return: list of tuples of form (workspace_name, [list of layers belonging to the workspace])
    :rtype: list
     
    """
    all_results = []
    for workspace in workspaces:
        spdss_ws = GeoServerWorkspace(gs_url, gs_user, gs_pwd, workspace)
        ws_layers = spdss_ws.get_ws_layers()
        unique_ws_layers = list(set(ws_layers))
        ws_results = (workspace, clean_layer_names(unique_ws_layers))
        all_results.append(ws_results)
    return all_results


def get_layer_styles(gs_url, gs_user, gs_pwd, ws_layer_content):
    """
    Get the default style for each layer.
    
    :param str gs_url: geoserver rest url
    :param str gs_user: geoserver username
    :param str gs_pwd: geoserver password
    :param list ws_layer_content: list of tuples of form (workspace_name, [list of layers belonging to the workspace])
    .. seealso :: :function:'get_ws_layers'
    :return: list of tuples containing the workspace name and a dictionary; the dictionary contains keys: layer_name, style_name
    :rtype: list
    
    """
    ws_info = []
    for element in ws_layer_content:
        ws_name, ws_layers = element
        ws_layer_info = []
        for layer in ws_layers:
            gs_layer = GeoServerLayers(gs_url, gs_user, gs_pwd, ws_name, layer, layer)
            layer_style_resp = gs_layer.get_layer_styles()
            layer_style_json = layer_style_resp.json()
            style_name = str(layer_style_json['layer']['defaultStyle']['name'])
            layer_info = {'layer_name': layer, 'style_name': style_name}
            ws_layer_info.append(layer_info)
        ws_lyr_def = (ws_name, ws_layer_info)
        ws_info.append(ws_lyr_def)
    return ws_info


def execute_seed_request(gwc_url, gs_user, gs_pwd, cache_data, grid='EPSG:4326', 
                         tile_format='image/png8', grid_number=4326, zoom_start=0, 
                         zoom_stop=3, threads=1, progress_check=5, exclude_layers=()):
    """
    Generate seeding xml and post it to Geoserver for each layer in a workspace.
    The starting zoom level defaults to 0.
    
    The progress of the tile caching will be printed to the console. In addition,
    a rough record of the layers cached is logged to seeding.log.
    
    :param str gwc_url: geoserver geowebcache rest url
    :param str gs_user: geoserver username
    :param str gs_pwd: geoserver password
    :param list cache_data: list of tuples containing the workspace name and a dictionary; the dictionary contains keys: layer_name, style_name
    .. seealso :: :function:'get_layer_styles'
    :param str grid: full name of the gridset to be used
    :param str tile_format: format that the tile images should be generated in
    :param int grid_number: integer value of the gridset
    :param int zoom_start: the minimum zoom level that should be cached
    :param int zoom_stop: the maximum zoom level that should be cached
    :param int threads: number of threads to be used when generating a tile cache
    :param float progress_check: interval in seconds between checks to GeoServer for progress on a caching job
    :param exclude_layers: iterable containing the names of layers from the workspace(s) that should not be cached; defaults to an empty tuple
    :type exclude_layers: list or tuple
    :return: requests objects from posting the seed requests
    :rtype: list
    
    """
    
    # sparrow-flowline-reusable:51N2043963353 - 6.67 GB... need more disk quota with zoom_stop=10
    
    # setup some basic logging
    logging.basicConfig(filename='seed_log.log', 
                        filemode='w', 
                        level=logging.INFO, 
                        format='%(asctime)s %(message)s'
                        )
    request_resps = []
    job_ids_with_tiles = []
    for cache_datum in cache_data:
        ws_name, layer_params = cache_datum
        layer_count = 'Total layers for {workspace_name}: {layer_count}'.format(workspace_name=ws_name,
                                                                                layer_count=len(layer_params)
                                                                                )
        logging.info(layer_count)
        for layer_param in layer_params:
            layer_name = layer_param['layer_name']
            started = 'Started - {workspace}:{layer}'.format(workspace=ws_name, layer=layer_name)
            logging.info(started)
            if layer_name in exclude_layers:
                finished = 'Did not cache - {workspace}:{layer}'.format(workspace=ws_name, layer=layer_name)
                seed_request = finished
                print(datetime.datetime.now())
                print(finished)
            else:
                style_name = layer_param['style_name']
                sp_gwc = GeoWebCacheSetUp(gwc_url, gs_user, gs_pwd, ws_name, 
                                          layer_name, cert_verify=False
                                          )
                seed_xml = sp_gwc.create_seed_xml(style=style_name,
                                                  tile_format=tile_format,
                                                  gridset_number=grid_number,
                                                  zoom_start=zoom_start, 
                                                  zoom_stop=zoom_stop,
                                                  threads=threads
                                                  )
                seed_request = sp_gwc.seed_request(seed_xml)
                url_message = 'Request URL: {0}'.format(seed_request.url)
                status_code_message = 'Status: {0}'.format(seed_request.status_code)
                print(url_message)
                print(status_code_message)
                array_length = 1
                while array_length > 0:
                    status = sp_gwc.query_task_status()
                    print(datetime.datetime.now())
                    status_message = '{workspace}:{layer} - {progress}'.format(workspace=ws_name, 
                                                                               layer=layer_name, 
                                                                               progress=status[1]
                                                                               )
                    print(status_message)
                    long_array = status[1]['long-array-array']
                    try:
                        thread1 = long_array[0]
                        tile_count = thread1[1]
                        job_id = thread1[3]
                        job_tile_count = (job_id, tile_count)
                        if job_tile_count not in job_ids_with_tiles:
                            job_ids_with_tiles.append(job_tile_count)
                    except IndexError:
                        pass
                    array_length = len(long_array)
                    time.sleep(progress_check)
                finished = 'Finished - {workspace}:{layer}'.format(workspace=ws_name, layer=layer_name)
            logging.info(finished)
            request_resps.append(seed_request)
        tile_counts = []
        for job_tile_tuple in job_ids_with_tiles:
            tile_count = float(job_tile_tuple[1])
            tile_counts.append(tile_counts)
        tile_sum = sum(tile_counts)
        print('Total tiles: {0}'.format(tile_sum))
    return request_resps
            

def clean_layer_names(layer_names):
    clean_names = []
    for layer_name in layer_names:
        layer_sans_ws_name = layer_name.split(':')[1]
        clean_names.append(layer_sans_ws_name)
    return clean_names


if __name__ == '__main__':
    
    from params import WORKSPACES
    
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
                                          zoom_stop=10, threads=2, 
                                          exclude_layers=('53N448826903',)
                                          )
    print(len(seed_responses))