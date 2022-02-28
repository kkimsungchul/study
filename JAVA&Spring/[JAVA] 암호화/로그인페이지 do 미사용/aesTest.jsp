<%@ page import="java.security.SecureRandom" %>
<%@ page import="java.security.NoSuchAlgorithmException" %>
<%@ page import="java.util.Hashtable" %><%--
  Created by IntelliJ IDEA.
  User: kimsc
  Date: 2018-04-12
  Time: 오후 5:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>

<html>
<script src="/js/jquery-3.3.1.min.js"></script>
<script src="/js/aes.js"></script>
<script src="/js/pbkdf2.js"></script>
<%

    session = request.getSession();


    long secureRandomLong_salt=0;
    long secureRandomLong_iv_1=0;
    long secureRandomLong_iv_2=0;
    try {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(secureRandom.generateSeed(128));
        secureRandomLong_salt= secureRandom.nextLong();
        secureRandomLong_iv_1= secureRandom.nextLong();
        secureRandomLong_iv_2= secureRandom.nextLong();
        while(secureRandomLong_salt<1160000000000000000L) {
            secureRandomLong_salt= secureRandom.nextLong();
        }
        while(secureRandomLong_iv_1<1160000000000000000L) {
            secureRandomLong_iv_1= secureRandom.nextLong();
        }
        while(secureRandomLong_iv_2<1160000000000000000L) {
            secureRandomLong_iv_2= secureRandom.nextLong();
        }
    } catch (NoSuchAlgorithmException e) {
        System.out.println("난수 발생 에러");
    }



    Hashtable<String,String> hashtable = new Hashtable<String,String>();

    String enc_salt=(String)Long.toHexString(secureRandomLong_salt);
    String enc_iv = (String)Long.toHexString(secureRandomLong_iv_1)+(String)Long.toHexString(secureRandomLong_iv_2);
    String enc_passPhrase = "1234";

    System.out.println("enc_salt : " + enc_salt);
    System.out.println("enc_iv : " + enc_iv);
    System.out.println("enc_passPhrase : " + enc_passPhrase);

    hashtable.put("enc_iv",enc_iv);
    hashtable.put("enc_salt",enc_salt);
    hashtable.put("enc_passPhrase",enc_passPhrase);
    session.setAttribute("hashtable",hashtable);

    %>



<script>

    var enc_iv = '${hashtable.enc_iv}';
    var enc_salt = '${hashtable.enc_salt}';
    var enc_passPhrase = '${hashtable.enc_passPhrase}';
    var enc_keySize = 128;
    var enc_iterationCount = 100;

    $(document).ready(function() {

        $("#btnLogin").click(function () {

            var tmpData = $("#userId").val() + "|" + $("#userPw").val();
            var key128Bits100Iterations = CryptoJS.PBKDF2(enc_passPhrase, CryptoJS.enc.Hex.parse(enc_salt), { keySize: enc_keySize/32, iterations: enc_iterationCount });
            var encData = CryptoJS.AES.encrypt(tmpData, key128Bits100Iterations, { iv: CryptoJS.enc.Hex.parse(enc_iv) });
            $("#encData").val(encData);
            console.log("click ok ");

            $("#frm").submit();
        });

    });
</script>
  <head>
    <title>환영합니다</title>
  </head>
  <body>

  <div id="div01">

      <form action="aesResult.do" id="frm" name="frm" method="post" >
          <input type="hidden" id="encData" name="encData" value="">
          <table>
              <tr><th>아이디 </th><td><input type="text" name="userId" id="userId"></td></tr>
              <tr><th>비밀번호</th><td><input type="password" name ="userPw" id="userPw">
              <tr><th colspan="2"><input type="button" id="btnLogin" name="btnLogin" value="로그인" class="button"></th></tr>
          </table>
      </form>




  </div>

  </body>
</html>
