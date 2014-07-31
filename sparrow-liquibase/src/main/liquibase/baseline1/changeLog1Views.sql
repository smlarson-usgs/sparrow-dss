--liquibase formatted sql

--This is for the sparrow_dss schema
 
--changeset drsteini:views1a
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_ATTRIB_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   FULL_IDENTIFIER,
   HYDSEQ,
   FNODE,
   TNODE,
   IFTRAN,
   FRAC,
   REACH_NAME,
   OPEN_WATER_NAME,
   MEANQ,
   MEANV,
   CATCH_AREA,
   CUM_CATCH_AREA,
   REACH_LENGTH,
   HUC2,
   HUC4,
   HUC6,
   HUC8,
   HEAD_REACH,
   SHORE_REACH,
   TERM_TRANS,
   TERM_ESTUARY,
   TERM_NONCONNECT,
   EDANAME,
   EDACODE,
   SOURCE,
   HUC2_NAME,
   HUC4_NAME,
   HUC6_NAME,
   HUC8_NAME
)
AS
   ( /* Selects attribs from MODEL_REACH and MODEL_REACH_ATTRIB for model reaches that have a corresponding row in the MODEL_REACH_ATTRIB table. */
    SELECT model.SPARROW_MODEL_ID SPARROW_MODEL_ID,
           model.MODEL_REACH_ID MODEL_REACH_ID,
           model.IDENTIFIER IDENTIFIER,
           model.FULL_IDENTIFIER FULL_IDENTIFIER,
           model.HYDSEQ HYDSEQ,
           model.FNODE FNODE,
           model.TNODE TNODE,
           model.IFTRAN,
           model.FRAC FRAC,
           attrib.REACH_NAME REACH_NAME,
           attrib.OPEN_WATER_NAME OPEN_WATER_NAME,
           attrib.MEANQ MEANQ,
           attrib.MEANV MEANV,
           attrib.CATCH_AREA CATCH_AREA,
           attrib.CUM_CATCH_AREA CUM_CATCH_AREA,
           attrib.REACH_LENGTH REACH_LENGTH,
           attrib.HUC2 HUC2,
           attrib.HUC4 HUC4,
           attrib.HUC6 HUC6,
           attrib.HUC8 HUC8,
           attrib.HEAD_REACH HEAD_REACH,
           attrib.SHORE_REACH SHORE_REACH,
           attrib.TERM_TRANS TERM_TRANS,
           attrib.TERM_ESTUARY TERM_ESTUARY,
           attrib.TERM_NONCONNECT TERM_NONCONNECT,
           attrib.EDANAME EDANAME,
           attrib.EDACODE EDACODE,
           'model' source,
           attrib.HUC2_NAME HUC2_NAME,
           attrib.HUC4_NAME HUC4_NAME,
           attrib.HUC6_NAME HUC6_NAME,
           attrib.HUC8_NAME HUC8_NAME
      FROM    MODEL_REACH model
           INNER JOIN
              MODEL_REACH_ATTRIB attrib
           ON (model.MODEL_REACH_ID = attrib.MODEL_REACH_ID)
    UNION ALL /* Selects attribs from MODEL_REACH and ENH_ATTRIB_VW for model reaches that do not have a corresponding row in the MODEL_REACH_ATTRIB table, but do have a corresponding row in the ENH_ATTRIB_VW view. */
    SELECT model.SPARROW_MODEL_ID SPARROW_MODEL_ID,
           model.MODEL_REACH_ID MODEL_REACH_ID,
           model.IDENTIFIER IDENTIFIER,
           model.FULL_IDENTIFIER FULL_IDENTIFIER,
           model.HYDSEQ HYDSEQ,
           model.FNODE FNODE,
           model.TNODE TNODE,
           model.IFTRAN,
           model.FRAC FRAC,
           attrib.REACH_NAME REACH_NAME,
           attrib.OPEN_WATER_NAME OPEN_WATER_NAME,
           attrib.MEANQ MEANQ,
           attrib.MEANV MEANV,
           attrib.CATCH_AREA CATCH_AREA,
           attrib.CUM_CATCH_AREA CUM_CATCH_AREA,
           attrib.REACH_LENGTH REACH_LENGTH,
           attrib.HUC2 HUC2,
           attrib.HUC4 HUC4,
           attrib.HUC6 HUC6,
           attrib.HUC8 HUC8,
           attrib.HEAD_REACH HEAD_REACH,
           attrib.SHORE_REACH SHORE_REACH,
           attrib.TERM_TRANS TERM_TRANS,
           attrib.TERM_ESTUARY TERM_ESTUARY,
           attrib.TERM_NONCONNECT TERM_NONCONNECT,
           attrib.EDANAME EDANAME,
           attrib.EDACDA EDACODE,
           'enhanced' source,
           attrib.HUC2_NAME HUC2_NAME,
           attrib.HUC4_NAME HUC4_NAME,
           attrib.HUC6_NAME HUC6_NAME,
           attrib.HUC8_NAME HUC8_NAME
      FROM    MODEL_REACH model
           INNER JOIN
              STREAM_NETWORK.ENH_ATTRIB_VW attrib
           ON (model.ENH_REACH_ID = attrib.ENH_REACH_ID)
     WHERE NOT EXISTS
              (SELECT MODEL_REACH_ID
                 FROM MODEL_REACH_ATTRIB modattrib
                WHERE modattrib.MODEL_REACH_ID = model.MODEL_REACH_ID)
    UNION ALL /* Selects attribs from MODEL_REACH only, setting values normally found in the XXX_ATTRIB tables to null. Applies to cases where a model reach has no corresponding row in ENH_ATTRIB_VW (i.e., reach is added at the model level) and does not have a row in MODEL_REACH_ATTRIB. This is a degenerate case: The attribs are not provided in the STREAM_NETWORK schema or at the model level and are basically missing. */
    SELECT model.SPARROW_MODEL_ID SPARROW_MODEL_ID,
           model.MODEL_REACH_ID MODEL_REACH_ID,
           model.IDENTIFIER IDENTIFIER,
           model.FULL_IDENTIFIER FULL_IDENTIFIER,
           model.HYDSEQ HYDSEQ,
           model.FNODE FNODE,
           model.TNODE TNODE,
           model.IFTRAN,
           model.FRAC FRAC,
           NULL REACH_NAME,
           NULL OPEN_WATER_NAME,
           NULL MEANQ,
           NULL MEANV,
           NULL CATCH_AREA,
           NULL CUM_CATCH_AREA,
           NULL REACH_LENGTH,
           NULL HUC2,
           NULL HUC4,
           NULL HUC6,
           NULL HUC8,
           NULL HEAD_REACH,
           NULL SHORE_REACH,
           NULL TERM_TRANS,
           NULL TERM_ESTUARY,
           NULL TERM_NONCONNECT,
           NULL EDANAME,
           NULL EDACODE,
           'model_no_attrib' source,
           NULL HUC2_NAME,
           NULL HUC4_NAME,
           NULL HUC6_NAME,
           NULL HUC8_NAME
      FROM MODEL_REACH model
     WHERE     NOT EXISTS
                  (SELECT ENH_REACH_ID
                     FROM STREAM_NETWORK.ENH_ATTRIB_VW enhvw
                    WHERE enhvw.ENH_REACH_ID = model.ENH_REACH_ID)
           AND NOT EXISTS
                  (SELECT MODEL_REACH_ID
                     FROM MODEL_REACH_ATTRIB modattrib
                    WHERE modattrib.MODEL_REACH_ID = model.MODEL_REACH_ID));
--rollback DROP VIEW SPARROW_DSS.MODEL_ATTRIB_VW;

--changeset drsteini:views1b
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_CALIB_SITE_VW
(
   MODEL_REACH_ID,
   IDENTIFIER,
   SPARROW_MODEL_ID,
   STATION_NAME,
   ACTUAL,
   SITE_GEOM,
   LATITUDE,
   LONGITUDE,
   PREDICT,
   STATION_ID
)
AS
   SELECT calib.model_reach_id AS model_reach_id,
          reach.identifier AS identifier,
          reach.sparrow_model_id AS sparrow_model_id,
          calib.station_name AS station_name,
          calib.actual AS actual,
          calib.site_geom AS site_geom,
          calib.latitude AS latitude,
          calib.longitude AS longitude,
          calib.predict AS predict,
          calib.station_id AS station_id
     FROM    model_reach reach
          JOIN
             model_calib_sites calib
          ON (reach.model_reach_id = calib.model_reach_id);
--rollback DROP VIEW SPARROW_DSS.MODEL_CALIB_SITE_VW;

--changeset drsteini:views1c
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_22_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
                Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
                .
                The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
                but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
                geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
                */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 22
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 22;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_22_VW;

--changeset drsteini:views1d
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_23_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 23
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 23;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_23_VW;

--changeset drsteini:views1e
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_24_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
                Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
                .
                The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
                but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
                geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
                */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 24
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 24;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_24_VW;

--changeset drsteini:views1f
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_30_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 30
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 30;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_30_VW;

--changeset drsteini:views1g
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_35_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 35
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 35;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_35_VW;

--changeset drsteini:views1h
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_36_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 36
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 36;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_36_VW;

--changeset drsteini:views1i
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_41_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 41
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 41;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_41_VW;

--changeset drsteini:views1j
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_42_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 42
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 42;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_42_VW;

--changeset drsteini:views1k
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_43_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 43
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 43;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_43_VW;

--changeset drsteini:views1l
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_44_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 44
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 44;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_44_VW;

--changeset drsteini:views1m
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_49_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 49
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 49;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_49_VW;

--changeset drsteini:views1n
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_50_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 50
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 50;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_50_VW;

--changeset drsteini:views1o
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_51_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
                               Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
                               .
                               The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
                               but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
                               geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
                               */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 51
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 51;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_51_VW;

--changeset drsteini:views1p
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_51_VW_FILTER
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
                            Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
                            .
                            The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
                            but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
                            geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
                            */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM MODEL_REACH model,
          STREAM_NETWORK.ENH_GEOM_VW enh,
          STREAM_NETWORK.ENH_REACH_ATTRIB attrib
    WHERE     model.ENH_REACH_ID = enh.ENH_REACH_ID
          AND model.ENH_REACH_ID = attrib.ENH_REACH_ID
          AND model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 51
          AND attrib.cum_catch_area > 20
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM MODEL_REACH model, MODEL_REACH_GEOM geo, MODEL_REACH_ATTRIB attrib
    WHERE     model.MODEL_REACH_ID = geo.MODEL_REACH_ID
          AND model.MODEL_REACH_ID = attrib.MODEL_REACH_ID
          AND model.sparrow_model_id = 51
          AND attrib.cum_catch_area > 20;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_51_VW_FILTER;

--changeset drsteini:views1q
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_52_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
                            Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
                            .
                            The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
                            but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
                            geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
                            */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 52
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 52;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_52_VW;

--changeset drsteini:views1r
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_52_VW_FILTER
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
                            Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
                            .
                            The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
                            but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
                            geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
                            */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM MODEL_REACH model,
          STREAM_NETWORK.ENH_GEOM_VW enh,
          STREAM_NETWORK.ENH_REACH_ATTRIB attrib
    WHERE     model.ENH_REACH_ID = enh.ENH_REACH_ID
          AND model.ENH_REACH_ID = attrib.ENH_REACH_ID
          AND model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 52
          AND attrib.cum_catch_area > 20
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM MODEL_REACH model, MODEL_REACH_GEOM geo, MODEL_REACH_ATTRIB attrib
    WHERE     model.MODEL_REACH_ID = geo.MODEL_REACH_ID
          AND model.MODEL_REACH_ID = attrib.MODEL_REACH_ID
          AND model.sparrow_model_id = 52
          AND attrib.cum_catch_area > 20;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_52_VW_FILTER;

--changeset drsteini:views1s
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_53_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
                Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
                .
                The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
                but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
                geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
                */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 53
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 53;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_53_VW;

--changeset drsteini:views1t
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_54_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
                   Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
                   .
                   The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
                   but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
                   geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
                   */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 54
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 54;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_54_VW;

--changeset drsteini:views1u
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_55_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
                   Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
                   .
                   The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
                   but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
                   geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
                   */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 55
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 55;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_55_VW;

--changeset drsteini:views1v
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_57_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 57
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 57;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_57_VW;

--changeset drsteini:views1w
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_58_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE     model.MODEL_REACH_ID NOT IN
                 (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
          AND model.sparrow_model_id = 58
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID)
    WHERE model.sparrow_model_id = 58;
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_58_VW;

--changeset drsteini:views1x
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_VW
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
             Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
             .
             The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
             but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
             geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
             */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_REACH_GEOM enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE model.MODEL_REACH_ID NOT IN
             (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID);
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_VW;

--changeset drsteini:views1y
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MODEL_GEOM_VW_OLD
(
   SPARROW_MODEL_ID,
   MODEL_REACH_ID,
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT /*
                   Note:  In this View, reaches which contain no geometry and are not related to a nominal reach are not returned.
                   .
                   The first query returns reaches which have no geometry at the model level (no entry in MODEL_REACH_GEOM),
                   but do inherit geometry from the enhanced level (entry in STREAM_NETWORK.ENH_GEOM_VW).  It is possible, however, that the
                   geometry in ENH_GEOM_VW or MODEL_REACH_GEOM may be null.
                   */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          enh.REACH_GEOM AS REACH_GEOM,
          enh.CATCH_GEOM AS CATCH_GEOM,
          enh.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'enhanced' source
     FROM    MODEL_REACH model
          INNER JOIN
             STREAM_NETWORK.ENH_GEOM_VW enh
          ON (model.ENH_REACH_ID = enh.ENH_REACH_ID)
    WHERE model.MODEL_REACH_ID NOT IN
             (SELECT MODEL_REACH_ID FROM MODEL_REACH_GEOM)
   UNION ALL
   SELECT /*
          The second query returns reaches which have geometry at the model level (an entry in MODEL_REACH_GEOM)
          */
         model.SPARROW_MODEL_ID AS SPARROW_MODEL_ID,
          model.MODEL_REACH_ID AS MODEL_REACH_ID,
          model.IDENTIFIER AS IDENTIFIER,
          geo.REACH_GEOM AS REACH_GEOM,
          geo.CATCH_GEOM AS CATCH_GEOM,
          geo.WATERSHED_GEOM AS WATERSHED_GEOM,
          model.REACH_SIZE AS REACH_SIZE,
          'model' source
     FROM    MODEL_REACH model
          INNER JOIN
             MODEL_REACH_GEOM geo
          ON (model.MODEL_REACH_ID = geo.MODEL_REACH_ID);
--rollback DROP VIEW SPARROW_DSS.MODEL_GEOM_VW_OLD;

--changeset drsteini:views1z
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MRB01_NHD_VW
(
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT IDENTIFIER,
          REACH_GEOM,
          CATCH_GEOM,
          WATERSHED_GEOM,
          REACH_SIZE,
          SOURCE
     FROM MODEL_GEOM_51_VW;
--rollback DROP VIEW SPARROW_DSS.MRB01_NHD_VW;

--changeset drsteini:views1aa
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MRB02_MRBE2RF1_VW
(
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT IDENTIFIER,
          REACH_GEOM,
          CATCH_GEOM,
          WATERSHED_GEOM,
          REACH_SIZE,
          SOURCE
     FROM MODEL_GEOM_50_VW;
--rollback DROP VIEW SPARROW_DSS.MRB02_MRBE2RF1_VW;

--changeset drsteini:views1ab
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MRB03_MRBE2RF1_VW
(
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT IDENTIFIER,
          REACH_GEOM,
          CATCH_GEOM,
          WATERSHED_GEOM,
          REACH_SIZE,
          SOURCE
     FROM MODEL_GEOM_41_VW;
--rollback DROP VIEW SPARROW_DSS.MRB03_MRBE2RF1_VW;

--changeset drsteini:views1ac
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MRB04_MRBE2RF1_VW
(
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT IDENTIFIER,
          REACH_GEOM,
          CATCH_GEOM,
          WATERSHED_GEOM,
          REACH_SIZE,
          SOURCE
     FROM MODEL_GEOM_57_VW;
--rollback DROP VIEW SPARROW_DSS.MRB04_MRBE2RF1_VW;

--changeset drsteini:views1ad
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MRB05_MRBE2RF1_VW
(
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT IDENTIFIER,
          REACH_GEOM,
          CATCH_GEOM,
          WATERSHED_GEOM,
          REACH_SIZE,
          SOURCE
     FROM MODEL_GEOM_35_VW;
--rollback DROP VIEW SPARROW_DSS.MRB05_MRBE2RF1_VW;

--changeset drsteini:views1ae
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MRB06_MRBE2RF1_VW
(
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT IDENTIFIER,
          REACH_GEOM,
          CATCH_GEOM,
          WATERSHED_GEOM,
          REACH_SIZE,
          SOURCE
     FROM MODEL_GEOM_53_VW;
--rollback DROP VIEW SPARROW_DSS.MRB06_MRBE2RF1_VW;

--changeset drsteini:views1af
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.MRB07_MRBE2RF1_VW
(
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT IDENTIFIER,
          REACH_GEOM,
          CATCH_GEOM,
          WATERSHED_GEOM,
          REACH_SIZE,
          SOURCE
     FROM MODEL_GEOM_43_VW;
--rollback DROP VIEW SPARROW_DSS.MRB07_MRBE2RF1_VW;

--changeset drsteini:views1ag
CREATE OR REPLACE FORCE VIEW SPARROW_DSS.NATIONAL_E2RF1_VW
(
   IDENTIFIER,
   REACH_GEOM,
   CATCH_GEOM,
   WATERSHED_GEOM,
   REACH_SIZE,
   SOURCE
)
AS
   SELECT IDENTIFIER,
          REACH_GEOM,
          CATCH_GEOM,
          WATERSHED_GEOM,
          REACH_SIZE,
          SOURCE
     FROM MODEL_GEOM_22_VW;
--rollback DROP VIEW SPARROW_DSS.NATIONAL_E2RF1_VW;
