<script type="text/javascript" language="javascript" runat="server" src="gibberish-aes.js"></script>
<script type="text/javascript" language="javascript" runat="server">
function AES_Encode(key, plain_text) {
    try    {
        GibberishAES.size(256);    
        return GibberishAES.aesEncrypt(plain_text, key).replace(/\n/g, "");        
    } catch (e) {
        return null;
    }
}

function AES_Decode(key, base64_text) {
    try    {
        GibberishAES.size(256);    
        return GibberishAES.aesDecrypt(base64_text.replace(/ /gi, "+"), key);        
    } catch (e) {
        return null;
    }
}
</script>
<%

' 256bit 암호화를 사용하기 위해서는 32Byte를 충족해야 하기 때문에
' Key는 32글짜를 입력하셔야 합니다.
Dim sKey : skey = "abcdefghijklmnopqrstuvwxyz123456"
Dim ko_encrypt, ko_decrypt
ko_encrypt = AES_Encode(skey, "kimsungchul")
ko_decrypt = AES_Decode(skey, ko_encrypt)

Response.Write ko_encrypt & "<br/>"
Response.Write ko_decrypt & "<br/>"
%>