package com.jiuyi.ndr.service.invest;

import com.duanrong.util.json.FastJsonUtil;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestUserAutoPreTransaction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jiuyi.ndr.dao.invest.InvestDao;
import com.jiuyi.ndr.dao.user.UserOtherInfoDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.service.config.ConfigService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.redis.RedisClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by drw on 2017/6/9.
 */
@Service
public class InvestService {

    @Autowired
    private InvestDao investDao;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private RedisClient redisClinet;
    @Autowired
    private UserOtherInfoDao userOtherInfoDao;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private TransactionService transactionService;
    public static final String ACTIVITY_INVEST = "activity_invest";
    public static final String PUSH_INVEST = "pushinvest";

    private static Logger logger = LoggerFactory.getLogger(InvestService.class);

    /**
     * 获取用户投资（除定期之外的非流标状态）总金额
     * @param userId
     * @return
     */
    public Double getInvestTotalMoney(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId不能为空");
        }
        return investDao.getInvestTotalMoney(userId);
    }

    /**
     * 获取新手剩余可用额度（分为单位）
     *
     * @param userId    用户id
     */
    @ProductSlave
    public Double getNewbieUsable(String userId) {
        if (StringUtils.isBlank(userId)) {
            return 0D;
        }
        //获取已投金额（散标、理财包已投金额+定期理财计划已投金额）
        Double investedAmt = iPlanService.findInvestedAmtByUserId(userId);
        //获取新手额度
        Config iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN_NEWBIE_AMT);

        double newbieAmt = 5000000;
        if (iPlanNewbieAmtConfig != null && org.apache.commons.lang3.StringUtils.isNotBlank(iPlanNewbieAmtConfig.getValue())) {
            newbieAmt = ArithUtil.round(Double.parseDouble(iPlanNewbieAmtConfig.getValue()), 2) * 100;
        }
        return newbieAmt - investedAmt;
    }
    /**
     * 获取新手剩余可用额度（分为单位） 散标、月月盈、省心投分别设置新手额度
     *
     * @param userId    用户id
     */
    @ProductSlave
    public Double getNewbieUsable(String userId,Integer newbietype) {
        Config iPlanNewbieAmtConfig=null;
        double newbieAmt = 0;
        double investedAmt=0;
        //获取共享新手额度
        iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN_NEWBIE_AMT);
        
        if (iPlanNewbieAmtConfig != null && org.apache.commons.lang3.StringUtils.isNotBlank(iPlanNewbieAmtConfig.getValue())
        		&&Double.parseDouble(iPlanNewbieAmtConfig.getValue())>0) {
        	newbieAmt = ArithUtil.round(Double.parseDouble(iPlanNewbieAmtConfig.getValue()), 2) * 100;	
        	if (StringUtils.isBlank(userId)) {
                return newbieAmt;
            }
        	investedAmt = iPlanService.findInvestedAmtByUserId(userId);
        		//获取已投金额（散标、理财包已投金额+定期理财计划已投金额）
        }else{
        	if(newbietype!=null){
        		if(newbietype==0){//月月盈
                	//获取月月盈新手额度
                	iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN0_NEWBIE_AMT);
                }
                if(newbietype==2){//省心投
                	//获取省心投新手额度
                	iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN2_NEWBIE_AMT);
                }
        	}else{
            	//获取散标新手额度
            	iPlanNewbieAmtConfig = configService.getConfigById(Config.SUBJECT_NEWBIE_AMT);
            }
            
            if (iPlanNewbieAmtConfig != null && org.apache.commons.lang3.StringUtils.isNotBlank(iPlanNewbieAmtConfig.getValue())) {
            	newbieAmt = ArithUtil.round(Double.parseDouble(iPlanNewbieAmtConfig.getValue()), 2) * 100;
            }else{
            	return 0D;
            }
            
            if (StringUtils.isBlank(userId)) {
                return newbieAmt;
            }
            
            if(newbietype!=null){
            	if(newbietype==0){//月月盈
            		//查询月月盈投资总额
            		investedAmt=iPlanAccountService.getIPlanTypeTotalMoney(userId, newbietype.toString());
            	}
            	if(newbietype==2){//省心投
            		//查询省心投投资总额
            		investedAmt=iPlanAccountService.getIPlanTypeTotalMoney(userId, newbietype.toString());
            	}
            }else{
            	investedAmt=subjectAccountService.getSubjectTotalMoney(userId);//查询散标投资总额
            }
        }
        if(newbieAmt - investedAmt<0){
        	return 0D;
        }
        return newbieAmt - investedAmt;
    }
    /**
     * 获取新手剩余可用额度（分为单位）
     *
     * @param userId    用户id
     */
    @ProductSlave
    public Double getWeChatNewbieUsable(String userId) {
        if (StringUtils.isBlank(userId)) {
            return 0D;
        }
        //获取已投金额（散标、理财包已投金额+定期理财计划已投金额）
        Double investedAmt = iPlanService.findInvestedAmtByUserId(userId);
        //获取新手额度
        Config iPlanWeChatNewbieAmtConfig = configService.getConfigById(Config.IPLAN_WEICHAT_AMT);
        double newbieAmt = 5000000;
        if (iPlanWeChatNewbieAmtConfig != null && org.apache.commons.lang3.StringUtils.isNotBlank(iPlanWeChatNewbieAmtConfig.getValue())) {
            newbieAmt = ArithUtil.round(Double.parseDouble(iPlanWeChatNewbieAmtConfig.getValue()), 2) * 100;
        }
        return newbieAmt - investedAmt;
    }

    /**
     * 获取配置的新手额度（元）
     * @return
     */
    public Double getNewbieAmt() {
        //获取新手额度
        Config iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN_NEWBIE_AMT);
        double newbieAmt = 50000;
        if (iPlanNewbieAmtConfig != null && org.apache.commons.lang3.StringUtils.isNotBlank(iPlanNewbieAmtConfig.getValue())) {
            newbieAmt = ArithUtil.round(Double.parseDouble(iPlanNewbieAmtConfig.getValue()), 2);
        }
        return newbieAmt;
    }

    /**
     * 获取配置的新手额度（元）
     * @return
     */
    public Double getNewbieAmt(Integer newbietype) {
        //获取新手额度
        Config iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN_NEWBIE_AMT);
        double newbieAmt = 0;
        if (iPlanNewbieAmtConfig != null && org.apache.commons.lang3.StringUtils.isNotBlank(iPlanNewbieAmtConfig.getValue())
        		&&Double.parseDouble(iPlanNewbieAmtConfig.getValue())>0) {
        		//获取已投金额（散标、理财包已投金额+定期理财计划已投金额）
            newbieAmt = ArithUtil.round(Double.parseDouble(iPlanNewbieAmtConfig.getValue()), 2);	
        }else{
        	iPlanNewbieAmtConfig=null;
        	if(newbietype!=null){
        		if(newbietype==0){//月月盈
                	//获取月月盈新手额度
                	iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN0_NEWBIE_AMT);
                }
                if(newbietype==2){//省心投
                	//获取省心投新手额度
                	iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN2_NEWBIE_AMT);
                }
        	}else{
            	//获取散标新手额度
            	iPlanNewbieAmtConfig = configService.getConfigById(Config.SUBJECT_NEWBIE_AMT);
            }
        	
        	if(iPlanNewbieAmtConfig != null && org.apache.commons.lang3.StringUtils.isNotBlank(iPlanNewbieAmtConfig.getValue())
            		&&Double.parseDouble(iPlanNewbieAmtConfig.getValue())>0){
        		newbieAmt = ArithUtil.round(Double.parseDouble(iPlanNewbieAmtConfig.getValue()), 2);	
        	}
        }
        return newbieAmt;
    }

    public void putInvestMsgToRedis(String key, String msg) {
        try {
            redisClinet.product(key, msg);
        } catch (Exception e) {
            logger.error("putInvestMsgToRedisError, key: {}, value: {}", key, msg);
        }
    }

    public void putInvestToRedis(String investRequestNo, String loanId) {
        Date nowTime = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("investId", investRequestNo);
        map.put("loanId", loanId);
        map.put("pushTime", nowTime);
        this.putInvestMsgToRedis(InvestService.PUSH_INVEST, FastJsonUtil.objToJson(map));
        this.putInvestMsgToRedis(InvestService.ACTIVITY_INVEST, investRequestNo);
    }

    /**
     * 散标及债权购买
     * @return
     */
    public BaseResponse sendSubjectAndCreditInvestToXM(String transfeeId , double actualPrincipal , String projectNo , String share , RedPacket redPacket , String extSn,BizType type , String code){
        String investRequestNo = IdUtil.getRequestNo();//请求流水号
        //用户预处理接口
        RequestUserAutoPreTransaction request = new RequestUserAutoPreTransaction();
        request.setRequestNo(investRequestNo);
        request.setPlatformUserNo(transfeeId);
        request.setTransCode(code);
        //预处理业务类型:
        request.setBizType(type);
        request.setAmount(actualPrincipal);
        request.setProjectNo(projectNo);
        if (BizType.TENDER.equals(type)){
            request.setTransCode(TransCode.SUBJECT_INVEST_FREEZE.getCode());
            request.setRemark("散标购买预处理");
        } else {
            request.setTransCode(TransCode.CREDIT_INVEST_FREEZE.getCode());
            request.setShare(share);
            request.setCreditsaleRequestNo(extSn);
            request.setRemark("债权购买预处理");
        }
        //判断是否有抵扣券大于0.01
        if (redPacket != null && redPacket.getId() > 0) {
            if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                int dedutionAmt=(int) (redPacket.getMoney() * 100);
                if(dedutionAmt>=1) {
                    request.setPreMarketingAmount(String.valueOf(redPacket.getMoney()));
                }
            }
        }
        return transactionService.userAutoPreTransaction(request);
    }
}
