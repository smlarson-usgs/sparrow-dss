'''
Created on Jan 27, 2015

@author: ayan
'''
import time
import datetime
import logging
from requests.exceptions import ConnectionError
from py_geoserver_rest_requests import GeoServerWorkspace, GeoWebCacheSetUp, GeoServerLayers
from params import OVERLAY_WORKSPACES
from utils import regex_matching
from sqlite_db import SqliteDB


class SuccessiveConnectionError(ConnectionError):
    pass


def get_ws_layers(gs_url, gs_user, gs_pwd, workspaces, model_number=None, latest_is_failure=False):
    """
    Get the layers belonging to a workspace.
    
    :param str gs_url: geoserver rest url
    :param str gs_user: geoserver username
    :param str gs_pwd: geoserver password
    :param list workspaces: list of geoserver workspaces
    :param str model_number: model number of interest
    :return: list of tuples of form (workspace_name, [list of layers belonging to the workspace])
    :rtype: list
     
    """
    all_results = []
    for workspace in workspaces:
        spdss_ws = GeoServerWorkspace(gs_url, gs_user, gs_pwd, workspace)
        ws_layers = spdss_ws.get_ws_layers()
        unique_ws_layers = list(set(ws_layers))
        cleaned_layers = clean_layer_names(unique_ws_layers, model_number)  # full list of layers
        if latest_is_failure:
            db = SqliteDB()
            try:
                query_results = db.query_db(workspace=workspace)
                already_cached_layers = [query_dict['layer'] for query_dict in query_results]  # layers that have already been cached
                layers_to_be_cached = [layer for layer in cleaned_layers if layer not in already_cached_layers]
                layer_list = layers_to_be_cached  # list of layers that haven't been cached because of a previous script exception
            except:  # probably means the database doesn't exist
                layer_list = cleaned_layers
        else:
            layer_list = cleaned_layers
        ws_results = (workspace, layer_list)
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
                         tile_format='image/png8', zoom_start=0, 
                         zoom_stop=3, threads=1, progress_check=5, exclude_layers=(),
                         latest_is_failure=False):
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
    db = SqliteDB()
    if not latest_is_failure:
        try:
            db.destroy_db()
        except:
            pass
        # create a new database if the last script run worked
        # otherwise append to the existing database
        db.create_db()
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
                # deal with overlay layers that do not have image/png8 as a caching option
                if ws_name in OVERLAY_WORKSPACES:  
                    tiling_config = sp_gwc.get_tile_cache_config()
                    config_content = tiling_config[1]
                    mime_formats = config_content['GeoServerLayer'].get('mimeFormats', [])
                    # check if image/png8 is one of the mimeFormats
                    # if it is not, add it
                    if tile_format not in mime_formats:
                        mime_formats.append(tile_format)
                        updated_cache_config_xml = sp_gwc.create_disable_enable_cache_xml(format_list=tuple(mime_formats),
                                                                                          style=style_name,
                                                                                          gridset_name=grid
                                                                                          )
                        update_cache_config = sp_gwc.disable_or_enable_cache(payload=updated_cache_config_xml)
                        update_config_message = 'Updated layer parmeters for {workspace}:{layer} - {status_code}'.format(workspace=ws_name,
                                                                                                                         layer=layer_name,
                                                                                                                         status_code=update_cache_config.status_code
                                                                                                                         )
                        print(update_config_message)
                        logging.info(update_config_message)
                seed_xml = sp_gwc.create_seed_xml(style=None,
                                                  tile_format=tile_format,
                                                  gridset_id=grid,
                                                  zoom_start=zoom_start, 
                                                  zoom_stop=zoom_stop,
                                                  threads=threads,
                                                  seed_type='seed'
                                                  )
                seed_request = sp_gwc.seed_request(seed_xml)
                url_message = 'Request URL: {0}'.format(seed_request.url)
                status_code_message = 'Status: {0}'.format(seed_request.status_code)
                print(url_message)
                print(status_code_message)
                array_length = 1
                while array_length > 0:
                    attempts = 0
                    while attempts <= 3:
                        try:
                            status = sp_gwc.query_task_status()
                            break
                        except ConnectionError:
                            print('Encountered a connection error.')
                            if attempts == 3:  # only try a total of 4 times
                                connection_error_message = 'Encountered a connection error 3 times. Aborting script.' 
                                print(connection_error_message)
                                raise SuccessiveConnectionError
                            attempts += 1
                            time.sleep(progress_check*2)  # provide sometime for the server to respond
                    print(datetime.datetime.now())
                    status_message = '{workspace}:{layer} - {progress}'.format(workspace=ws_name, 
                                                                               layer=layer_name, 
                                                                               progress=status[1]
                                                                               )
                    print(status_message)
                    long_array = status[1]['long-array-array']
                    try:
                        thread0 = long_array[0]
                        tile_count = thread0[1]
                        job_id = thread0[3]
                        job_tile_count = (job_id, tile_count)
                        if job_tile_count not in job_ids_with_tiles:
                            job_ids_with_tiles.append(job_tile_count)
                    except IndexError:
                        pass
                    array_length = len(long_array)
                    time.sleep(progress_check)
                finished = 'Finished - {workspace}:{layer}'.format(workspace=ws_name, layer=layer_name)
                complete_dt = str(datetime.datetime.now())
                # keep track of the layers that have already been cached
                # save time by checking this database when re-running after a failure
                db.insert_data(workspace=ws_name, layer=layer_name, complete_datetime=complete_dt)
            logging.info(finished)
            request_resps.append(seed_request)
    tile_counts = []
    for job_tile_tuple in job_ids_with_tiles:
        tile_count = float(job_tile_tuple[1])
        tile_counts.append(tile_count)
    print(tile_counts)
    tile_sum = sum(tile_counts)
    print('Total tiles: {0}'.format(tile_sum))
    return request_resps
            

def clean_layer_names(layer_names, model_number=None):
    clean_names = []
    for layer_name in layer_names:
        layer_sans_ws_name = layer_name.split(':')[1]
        clean_names.append(layer_sans_ws_name)
    if model_number is not None:
        filtered_names = regex_matching(clean_names, model_number)
    else:
        filtered_names = clean_names
    return filtered_names