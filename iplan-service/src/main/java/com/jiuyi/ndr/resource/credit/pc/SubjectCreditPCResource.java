package com.jiuyi.ndr.resource.credit.pc;

import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.dto.subject.SubjectInvestDetailDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * Created by YUMIN on 2017/11/3.
 */
@RestController
@RequestMapping(value = "/pc/credit")
public class SubjectCreditPCResource {

    private final static Logger logger = LoggerFactory.getLogger(SubjectCreditPCResource.class);

    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;


    private DecimalFormat df = new DecimalFormat("######0.00");
    private DecimalFormat df2 = new DecimalFormat("######0");
    private static DecimalFormat df3 = new DecimalFormat("0.####");
    private DecimalFormat df4 = new DecimalFormat("######0.##");

    /**
     * PC债权购买
     * @param userId
     * @param openingCreditId
     * @param params
     * @return
     */
    @PostMapping("/{userId}/investCredit/{openingCreditId}")
    public RestResponse invest(@PathVariable("userId") String userId,
                               @PathVariable("openingCreditId") int openingCreditId,
                               @RequestBody Map<String, String> params) {
        int principal = 0;
        if (!StringUtils.hasText(params.get("principal"))) {
            throw new IllegalArgumentException("principal不能为空");
        }
        principal =Integer.parseInt(params.get("principal"));
        //double discount = 0;
        if (!StringUtils.hasText(params.get("discount"))) {
            throw new IllegalArgumentException("discount不能为空");
        }
        //discount =Double.valueOf(params.get("discount"));
        String device="";
        if (params.containsKey("requestSource")) {
            device = params.get("requestSource");
        }
        Integer redPackedId = 0;
        if (params.containsKey("redPackedId")) {
            redPackedId = Integer.valueOf(params.get("redPackedId"));
        }
        //实际支付金额=购买金额*折扣率
        double actualPrincipal = principal*(creditOpeningDao.findById(openingCreditId).getTransferDiscount().doubleValue());

        String investRequestNo = subjectAccountService.investSubjectCredit(openingCreditId,principal,
                actualPrincipal,userId,device,redPackedId);
        SubjectInvestDetailDto subjectInvestDetailDto = new SubjectInvestDetailDto();
        String iplanInvestSuccessDesc = "";
        double money = 0;
        //根据请求流水号查询标的投资账户
        SubjectAccount subjectAccount = subjectAccountService.getSubjectAccountByRequestNo(investRequestNo);

        //预期利息（分）
        int expectedInterest = (subjectAccount != null && subjectAccount.getExpectedInterest() != null) ? subjectAccount.getExpectedInterest() : 0;
        //预期加息利息
        int expectedBonusInterest = (subjectAccount != null && subjectAccount.getSubjectExpectedBonusInterest() != null) ? subjectAccount.getSubjectExpectedBonusInterest() : 0;
        //预期vip特权加息
        //int expectedVipInterest = (subjectAccount != null && subjectAccount.getSubjectExpectedVipInterest() != null) ? subjectAccount.getSubjectExpectedVipInterest() : 0;
        //预期奖励
        int expectedReward =(subjectAccount != null && subjectAccount.getExpectedReward() != null) ? subjectAccount.getExpectedReward() : 0;

        //得到得到抵扣金额
        int  dedutionAmt =(subjectAccount != null && subjectAccount.getDedutionAmt() !=null) ?subjectAccount.getDedutionAmt():0;
        int totalMoney = expectedInterest + expectedBonusInterest+expectedReward - dedutionAmt;

        money += totalMoney/100.0;
        iplanInvestSuccessDesc += "预计赚取" + df.format(money) + "元。";
        iplanInvestSuccessDesc += "您可以使用App邀请好友继续赚取更多奖励。";
        subjectInvestDetailDto.setTitle("购买成功");
        subjectInvestDetailDto.setInvestSuccessDesc(iplanInvestSuccessDesc);

        return new RestResponseBuilder<SubjectInvestDetailDto>().success(subjectInvestDetailDto);
    }

}

