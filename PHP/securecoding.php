<?php
// 공통 시큐어코딩 함수.


// ★ SQL Injection 취약점 시큐어코딩
// eregi_replace 함수를 이용하여 특수문자 혹은 SQL 명령 구문을 검증
// 5.X 버전이후에는 아래 메소드 사용.
function sql_filter($get_Str) {
	return preg_replace('/( select| having| union| insert| update| delete| drop|\"|\'|#|\/\*|\*\/|\\\|\;)+/i', '', $get_Str);
}
// ★ 설명 :: 해당 프로그램에 아래처럼 코드 삽입하면 SQL 인젝션 취약점이 예방 됨.
 //$name = sql_filter($name);
// $sqlstr="SELECT * FROM users WHERE username = '$name'";




// ★ XSS 취약점 시큐어코딩
function xss_filter($content) {

	// 2019-12-27 ::: 추가 보완 - XSS 필터에 expression 문자열 추가
	// 2019-09-16 ::: 추가 보완 - %27 방어, 공백 방어
	$content = preg_replace('/(\"|\'|\+|\ |\%00|\!|\?|html|head|title|meta|body|script|style|base|noscript|
	  form|input|select|option|optgroup|textarea|button|label|fieldset|legend|iframe|embed|object|param|
	  frameset|frame|noframes|basefont|applet| isindex|xmp|plaintext|listing|bgsound|marquee|blink|
	  noembed|comment|xml|\/|expression)+/i', '', $content);

	$content = preg_replace_callback("/([^a-z])(o)(n)/i",
	  create_function('$matches', 'if($matches[2]=="o") $matches[2] = "&#111;";
	  else $matches[2] = "&#79;"; return $matches[1].$matches[2].$matches[3];'), $content);

	//tag 사용안하게 될 경우
	$content = str_replace( "<", "&lt;", $content );
	$content = str_replace( ">", "&gt;", $content );

	return $content;
}
// ★ 설명 :: 해당 프로그램에 아래처럼 코드 삽입하면 XSS 취약점이 예방 됨.
//$name = xss_filter($name);


// 2019-09-18 ::: 추가 보완 
//야마하코리아 URL 스크립트 취약점 조치를 위해 생성
function url_xss_filter($url_cintent){

	//(|\/) 해당 부분 삭제, URL 이여서 삭제가되면 안됨
	// 2019-12-27 ::: 추가 보완 - XSS 필터에 expression 문자열 추가
	// 2019-09-16 ::: 추가 보완 - %27 방어, 공백 방어
	$content = preg_replace('/(\"|\'|\+|\ |\%00|\!|\?|html|head|title|meta|body|script|style|base|noscript|
	  form|input|select|option|optgroup|textarea|button|label|fieldset|legend|iframe|embed|object|param|
	  frameset|frame|noframes|basefont|applet| isindex|xmp|plaintext|listing|bgsound|marquee|blink|
	  noembed|comment|xml|expression)+/i', '', $content);

	$url_cintent = preg_replace_callback("/([^a-z])(o)(n)/i",
	  create_function('$matches', 'if($matches[2]=="o") $matches[2] = "&#111;";
	  else $matches[2] = "&#79;"; return $matches[1].$matches[2].$matches[3];'), $url_cintent);

	//tag 사용안하게 될 경우
	$url_cintent = str_replace( "<", "&lt;", $url_cintent );
	$url_cintent = str_replace( ">", "&gt;", $url_cintent );

	return $url_cintent;
}






// ★ 신뢰되지 않은 URL 접속 취약점 시큐어코딩
function url_location_filter($url, $host=false) {
	$url_arr = parse_url(urldecode($url));
	return $host . $url_arr['path'] . "?" . $url_arr['query'];
}
// ★ 설명 :: 해당 프로그램에 아래처럼 코드 삽입하면 신뢰되지 않은 URL 주소로 자동 접속 취약점이 예방 됨.
// HEADER("location:$URL");
// ==> 위처럼 되어 있는 코드를 아래처럼 코딩.
// HEADER("location:".url_location_filter($URL));




function file_extension_check($filename, $allowed_types = array()) {
    $x = explode('.', urldecode($filename));
    $extension = strtolower(end($x));

    if(empty($allowed_types)) $allowed_types = array("php", "php3", "html", "htm");
    if (in_array($extension, $allowed_types, FALSE)) {
        return TRUE;
    }
    return FALSE;
}
// ★ 설명 :: 해당 프로그램에 아래처럼 코드 삽입하면
//            ".." 등의 파일 이름에 경로 문자열이 들어가 있는지 체크함과 동시에
//            파일확장자가 서버실행파일의 경우 다운로드를 못하게 처리함.
//	if( file_extension_check($filename) ) {
//		return FALSE;
//	}




// ★ 파일 다운로드 취약점 시큐어코딩
function file_name_filter($filename) {
	$bad = array(
		"<!--",
		"-->",
		"'",
		"<",
		">",
		'"',
		'&',
		'$',
		'=',
		';',
		'?',
		'/',
		"%20",
		"%22",
		"%3c",		// <
		"%253c", 	// <
		"%3e", 		// >
		"%0e", 		// >
		"%28", 		// (
		"%29", 		// )
		"%2528", 	// (
		"%26", 		// &
		"%24", 		// $
		"%3f", 		// ?
		"%3b", 		// ;
		"%3d"		// =
	);
	$filename = str_replace($bad, '', $filename);
	$filename = stripslashes($filename);
	// Remove white spaces in the name and replace
	$filename = preg_replace("/\s+/", "_", $filename);

	return $filename;
}
// ★ 설명 :: 해당 프로그램에 아래처럼 코드 삽입하면 파일이름에 비정상적인 문자열을 걸러내여 파일 다운로드 취약점이 예방.
//$name = file_name_filter($name);



// ★ 파일 업로드 취약점
// ★ 설명 :: 해당 프로그램에 아래처럼 코드 삽입하면
//            ".." 등의 파일 이름에 경로 문자열이 들어가 있는지 체크함과 동시에
//            파일확장자가 서버실행파일의 경우 업로드를 못하게 처리함.
//
//if( file_extension_check($filename) ) {
//	return "오류";
//}
//$allowed_types	= array("xls", "xlsx", "pdf", "doc", "docx", "ppt", "pptx", "hwp", "alz", "zip", "jpg", "jpeg", "gif", "bmp", "png", "txt");
//if( file_extension_check($filename, $allowed_types) ) {
//$filename = file_name_filter($filename);
//	...
//	업로드 처리
//}


?>
