'''
Created on Jan 30, 2015

@author: ayan
'''


def search_log(log_pathname, search_string):
    line_search_results = []
    with open(log_pathname, 'r') as log_file:
        content = log_file.readlines()
        for line in content:
            if search_string in line:
                line_search_results.append(line)
    return line_search_results


if __name__ == '__main__':
    results = search_log(r'C:\Users\ayan\git\sparrow_tile_cache\spdss_tile_cache\seed_log_20150129.log',
                         '- sparrow-flowline-reusable:'
                         )
    layers = []
    for result in results:
        split_result = result.split('sparrow-flowline-reusable:')[1]
        clean_result = split_result.replace('\n', '')
        layers.append(clean_result)
    print(list(set(layers)))