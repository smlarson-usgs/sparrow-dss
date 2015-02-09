'''
Created on Jan 29, 2015

@author: ayan
'''
try:
    from secure_params import *
except ImportError:
    raise Exception('Failed to find user and password information.')

REUSABLE_WORKSPACES = ['sparrow-catchment-reusable', 
                       'sparrow-flowline-reusable',
                       ]

OVERLAY_WORKSPACES = ['huc8-overlay',
                      'catchment-overlay',
                      'reach-overlay'
                      ]

WORKSPACES = REUSABLE_WORKSPACES + OVERLAY_WORKSPACES


DEV = {'GS_HOST': 'http://cida-eros-sparrowdev.er.usgs.gov:8081/sparrowgeoserver/rest',
       'GWC_HOST': 'http://cida-eros-sparrowdev.er.usgs.gov:8081/sparrowgeoserver/gwc/rest',
       'USER': USER,
       'PWD': PWD
       }

QA = {'GS_HOST': None,
      'GWC_HOST': None,
      'USER': None,
      'PWD': None
      }

PROD = QA