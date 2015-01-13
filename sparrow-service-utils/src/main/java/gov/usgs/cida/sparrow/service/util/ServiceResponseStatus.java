package gov.usgs.cida.sparrow.service.util;

public enum ServiceResponseStatus {
	OK,
	OK_ALREADY_EXISTS,
	OK_ALREADY_EXISTED_BUT_WAS_UPDATED,
	FAIL,
	PARTIAL,
	UNKNOWN
}
