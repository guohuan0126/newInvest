package com.jiuyi.ndr.service.subject;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.dao.agricultureloaninfo.AgricultureLoaninfoDao;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.subject.ContractInterMediacyDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.transferstation.TransferStationDao;
import com.jiuyi.ndr.domain.agricultureloaninfo.AgricultureLoaninfo;
import com.jiuyi.ndr.domain.subject.ContractInterMediacy;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.WithdrawEnum;
import com.jiuyi.ndr.domain.transferstation.TransferStation;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.drpay.DrpayResponse;
import com.jiuyi.ndr.service.drpay.DrpayService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 标的提现服务
 * Created by mayongbo on 2017/9/7.
 */

@Service
public class SubjectWithdrawService {
    private final static Logger logger = LoggerFactory.getLogger(SubjectWithdrawService.class);

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private AgricultureLoaninfoDao agricultureLoaninfoDao;

    @Autowired
    private TransferStationDao transferStationDao;

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ConfigDao configDao;
    @Autowired
    private ContractInterMediacyDao contractInterMediacyDao;


    /**
     * 提现
     */
    @Transactional(noRollbackFor = ProcessException.class)
    public Subject withdraw(String subjectId) {
        Subject subject = subjectDao.findBySubjectIdForUpdate(subjectId); //查询出标的
        if (Subject.DIRECT_FLAG_YES.equals(subject.getDirectFlag())) {
            sendRedis(subjectId,subject);
            subject.setWithdrawStatus(Subject.WITHDRAW_SUCCESSED);
            subjectDao.update(subject);
            return subject;
        }
        List<String> lists = Arrays.asList(configDao.getConfigById("auto_withdraw").getValue().split(","));
        if (lists!=null && lists.contains(subject.getType())) {

            double money = (subject.getTotalAmt() - subject.getFeeAmt() - subject.getMiscellaneousAmt()) / 100.0;
            DrpayResponse drpayResponse = null;

            //请求流水号
            String requestNo = IdUtil.getRequestNo();
            if (Subject.WITHDRAW_NO.equals(subject.getWithdrawStatus())) {
                sendRedis(subjectId,subject);
            }
            //提现申请
            drpayResponse = this.getDrpayResponse(subject.getContractNo(), subject.getBorrowerId(), getWithdrawType(subject), money, requestNo);
            subject.setWithdrawSn(requestNo);
            if (drpayResponse != null && DrpayResponse.SUCCESS.equals(drpayResponse.getCode())) {
                Map<String, String> result = (Map<String, String>) drpayResponse.getData();
                if (result != null && "success".equals(result.get("status"))) {
                    logger.info("自动提现请求成功：[{}]", drpayResponse.toString());
                    logger.info("用户Id[{}]自动提现请求成功", subject.getBorrowerId());
                    subject.setWithdrawStatus(Subject.WITHDRAW_SUCCESSED);
                } else {
                    subject.setWithdrawStatus(Subject.WITHDRAW__FAILED);
                }
            } else {
                subject.setWithdrawStatus(Subject.WITHDRAW__FAILED);
                logger.info("用户Id[{}]自动提现请求失败", subject.getBorrowerId());
                logger.error("自动提现请求失败,错误信息[{}]", drpayResponse != null ? drpayResponse.getMsg() : Error.NDR_0458.getMessage());
            }
        } else {
            sendRedis(subjectId,subject);
            subject.setWithdrawStatus(Subject.WITHDRAW_SUCCESSED);
        }
        subjectDao.update(subject); //更新标的
        return subject;
    }

    private void sendRedis(String subjectId,Subject subject) {
        Map<String, String> map1 = new HashMap<>();
        map1.put("signType", "giveMoneyToBorrower");
        map1.put("subjectId", subjectId);
        String contract = JSON.toJSONString(map1);
        redisClient.product("contract", contract);
        redisClient.product("contract1", contract);
        if(Subject.SUBJECT_TYPE_COMPANY.equals(subject.getType())){
            ContractInterMediacy contractInterMediacy = new ContractInterMediacy();
            contractInterMediacy.setSubjectId(subjectId);
            contractInterMediacy.setContractId(subject.getContractNo());
            contractInterMediacy.setCreateTime(new Date());
            contractInterMediacyDao.insert(contractInterMediacy);
        }
        logger.info("直贷二期redis消息通知,生成合同号[{}]", contract);
    }

    private DrpayResponse getDrpayResponse(String contractNo, String userId, WithdrawEnum withdrawType, double withdrawAmt, String requestNo) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("withdrawType", withdrawType);
        map.put("money", withdrawAmt);
        map.put("requestNo", requestNo);
        map.put("contractNo", contractNo);
        return DrpayResponse.toGeneratorJSON(DrpayService.post(DrpayService.AUTO_WITHDRAW, map));
    }

    //查询农贷或者车贷的提现类型
    public WithdrawEnum getWithdrawType(Subject subject) {
        if (Subject.AGRICULTURE.equals(subject.getAssetsSource())) {//农贷标
            AgricultureLoaninfo agricultureLoaninfo = agricultureLoaninfoDao.findByContractId(subject.getContractNo());
            if (Subject.Y.equals(agricultureLoaninfo.getWithdrawWay())) {
                return WithdrawEnum.QUICK;
            } else {
                return WithdrawEnum.NORMAL;
            }
        } else {
            TransferStation transferStation = transferStationDao.findByContractId(subject.getContractNo());
            if (Subject.T0.equals(transferStation.getWithdrawWay())) {
                return WithdrawEnum.QUICK;
            } else {
                return WithdrawEnum.NORMAL;
            }
        }
    }
}
