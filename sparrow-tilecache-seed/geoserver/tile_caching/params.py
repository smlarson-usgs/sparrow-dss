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