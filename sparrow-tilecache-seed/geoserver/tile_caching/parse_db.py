'''
Created on Mar 25, 2015

@author: ayan
'''
from sqlite_db import SqliteDB

if __name__ == '__main__':
    
    sketchy_layers = ['catchment-overlay:mrb07_mrbe2rf1',
                      'catchment-overlay:national_mrb_e2rf1',
                      'catchment-overlay:mrb01_nhd',
                      'catchment-overlay:chesa_nhd',
                      'catchment-overlay:national_e2rf1',
                      'catchment-overlay:marb_mrbe2rf1',
                      'catchment-overlay:mrb04_mrbe2rf1',
                      'huc8-overlay:marb_mrbe2rf1',
                      'huc8-overlay:chesa_nhd',
                      'huc8-overlay:mrb03_mrbe2rf1',
                      'huc8-overlay:national_mrb_e2rf1',
                      'huc8-overlay:mrb05_mrbe2rf1',
                      'huc8-overlay:national_e2rf1',
                      'huc8-overlay:mrb07_mrbe2rf1',
                      'huc8-overlay:mrb02_mrbe2rf1',
                      'huc8-overlay:mrb06_mrbe2rf1',
                      'huc8-overlay:mrb04_mrbe2rf1',
                      'huc8-overlay:mrb01_nhd'
                      ]
    
    db_path = r'C:\Users\ayan\Desktop\sparrow_caching.db'
    s = SqliteDB(db_path)
    cached_layers = []
    result_set = s.query_db()
    for result in result_set:
        workspace_name = result['workspace']
        layer_name = result['layer']
        ws_layer = '{0}:{1}'.format(workspace_name, layer_name)
        cached_layers.append(ws_layer)
    with open(r'C:\Users\ayan\Desktop\caching_aftermath.txt', 'w') as f:
        for cached_lyr_str in cached_layers:
            if cached_lyr_str in sketchy_layers:
                mod_lyr_str = '**{0}'.format(cached_lyr_str)
                f.write(mod_lyr_str)
                f.write('\n')
            else:
                f.write(cached_lyr_str)
                f.write('\n')
    s.close_db()