package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import util.CryptoJSCipherAES128;
import util.StringUtil;
import javax.servlet.http.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.util.Hashtable;


@Controller
public class AesController extends HttpServlet{
    @RequestMapping(value="aesTest.do")
    public ModelAndView aesTest (ModelAndView mav, HttpServletRequest request){

        HttpSession session = request.getSession();


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


        String sessionId = session.getId();
        Hashtable<String,String> hashtable = new Hashtable<String,String>();

        String enc_salt=(String)Long.toHexString(secureRandomLong_salt);
        String enc_iv = (String)Long.toHexString(secureRandomLong_iv_1)+(String)Long.toHexString(secureRandomLong_iv_2);
        String enc_passPhrase = "1234";


        hashtable.put("enc_iv",enc_iv);
        hashtable.put("enc_salt",enc_salt);
        hashtable.put("enc_passPhrase",enc_passPhrase);
        session.setAttribute("hashtable",hashtable);

        mav.setViewName("aesTest/aesTest.jsp");
        return mav;
    }

    @RequestMapping(value="aesResult.do")
    public void aesResult(HttpServletRequest req)throws Exception{
        HttpSession session = req.getSession();
        String userId = "";
        String userPw = "";

        String encData = req.getParameter("encData");

        System.out.println("encData : " + encData);

        Hashtable<String,String>hashtable = (Hashtable)session.getAttribute("hashtable");

        String enc_iv = hashtable.get("enc_iv");
        String enc_salt = hashtable.get("enc_salt");
        String enc_passPhrase = hashtable.get("enc_passPhrase");

        String decrypted = CryptoJSCipherAES128.decrypt(enc_salt, enc_iv, enc_passPhrase, encData, 100, 128);

        // 아이디&비밀번호 분리
        String[] data = StringUtil.parseSplit(decrypted, "|");
        if( (data[0] != null && !data[0].equals("")) && (data[1] != null && !data[1].equals("")) ) {

            userId = data[0];
            userPw = data[1];

            System.out.println("userid : " + userId);
            System.out.println("userPw : " + userPw);
        }

    }


 }
