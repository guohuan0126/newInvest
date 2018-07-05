package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.agricultureloaninfo.AgricultureLoaninfoDao;
import com.jiuyi.ndr.dao.subject.SubjectPayoffRegDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.agricultureloaninfo.AgricultureLoaninfo;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectPayoffReg;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by lixiaolei on 2017/6/5.
 */
@Service
public class SubjectPayoffRegService {
    private final static Logger logger = LoggerFactory.getLogger(SubjectPayoffRegService.class);
    @Autowired
    private SubjectPayoffRegDao subjectPayoffRegDao;
    @Autowired
    private AgricultureLoaninfoDao agricultureLoaninfoDao;
    @Autowired
    private UserService userService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private  SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;

    public SubjectPayoffReg getSubjectPayoffReg(String subjectId) {
        return subjectPayoffRegDao.findBySubjectId(subjectId);
    }

    public List<SubjectPayoffReg> getSubjectPayoffRegs(String subjectId, Integer repayStatus, Integer openChannel) {
        return subjectPayoffRegDao.findBySubjectIdAndRepayStatusAndOpenChannel(subjectId, repayStatus, openChannel);
    }
    @ProductSlave
    public List<SubjectPayoffReg> findByConditions(Map<String, String> conditions) {
        String subjectName = StringUtils.hasText(conditions.get("subjectName")) ? "%" + conditions.get("subjectName") + "%" : "%%";
        String intermediatorId = conditions.get("intermediatorId");
        Integer isDirect = StringUtils.hasText(conditions.get("isDirect")) ? Integer.valueOf(conditions.get("isDirect")) : 1;
        Integer openChannel = StringUtils.hasText(conditions.get("openChannel")) ? Integer.valueOf(conditions.get("openChannel")) : 2;
        return subjectPayoffRegDao.findByConditions(subjectName, intermediatorId, isDirect, SubjectPayoffReg.REPAY_STATUS_PROCESS_NOT_YET, openChannel);
    }

    /**
     * 线上结清
     */
    public SubjectPayoffReg onLinePayOff(String subjectId) {
        SubjectPayoffReg payoffReg = subjectPayoffRegDao.findBySubjectId(subjectId);
        payoffReg.setRepayStatus(SubjectPayoffReg.REPAY_STATUS_PROCESSED);
        payoffReg.setActualDate(DateUtil.getCurrentDateShort());
        payoffReg.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectPayoffRegDao.updateById(payoffReg);
        return payoffReg;
    }

    /**
     * 插入提前结清
     * @param subjectPayoffReg
     * @return
     */
    public SubjectPayoffReg insert(SubjectPayoffReg subjectPayoffReg) {
        if (subjectPayoffReg == null) {
            throw new IllegalArgumentException("subjectPayoffReg is can not null");
        }
        subjectPayoffReg.setCreateTime(DateUtil.getCurrentDateTime19());
        subjectPayoffReg.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectPayoffRegDao.insert(subjectPayoffReg);
        return subjectPayoffReg;
    }

    /**
     * 标的提前结清处理
     * @param subjectId
     * @return
     */
    public SubjectPayoffReg subjectSettle(String subjectId){
        if (subjectId == null) {
            throw new IllegalArgumentException("subject is can not null");
        }
        Subject subject = subjectService.findSubjectBySubjectId(subjectId);
        SubjectPayoffReg subjectPayoffReg = new SubjectPayoffReg();
        SubjectRepaySchedule repaySchedule = subjectRepayScheduleDao.findBySubjectId(subject.getSubjectId());
        //判断正在还款中还是没生成repaySchedule
        if(repaySchedule==null){
            return null;
        }
        if(!subject.getDirectFlag().equals(Subject.DIRECT_FLAG_YES)){
            logger.info("标的不是直贷一期，参数subjectId=[{}],directFlag=[{}]",subject.getSubjectId(), subject.getDirectFlag());
            return null;
        }
       //只有农贷和车贷能结清发行
       if(subject.getType().equals(Subject.SUBJECT_TYPE_AGRICULTURAL)){
          //如果是农贷只有放款来源是久亿的才可以做结清
           AgricultureLoaninfo agricultureLoaninfo = agricultureLoaninfoDao.findByContractId(subject.getContractNo());
           if(agricultureLoaninfo!=null){
               if(agricultureLoaninfo.getLoanSource()!=null && "B".equals(agricultureLoaninfo.getLoanSource())){
                   return  null;
               }
               if(!subject.getAssetsSource().equals(Subject.AGRICULTURE)){
                   return null;
               }
           }
       }
       if(subject.getType().equals(Subject.SUBJECT_TYPE_CAR)){
           if(!subject.getAssetsSource().equals(Subject.INTERMEDIARY)){
               return null;
           }
       }
       //判断是否开户
       if(checkUser(subject.getBorrowerId())){
           subjectPayoffReg.setSubjectId(subject.getSubjectId());
         //1,2,4,8，第一位是否开放到散标，第二位是否开放到定期，第三位是否开放到活期，
           if(subject.getOpenChannel()==1){
               subjectPayoffReg.setOpenChannel(SubjectPayoffReg.OPEN_CHANNEL_SUBJECT);
           }else if(subject.getOpenChannel()==2){
               subjectPayoffReg.setOpenChannel(SubjectPayoffReg.OPEN_CHANNEL_IPLAN);
           }else if(subject.getOpenChannel()==4){
               subjectPayoffReg.setOpenChannel(SubjectPayoffReg.OPEN_CHANNEL_LPLAN);
           }
           subjectPayoffReg.setRepayStatus(SubjectPayoffReg.REPAY_STATUS_PROCESS_NOT_YET);
           subjectPayoffReg.setSettlementType(SubjectPayoffReg.STATUS_ADVANCE_PAYOFF_FORCE);
           subjectPayoffReg.setRepayDate(DateUtil.getCurrentDateShort());
           //插入提前结清
           subjectPayoffReg=this.insert(subjectPayoffReg);
       }
        return subjectPayoffReg;
    }

    /**
     * 检查用户是否注册及开户
     * @param userId
     * @return
     */
    private boolean checkUser(String userId){
        //检查用户是否注册及开户（账户状态是否正常）
        User user = userService.getUserById(userId);
        if (user == null) {
            //用户不存在
           return false;
        }
        UserAccount userAccount = userAccountService.getUserAccount(userId);
        if (userAccount == null) {
            //未开户
           return false;
        }
        if (!UserAccount.STATUS_ACTIVE_Y.equals(userAccount.getStatus())) {
            return false;
        }
        return true;
    }
}
