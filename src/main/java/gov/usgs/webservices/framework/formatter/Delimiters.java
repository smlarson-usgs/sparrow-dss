package gov.usgs.webservices.framework.formatter;

public class Delimiters {
	public String sheetStart, sheetEnd;
	public String headerStart, headerRowStart, headerRowEnd, headerEnd;
	public String headerCellStart, headerCellEnd;
	public String bodyStart, bodyRowStart, bodyRowEnd, bodyEnd;
	public String bodyCellStart, bodyCellEnd;
	public String footerStart, footerRowStart, footerRowEnd, footerEnd;
	public String footerCellStart, footerCellEnd;
	
	// CONSTANT DELIMITERS
	public static final Delimiters HTML_DELIMITERS = makeHTMLDelimiter();
	public static final Delimiters CSV_DELIMITERS = makeCSVDelimiter();
	public static final Delimiters TAB_DELIMITERS = makeTabDelimiter();
//	public static final Delimiters HTML_DELIMITERS_WITH_HEADER = makeHTMLDelimiter(true);
	
	@SuppressWarnings("unused")
	private final int type;
	private static final int HTML_TYPE = 1;
	private static final int CSV_TYPE = 2;
	private static final int TAB_TYPE = 3;
	private static final int EXCEL_TYPE = 4;

	// ==========================
	// STATIC INITIALIZER METHODS
	// ==========================
	public static Delimiters makeHTMLDelimiter(/* boolean useTableHeadMarkup */) {
		Delimiters delims= new Delimiters(HTML_TYPE);
		
		
		delims.sheetStart = "<table>";
		delims.sheetEnd = "</table>";
		
		//header
		delims.headerStart = "<thead>";
		delims.headerRowStart = "<tr>";
		delims.headerCellStart = "<th>";
		delims.headerCellEnd = "</th>";
		delims.headerRowEnd = "</tr>\n";
		delims.headerEnd = "</thead>\n";
		
		
		//
		//Note that it is an HTML requirement that the tfoot come BEFORE
		//the tbody of a table.  This allows the footer to be displayed as
		//content is loaded.
		delims.footerStart = "<tfoot>";
		delims.footerRowStart = "<tr>";
		delims.footerCellStart = "<td>";
		delims.footerCellEnd = "</td>";
		delims.footerRowEnd = "</tr>";
		delims.footerEnd = "</tfoot>\n";

		//body - there can be multiple tbody sections
		delims.bodyStart = "<tbody>";
		delims.bodyRowStart = "<tr>";
		delims.bodyCellStart = "<td>";
		delims.bodyCellEnd = "</td>";
		delims.bodyRowEnd = "</tr>\n";
		delims.bodyEnd = "</tbody>\n";
		

		return delims;
	}
	
	public static Delimiters makeExcelDelimiter(String author, String creationDate) {
		Delimiters delims= new Delimiters(EXCEL_TYPE);
		delims.sheetStart = "<?xml version=\"1.0\"?>"
			+ "<?mso-application progid=\"Excel.Sheet\"?>"
			+ "<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\""
			+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
			+ " xmlns:x=\"urn:schemas-microsoft-com:office:excel\""
			+ " xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\""
			+ " xmlns:html=\"http://www.w3.org/TR/REC-html40\">"
			+ " <DocumentProperties xmlns=\"urn:schemas-microsoft-com:office:office\">"
			+ "  <LastAuthor>" + author + "</LastAuthor>"
			+ "  <Created>" + creationDate + "</Created>" //2007-11-02T21:34:20Z
			+ "  <Version>11.8132</Version>"
			+ " </DocumentProperties>"
			+ " <OfficeDocumentSettings xmlns=\"urn:schemas-microsoft-com:office:office\"/>"
			+ " <ExcelWorkbook xmlns=\"urn:schemas-microsoft-com:office:excel\">"
			+ "  <ProtectStructure>False</ProtectStructure>"
			+ "  <ProtectWindows>False</ProtectWindows>"
			+ " </ExcelWorkbook>"
			+ " <Styles>"
			+ "  <Style ss:ID=\"Default\" ss:Name=\"Normal\">"
			+ "   <Alignment ss:Vertical=\"Bottom\"/>"
			+ "  </Style>"
			+ "  <Style ss:ID=\"s21\">"
			+ "   <NumberFormat ss:Format=\"Fixed\"/>"
			+ "  </Style>"
			+ "  <Style ss:ID=\"s22\">" /* Allows wraped text for notes */
			+ "   <Alignment ss:Vertical=\"Bottom\" ss:WrapText=\"1\"/>"
			+ "  </Style>"
			+ "  <Style ss:ID=\"s23\">"
			+ "   <Font x:Family=\"Swiss\" ss:Bold=\"1\"/>"
			+ "  </Style>"
			+ " </Styles>"
			+ " <Worksheet ss:Name=\"data\">"
			+ "  <Table x:FullColumns=\"1\" x:FullRows=\"1\">";
		delims.sheetEnd = "  </Table>"
			+ "  <WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\">"
			+ "   <Print>"
			+ "    <ValidPrinterInfo/>"
			+ "    <HorizontalResolution>600</HorizontalResolution>"
			+ "    <VerticalResolution>600</VerticalResolution>"
			+ "   </Print>"
			+ "   <Selected/>"
			+ "   <FreezePanes/>"
			+ "   <FrozenNoSplit/>"
			+ "   <SplitHorizontal>1</SplitHorizontal>"
			+ "   <TopRowBottomPane>1</TopRowBottomPane>"
			+ "   <ActivePane>2</ActivePane>"
			+ "   <Panes>"
			+ "    <Pane>"
			+ "     <Number>3</Number>"
			+ "    </Pane>"
			+ "    <Pane>"
			+ "     <Number>2</Number>"
			+ "     <RangeSelection>R2</RangeSelection>"
			+ "    </Pane>"
			+ "   </Panes>"
			+ "   <ProtectObjects>False</ProtectObjects>"
			+ "   <ProtectScenarios>False</ProtectScenarios>"
			+ "  </WorksheetOptions>"
			+ " </Worksheet>"
			+ "</Workbook>";
		delims.headerRowStart = "<Row ss:StyleID=\"s23\">";
		delims.headerRowEnd = "</Row>\n";
		delims.headerCellStart = "<Cell><Data ss:Type=\"String\">";
		delims.headerCellEnd = "</Data></Cell>";
		delims.bodyRowStart = "<Row>";
		delims.bodyRowEnd = delims.headerRowEnd;
		delims.bodyCellStart = delims.headerCellStart;
		delims.bodyCellEnd = delims.headerCellEnd;
//		delims.footerRowStart = "<tr>";
//		delims.footerRowEnd = "</tr>";
//		delims.footerCellStart = "<td>";
//		delims.footerCellEnd = "</td>";
		return delims;
	}
	
	public static Delimiters makeCSVDelimiter() {
		Delimiters delims= new Delimiters(CSV_TYPE);
		// TODO csv format is actually more than using "," for delimiters
		// double-quotes allow comma-escaping, and \n's may be enclosed as well.
		// see http://www.creativyst.com/Doc/Articles/CSV/CSV01.htm
		// see http://en.wikipedia.org/wiki/Comma-separated_values
		delims.sheetStart = "";
		delims.sheetEnd = "";
		delims.headerRowStart = "";
		delims.headerRowEnd = "\n";
		delims.headerCellStart = "";
		delims.headerCellEnd = ",";
		delims.bodyRowStart = "";
		delims.bodyRowEnd = "\n";
		delims.bodyCellStart = "";
		delims.bodyCellEnd = ",";
//		delims.footerRowStart = "";
//		delims.footerRowEnd = "";
//		delims.footerCellStart = "";
//		delims.footerCellEnd = "";
		return delims;
	}
	
	public static Delimiters makeTabDelimiter() {
		Delimiters delims= new Delimiters(TAB_TYPE);
		delims.sheetStart = "";
		delims.sheetEnd = "";
		delims.headerRowStart = "";
		delims.headerRowEnd = "\n";
		delims.headerCellStart = "";
		delims.headerCellEnd = "\t";
		delims.bodyRowStart = "";
		delims.bodyRowEnd = "\n";
		delims.bodyCellStart = "";
		delims.bodyCellEnd = "\t";
//		delims.footerRowStart = "";
//		delims.footerRowEnd = "";
//		delims.footerCellStart = "";
//		delims.footerCellEnd = "";
		return delims;
	}
	// ================
	// CONSTRUCTORS
	// ================
	public Delimiters(int type) {
		this.type = type;
	}
	
	// ================
	// INSTANCE METHODS
	// ================
	public String makeWideHeaderCell(String content, int width) {
		return "";
		// TODO work out the rest
//		StringBuilder result = new StringBuilder();
//		switch (type) {
//			case EXCEL_TYPE:
//				// same as html type
//			case HTML_TYPE:
//				result.append("<td colspan=\"").append(width).append("\"><b>").append(content).append("</b></td>");
//				break;
//			case CSV_TYPE:
//				// don't do this for csv
//				break;
//			case TAB_TYPE:
//				result.append(content);
//				for (int i=0; i< width; i++) {
//					result.append("\t");
//				}
//				break;
//		}
//		return result.toString();
	}
	
}
