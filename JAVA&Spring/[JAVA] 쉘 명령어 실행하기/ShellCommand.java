import java.io.BufferedReader;

import java.io.IOException;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.util.ArrayList;

import java.util.List;


import org.slf4j.Logger;

import org.slf4j.LoggerFactory;



public class ShellCommand {

   
	private static final Logger LOGGER = LoggerFactory.getLogger(ShellCommand.class);

	
//	public static void shellCmd(String command){

//		try {

//			Runtime runtime = Runtime.getRuntime();

//			Process process = runtime.exec(command);

//			InputStream is = process.getInputStream();

//			InputStreamReader isr = new InputStreamReader(is);

//			BufferedReader br = new BufferedReader(isr);

//			String line;

//			while((line = br.readLine()) != null) {

//				System.out.println(line);

//			}

//		}catch (Exception e) {

//			e.printStackTrace();

//		}

//	}

	
    public static void execute(String cmd) {

        Process process = null;

        Runtime runtime = Runtime.getRuntime();

        StringBuffer successOutput = new StringBuffer(); // 성공 스트링 버퍼

        StringBuffer errorOutput = new StringBuffer(); // 오류 스트링 버퍼

        BufferedReader successBufferReader = null; // 성공 버퍼

        BufferedReader errorBufferReader = null; // 오류 버퍼

        String msg = null; // 메시지

        boolean errorFlag = false;

        List<String> cmdList = new ArrayList<String>();

 
        // 운영체제 구분 (window, window 가 아니면 무조건 linux 로 판단)

        if (System.getProperty("os.name").indexOf("Windows") > -1) {

            cmdList.add("cmd");

            cmdList.add("/c");

        } else {

            cmdList.add("/bin/sh");

            cmdList.add("-c");

        }

        // 명령어 셋팅

        cmdList.add(cmd);

        String[] array = cmdList.toArray(new String[cmdList.size()]);

 
        try {

 
            // 명령어 실행

            process = runtime.exec(array);

            
            
            // shell 실행이 정상 동작했을 경우

            successBufferReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "EUC-KR"));

 
            while ((msg = successBufferReader.readLine()) != null) {

                successOutput.append(msg + System.getProperty("line.separator"));

            }

 
            // shell 실행시 에러가 발생했을 경우

            errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "EUC-KR"));

            while ((msg = errorBufferReader.readLine()) != null) {

                errorOutput.append(msg + System.getProperty("line.separator"));

                errorFlag = true;

            }

            //윈도우일 경우 아래의 주석을 풀고서 해야함, 윈도우의 경우 행걸려서 멈춤

//            process.getErrorStream().close();

//            process.getInputStream().close();

//            process.getOutputStream().close();


            // 프로세스의 수행이 끝날때까지 대기

            process.waitFor();

            // shell 실행이 정상 종료되었을 경우

            if (process.exitValue() == 0) {

            	LOGGER.info("성공");

            	LOGGER.info(successOutput.toString());

            } else {

                // shell 실행이 비정상 종료되었을 경우

            	LOGGER.info("비정상 종료");

            	LOGGER.info(errorOutput.toString());

            }

 
            // shell 실행시 에러가 발생

            if (errorFlag) {

                // shell 실행이 비정상 종료되었을 경우

            	LOGGER.info("오류");

            	LOGGER.info(errorOutput.toString());

            }

            
            
        } catch (IOException e) {

        	LOGGER.error("### ShellCommand execute - IOException");

        } catch (InterruptedException e) {

        	LOGGER.error("### ShellCommand execute - InterruptedException");

        } catch (Exception e) {

        	LOGGER.error("### ShellCommand execute - Exception");

        } finally {

        	process.destroy();

        }

    }

	
}
