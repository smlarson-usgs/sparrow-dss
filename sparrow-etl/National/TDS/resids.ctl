OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(SITE_NUM filler
,STATION_ID
,STATION_NAME
,HUC2 filler
,LAT
,LON
,MRB_ID
,WT filler
,NESTED_AREA filler
,ACTUAL
,PREDICT filler
,LN_ACTUAL filler
,LN_PREDICT filler
,LN_PRED_YIELD filler
,LN_RESID filler
,WEIGHTED_LN_RESID filler
,MAP_RESID filler
,BOOT_RESID filler
,LEVERAGE filler
,Z_MAP_RESID filler
,BL_RMC_HI filler
,BL_RMC_LO filler
,BL_RMNC_HI filler
,BL_RMNC_LO filler
,BL_VRIA filler
,BL_RMS_HI filler
,BL_RMS_LO filler
,BL_VRE filler
,BL_CS_HI filler
,BL_CS_MOD filler
,BL_CS_LO filler
,BL_GTC filler
,BL_GTL_HI filler
,BL_GTL_LO filler
,BL_GTCT_HI filler
,BL_GTCT_LO filler
,BL_GLCT_HI filler
,BL_GLCT_LO filler
,BL_GLFT_HI filler
,BL_GLFT_LO filler
,BL_ESCT filler
,BL_ESFT_HI filler
,BL_ESFT_MOD filler
,BL_ESFT_LO filler
,BL_SLS filler
,BL_SFT_HI filler
,BL_SFT_LO filler
,BL_SCT_HI filler
,BL_SCT_LO filler
,BL_HYDRIL filler
,BL_WATER filler
,BCULTIVATED filler
,BPASTURE filler
,BURBAN filler
,BROAD_SALT filler
,BNON_US_INFLOW filler
,BNOMINAL_INPUT filler
,BPPT filler
,BATM_DEP_2000 filler
,BSLP_DEG filler
,BCLAYAVE filler
,BSANDAVE filler
,BPCT_FOREST_EAST filler
,BPCT_FOREST_WEST filler
,BPVEG2000DRY filler
,BLOWFLOW filler
,BPCT_TILES filler
,BPCT_DITCHES filler
,BAG_PPT filler
,BAG_BFI filler
,BIRRIG_WATER_USE filler
,BPOP_2000 filler
,id filler
)
