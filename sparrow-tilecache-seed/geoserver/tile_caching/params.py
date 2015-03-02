'''
Created on Jan 29, 2015

@author: ayan
'''
try:
    from secure_params import USER, PWD
except ImportError:
    raise Exception('Failed to find user and password information.')

CATCHMENT_WORKSPACES = ['sparrow-catchment-reusable']
FLOWLINE_WORKSPACES = ['sparrow-flowline-reusable']

REUSABLE_WORKSPACES = CATCHMENT_WORKSPACES + FLOWLINE_WORKSPACES

OVERLAY_WORKSPACES = ['huc8-overlay',
                      'catchment-overlay',
                      'reach-overlay'
                      ]

WORKSPACES = REUSABLE_WORKSPACES + OVERLAY_WORKSPACES

USER = USER
PWD = PWD