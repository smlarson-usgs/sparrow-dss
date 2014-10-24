OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_PREDICT"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(MRB_ID
,PNAME filler
,RCHTYPE filler
,HEADFLAG filler
,TERMFLAG filler
,STATION_ID filler
,STATION_NAME filler
,DEMTAREA filler
,DEMIAREA filler
,Q_REACH filler
,FNODE filler
,TNODE filler
,HYDSEQ filler
,FRAC filler
,iftran filler
,delivery_target filler
,WT filler
,SITE_NUM filler
,REACH_LOAD filler
,PLOAD_TOTAL
,PLOAD_L_RMC_HI filler
,PLOAD_L_RMC_LO filler
,PLOAD_L_RMNC_HI filler
,PLOAD_L_RMNC_LO filler
,PLOAD_L_VRIA filler
,PLOAD_L_RMS_HI filler
,PLOAD_L_RMS_LO filler
,PLOAD_L_VRE filler
,PLOAD_L_CS_HI filler
,PLOAD_L_CS_MOD filler
,PLOAD_L_CS_LO filler
,PLOAD_L_GTC filler
,PLOAD_L_GTL_HI filler
,PLOAD_L_GTL_LO filler
,PLOAD_L_GTCT_HI filler
,PLOAD_L_GTCT_LO filler
,PLOAD_L_GLCT_HI filler
,PLOAD_L_GLCT_LO filler
,PLOAD_L_GLFT_HI filler
,PLOAD_L_GLFT_LO filler
,PLOAD_L_ESCT filler
,PLOAD_L_ESFT_HI filler
,PLOAD_L_ESFT_MOD filler
,PLOAD_L_ESFT_LO filler
,PLOAD_L_SLS filler
,PLOAD_L_SFT_HI filler
,PLOAD_L_SFT_LO filler
,PLOAD_L_SCT_HI filler
,PLOAD_L_SCT_LO filler
,PLOAD_L_HYDRIL filler
,PLOAD_L_WATER filler
,PLOAD_CULTIVATED filler
,PLOAD_PASTURE filler
,PLOAD_URBAN filler
,PLOAD_ROAD_SALT filler
,PLOAD_NON_US_INFLOW filler
,PLOAD_NOMINAL_INPUT filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_L_RMC_HI filler
,PLOAD_ND_L_RMC_LO filler
,PLOAD_ND_L_RMNC_HI filler
,PLOAD_ND_L_RMNC_LO filler
,PLOAD_ND_L_VRIA filler
,PLOAD_ND_L_RMS_HI filler
,PLOAD_ND_L_RMS_LO filler
,PLOAD_ND_L_VRE filler
,PLOAD_ND_L_CS_HI filler
,PLOAD_ND_L_CS_MOD filler
,PLOAD_ND_L_CS_LO filler
,PLOAD_ND_L_GTC filler
,PLOAD_ND_L_GTL_HI filler
,PLOAD_ND_L_GTL_LO filler
,PLOAD_ND_L_GTCT_HI filler
,PLOAD_ND_L_GTCT_LO filler
,PLOAD_ND_L_GLCT_HI filler
,PLOAD_ND_L_GLCT_LO filler
,PLOAD_ND_L_GLFT_HI filler
,PLOAD_ND_L_GLFT_LO filler
,PLOAD_ND_L_ESCT filler
,PLOAD_ND_L_ESFT_HI filler
,PLOAD_ND_L_ESFT_MOD filler
,PLOAD_ND_L_ESFT_LO filler
,PLOAD_ND_L_SLS filler
,PLOAD_ND_L_SFT_HI filler
,PLOAD_ND_L_SFT_LO filler
,PLOAD_ND_L_SCT_HI filler
,PLOAD_ND_L_SCT_LO filler
,PLOAD_ND_L_HYDRIL filler
,PLOAD_ND_L_WATER filler
,PLOAD_ND_CULTIVATED filler
,PLOAD_ND_PASTURE filler
,PLOAD_ND_URBAN filler
,PLOAD_ND_ROAD_SALT filler
,PLOAD_ND_NON_US_INFLOW filler
,PLOAD_ND_NOMINAL_INPUT filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_L_RMC_HI filler
,PLOAD_INC_L_RMC_LO filler
,PLOAD_INC_L_RMNC_HI filler
,PLOAD_INC_L_RMNC_LO filler
,PLOAD_INC_L_VRIA filler
,PLOAD_INC_L_RMS_HI filler
,PLOAD_INC_L_RMS_LO filler
,PLOAD_INC_L_VRE filler
,PLOAD_INC_L_CS_HI filler
,PLOAD_INC_L_CS_MOD filler
,PLOAD_INC_L_CS_LO filler
,PLOAD_INC_L_GTC filler
,PLOAD_INC_L_GTL_HI filler
,PLOAD_INC_L_GTL_LO filler
,PLOAD_INC_L_GTCT_HI filler
,PLOAD_INC_L_GTCT_LO filler
,PLOAD_INC_L_GLCT_HI filler
,PLOAD_INC_L_GLCT_LO filler
,PLOAD_INC_L_GLFT_HI filler
,PLOAD_INC_L_GLFT_LO filler
,PLOAD_INC_L_ESCT filler
,PLOAD_INC_L_ESFT_HI filler
,PLOAD_INC_L_ESFT_MOD filler
,PLOAD_INC_L_ESFT_LO filler
,PLOAD_INC_L_SLS filler
,PLOAD_INC_L_SFT_HI filler
,PLOAD_INC_L_SFT_LO filler
,PLOAD_INC_L_SCT_HI filler
,PLOAD_INC_L_SCT_LO filler
,PLOAD_INC_L_HYDRIL filler
,PLOAD_INC_L_WATER filler
,PLOAD_INC_CULTIVATED filler
,PLOAD_INC_PASTURE filler
,PLOAD_INC_URBAN filler
,PLOAD_INC_ROAD_SALT filler
,PLOAD_INC_NON_US_INFLOW filler
,PLOAD_INC_NOMINAL_INPUT filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_L_RMC_HI filler
,ci_hi_PLOAD_L_RMC_HI filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_L_RMC_LO filler
,ci_hi_PLOAD_L_RMC_LO filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_L_RMNC_HI filler
,ci_hi_PLOAD_L_RMNC_HI filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_L_RMNC_LO filler
,ci_hi_PLOAD_L_RMNC_LO filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_L_VRIA filler
,ci_hi_PLOAD_L_VRIA filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_L_RMS_HI filler
,ci_hi_PLOAD_L_RMS_HI filler
,MEAN_PLOAD_07
,SE_PLOAD_07
,ci_lo_PLOAD_L_RMS_LO filler
,ci_hi_PLOAD_L_RMS_LO filler
,MEAN_PLOAD_08
,SE_PLOAD_08
,ci_lo_PLOAD_L_VRE filler
,ci_hi_PLOAD_L_VRE filler
,MEAN_PLOAD_09
,SE_PLOAD_09
,ci_lo_PLOAD_L_CS_HI filler
,ci_hi_PLOAD_L_CS_HI filler
,MEAN_PLOAD_10
,SE_PLOAD_10
,ci_lo_PLOAD_L_CS_MOD filler
,ci_hi_PLOAD_L_CS_MOD filler
,MEAN_PLOAD_11
,SE_PLOAD_11
,ci_lo_PLOAD_L_CS_LO filler
,ci_hi_PLOAD_L_CS_LO filler
,MEAN_PLOAD_12
,SE_PLOAD_12
,ci_lo_PLOAD_L_GTC filler
,ci_hi_PLOAD_L_GTC filler
,MEAN_PLOAD_13
,SE_PLOAD_13
,ci_lo_PLOAD_L_GTL_HI filler
,ci_hi_PLOAD_L_GTL_HI filler
,MEAN_PLOAD_14
,SE_PLOAD_14
,ci_lo_PLOAD_L_GTL_LO filler
,ci_hi_PLOAD_L_GTL_LO filler
,MEAN_PLOAD_15
,SE_PLOAD_15
,ci_lo_PLOAD_L_GTCT_HI filler
,ci_hi_PLOAD_L_GTCT_HI filler
,MEAN_PLOAD_16
,SE_PLOAD_16
,ci_lo_PLOAD_L_GTCT_LO filler
,ci_hi_PLOAD_L_GTCT_LO filler
,MEAN_PLOAD_17
,SE_PLOAD_17
,ci_lo_PLOAD_L_GLCT_HI filler
,ci_hi_PLOAD_L_GLCT_HI filler
,MEAN_PLOAD_18
,SE_PLOAD_18
,ci_lo_PLOAD_L_GLCT_LO filler
,ci_hi_PLOAD_L_GLCT_LO filler
,MEAN_PLOAD_19
,SE_PLOAD_19
,ci_lo_PLOAD_L_GLFT_HI filler
,ci_hi_PLOAD_L_GLFT_HI filler
,MEAN_PLOAD_20
,SE_PLOAD_20
,ci_lo_PLOAD_L_GLFT_LO filler
,ci_hi_PLOAD_L_GLFT_LO filler
,MEAN_PLOAD_21
,SE_PLOAD_21
,ci_lo_PLOAD_L_ESCT filler
,ci_hi_PLOAD_L_ESCT filler
,MEAN_PLOAD_22
,SE_PLOAD_22
,ci_lo_PLOAD_L_ESFT_HI filler
,ci_hi_PLOAD_L_ESFT_HI filler
,MEAN_PLOAD_23
,SE_PLOAD_23
,ci_lo_PLOAD_L_ESFT_MOD filler
,ci_hi_PLOAD_L_ESFT_MOD filler
,MEAN_PLOAD_24
,SE_PLOAD_24
,ci_lo_PLOAD_L_ESFT_LO filler
,ci_hi_PLOAD_L_ESFT_LO filler
,MEAN_PLOAD_25
,SE_PLOAD_25
,ci_lo_PLOAD_L_SLS filler
,ci_hi_PLOAD_L_SLS filler
,MEAN_PLOAD_26
,SE_PLOAD_26
,ci_lo_PLOAD_L_SFT_HI filler
,ci_hi_PLOAD_L_SFT_HI filler
,MEAN_PLOAD_27
,SE_PLOAD_27
,ci_lo_PLOAD_L_SFT_LO filler
,ci_hi_PLOAD_L_SFT_LO filler
,MEAN_PLOAD_28
,SE_PLOAD_28
,ci_lo_PLOAD_L_SCT_HI filler
,ci_hi_PLOAD_L_SCT_HI filler
,MEAN_PLOAD_29
,SE_PLOAD_29
,ci_lo_PLOAD_L_SCT_LO filler
,ci_hi_PLOAD_L_SCT_LO filler
,MEAN_PLOAD_30
,SE_PLOAD_30
,ci_lo_PLOAD_L_HYDRIL filler
,ci_hi_PLOAD_L_HYDRIL filler
,MEAN_PLOAD_31
,SE_PLOAD_31
,ci_lo_PLOAD_L_WATER filler
,ci_hi_PLOAD_L_WATER filler
,MEAN_PLOAD_32
,SE_PLOAD_32
,ci_lo_PLOAD_CULTIVATED filler
,ci_hi_PLOAD_CULTIVATED filler
,MEAN_PLOAD_33
,SE_PLOAD_33
,ci_lo_PLOAD_PASTURE filler
,ci_hi_PLOAD_PASTURE filler
,MEAN_PLOAD_34
,SE_PLOAD_34
,ci_lo_PLOAD_URBAN filler
,ci_hi_PLOAD_URBAN filler
,MEAN_PLOAD_35
,SE_PLOAD_35
,ci_lo_PLOAD_ROAD_SALT filler
,ci_hi_PLOAD_ROAD_SALT filler
,MEAN_PLOAD_36
,SE_PLOAD_36
,ci_lo_PLOAD_NON_US_INFLOW filler
,ci_hi_PLOAD_NON_US_INFLOW filler
,MEAN_PLOAD_37
,SE_PLOAD_37
,ci_lo_PLOAD_NOMINAL_INPUT filler
,ci_hi_PLOAD_NOMINAL_INPUT filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_L_RMC_HI filler
,SE_PLOAD_ND_L_RMC_HI filler
,ci_lo_PLOAD_ND_L_RMC_HI filler
,ci_hi_PLOAD_ND_L_RMC_HI filler
,MEAN_PLOAD_ND_L_RMC_LO filler
,SE_PLOAD_ND_L_RMC_LO filler
,ci_lo_PLOAD_ND_L_RMC_LO filler
,ci_hi_PLOAD_ND_L_RMC_LO filler
,MEAN_PLOAD_ND_L_RMNC_HI filler
,SE_PLOAD_ND_L_RMNC_HI filler
,ci_lo_PLOAD_ND_L_RMNC_HI filler
,ci_hi_PLOAD_ND_L_RMNC_HI filler
,MEAN_PLOAD_ND_L_RMNC_LO filler
,SE_PLOAD_ND_L_RMNC_LO filler
,ci_lo_PLOAD_ND_L_RMNC_LO filler
,ci_hi_PLOAD_ND_L_RMNC_LO filler
,MEAN_PLOAD_ND_L_VRIA filler
,SE_PLOAD_ND_L_VRIA filler
,ci_lo_PLOAD_ND_L_VRIA filler
,ci_hi_PLOAD_ND_L_VRIA filler
,MEAN_PLOAD_ND_L_RMS_HI filler
,SE_PLOAD_ND_L_RMS_HI filler
,ci_lo_PLOAD_ND_L_RMS_HI filler
,ci_hi_PLOAD_ND_L_RMS_HI filler
,MEAN_PLOAD_ND_L_RMS_LO filler
,SE_PLOAD_ND_L_RMS_LO filler
,ci_lo_PLOAD_ND_L_RMS_LO filler
,ci_hi_PLOAD_ND_L_RMS_LO filler
,MEAN_PLOAD_ND_L_VRE filler
,SE_PLOAD_ND_L_VRE filler
,ci_lo_PLOAD_ND_L_VRE filler
,ci_hi_PLOAD_ND_L_VRE filler
,MEAN_PLOAD_ND_L_CS_HI filler
,SE_PLOAD_ND_L_CS_HI filler
,ci_lo_PLOAD_ND_L_CS_HI filler
,ci_hi_PLOAD_ND_L_CS_HI filler
,MEAN_PLOAD_ND_L_CS_MOD filler
,SE_PLOAD_ND_L_CS_MOD filler
,ci_lo_PLOAD_ND_L_CS_MOD filler
,ci_hi_PLOAD_ND_L_CS_MOD filler
,MEAN_PLOAD_ND_L_CS_LO filler
,SE_PLOAD_ND_L_CS_LO filler
,ci_lo_PLOAD_ND_L_CS_LO filler
,ci_hi_PLOAD_ND_L_CS_LO filler
,MEAN_PLOAD_ND_L_GTC filler
,SE_PLOAD_ND_L_GTC filler
,ci_lo_PLOAD_ND_L_GTC filler
,ci_hi_PLOAD_ND_L_GTC filler
,MEAN_PLOAD_ND_L_GTL_HI filler
,SE_PLOAD_ND_L_GTL_HI filler
,ci_lo_PLOAD_ND_L_GTL_HI filler
,ci_hi_PLOAD_ND_L_GTL_HI filler
,MEAN_PLOAD_ND_L_GTL_LO filler
,SE_PLOAD_ND_L_GTL_LO filler
,ci_lo_PLOAD_ND_L_GTL_LO filler
,ci_hi_PLOAD_ND_L_GTL_LO filler
,MEAN_PLOAD_ND_L_GTCT_HI filler
,SE_PLOAD_ND_L_GTCT_HI filler
,ci_lo_PLOAD_ND_L_GTCT_HI filler
,ci_hi_PLOAD_ND_L_GTCT_HI filler
,MEAN_PLOAD_ND_L_GTCT_LO filler
,SE_PLOAD_ND_L_GTCT_LO filler
,ci_lo_PLOAD_ND_L_GTCT_LO filler
,ci_hi_PLOAD_ND_L_GTCT_LO filler
,MEAN_PLOAD_ND_L_GLCT_HI filler
,SE_PLOAD_ND_L_GLCT_HI filler
,ci_lo_PLOAD_ND_L_GLCT_HI filler
,ci_hi_PLOAD_ND_L_GLCT_HI filler
,MEAN_PLOAD_ND_L_GLCT_LO filler
,SE_PLOAD_ND_L_GLCT_LO filler
,ci_lo_PLOAD_ND_L_GLCT_LO filler
,ci_hi_PLOAD_ND_L_GLCT_LO filler
,MEAN_PLOAD_ND_L_GLFT_HI filler
,SE_PLOAD_ND_L_GLFT_HI filler
,ci_lo_PLOAD_ND_L_GLFT_HI filler
,ci_hi_PLOAD_ND_L_GLFT_HI filler
,MEAN_PLOAD_ND_L_GLFT_LO filler
,SE_PLOAD_ND_L_GLFT_LO filler
,ci_lo_PLOAD_ND_L_GLFT_LO filler
,ci_hi_PLOAD_ND_L_GLFT_LO filler
,MEAN_PLOAD_ND_L_ESCT filler
,SE_PLOAD_ND_L_ESCT filler
,ci_lo_PLOAD_ND_L_ESCT filler
,ci_hi_PLOAD_ND_L_ESCT filler
,MEAN_PLOAD_ND_L_ESFT_HI filler
,SE_PLOAD_ND_L_ESFT_HI filler
,ci_lo_PLOAD_ND_L_ESFT_HI filler
,ci_hi_PLOAD_ND_L_ESFT_HI filler
,MEAN_PLOAD_ND_L_ESFT_MOD filler
,SE_PLOAD_ND_L_ESFT_MOD filler
,ci_lo_PLOAD_ND_L_ESFT_MOD filler
,ci_hi_PLOAD_ND_L_ESFT_MOD filler
,MEAN_PLOAD_ND_L_ESFT_LO filler
,SE_PLOAD_ND_L_ESFT_LO filler
,ci_lo_PLOAD_ND_L_ESFT_LO filler
,ci_hi_PLOAD_ND_L_ESFT_LO filler
,MEAN_PLOAD_ND_L_SLS filler
,SE_PLOAD_ND_L_SLS filler
,ci_lo_PLOAD_ND_L_SLS filler
,ci_hi_PLOAD_ND_L_SLS filler
,MEAN_PLOAD_ND_L_SFT_HI filler
,SE_PLOAD_ND_L_SFT_HI filler
,ci_lo_PLOAD_ND_L_SFT_HI filler
,ci_hi_PLOAD_ND_L_SFT_HI filler
,MEAN_PLOAD_ND_L_SFT_LO filler
,SE_PLOAD_ND_L_SFT_LO filler
,ci_lo_PLOAD_ND_L_SFT_LO filler
,ci_hi_PLOAD_ND_L_SFT_LO filler
,MEAN_PLOAD_ND_L_SCT_HI filler
,SE_PLOAD_ND_L_SCT_HI filler
,ci_lo_PLOAD_ND_L_SCT_HI filler
,ci_hi_PLOAD_ND_L_SCT_HI filler
,MEAN_PLOAD_ND_L_SCT_LO filler
,SE_PLOAD_ND_L_SCT_LO filler
,ci_lo_PLOAD_ND_L_SCT_LO filler
,ci_hi_PLOAD_ND_L_SCT_LO filler
,MEAN_PLOAD_ND_L_HYDRIL filler
,SE_PLOAD_ND_L_HYDRIL filler
,ci_lo_PLOAD_ND_L_HYDRIL filler
,ci_hi_PLOAD_ND_L_HYDRIL filler
,MEAN_PLOAD_ND_L_WATER filler
,SE_PLOAD_ND_L_WATER filler
,ci_lo_PLOAD_ND_L_WATER filler
,ci_hi_PLOAD_ND_L_WATER filler
,MEAN_PLOAD_ND_CULTIVATED filler
,SE_PLOAD_ND_CULTIVATED filler
,ci_lo_PLOAD_ND_CULTIVATED filler
,ci_hi_PLOAD_ND_CULTIVATED filler
,MEAN_PLOAD_ND_PASTURE filler
,SE_PLOAD_ND_PASTURE filler
,ci_lo_PLOAD_ND_PASTURE filler
,ci_hi_PLOAD_ND_PASTURE filler
,MEAN_PLOAD_ND_URBAN filler
,SE_PLOAD_ND_URBAN filler
,ci_lo_PLOAD_ND_URBAN filler
,ci_hi_PLOAD_ND_URBAN filler
,MEAN_PLOAD_ND_ROAD_SALT filler
,SE_PLOAD_ND_ROAD_SALT filler
,ci_lo_PLOAD_ND_ROAD_SALT filler
,ci_hi_PLOAD_ND_ROAD_SALT filler
,MEAN_PLOAD_ND_NON_US_INFLOW filler
,SE_PLOAD_ND_NON_US_INFLOW filler
,ci_lo_PLOAD_ND_NON_US_INFLOW filler
,ci_hi_PLOAD_ND_NON_US_INFLOW filler
,MEAN_PLOAD_ND_NOMINAL_INPUT filler
,SE_PLOAD_ND_NOMINAL_INPUT filler
,ci_lo_PLOAD_ND_NOMINAL_INPUT filler
,ci_hi_PLOAD_ND_NOMINAL_INPUT filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_L_RMC_HI filler
,ci_hi_PLOAD_INC_L_RMC_HI filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_L_RMC_LO filler
,ci_hi_PLOAD_INC_L_RMC_LO filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_L_RMNC_HI filler
,ci_hi_PLOAD_INC_L_RMNC_HI filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_L_RMNC_LO filler
,ci_hi_PLOAD_INC_L_RMNC_LO filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_L_VRIA filler
,ci_hi_PLOAD_INC_L_VRIA filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_L_RMS_HI filler
,ci_hi_PLOAD_INC_L_RMS_HI filler
,MEAN_PLOAD_INC_07
,SE_PLOAD_INC_07
,ci_lo_PLOAD_INC_L_RMS_LO filler
,ci_hi_PLOAD_INC_L_RMS_LO filler
,MEAN_PLOAD_INC_08
,SE_PLOAD_INC_08
,ci_lo_PLOAD_INC_L_VRE filler
,ci_hi_PLOAD_INC_L_VRE filler
,MEAN_PLOAD_INC_09
,SE_PLOAD_INC_09
,ci_lo_PLOAD_INC_L_CS_HI filler
,ci_hi_PLOAD_INC_L_CS_HI filler
,MEAN_PLOAD_INC_10
,SE_PLOAD_INC_10
,ci_lo_PLOAD_INC_L_CS_MOD filler
,ci_hi_PLOAD_INC_L_CS_MOD filler
,MEAN_PLOAD_INC_11
,SE_PLOAD_INC_11
,ci_lo_PLOAD_INC_L_CS_LO filler
,ci_hi_PLOAD_INC_L_CS_LO filler
,MEAN_PLOAD_INC_12
,SE_PLOAD_INC_12
,ci_lo_PLOAD_INC_L_GTC filler
,ci_hi_PLOAD_INC_L_GTC filler
,MEAN_PLOAD_INC_13
,SE_PLOAD_INC_13
,ci_lo_PLOAD_INC_L_GTL_HI filler
,ci_hi_PLOAD_INC_L_GTL_HI filler
,MEAN_PLOAD_INC_14
,SE_PLOAD_INC_14
,ci_lo_PLOAD_INC_L_GTL_LO filler
,ci_hi_PLOAD_INC_L_GTL_LO filler
,MEAN_PLOAD_INC_15
,SE_PLOAD_INC_15
,ci_lo_PLOAD_INC_L_GTCT_HI filler
,ci_hi_PLOAD_INC_L_GTCT_HI filler
,MEAN_PLOAD_INC_16
,SE_PLOAD_INC_16
,ci_lo_PLOAD_INC_L_GTCT_LO filler
,ci_hi_PLOAD_INC_L_GTCT_LO filler
,MEAN_PLOAD_INC_17
,SE_PLOAD_INC_17
,ci_lo_PLOAD_INC_L_GLCT_HI filler
,ci_hi_PLOAD_INC_L_GLCT_HI filler
,MEAN_PLOAD_INC_18
,SE_PLOAD_INC_18
,ci_lo_PLOAD_INC_L_GLCT_LO filler
,ci_hi_PLOAD_INC_L_GLCT_LO filler
,MEAN_PLOAD_INC_19
,SE_PLOAD_INC_19
,ci_lo_PLOAD_INC_L_GLFT_HI filler
,ci_hi_PLOAD_INC_L_GLFT_HI filler
,MEAN_PLOAD_INC_20
,SE_PLOAD_INC_20
,ci_lo_PLOAD_INC_L_GLFT_LO filler
,ci_hi_PLOAD_INC_L_GLFT_LO filler
,MEAN_PLOAD_INC_21
,SE_PLOAD_INC_21
,ci_lo_PLOAD_INC_L_ESCT filler
,ci_hi_PLOAD_INC_L_ESCT filler
,MEAN_PLOAD_INC_22
,SE_PLOAD_INC_22
,ci_lo_PLOAD_INC_L_ESFT_HI filler
,ci_hi_PLOAD_INC_L_ESFT_HI filler
,MEAN_PLOAD_INC_23
,SE_PLOAD_INC_23
,ci_lo_PLOAD_INC_L_ESFT_MOD filler
,ci_hi_PLOAD_INC_L_ESFT_MOD filler
,MEAN_PLOAD_INC_24
,SE_PLOAD_INC_24
,ci_lo_PLOAD_INC_L_ESFT_LO filler
,ci_hi_PLOAD_INC_L_ESFT_LO filler
,MEAN_PLOAD_INC_25
,SE_PLOAD_INC_25
,ci_lo_PLOAD_INC_L_SLS filler
,ci_hi_PLOAD_INC_L_SLS filler
,MEAN_PLOAD_INC_26
,SE_PLOAD_INC_26
,ci_lo_PLOAD_INC_L_SFT_HI filler
,ci_hi_PLOAD_INC_L_SFT_HI filler
,MEAN_PLOAD_INC_27
,SE_PLOAD_INC_27
,ci_lo_PLOAD_INC_L_SFT_LO filler
,ci_hi_PLOAD_INC_L_SFT_LO filler
,MEAN_PLOAD_INC_28
,SE_PLOAD_INC_28
,ci_lo_PLOAD_INC_L_SCT_HI filler
,ci_hi_PLOAD_INC_L_SCT_HI filler
,MEAN_PLOAD_INC_29
,SE_PLOAD_INC_29
,ci_lo_PLOAD_INC_L_SCT_LO filler
,ci_hi_PLOAD_INC_L_SCT_LO filler
,MEAN_PLOAD_INC_30
,SE_PLOAD_INC_30
,ci_lo_PLOAD_INC_L_HYDRIL filler
,ci_hi_PLOAD_INC_L_HYDRIL filler
,MEAN_PLOAD_INC_31
,SE_PLOAD_INC_31
,ci_lo_PLOAD_INC_L_WATER filler
,ci_hi_PLOAD_INC_L_WATER filler
,MEAN_PLOAD_INC_32
,SE_PLOAD_INC_32
,ci_lo_PLOAD_INC_CULTIVATED filler
,ci_hi_PLOAD_INC_CULTIVATED filler
,MEAN_PLOAD_INC_33
,SE_PLOAD_INC_33
,ci_lo_PLOAD_INC_PASTURE filler
,ci_hi_PLOAD_INC_PASTURE filler
,MEAN_PLOAD_INC_34
,SE_PLOAD_INC_34
,ci_lo_PLOAD_INC_URBAN filler
,ci_hi_PLOAD_INC_URBAN filler
,MEAN_PLOAD_INC_35
,SE_PLOAD_INC_35
,ci_lo_PLOAD_INC_ROAD_SALT filler
,ci_hi_PLOAD_INC_ROAD_SALT filler
,MEAN_PLOAD_INC_36
,SE_PLOAD_INC_36
,ci_lo_PLOAD_INC_NON_US_INFLOW filler
,ci_hi_PLOAD_INC_NON_US_INFLOW filler
,MEAN_PLOAD_INC_37
,SE_PLOAD_INC_37
,ci_lo_PLOAD_INC_NOMINAL_INPUT filler
,ci_hi_PLOAD_INC_NOMINAL_INPUT filler
,MEAN_RES_DECAY filler
,SE_RES_DECAY filler
,ci_lo_RES_DECAY filler
,ci_hi_RES_DECAY filler
,MEAN_DEL_FRAC filler
,SE_DEL_FRAC filler
,ci_lo_DEL_FRAC filler
,ci_hi_DEL_FRAC filler
,total_yield filler
,inc_total_yield filler
,concentration filler
,map_del_frac filler
,sh_L_RMC_HI filler
,sh_L_RMC_LO filler
,sh_L_RMNC_HI filler
,sh_L_RMNC_LO filler
,sh_L_VRIA filler
,sh_L_RMS_HI filler
,sh_L_RMS_LO filler
,sh_L_VRE filler
,sh_L_CS_HI filler
,sh_L_CS_MOD filler
,sh_L_CS_LO filler
,sh_L_GTC filler
,sh_L_GTL_HI filler
,sh_L_GTL_LO filler
,sh_L_GTCT_HI filler
,sh_L_GTCT_LO filler
,sh_L_GLCT_HI filler
,sh_L_GLCT_LO filler
,sh_L_GLFT_HI filler
,sh_L_GLFT_LO filler
,sh_L_ESCT filler
,sh_L_ESFT_HI filler
,sh_L_ESFT_MOD filler
,sh_L_ESFT_LO filler
,sh_L_SLS filler
,sh_L_SFT_HI filler
,sh_L_SFT_LO filler
,sh_L_SCT_HI filler
,sh_L_SCT_LO filler
,sh_L_HYDRIL filler
,sh_L_WATER filler
,sh_CULTIVATED filler
,sh_PASTURE filler
,sh_URBAN filler
,sh_ROAD_SALT filler
,sh_NON_US_INFLOW filler
,sh_NOMINAL_INPUT filler
)
