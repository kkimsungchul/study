package smartonek.solution.bp.comm.util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelDownloadUtil {

	
	//파일 작성 후 파일 경로와 파일명을 리턴하여 해당 경로에서 파일을 다운받을 수 있도록 함
	/*	
	 * @Param
	 * List<HashMap<String, String>> mapList : 엑셀에 찍을 데이터
	 * keyArray[] : 맵에서 꺼낼 키값
	 * listTitle : 엑셀에 표시할 분류 이름
	 * Path : 저장 경로 
	 * subPath : 저장경로의 하위 폴더 경로 
	 * title : 엑셀파일 상단의 제목, 파일명
	 * 
	 * @Return
	 * String path[]
	 * 0 : 전체경로
	 * 1 : 서브경로
	 * */
	public String[] excelDownload(List<HashMap<String, String>> mapList , String[] keyArray,String listTitle[], String Path , String subPath , String title) throws Exception{

		SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd-HH_mm_ss");
		Date time = new Date();
		String time1 = format1.format(time);
		String fileName = title+time1+".xlsx";
		String localFile = Path + subPath + fileName;
		
		String returnPath[] = new String[2];
		returnPath[0]=localFile;
		returnPath[1]="\\"+subPath + fileName;
				//"\\"+subPath + fileName;

		

		//.xls 확장자 지원
				HSSFWorkbook wb = null;
				HSSFSheet sheet = null;
				Row row = null;
				Cell cell = null;
				
				//.xlsx 확장자 지원
				XSSFWorkbook xssfWb = null; // .xlsx
				XSSFSheet xssfSheet = null; // .xlsx
				XSSFRow xssfRow = null; // .xlsx
				XSSFCell xssfCell = null;// .xlsx
					
					try {
						// 워크북 생성
						xssfWb = new XSSFWorkbook();
						xssfSheet = xssfWb.createSheet(title); // 워크시트 이름
					
						
						//타이틀 시작
						
						//헤더용 폰트 스타일
						XSSFFont font = xssfWb.createFont();
						font.setFontName(HSSFFont.FONT_ARIAL); //폰트스타일
						font.setFontHeightInPoints((short)14); //폰트크기
						font.setBold(true); //Bold 유무
						
						//테이블 타이틀 스타일
						CellStyle cellStyleTitle = xssfWb.createCellStyle();
						//셀정렬 1좌측 , 2가운데, 3우측 
						cellStyleTitle.setAlignment((short)2);
						
						//셀 테두리
						cellStyleTitle.setBorderBottom((short)1);
						cellStyleTitle.setBorderLeft((short)1);
						cellStyleTitle.setBorderRight((short)1);
						cellStyleTitle.setBorderTop((short)1);
						
						cellStyleTitle.setFont(font); // cellStyle에 font를 적용
						//배경색 적용
						cellStyleTitle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
						cellStyleTitle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
						
						
						
						
						xssfSheet.setColumnWidth(0, (xssfSheet.getColumnWidth(0))+(short)1024); //열 너비 조절
						xssfSheet.setColumnWidth(1, (xssfSheet.getColumnWidth(1))+(short)5120); 
						xssfSheet.setColumnWidth(2, (xssfSheet.getColumnWidth(2))+(short)3072); 
						xssfSheet.setColumnWidth(3, (xssfSheet.getColumnWidth(3))+(short)2048); 
						xssfSheet.setColumnWidth(4, (xssfSheet.getColumnWidth(4))+(short)5120);


						//셀병합
						xssfSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, keyArray.length-1 )); //첫행, 마지막행, 첫열, 마지막열( 0번째 행의 0~8번째 컬럼을 병합한다)
						
						//타이틀 생성
						xssfRow = xssfSheet.createRow(0); //행 객체 추가
						xssfCell = xssfRow.createCell((short) 0); // 추가한 행에 셀 객체 추가
						xssfCell.setCellStyle(cellStyleTitle); // 셀에 스타일 지정
						xssfCell.setCellValue(title); // 데이터 입력
						
						
						//타이틀 끝
						
						
						//하단의 내용 셀 스타일
						CellStyle cellStyleContent = xssfWb.createCellStyle();
						cellStyleContent.setBorderBottom((short)1);
						cellStyleContent.setBorderLeft((short)1);
						cellStyleContent.setBorderRight((short)1);
						cellStyleContent.setBorderTop((short)1);
						
						
						
						//상단 분류값 생성
						xssfRow = xssfSheet.createRow(1); 
						for(int k=0;k<listTitle.length;k++) {
							xssfCell = xssfRow.createCell((short) k);
							xssfCell.setCellValue(listTitle[k]);
							xssfCell.setCellStyle(cellStyleContent); // 셀에 스타일 지정
						}

						
						
						for(int i=0;i<mapList.size();i++) {
						
							xssfRow = xssfSheet.createRow(i+2);
							for(int j=0;j<keyArray.length ; j++) {
								xssfCell = xssfRow.createCell((short) j);
								xssfCell.setCellValue(mapList.get(i).get(keyArray[j]));
								xssfCell.setCellStyle(cellStyleContent); // 셀에 스타일 지정
								
							}

							
							
						}
																		
						File file = new File(localFile);
						FileOutputStream fos = null;
						fos = new FileOutputStream(file);
						xssfWb.write(fos);
	
	
						if (fos != null) fos.close();
						
						}
					catch(Exception e){
			        	System.out.println(e);
					}finally{
						
				    }
		
				return returnPath;
	}
	
	
}
