<?php
// ���� ��ť���ڵ� �Լ�.


// �� SQL Injection ����� ��ť���ڵ�
// eregi_replace �Լ��� �̿��Ͽ� Ư������ Ȥ�� SQL ��� ������ ����
// 5.X �������Ŀ��� �Ʒ� �޼ҵ� ���.
function sql_filter($get_Str) {
	return preg_replace('/( select| having| union| insert| update| delete| drop|\"|\'|#|\/\*|\*\/|\\\|\;)+/i', '', $get_Str);
}
// �� ���� :: �ش� ���α׷��� �Ʒ�ó�� �ڵ� �����ϸ� SQL ������ ������� ���� ��.
 //$name = sql_filter($name);
// $sqlstr="SELECT * FROM users WHERE username = '$name'";




// �� XSS ����� ��ť���ڵ�
function xss_filter($content) {

	// 2019-12-27 ::: �߰� ���� - XSS ���Ϳ� expression ���ڿ� �߰�
	// 2019-09-16 ::: �߰� ���� - %27 ���, ���� ���
	$content = preg_replace('/(\"|\'|\+|\ |\%00|\!|\?|html|head|title|meta|body|script|style|base|noscript|
	  form|input|select|option|optgroup|textarea|button|label|fieldset|legend|iframe|embed|object|param|
	  frameset|frame|noframes|basefont|applet| isindex|xmp|plaintext|listing|bgsound|marquee|blink|
	  noembed|comment|xml|\/|expression)+/i', '', $content);

	$content = preg_replace_callback("/([^a-z])(o)(n)/i",
	  create_function('$matches', 'if($matches[2]=="o") $matches[2] = "&#111;";
	  else $matches[2] = "&#79;"; return $matches[1].$matches[2].$matches[3];'), $content);

	//tag �����ϰ� �� ���
	$content = str_replace( "<", "&lt;", $content );
	$content = str_replace( ">", "&gt;", $content );

	return $content;
}
// �� ���� :: �ش� ���α׷��� �Ʒ�ó�� �ڵ� �����ϸ� XSS ������� ���� ��.
//$name = xss_filter($name);


// 2019-09-18 ::: �߰� ���� 
//�߸����ڸ��� URL ��ũ��Ʈ ����� ��ġ�� ���� ����
function url_xss_filter($url_cintent){

	//(|\/) �ش� �κ� ����, URL �̿��� �������Ǹ� �ȵ�
	// 2019-12-27 ::: �߰� ���� - XSS ���Ϳ� expression ���ڿ� �߰�
	// 2019-09-16 ::: �߰� ���� - %27 ���, ���� ���
	$content = preg_replace('/(\"|\'|\+|\ |\%00|\!|\?|html|head|title|meta|body|script|style|base|noscript|
	  form|input|select|option|optgroup|textarea|button|label|fieldset|legend|iframe|embed|object|param|
	  frameset|frame|noframes|basefont|applet| isindex|xmp|plaintext|listing|bgsound|marquee|blink|
	  noembed|comment|xml|expression)+/i', '', $content);

	$url_cintent = preg_replace_callback("/([^a-z])(o)(n)/i",
	  create_function('$matches', 'if($matches[2]=="o") $matches[2] = "&#111;";
	  else $matches[2] = "&#79;"; return $matches[1].$matches[2].$matches[3];'), $url_cintent);

	//tag �����ϰ� �� ���
	$url_cintent = str_replace( "<", "&lt;", $url_cintent );
	$url_cintent = str_replace( ">", "&gt;", $url_cintent );

	return $url_cintent;
}






// �� �ŷڵ��� ���� URL ���� ����� ��ť���ڵ�
function url_location_filter($url, $host=false) {
	$url_arr = parse_url(urldecode($url));
	return $host . $url_arr['path'] . "?" . $url_arr['query'];
}
// �� ���� :: �ش� ���α׷��� �Ʒ�ó�� �ڵ� �����ϸ� �ŷڵ��� ���� URL �ּҷ� �ڵ� ���� ������� ���� ��.
// HEADER("location:$URL");
// ==> ��ó�� �Ǿ� �ִ� �ڵ带 �Ʒ�ó�� �ڵ�.
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
// �� ���� :: �ش� ���α׷��� �Ʒ�ó�� �ڵ� �����ϸ�
//            ".." ���� ���� �̸��� ��� ���ڿ��� �� �ִ��� üũ�԰� ���ÿ�
//            ����Ȯ���ڰ� �������������� ��� �ٿ�ε带 ���ϰ� ó����.
//	if( file_extension_check($filename) ) {
//		return FALSE;
//	}




// �� ���� �ٿ�ε� ����� ��ť���ڵ�
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
// �� ���� :: �ش� ���α׷��� �Ʒ�ó�� �ڵ� �����ϸ� �����̸��� ���������� ���ڿ��� �ɷ����� ���� �ٿ�ε� ������� ����.
//$name = file_name_filter($name);



// �� ���� ���ε� �����
// �� ���� :: �ش� ���α׷��� �Ʒ�ó�� �ڵ� �����ϸ�
//            ".." ���� ���� �̸��� ��� ���ڿ��� �� �ִ��� üũ�԰� ���ÿ�
//            ����Ȯ���ڰ� �������������� ��� ���ε带 ���ϰ� ó����.
//
//if( file_extension_check($filename) ) {
//	return "����";
//}
//$allowed_types	= array("xls", "xlsx", "pdf", "doc", "docx", "ppt", "pptx", "hwp", "alz", "zip", "jpg", "jpeg", "gif", "bmp", "png", "txt");
//if( file_extension_check($filename, $allowed_types) ) {
//$filename = file_name_filter($filename);
//	...
//	���ε� ó��
//}


?>
