package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.subject.SubjectAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectTransLogDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.marketing.MarketService;
import com.jiuyi.ndr.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import java.util.List;

/**
 * created by mayongbo on 2017/10/18
 *
 */
@Service
public class SubjectTransLogService {
    private final static Logger logger = LoggerFactory.getLogger(SubjectTransLogService.class);
    @Autowired
    private SubjectTransLogDao subjectTransLogDao;
    @Autowired
    private SubjectAccountDao subjectAccountDao;
    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;
    @Autowired
    private MarketService marketService;

    public SubjectTransLog insert(SubjectTransLog subjectTransLog) {
        if (subjectTransLog == null) {
            throw new IllegalArgumentException("subjectTransLog can not null");
        }
        subjectTransLog.setCreateTime(DateUtil.getCurrentDateTime19());
        if (subjectTransLog.getAutoInvest() == null) {
            subjectTransLog.setAutoInvest(0);
        }
        subjectTransLogDao.insert(subjectTransLog);
        return subjectTransLog;
    }
    @Transactional
    public SubjectTransLog getByIdLocked(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("subjectTransLog id is can not null");
        }
        return subjectTransLogDao.findByIdForUpdate(id);
    }
    @ProductSlave
    public SubjectTransLog getById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("subjectTransLog id is can not null");
        }
        return subjectTransLogDao.findById(id);
    }

    public SubjectTransLog update(SubjectTransLog subjectTransLog) {
        if (subjectTransLog == null || subjectTransLog.getId() == null) {
            throw new IllegalArgumentException("subjectTransLog or subjectTransLog id can not null when update");
        }
        subjectTransLog.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectTransLogDao.update(subjectTransLog);
        return subjectTransLog;
    }

    public List<SubjectTransLog> getBySubjectIdAndTransStatusAndTransTypeIn(String subjectId, String transTypes, String transStatuses) {
        Set<Integer> transTypesSet = new HashSet(Arrays.asList(transTypes.split(",")));
        Set<Integer> transStatusesSet = new HashSet(Arrays.asList(transStatuses.split(",")));
        return this.getBySubjectIdAndTransStatusAndTransTypeIn(subjectId, transTypesSet, transStatusesSet);
    }
    @ProductSlave
    private List<SubjectTransLog> getBySubjectIdAndTransStatusAndTransTypeIn(String subjectId, Set<Integer> transTypes, Set<Integer> transStatuses) {
        if ( subjectId != null && subjectId != "" && !transTypes.isEmpty() && !transStatuses.isEmpty()) {
            return subjectTransLogDao.findBySubjectIdAndTransStatusAndTransTypeIn(subjectId, transTypes, transStatuses);
        } else {
            throw new IllegalArgumentException("userId and subjectId and transTypes and transStatus is can not null");
        }
    }
    @ProductSlave
    public List<SubjectTransLog> getAllSubjectTransLogByTargetId(Integer targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException("subjectTransLog id is can not null");
        }
        return subjectTransLogDao.findAllByTargetId(targetId);
    }
    @ProductSlave
    public List<SubjectTransLog> getSubjectTransLogByUserIdAndStatus(String userId){
        if (StringUtils.isBlank(userId)){
            throw  new IllegalArgumentException("usrId can not be null");
        }
        return subjectTransLogDao.findByUserIdAndStatus(userId);
    }

    public List<SubjectTransLog> sortBySource(List<SubjectTransLog> subjectTransLogs, Integer source) {
        List<SubjectTransLog> newList = new ArrayList<>();
        for (SubjectTransLog subjectTransLog : subjectTransLogs) {
            SubjectAccount subjectAccount = subjectAccountDao.findByIdAndSource(subjectTransLog.getAccountId(), source);
            if (subjectAccount != null){
                newList.add(subjectTransLog);
            }
        }
        return newList;
    }
    @ProductSlave
    public List<SubjectTransLog> getByAccountIdAndType(Integer id) {
        return subjectTransLogDao.findByAccountIdAndType(id);
    }
    @ProductSlave
    public SubjectTransLog getTransLogByAccountId(Integer accountId){
        return  subjectTransLogDao.findByAccountId(accountId);
    }
    @ProductSlave
    public Integer getTimes(String userId){
        Integer times = 0;
        if(StringUtils.isNotBlank(userId)){
            Integer times1 = subjectTransLogDao.findByUserId(userId);
            Integer times2 = iPlanTransLogDao.findByUserIdTimes(userId);
            times = marketService.getCreditFreeTimes(userId) - times1 - times2;
        }
        return times > 0 ? times : 0;
    }
}
