package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * created by mayongbo on 2017/10/18
 *
 */
@Service
public class SubjectRepayDetailService {

    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;

    public Integer getPrincipal(String subjectId, String userId, Integer accountId){
     if(StringUtils.isBlank(subjectId)){
         throw new IllegalArgumentException("subjectId can not be null");
     }
     if(StringUtils.isBlank(userId)){
         throw new IllegalArgumentException("userId can not be null");
     }
     if(accountId == null){
         throw new IllegalArgumentException("accountId can not be null");
     }
     return subjectRepayDetailDao.findPrincipal(subjectId,userId,accountId);
    }

    public Integer getInterest(String subjectId, String userId, Integer accountId){
        if(StringUtils.isBlank(subjectId)){
            throw new IllegalArgumentException("subjectId can not be null");
        }
        if(StringUtils.isBlank(userId)){
            throw new IllegalArgumentException("userId can not be null");
        }
        if(accountId == null){
            throw new IllegalArgumentException("accountId can not be null");
        }
        return subjectRepayDetailDao.findInterest(subjectId,userId,accountId);
    }


    public Integer getNoPaidPrincipal(Integer status,String subjectId, String userId, Integer accountId){
        if(StringUtils.isBlank(subjectId)){
            throw new IllegalArgumentException("subjectId can not be null");
        }
        if(StringUtils.isBlank(userId)){
            throw new IllegalArgumentException("userId can not be null");
        }
        if(accountId == null || status==null){
            throw new IllegalArgumentException("accountId or status can not be null");
        }
        return subjectRepayDetailDao.findNoPaidPrincipal(status,subjectId,userId,accountId);
    }

    public Integer getNoPaidInterest(Integer status,String subjectId, String userId, Integer accountId){
        if(StringUtils.isBlank(subjectId)){
            throw new IllegalArgumentException("subjectId can not be null");
        }
        if(StringUtils.isBlank(userId)){
            throw new IllegalArgumentException("userId can not be null");
        }
        if(accountId == null || status==null){
            throw new IllegalArgumentException("accountId or status can not be null");
        }
        return subjectRepayDetailDao.findNoPaidInterest(status,subjectId,userId,accountId);
    }


}
