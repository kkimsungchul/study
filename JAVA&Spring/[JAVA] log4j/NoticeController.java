package com.cybertec.healingsecu.admin.controller.board;


import com.cybertec.healingsecu.admin.domain.board.MultiBoardDto;
import com.cybertec.healingsecu.admin.domain.system.SysLogDto;
import com.cybertec.healingsecu.admin.service.board.INoticeService;
import com.cybertec.healingsecu.admin.service.system.ISysLogService;
import com.cybertec.healingsecu.common.util.NumberPageNaviManager;
import com.cybertec.healingsecu.common.util.ScanJException;
import com.cybertec.healingsecu.common.util.SysLogUtil;
import com.cybertec.healingsecu.common.util.UtilBean;
import com.cybertec.healingsecu.user.domain.login.LoginUsersDto;
import com.cybertec.healingsecu.agent.log.Logger;
import com.cybertec.healingsecu.agent.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;


@Controller
public class NoticeController
{
    // Servcie Layer Interface
    @Autowired INoticeService miNoticeservice = null;
    @Autowired private ISysLogService miSysLogService;
    @Autowired SysLogUtil logUtil = SysLogUtil.instance();

    // logger
    private static final Logger logger = LoggerFactory.getLogger(NoticeController.class);
    UtilBean utilbean = new UtilBean();

    // 사용자요청 정보
    @ModelAttribute("requestInfo")
    public SysLogDto fillRequestInfo(HttpServletRequest request)
    {
        SysLogDto requestInfo = new SysLogDto();

        StringBuffer sbReqUrl = request.getRequestURL();
        String reqUrl = sbReqUrl.toString();
        requestInfo.setReq_path( reqUrl );
        requestInfo.setIp( logUtil.getIpAddr( request ) );

        return requestInfo;
    }

    //게시판 관리 처음 화면    oo
    @RequestMapping(value = "/{admin}/boardBase.do", method = RequestMethod.GET)
    public String boardBase(String checkMark, Integer tagetPage, Integer startPage, Integer endPage ,Model model ,HttpSession session, @ModelAttribute("requestInfo") SysLogDto requestInfo) {
        logger.info("Welcome NoticeController : /{admin}/boardBase.do");

        LoginUsersDto sessionData = (LoginUsersDto)session.getAttribute( "logindto" );
        String role_code = null;

        if(sessionData == null){

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-00-00-00", "N", "", "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";

        }else{

            role_code = sessionData.getRole_code();
        }

        if(role_code.equalsIgnoreCase("role01") || role_code.equalsIgnoreCase("role02") || role_code.equalsIgnoreCase("role03") || role_code.equalsIgnoreCase("role04")){

            //게시판 관리 active 를 위한 String
            String active = "boardGo";
            session.setAttribute("active", active);

            String leftActive = "boardBase";
            session.setAttribute("leftActive", leftActive);

            MultiBoardDto cntNotice = miNoticeservice.cntNotice();
            int totalNotice = cntNotice.getBoard_end();

            NumberPageNaviManager npvm = new NumberPageNaviManager(totalNotice, 5, 20);

            Map<String, Integer> naviResult = new HashMap<String, Integer>();
            int board_start = 0;
            int board_end = 0;

            if(totalNotice == 0){

                naviResult = npvm.getPPrevInfo();
                board_start = naviResult.get("begin");
                int tempEnd = naviResult.get("end");
                board_end = tempEnd - board_start;

            }else if(checkMark == null){

                naviResult = npvm.getPPrevInfo();
                board_start = naviResult.get("begin") -1 ;
                int tempEnd = naviResult.get("end");
                board_end = tempEnd - board_start;

            }else if(checkMark.equalsIgnoreCase("prev")){

                naviResult = npvm.getPrevInfo(startPage);
                board_start = naviResult.get("begin") -1;
                int tempEnd = naviResult.get("end");
                board_end = tempEnd - board_start;

            }else if(checkMark.equalsIgnoreCase("next")){

                naviResult = npvm.getNextInfo(endPage);
                board_start = naviResult.get("begin") -1;
                int tempEnd = naviResult.get("end");
                board_end = tempEnd - board_start;

            }else if(checkMark.equalsIgnoreCase("nnext")){

                naviResult = npvm.getNNextInfo();
                board_start = naviResult.get("begin") -1;
                int tempEnd = naviResult.get("end");
                board_end = tempEnd - board_start;

            }else if(checkMark.equalsIgnoreCase("target")){

                naviResult = npvm.getTargetPage(endPage, tagetPage);
                board_start = naviResult.get("begin") -1;
                int tempEnd = naviResult.get("end");
                board_end = tempEnd - board_start;

            }else{

                naviResult = npvm.getPPrevInfo();
                board_start = naviResult.get("begin") -1;
                int tempEnd = naviResult.get("end");
                board_end = tempEnd - board_start;

            }

            MultiBoardDto mbdto = new MultiBoardDto();
            mbdto.setBoard_start(board_start);
            mbdto.setBoard_end(board_end);

            List<MultiBoardDto> boardList = new ArrayList<MultiBoardDto>();
            boardList = miNoticeservice.boardList(mbdto);           //공지사항 0 ~ 25개 불러오는 쿼리

            List<MultiBoardDto> boardListSub = new ArrayList<MultiBoardDto>();
            boardListSub = utilbean.subStrDto(boardList, 40);              //제목이 30자 이상이면 ... 으로 표시되는 함수 utilstr.subStrDto

            //특수문자 치환
            /*for(int i = 0; i < boardListSub.size(); i++){

                String title = boardListSub.get(i).getTitle();

                title = utilbean.readFilterDB(title);

                boardList.set(i, boardListSub.get(i)).setTitle(title);

            }*/

            if(tagetPage == null){

                tagetPage = 1;

            }

            model.addAttribute("tagetPage", tagetPage);
            model.addAttribute("naviResult", naviResult);
            model.addAttribute("boardList", boardListSub);

            return "admin/boardList.tiles";

        }else{

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-00", "N", sessionData.getUser_id(), "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";
        }
    }


    //공지사항 상세보기              oo
    @RequestMapping(value = "/{admin}/detailNotice.do", method = RequestMethod.GET)
    public String detailNotice(Model model, int board_id, HttpSession session, @ModelAttribute("requestInfo") SysLogDto requestInfo) {
        logger.debug("Welcome NoticeController : /{admin}/detailNotice.do");

        LoginUsersDto sessionData = (LoginUsersDto)session.getAttribute( "logindto" );
        String role_code = null;

        if(sessionData == null){

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-00-00-00", "N", "", "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";

        }else{

            role_code = sessionData.getRole_code();
        }

        if(role_code.equalsIgnoreCase("role01") || role_code.equalsIgnoreCase("role02") || role_code.equalsIgnoreCase("role03") || role_code.equalsIgnoreCase("role04")){

            //게시판 관리 active 를 위한 String
            String active = "boardGo";
            session.setAttribute("active", active);

            MultiBoardDto detailNotice = new MultiBoardDto();

            miNoticeservice.readCountUp(board_id);                                      //조회수 증가 쿼리

            detailNotice = miNoticeservice.detailNotice(board_id);                      //공지사항 상세보기 쿼리

            /*//String content = utilbean.readFilterDB(detailNotice.getContent());
            String title = utilbean.readFilterDB(detailNotice.getTitle());
            detailNotice.setContent(detailNotice.getContent());
            detailNotice.setTitle(title);*/

            model.addAttribute("detailNotice", detailNotice);

            return "admin/detailNotice.tiles";

        }else{

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-00", "N", sessionData.getUser_id(), "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";
        }
    }

    //공지사항 수정화면       oo
    @RequestMapping(value = "/{admin}/updateNotice.do", method = RequestMethod.POST)
    public String updateNotice(Model model, int board_id, HttpSession session, @ModelAttribute("requestInfo") SysLogDto requestInfo) {
        logger.debug("Welcome NoticeController : /{admin}/updateNotice.do");

        LoginUsersDto sessionData = (LoginUsersDto)session.getAttribute( "logindto" );
        String role_code = null;

        if(sessionData == null){

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-00-00-00", "N", "", "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";

        }else{

            role_code = sessionData.getRole_code();
        }

        if(role_code.equalsIgnoreCase("role01") || role_code.equalsIgnoreCase("role02")){

            MultiBoardDto detailNotice = new MultiBoardDto();
            detailNotice = miNoticeservice.detailNotice(board_id);                  //공지사항 상세정보 불러오기

            model.addAttribute("detailNotice", detailNotice);

            return "admin/updateNotice.tiles";

        }else{

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-00", "N", sessionData.getUser_id(), "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";
        }
    }

    //공지사항 삭제하기        oo
    @RequestMapping(value = "/{admin}/deleteNotice.do", method = RequestMethod.GET)
    public String deleteNotice(Model model, int board_id, HttpSession session, @ModelAttribute("requestInfo") SysLogDto requestInfo) throws ScanJException{
        logger.debug("Welcome NoticeController : /{admin}/deleteNotice.do");

        LoginUsersDto sessionData = (LoginUsersDto)session.getAttribute( "logindto" );
        String role_code = null;

        if(sessionData == null){


            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-00-00-00", "N", "", "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";

        }else{

            role_code = sessionData.getRole_code();

        }

        if(role_code.equalsIgnoreCase("role01") || role_code.equalsIgnoreCase("role02")){

            try{

                miNoticeservice.deleteNotice(board_id);                                 //상세보기에서 삭제

                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-03", "Y", sessionData.getUser_id(), "공지사항 삭제에 성공하였습니다." );

            }catch ( DataAccessException se){
                logger.error("", se);
                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( null, requestInfo, "log-05-01-03", "N", sessionData.getUser_id(), "공지사항 삭제에 실패하였습니다." );

                throw new ScanJException("공지사항 삭제에 실패하였습니다.");

            }catch (Exception e){

                logger.error("", e);
                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( null, requestInfo, "log-05-01-03", "N", sessionData.getUser_id(), "공지사항 삭제에 실패하였습니다." );
                throw new ScanJException("공지사항 삭제에 실패하였습니다.");

            }

            return "redirect:/{admin}/boardBase.do";

        }else{

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-03", "N", sessionData.getUser_id(), "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";
        }
    }

    //공지사항 체크 삭제하기        oo
    @RequestMapping(value = "/{admin}/checkDeleteNotice.do", method = RequestMethod.GET)
    public String checkDeleteNotice(Model model, int[] checkDelList, HttpSession session, @ModelAttribute("requestInfo") SysLogDto requestInfo) throws ScanJException{
        logger.debug("Welcome NoticeController : /{admin}/checkDeleteNotice.do");

        LoginUsersDto sessionData = (LoginUsersDto)session.getAttribute( "logindto" );
        String role_code = null;

        if(sessionData == null){

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-00-00-00", "N", "", "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";

        }else{

            role_code = sessionData.getRole_code();

        }

        if(role_code.equalsIgnoreCase("role01") || role_code.equalsIgnoreCase("role02")){

            try{

                miNoticeservice.deleteCheckNotice(checkDelList);                      //공지사항 목록에서 선택 항목 삭제

                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-03", "Y", sessionData.getUser_id(), "공지사항 삭제에 성공하였습니다." );

            }catch (DataAccessException se){

                logger.error("", se);
                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( null, requestInfo, "log-05-01-03", "N", sessionData.getUser_id(), "공지사항 삭제에 실패하였습니다." );
                throw new ScanJException("공지사항 삭제에 실패하였습니다.");

            }catch (Exception e){

                logger.error("", e);
                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( null, requestInfo, "log-05-01-03", "N", sessionData.getUser_id(), "공지사항 삭제에 실패하였습니다." );
                throw new ScanJException("공지사항 삭제에 실패하였습니다.");
            }

            return "redirect:/{admin}/boardBase.do";

        }else {

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-03", "N", sessionData.getUser_id(), "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";

        }

    }

    //공지사항 등록 화면              oo
    @RequestMapping(value = "/{admin}/inserNotice.do", method = RequestMethod.GET)
    public String inserNotice(Model model, HttpSession session, @ModelAttribute("requestInfo") SysLogDto requestInfo) {
        logger.debug("Welcome NoticeController : /{admin}/inserNotice.do");

        LoginUsersDto sessionData = (LoginUsersDto)session.getAttribute( "logindto" );
        String role_code = null;

        if(sessionData == null){

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-00-00-00", "N", "", "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";

        }else{

            role_code = sessionData.getRole_code();
        }

        if(role_code.equalsIgnoreCase("role01") || role_code.equalsIgnoreCase("role02")){

            return "admin/inserNotice.tiles";

        }else{

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-00", "N", sessionData.getUser_id(), "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";
        }
    }

    //공지사항 등록          oo
    @RequestMapping(value = "/{admin}/afInsertNotice.do", method = RequestMethod.POST)
    public String afInsertNotice(Model model, MultiBoardDto mbdto, HttpSession session, @ModelAttribute("requestInfo") SysLogDto requestInfo) throws ScanJException{
        logger.debug("Welcome NoticeController /{admin}/afInsertNotice.do " + new Date());

        LoginUsersDto sessionData = (LoginUsersDto)session.getAttribute( "logindto" );
        String role_code = null;

        if(sessionData == null){

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-00-00-00", "N", "", "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";

        }else{

            role_code = sessionData.getRole_code();
        }

        if(role_code.equalsIgnoreCase("role01") || role_code.equalsIgnoreCase("role02")){

            try{

                String title = utilbean.writeFilterDB(mbdto.getTitle());
                String content = utilbean.writeFilterDB(mbdto.getContent());

                mbdto.setBoard_type("0");                                                        //공지사항 보드 타입
                mbdto.setTitle(title);
                mbdto.setContent(content);

                miNoticeservice.insertNotice(mbdto);

                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-01", "Y", sessionData.getUser_id(), "공지사항 등록에 성공하였습니다." );

            }catch (DataAccessException se){

                logger.error("", se);
                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( null, requestInfo, "log-05-01-01", "N", sessionData.getUser_id(), "공지사항 등록에 실패하였습니다." );
                throw new ScanJException("공지사항 등록에 실패하였습니다.");

            }catch (Exception e){

                logger.error("", e);
                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( null, requestInfo, "log-05-01-01", "N", sessionData.getUser_id(), "공지사항 등록에 실패하였습니다." );
                throw new ScanJException("공지사항 등록에 실패하였습니다.");
            }

            return "redirect:/{admin}/boardBase.do";

        }else{

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-01", "N", sessionData.getUser_id(), "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";

        }
    }

    //공지사항 수정     oo
    @RequestMapping(value = "/{admin}/afUpdateNotice.do", method = RequestMethod.POST)
    public String afUpdateNotice(Model model, MultiBoardDto mbdto, HttpSession session, @ModelAttribute("requestInfo") SysLogDto requestInfo) throws ScanJException{
        logger.debug("Welcome NoticeController : /{admin}/afUpdateNotice.do");

        LoginUsersDto sessionData = (LoginUsersDto)session.getAttribute( "logindto" );
        String role_code = null;

        if(sessionData == null){

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-00-00-00", "N", "", "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";

        }else{

            role_code = sessionData.getRole_code();
        }

        if(role_code.equalsIgnoreCase("role01") || role_code.equalsIgnoreCase("role02")){

            try{

                String title = utilbean.writeFilterDB(mbdto.getTitle());
                String content = utilbean.writeFilterDB(mbdto.getContent());

                mbdto.setTitle(title);
                mbdto.setContent(content);

                miNoticeservice.updateNotice(mbdto);

                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-02", "Y", sessionData.getUser_id(), "공지사항 수정에 성공하였습니다." );

            }catch (DataAccessException se){

                logger.error("", se);
                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( null, requestInfo, "log-05-01-02", "N", sessionData.getUser_id(), "공지사항 수정에 실패하였습니다." );
                throw new ScanJException("공지사항 수정에 실패하였습니다.");

            }catch (Exception e){

                logger.error("", e);
                // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
                logUtil.setSysLogInfo( null, requestInfo, "log-05-01-02", "N", sessionData.getUser_id(), "공지사항 수정에 실패하였습니다." );
                throw new ScanJException("공지사항 수정에 실패하였습니다.");

            }

            return "redirect:/{admin}/boardBase.do";

        }else{

            // setSysLogInfo( logger, requestInfo, _logType, _auditType, _user_id, _logMessage )
            logUtil.setSysLogInfo( logger, requestInfo, "log-05-01-02", "N", sessionData.getUser_id(), "유효한 사용자 요청이 아닙니다." );

            return "user/userlogin.tiles";
        }
    }

    public void setSysLogInfo( Logger _logger,  SysLogDto _requestInfo, String _logType, String _auditType, String _userId, String _logMessage  )
    {

        SysLogDto sysLog = logUtil.setSysLog( _requestInfo, _logType, _auditType, _userId, _logMessage );

        if( _auditType.equalsIgnoreCase("Y") )
        {
            if( _logger != null )
            {
                _logger.info( "IP : " + " UserID : " + _userId + _requestInfo.getIp() + " URL : " + _requestInfo.getReq_path() + " -> " + _logMessage );
            }
            miSysLogService.saveSysLog( sysLog );
        }
        else if( _auditType.equalsIgnoreCase("N") )
        {
            if( _logger != null )
            {
                _logger.error( "IP : " + _requestInfo.getIp() + " UserID : " + _userId + " URL : " + _requestInfo.getReq_path() + " -> " + _logMessage );
            }
            miSysLogService.saveSysLog( sysLog );
        }
        else
        {
            if( _logger == null )
            {
                _logger.error( "IP : " + _requestInfo.getIp() + " UserID : " + _userId + " URL : " + _requestInfo.getReq_path() + " -> 처리결과가 잘못입력되었습니다." );
            }
        }
    }

}




