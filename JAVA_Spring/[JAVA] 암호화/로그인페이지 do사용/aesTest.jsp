<%--
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

<script>

    var enc_iv = '${hashtable.enc_iv}';
    var enc_salt = '${hashtable.enc_salt}';
    var enc_passPhrase = '${hashtable.enc_passPhrase}';
    var enc_keySize = 128;
    var enc_iterationCount = 100;

    //제이쿼리 사용하여 전송
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

    //순수 자바스크립트만 사용하여 전송
    function login() {
        var f = document.frm;
        var tmpData = f.userId.value + "|" + f.userPw.value;
        var key128Bits100Iterations = CryptoJS.PBKDF2(enc_passPhrase, CryptoJS.enc.Hex.parse(enc_salt), { keySize: enc_keySize/32, iterations: enc_iterationCount });
        var encData = CryptoJS.AES.encrypt(tmpData, key128Bits100Iterations, { iv: CryptoJS.enc.Hex.parse(enc_iv) });
        f.encData.value=encData;
        f.action = "aesResult.do";
        f.submit();
    }


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
              <%--<tr><th colspan="2"><input type="button" id="btnLogin" name="btnLogin" value="로그인" class="button"></th></tr>--%>
              <tr><th colspan="2"><input type="button" id="btnLogin" name="btnLogin" value="로그인" class="button" onClick="login();"></th></tr>
          </table>
      </form>




  </div>

  </body>
</html>
