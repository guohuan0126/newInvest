package com.jiuyi.ndr.service.invest;

import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.invest.InvestDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.redis.RedisClient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private RedisClient redisClinet;
    @Autowired
    private ConfigDao configDao;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private SubjectAccountService subjectAccountService;
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

    public void putInvestMsgToRedis(String key, String msg) {
        try {
            redisClinet.product(key, msg);
        } catch (Exception e) {
            logger.error("putInvestMsgToRedisError, key: {}, value: {}", key, msg);
        }
    }

    /**
     * 获取新手剩余可用额度（分为单位）
     *
     * @param userId    用户id
     */
    public Double getWeChatNewbieUsable(String userId) {
        if (StringUtils.isBlank(userId)) {
            return 0D;
        }
        //获取已投金额（散标、理财包已投金额+定期理财计划已投金额）
        Double investedAmt = iPlanService.findInvestedAmtByUserId(userId);
        //获取新手额度
        Config iPlanWeChatNewbieAmtConfig = configDao.getConfigById(Config.IPLAN_WEICHAT_AMT);
        double newbieAmt = 5000000;
        if (iPlanWeChatNewbieAmtConfig != null && org.apache.commons.lang3.StringUtils.isNotBlank(iPlanWeChatNewbieAmtConfig.getValue())) {
            newbieAmt = ArithUtil.round(Double.parseDouble(iPlanWeChatNewbieAmtConfig.getValue()), 2) * 100;
        }
        return newbieAmt - investedAmt;
    }

    /**
     * 获取新手剩余可用额度（分为单位）
     *
     * @param userId    用户id
     */
    public Double getNewbieUsable(String userId) {
        if (StringUtils.isBlank(userId)) {
            return 0D;
        }
        //获取已投金额（散标、理财包已投金额+定期理财计划已投金额）
        Double investedAmt = iPlanService.findInvestedAmtByUserId(userId);
        //获取新手额度
        Config iPlanNewbieAmtConfig = configDao.getConfigById(Config.IPLAN_NEWBIE_AMT);
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
    public Double getNewbieUsable(String userId,Integer Newbietype) {
        if (StringUtils.isBlank(userId)) {
            return 0D;
        }
        Config iPlanNewbieAmtConfig=null;
        double newbieAmt = 0;
        double investedAmt=0;
        //获取共享新手额度
        iPlanNewbieAmtConfig = configDao.getConfigById(Config.IPLAN_NEWBIE_AMT);
        
        if (iPlanNewbieAmtConfig != null && org.apache.commons.lang3.StringUtils.isNotBlank(iPlanNewbieAmtConfig.getValue())
        		&&Double.parseDouble(iPlanNewbieAmtConfig.getValue())>0) {
        	investedAmt = iPlanService.findInvestedAmtByUserId(userId);
        		//获取已投金额（散标、理财包已投金额+定期理财计划已投金额）
            newbieAmt = ArithUtil.round(Double.parseDouble(iPlanNewbieAmtConfig.getValue()), 2) * 100;	
        }else{
        	if(Newbietype!=null){
        		if(Newbietype==0){//月月盈
                	//获取月月盈新手额度
                	iPlanNewbieAmtConfig = configDao.getConfigById(Config.IPLAN0_NEWBIE_AMT);
                }
                if(Newbietype==2){//省心投
                	//获取省心投新手额度
                	iPlanNewbieAmtConfig = configDao.getConfigById(Config.IPLAN2_NEWBIE_AMT);
                }
        	}else{
            	//获取散标新手额度
            	iPlanNewbieAmtConfig = configDao.getConfigById(Config.SUBJECT_NEWBIE_AMT);
            }
            
            if (iPlanNewbieAmtConfig != null && org.apache.commons.lang3.StringUtils.isNotBlank(iPlanNewbieAmtConfig.getValue())) {
            	newbieAmt = ArithUtil.round(Double.parseDouble(iPlanNewbieAmtConfig.getValue()), 2) * 100;
            }else{
            	return 0d;
            }
            
            if(Newbietype!=null){
            	if(Newbietype==0){//月月盈
            		//查询月月盈投资总额
            		investedAmt=iPlanAccountService.getIPlanTypeTotalMoney(userId, Newbietype.toString());
            	}
            	if(Newbietype==2){//省心投
            		//查询省心投投资总额
            		investedAmt=iPlanAccountService.getIPlanTypeTotalMoney(userId, Newbietype.toString());
            	}
            }else{
            	investedAmt=subjectAccountService.getSubjectTotalMoney(userId);//查询散标投资总额
            }
        }
        
        return newbieAmt - investedAmt;
    }
}
