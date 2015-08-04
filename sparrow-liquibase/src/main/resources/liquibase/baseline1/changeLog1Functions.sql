--liquibase formatted sql

--This is for the sparrow_dss schema
  
--logicalFilePath: changeLog1Functions.sql

--changeset drsteini:functions1a endDelimiter:/ splitStatements:false
CREATE OR REPLACE function SPARROW_DSS.get_median_point(
	geom SDO_GEOMETRY
) RETURN SDO_GEOMETRY
IS

	num_points NUMBER;
	p1 SDO_GEOMETRY;
	p2 SDO_GEOMETRY;
	i1 INTEGER;

BEGIN
	num_points := get_num_points(geom);
	IF num_points < 2 THEN
		RETURN get_point(geom);
	ELSE

		IF MOD(num_points,2) = 0 THEN
			--even number of pts

			SELECT cast((num_points/2) as INTEGER) INTO i1 FROM dual;

			p1 := get_point(geom, i1);
			p2 := get_point(geom, i1 + 1);

			--AVG POINTS and return new point
			RETURN
				MDSYS.SDO_GEOMETRY(
					2001,
					geom.SDO_SRID,
					SDO_POINT_TYPE(
						(p1.sdo_point.x + p2.sdo_point.x) / 2,
						(p1.sdo_point.y + p2.sdo_point.y) / 2,
						NULL
					),NULL,NULL);

		ELSE
			--odd number of pts
			SELECT cast((num_points/2) as INTEGER) INTO i1 FROM dual;
			RETURN get_point(geom, i1 + 1);

		END IF;

	END IF;
END;
--rollback drop function SPARROW_DSS.get_median_point;

--changeset drsteini:functions1b endDelimiter:/ splitStatements:false
CREATE OR REPLACE function SPARROW_DSS.get_num_points(
	geom SDO_GEOMETRY
) RETURN NUMBER
IS
	d NUMBER;
BEGIN

	d := SUBSTR(geom.SDO_GTYPE, 1, 1);
	IF d > 0 THEN
		RETURN geom.SDO_ORDINATES.COUNT()/d;
	ELSE
		RETURN 0;
	END IF;

END;
--rollback drop function SPARROW_DSS.get_num_points;

--changeset drsteini:functions1c endDelimiter:/ splitStatements:false
CREATE OR REPLACE function SPARROW_DSS.get_point(
	geom SDO_GEOMETRY, point_number NUMBER DEFAULT 1
) RETURN SDO_GEOMETRY
IS
	g MDSYS.SDO_GEOMETRY;
	d NUMBER;
	p NUMBER;
	px NUMBER;
	py NUMBER;

BEGIN

	d := SUBSTR(geom.SDO_GTYPE, 1, 1);

	IF point_number < 1
	OR point_number > geom.SDO_ORDINATES.COUNT()/d THEN
		RETURN NULL;
	END IF;

	p := (point_number-1)*d+1;

	px := geom.SDO_ORDINATES(p);
	py := geom.SDO_ORDINATES(p+1);

	RETURN
		MDSYS.SDO_GEOMETRY(
			2001,
			geom.SDO_SRID,
			SDO_POINT_TYPE(px,py,NULL),
			NULL,NULL);
END;
--rollback drop function SPARROW_DSS.get_point;

--changeset drsteini:functions1d endDelimiter:/ splitStatements:false
CREATE OR REPLACE function SPARROW_DSS.SQUIRREL_GET_ERROR_OFFSET (query IN varchar2) return number authid current_user is
	l_theCursor     integer default dbms_sql.open_cursor;
	l_status        integer;
begin
	begin
		dbms_sql.parse(  l_theCursor, query, dbms_sql.native );
	exception
		when others then l_status := dbms_sql.last_error_position;
	end;
	dbms_sql.close_cursor( l_theCursor );
	return l_status;
end;
--rollback drop function SPARROW_DSS.SQUIRREL_GET_ERROR_OFFSET
