'''
Created on Jan 29, 2015

@author: ayan
'''
try:
    from secure_params import *
except ImportError:
    raise Exception('Failed to find user and password information.')

WORKSPACES = ['sparrow-catchment-reusable', 
              'sparrow-flowline-reusable'
              ]


DEV = {'GS_HOST': 'http://cida-eros-sparrowdev.er.usgs.gov:8081/sparrowgeoserver/rest',
       'GWC_HOST': 'http://cida-eros-sparrowdev.er.usgs.gov:8081/sparrowgeoserver/gwc/rest',
       'USER': USER,
       'PWD': PWD
       }

QA = DEV

PROD = DEV