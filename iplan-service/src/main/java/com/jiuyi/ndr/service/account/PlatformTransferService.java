package com.jiuyi.ndr.service.account;

import com.jiuyi.ndr.dao.account.PlatformTransferDao;
import com.jiuyi.ndr.domain.account.PlatformTransfer;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author ke 2017/5/5
 */
@Service
public class PlatformTransferService {

    @Autowired
    private PlatformTransferDao platformTransferDao;

    /**
     * 营销款PLATFORM_ID_002_01出账
     *
     * @param username      账户名
     * @param actualMoney   实际金额
     * @param loanId        项目id
     * @param orderId       请求存管的流水号
     */
    public void out(String username, Double actualMoney, String loanId, String orderId,String interviewerId) {
        PlatformTransfer platformTransfer = new PlatformTransfer();
        platformTransfer.setUsername(username);
        platformTransfer.setActualMoney(actualMoney);
        platformTransfer.setLoanId(loanId);
        platformTransfer.setOrderId(orderId);
        platformTransfer.setPlatformId(PlatformTransfer.PLATFORM_ID_002_01);
        if (StringUtils.isEmpty(platformTransfer.getId())) {
            platformTransfer.setId(IdUtil.randomUUID());//32位
        }
        platformTransfer.setRemarks("营销款账户002_01，给还款人划款");
        platformTransfer.setStatus("平台划款成功");
        platformTransfer.setTime(DateUtil.getCurrentDateTime19());
        platformTransfer.setSuccessTime(DateUtil.getCurrentDateTime19());
        platformTransfer.setBillType(PlatformTransfer.BILL_TYPE_OUT);
        platformTransfer.setType(PlatformTransfer.TYPE);
        platformTransfer.setInterviewerId(interviewerId);
        platformTransferDao.insert(platformTransfer);
    }

    /**
     * 营销款PLATFORM_ID_002出账
     *
     * @param username      账户名
     * @param actualMoney   实际金额
     * @param loanId        项目id
     * @param orderId       请求存管的流水号
     */
    public void out002(String username, Double actualMoney, String loanId, String orderId) {
        PlatformTransfer platformTransfer = new PlatformTransfer();
        platformTransfer.setUsername(username);
        platformTransfer.setActualMoney(actualMoney);
        platformTransfer.setLoanId(loanId);
        platformTransfer.setOrderId(orderId);
        platformTransfer.setPlatformId(PlatformTransfer.PLATFORM_ID_002);
        if (StringUtils.isEmpty(platformTransfer.getId())) {
            platformTransfer.setId(IdUtil.randomUUID());//32位
        }
        platformTransfer.setRemarks("营销款账户002，给投资人补息");
        platformTransfer.setStatus("平台划款成功");
        platformTransfer.setTime(DateUtil.getCurrentDateTime19());
        platformTransfer.setSuccessTime(DateUtil.getCurrentDateTime19());
        platformTransfer.setBillType(PlatformTransfer.BILL_TYPE_OUT);
        platformTransfer.setType(PlatformTransfer.TYPE_2);
        platformTransferDao.insert(platformTransfer);

    }

    /**
     * 营销款入账
     *
     * @param username      转账人
     * @param actualMoney   实际金额
     * @param loanId        项目id
     * @param orderId       存管流水号id
     */
    public void in(String username, Double actualMoney, String loanId, String orderId){
        PlatformTransfer platformTransfer = new PlatformTransfer();
        platformTransfer.setUsername(username);
        platformTransfer.setActualMoney(actualMoney);
        platformTransfer.setLoanId(loanId);
        platformTransfer.setOrderId(orderId);
        platformTransfer.setPlatformId(PlatformTransfer.PLATFORM_ID_002);
        if (StringUtils.isEmpty(platformTransfer.getId())) {
            platformTransfer.setId(IdUtil.randomUUID());//32位
        }
        platformTransfer.setRemarks("营销款账户002收款");
        platformTransfer.setStatus("平台收款成功");
        platformTransfer.setTime(DateUtil.getCurrentDateTime19());
        platformTransfer.setSuccessTime(DateUtil.getCurrentDateTime19());
        platformTransfer.setBillType(PlatformTransfer.BILL_TYPE_IN);
        platformTransfer.setType(PlatformTransfer.TYPE);
        platformTransferDao.insert(platformTransfer);
    }
    //查询居间人在营销款账户余额
    public double selectTotalSctualMoneyByInterviewerId(String interviewerId){
        return platformTransferDao.selectTotalSctualMoneyByInterviewerId(interviewerId);
    }
}
