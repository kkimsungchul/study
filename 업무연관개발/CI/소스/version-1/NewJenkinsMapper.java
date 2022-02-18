

import java.util.List;
import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface NewJenkinsMapper
{
    public List<NewJenkinsParamVO> getJenkinsList(Long projectId);
    
    public Long insertJenkins(NewInsertJenkinsVO vo);

    public int insertValidJenkinsCheck(NewInsertJenkinsVO vo);
    public int updateValidJenkinsCheck(NewUpdateJenkinsVO vo);
    public int updateJenkins(NewUpdateJenkinsVO newUpdateJenkinsVO);
    
}
