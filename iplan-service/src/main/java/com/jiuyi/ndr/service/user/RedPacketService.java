package com.jiuyi.ndr.service.user;

import com.duanrong.util.InterestUtil;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.user.RedPacketDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.autoinvest.AutoInvest;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.redpacket.InvestRedpacket;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhq on 2017/6/12.
 */
@Service
public class RedPacketService {

    private static final Logger logger = LoggerFactory.getLogger(RedPacketService.class);

    @Autowired
    private RedPacketDao redPacketDao;
    @Autowired
    private UserService userService;
    @Autowired
    private InvestService investService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private CreditDao creditDao;
    @Autowired
    private IPlanAccountService iPlanAccountService;

    private static DecimalFormat df2 = new DecimalFormat("##0.00");
    @ProductSlave
    public RedPacket getRedPacketById(int redPacketId) {
        if (redPacketId == 0) {
            throw new IllegalArgumentException("redPacketId不能为空");
        }
        return redPacketDao.getRedPacketById(redPacketId);
    }

    public RedPacket getRedPacketByIdLocked(int redPacketId) {
        if (redPacketId == 0) {
            throw new IllegalArgumentException("redPacketId不能为空");
        }
        return redPacketDao.getRedPacketByIdLocked(redPacketId);
    }

    public List<RedPacket> getRedPacketsByCondition(RedPacket redPacket) {
        if (redPacket == null) {
            throw new IllegalArgumentException("redPacket不能为空");
        }
        return redPacketDao.getRedPacketsByCondition(redPacket);
    }

    public void verifyRedPacket(String userId, int redPacketId, IPlan iPlan, String userSource, double money) {
        logger.warn("校验红包是否可用，userId:" + userId + ",redPacketId:" + redPacketId + ",iPlan:" + iPlan.toString() + ",userSource:"
                + ",money:" + money);
        if (StringUtils.isBlank(userId) || redPacketId == 0 || iPlan == null || StringUtils.isBlank(userSource)) {
            logger.warn("用户: " + userId + "使用红包id为0");
            throw new ProcessException(Error.NDR_0101);
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            logger.warn("用户: " + userId +"不存在");
            throw new ProcessException(Error.NDR_0419);
        }
        String mobileNumber = user.getMobileNumber();
        if (mobileNumber == null){
            logger.warn("用户：" + userId + "手机号不存在");
            throw new ProcessException(Error.NDR_0431);
        }
        RedPacket redPacket = new RedPacket(redPacketId, mobileNumber, null, null, null,
                null, 0);
        List<RedPacket> redPackets = redPacketDao.getRedPacketsByCondition(redPacket);
        if (redPackets != null && redPackets.size() == 1) {
            redPacket = redPackets.get(0);
        } else {
            logger.warn("用户：" + userId + "使用的红包：" + redPacketId + "不存在");
            throw new ProcessException(Error.NDR_0432);
        }
        if (redPacket != null) {
            //排除天天赚红包
            if (StringUtils.equals(RedPacket.TYPE_RATELPLAN, redPacket.getType())) {
                throw new ProcessException(Error.NDR_04391);
            }
            //排除天天赚转投月月盈专属红包
            if(redPacket.getSpecificType() != null && redPacket.getSpecificType() == 1){
                throw new ProcessException(Error.NDR_04393);
            }
            /*
            *   useLoanType：1,新手标不可使用，2，APP专享，3，APP专享且新手标不可用
            */
            if (iPlan.getNewbieOnly().equals(IPlan.NEWBIE_ONLY_Y) && redPacket.getUseLoanType() == 1) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此加息券新手标不可使用");
                throw new ProcessException(Error.NDR_0433);
            }
            if (iPlan.getNewbieOnly().equals(IPlan.NEWBIE_ONLY_Y) && redPacket.getUseLoanType() == 3) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此红包券APP投资专享且新手标不可用");
                throw new ProcessException(Error.NDR_0434);
            }
            if (redPacket.getUseLoanType() == 2 && !userSource.contains("ios") && !userSource.contains("android")) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此加息券APP专享");
                throw new ProcessException(Error.NDR_0435);
            }
            if (redPacket.getUseLoanType() == 3 && !userSource.contains("ios") && !userSource.contains("android")) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此红包券APP投资专享且新手标不可用");
                throw new ProcessException(Error.NDR_0434);
            }
            // 投资周期限制
            if (redPacket.getInvestCycle() > 0 && redPacket.getInvestCycle() > (iPlanAccountService.isNewIplan(iPlan)?iPlan.getExitLockDays()/31:iPlan.getTerm())) {
                logger.warn("用户ID：" + userId + "，项目ID：" + iPlan.getId()
                        + "redPacketId:" + redPacketId + "此红包券仅限" + redPacket.getInvestCycle() + "个月及以上标的使用");
                throw new ProcessException(Error.NDR_0436);
            }
            if (!"unused".equals(redPacket.getSendStatus())) {
                logger.warn("用户ID：" + userId + "，项目ID：" + iPlan.getId()
                        + "redPacketId:" + redPacketId + "状态不是未使用" + redPacket.toString());
                String status = "被使用";
                if ("expired".equals(redPacket.getSendStatus())) {
                    status = "过期";
                    throw new ProcessException(Error.NDR_0438);
                }
                throw new ProcessException(Error.NDR_0437);
            }
            if (!StringUtils.equals(RedPacket.USAGEDETAIL_INVEST, redPacket.getUsageDetail())) {
                logger.warn("投资不可使用" + redPacketId);
                throw new ProcessException(Error.NDR_0439);
            }
            if (money < redPacket.getInvestMoney()) {
                logger.warn("投资金额小于限制金额;投资金额：" + money + ";限制金额：" + redPacket.getInvestMoney());
                throw new ProcessException(Error.NDR_0440);
            }
            if (iPlan.getRateType().equals(IPlan.RATE_TYPE_FIX) && redPacket.getInvestRate() > 0
                    && iPlan.getFixRate() != null && iPlan.getFixRate().doubleValue()> redPacket.getInvestRate()) {
                logger.warn("投资利率大于限制利率;投资利率：" + iPlan.getFixRate() + ";限制利率："
                        + redPacket.getInvestRate());
                throw new ProcessException(Error.NDR_0441);
            }
            if (redPacket.getRuleId() == 31) {
                logger.warn("userId:" + userId + ",红包ID：" + redPacket.getId());
                double newbieUsable = investService.getNewbieUsable(userId,iPlan.getIplanType());
                if (newbieUsable <= 0) {
                    logger.warn("老用户不能使用此类红包券userId:" + userId + "red" + redPacket.toString());
                    throw new ProcessException(Error.NDR_0442);
                }
            }
            /*if ("rateByDay".equals(redPacket.getType())) {
                int rateDay = redPacket.getRateDay();
                int periods = iPlan.getExitLockDays();
                if (rateDay > periods) {
                    logger.warn("userId:" + userId + "使用按天加息红包券，红包加息时间：" + rateDay + "大于项目锁定时间：" + periods);
                    throw new ProcessException(Error.NDR_0443);
                }
            }*/
        } else {
            logger.warn("用户：" + userId + "使用的红包：" + redPacketId + "不存在");
            throw new ProcessException(Error.NDR_0432);
        }
    }

    /**
     * 散标红包检验条件
     * @param userId
     * @param redPacketId
     * @param subject
     * @param userSource
     * @param money
     * @param subjectType 项目类型，subject散标，credit 债权
     */
    public void verifyRedPacketsSuject(String userId, int redPacketId, Subject subject, String userSource, double money,String subjectType) {
        logger.warn("校验红包是否可用，userId:" + userId + ",redPacketId:" + redPacketId + ",subject:" + subject.toString() + ",userSource:"
                + ",money:" + money);
        if (StringUtils.isBlank(userId) || redPacketId == 0 || subject == null || StringUtils.isBlank(userSource)) {
            logger.warn("用户: " + userId + "使用红包id为0");
            throw new ProcessException(Error.NDR_0101);
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            logger.warn("用户: " + userId +"不存在");
            throw new ProcessException(Error.NDR_0419);
        }
        String mobileNumber = user.getMobileNumber();
        if (mobileNumber == null){
            logger.warn("用户：" + userId + "手机号不存在");
            throw new ProcessException(Error.NDR_0431);
        }
        RedPacket redPacket = new RedPacket(redPacketId, mobileNumber, null, null, null,
                null, 0);
        List<RedPacket> redPackets = redPacketDao.getRedPacketsByCondition(redPacket);
        if (redPackets != null && redPackets.size() == 1) {
            redPacket = redPackets.get(0);
        } else {
            logger.warn("用户：" + userId + "使用的红包：" + redPacketId + "不存在");
            throw new ProcessException(Error.NDR_0432);
        }
        if (redPacket != null) {
            //排除天天赚红包
            if (StringUtils.equals(RedPacket.TYPE_RATELPLAN, redPacket.getType())) {
                throw new ProcessException(Error.NDR_04392);
            }
            //排除天天赚转投月月盈专属红包
            if(redPacket.getSpecificType() != null && redPacket.getSpecificType() == 1){
                throw new ProcessException(Error.NDR_04394);
            }
            //如果是散标购买就判断 是否债权购买专享
            if(StringUtils.isNotBlank(subjectType) && "subject".equals(subjectType)){
                //排除债权购买专属红包
                if(redPacket.getSpecificType() != null && redPacket.getSpecificType() == 2){
                    throw new ProcessException(Error.NDR_04395);
                }
            }

            /*
            *   useLoanType：1,新手标不可使用，2，APP专享，3，APP专享且新手标不可用
            */
            if (Subject.NEWBIE_ONLY_Y.equals(subject.getNewbieOnly()) && redPacket.getUseLoanType() == 1) {
                logger.warn("用户ID：" + userId + ",项目ID：" + subject.getId()
                        + "redpacketId:" + redPacketId + "此加息券新手标不可使用");
                throw new ProcessException(Error.NDR_0433);
            }
            if (Subject.NEWBIE_ONLY_Y.equals(subject.getNewbieOnly() )&& redPacket.getUseLoanType() == 3) {
                logger.warn("用户ID：" + userId + ",项目ID：" + subject.getId()
                        + "redpacketId:" + redPacketId + "此红包券APP投资专享且新手标不可用");
                throw new ProcessException(Error.NDR_0434);
            }
            if (redPacket.getUseLoanType() == 2 && !userSource.contains("ios") && !userSource.contains("android")) {
                logger.warn("用户ID：" + userId + ",项目ID：" + subject.getId()
                        + "redpacketId:" + redPacketId + "此加息券APP专享");
                throw new ProcessException(Error.NDR_0435);
            }
            if (redPacket.getUseLoanType() == 3 && !userSource.contains("ios") && !userSource.contains("android")) {
                logger.warn("用户ID：" + userId + ",项目ID：" + subject.getId()
                        + "redpacketId:" + redPacketId + "此红包券APP投资专享且新手标不可用");
                throw new ProcessException(Error.NDR_0434);
            }
            // 投资周期限制
            if("subject".equals(subjectType)){
                if (redPacket.getInvestCycle() > 0 && redPacket.getInvestCycle() > subject.getTerm()) {
                    logger.warn("用户ID：" + userId + "，项目ID：" + subject.getId()
                            + "redPacketId:" + redPacketId + "此红包券仅限" + redPacket.getInvestCycle() + "个月及以上标的使用");
                    throw new ProcessException(Error.NDR_0760);
                }
            }else{
                CreditOpening creditOpening = creditOpeningDao.findBySubjectIdAndStatus(subject.getSubjectId());
                if(creditOpening != null){
                    Credit credit = creditDao.findById(creditOpening.getCreditId());
                    if (redPacket.getInvestCycle() > 0 && redPacket.getInvestCycle() > credit.getResidualTerm()) {
                        logger.warn("用户ID：" + userId + "，项目ID：" + subject.getId()
                                + "redPacketId:" + redPacketId + "此红包券仅限" + redPacket.getInvestCycle() + "个月及以上债转项目使用");
                        throw new ProcessException(Error.NDR_0908);
                    }
                }else{
                    throw new ProcessException(Error.NDR_0907);
                }
            }
            if (!"unused".equals(redPacket.getSendStatus())) {
                logger.warn("用户ID：" + userId + "，项目ID：" + subject.getId()
                        + "redPacketId:" + redPacketId + "状态不是未使用" + redPacket.toString());
                String status = "被使用";
                if ("expired".equals(redPacket.getSendStatus())) {
                    status = "过期";
                    throw new ProcessException(Error.NDR_0438);
                }
                throw new ProcessException(Error.NDR_0437);
            }
            if (!StringUtils.equals(RedPacket.USAGEDETAIL_INVEST, redPacket.getUsageDetail())) {
                logger.warn("投资不可使用" + redPacketId);
                throw new ProcessException(Error.NDR_0439);
            }
            if (money < redPacket.getInvestMoney()) {
                logger.warn("投资金额小于限制金额;投资金额：" + money + ";限制金额：" + redPacket.getInvestMoney());
                throw new ProcessException(Error.NDR_0440);
            }
            if (redPacket.getInvestRate() > 0
                    && subject.getInvestRate() != null && subject.getInvestRate().doubleValue()> redPacket.getInvestRate()) {
                logger.warn("投资利率大于限制利率;投资利率：" + subject.getInvestRate() + ";限制利率："
                        + redPacket.getInvestRate());
                throw new ProcessException(Error.NDR_0441);
            }
            if (redPacket.getRuleId() == 31) {
                logger.warn("userId:" + userId + ",红包ID：" + redPacket.getId());
                double newbieUsable = investService.getNewbieUsable(userId,null);
                if (newbieUsable <= 0) {
                    logger.warn("老用户不能使用此类红包券userId:" + userId + "red" + redPacket.toString());
                    throw new ProcessException(Error.NDR_0442);
                }
            }
        } else {
            logger.warn("用户：" + userId + "使用的红包：" + redPacketId + "不存在");
            throw new ProcessException(Error.NDR_0432);
        }
    }
    public void verifyRedPacket(String userId, int redPacketId, IPlan iPlan, String userSource) {
        logger.warn("校验红包是否可用，userId:" + userId + ",redPacketId:" + redPacketId + ",iPlan:" + iPlan.toString() + ",userSource:"
                + userSource);
        if (StringUtils.isBlank(userId) || redPacketId == 0 || iPlan == null || StringUtils.isBlank(userSource)) {
            logger.warn("用户: " + userId + "使用红包id为0");
            throw new ProcessException(Error.NDR_0101);
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            logger.warn("用户: " + userId +"不存在");
            throw new ProcessException(Error.NDR_0419);
        }
        String mobileNumber = user.getMobileNumber();
        if (mobileNumber == null){
            logger.warn("用户：" + userId + "手机号不存在");
            throw new ProcessException(Error.NDR_0431);
        }
        RedPacket redPacket = new RedPacket(redPacketId, mobileNumber, null, null, null,
                null, 0);
        List<RedPacket> redPackets = redPacketDao.getRedPacketsByCondition(redPacket);
        if (redPackets != null && redPackets.size() == 1) {
            redPacket = redPackets.get(0);
        } else {
            logger.warn("用户：" + userId + "使用的红包：" + redPacketId + "不存在");
            throw new ProcessException(Error.NDR_0432);
        }
        if (redPacket != null) {
            //排除天天赚红包
            if (StringUtils.equals(RedPacket.TYPE_RATELPLAN, redPacket.getType())) {
                throw new ProcessException(Error.NDR_04391);
            }
            //排除天天赚转投月月盈专属红包
            if(redPacket.getSpecificType() != null && redPacket.getSpecificType() == 1){
                throw new ProcessException(Error.NDR_04394);
            }
            //排除债权购买专属红包
            if(redPacket.getSpecificType() != null && redPacket.getSpecificType() == 2){
                throw new ProcessException(Error.NDR_04395);
            }

            /*
            *   useLoanType：1,新手标不可使用，2，APP专享，3，APP专享且新手标不可用
            */
            if (iPlan.getNewbieOnly().equals(IPlan.NEWBIE_ONLY_Y) && redPacket.getUseLoanType() == 1) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此加息券新手标不可使用");
                throw new ProcessException(Error.NDR_0433);
            }
            if (iPlan.getNewbieOnly().equals(IPlan.NEWBIE_ONLY_Y) && redPacket.getUseLoanType() == 3) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此红包券APP投资专享且新手标不可用");
                throw new ProcessException(Error.NDR_0434);
            }
            if (redPacket.getUseLoanType() == 2 && !userSource.contains("ios") && !userSource.contains("android")) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此加息券APP专享");
                throw new ProcessException(Error.NDR_0435);
            }
            if (redPacket.getUseLoanType() == 3 && !userSource.contains("ios") && !userSource.contains("android")) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此红包券APP投资专享且新手标不可用");
                throw new ProcessException(Error.NDR_0434);
            }
            // 投资周期限制
            if (redPacket.getInvestCycle() > 0 && redPacket.getInvestCycle() > (iPlanAccountService.isNewIplan(iPlan)?iPlan.getExitLockDays()/31:iPlan.getTerm())) {
                logger.warn("用户ID：" + userId + "，项目ID：" + iPlan.getId()
                        + "redPacketId:" + redPacketId + "此红包券仅限" + redPacket.getInvestCycle() + "个月及以上标的使用");
                throw new ProcessException(Error.NDR_0436);
            }
            if (!"unused".equals(redPacket.getSendStatus())) {
                logger.warn("用户ID：" + userId + "，项目ID：" + iPlan.getId()
                        + "redPacketId:" + redPacketId + "状态不是未使用" + redPacket.toString());
                String status = "被使用";
                if ("expired".equals(redPacket.getSendStatus())) {
                    status = "过期";
                    throw new ProcessException(Error.NDR_0438);
                }
                throw new ProcessException(Error.NDR_0437);
            }
            if (!StringUtils.equals(RedPacket.USAGEDETAIL_INVEST, redPacket.getUsageDetail())) {
                logger.warn("投资不可使用 : {}",redPacketId);
                throw new ProcessException(Error.NDR_0439);
            }
            //定期投资详情页红包无金额
            /*if (money < redPacket.getInvestMoney()) {
                logger.warn("投资金额小于限制金额;投资金额：" + money + ";限制金额：" + redPacket.getInvestMoney());
                throw new ProcessException(Error.NDR_0440);
            }*/
            if (iPlan.getRateType().equals(IPlan.RATE_TYPE_FIX) && redPacket.getInvestRate() > 0
                    && iPlan.getFixRate() != null && iPlan.getFixRate().doubleValue()> redPacket.getInvestRate()) {
                logger.warn("投资利率大于限制利率;投资利率：" + iPlan.getFixRate() + ";限制利率："
                        + redPacket.getInvestRate());
                throw new ProcessException(Error.NDR_0441);
            }
            if (redPacket.getRuleId() == 31) {
                logger.warn("userId:" + userId + ",红包ID：" + redPacket.getId());
                double newbieUsable = investService.getNewbieUsable(userId,iPlan.getIplanType());
                if (newbieUsable <= 0) {
                    logger.warn("老用户不能使用此类红包券userId:" + userId + "red" + redPacket.toString());
                    throw new ProcessException(Error.NDR_0442);
                }
            }
            /*if ("rateByDay".equals(redPacket.getType())) {
                int rateDay = redPacket.getRateDay();
                int periods = iPlan.getExitLockDays();
                if (rateDay > periods) {
                    logger.warn("userId:" + userId + "使用按天加息红包券，红包加息时间：" + rateDay + "大于项目时间：" + periods);
                    throw new ProcessException(Error.NDR_0443);
                }
            }*/
        } else {
            logger.warn("用户：" + userId + "使用的红包：" + redPacketId + "不存在");
            throw new ProcessException(Error.NDR_0432);
        }
    }
    public void update(RedPacket redPacket) {
        if (redPacket == null) {
            throw new IllegalArgumentException("redPacket can not be null");
        }
        redPacketDao.update(redPacket);
    }

    public RedPacket getBestRedPackets(String userId, IPlan iPlan, double amount, String redPacketRule) {
        if ( StringUtils.isBlank(redPacketRule)
                || StringUtils.isBlank(userId)
                || iPlan == null
                || amount == 0) {
            return null;
        }
        List<RedPacket> list = getUsablePackets(userId, amount, iPlan);
        if (list == null || list.size() <= 0) {
            return null;
        }
        List<RedPacket> packetsByTime = new ArrayList<>();
        if (StringUtils.equals(redPacketRule, AutoInvest.REDPACKET_RULE_TIME_FIRST)) {
            String time = list.get(0).getDeadLine();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getDeadLine().equals(time)) {
                    packetsByTime.add(list.get(i));
                }
            }
            RedPacket maxRedPacket = packetsByTime.get(0);
            if (packetsByTime.size() > 1) {
                double money = this.getPacketMoney(iPlan, amount, maxRedPacket);
                for (RedPacket redPacket : packetsByTime) {
                    double tempMoney = this.getPacketMoney(iPlan, amount, redPacket);
                    if (money < tempMoney) {
                        money = tempMoney;
                        maxRedPacket = redPacket;
                    }
                }
            }
            return maxRedPacket;
        }
        if (StringUtils.equals(redPacketRule, AutoInvest.REDPACKET_RULE_MONEY_FIRST)) {
            List<RedPacket> redPacketsByMoney = new ArrayList<>();
            List<RedPacket> redPacketsByRate = new ArrayList<>();
            List<RedPacket> redPacketsByRateDay = new ArrayList<>();
            for (RedPacket redPacket : list) {
                if (StringUtils.equals(redPacket.getType(), RedPacket.TYPE_MONEY) || StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                    redPacketsByMoney.add(redPacket);
                } else if (StringUtils.equals(redPacket.getType(), RedPacket.TYPE_RATE)) {
                    redPacketsByRate.add(redPacket);
                } else if (StringUtils.equals(redPacket.getType(), RedPacket.TYPE_RATE_BY_DAY)) {
                    redPacketsByRateDay.add(redPacket);
                }
            }
            double money = 0;
            List<RedPacket> maxRedPacketsByMoney = this.getMaxRedPackets(redPacketsByMoney, RedPacket.TYPE_MONEY);
            if (maxRedPacketsByMoney.size() > 0) {
                money = maxRedPacketsByMoney.get(0).getMoney();
            }
            double maxMoneyByRate = 0;
            List<RedPacket> maxRedPacketsByRate = this.getMaxRedPackets(redPacketsByRate, RedPacket.TYPE_RATE);
            if (maxRedPacketsByRate.size() >0) {
                maxMoneyByRate = this.getPacketMoney(iPlan, amount, maxRedPacketsByRate.get(0));
            }
            double maxMoneyByRateDay = 0;
            List<RedPacket> maxRedPacketsByRateDay = new ArrayList<>();
            if (redPacketsByRateDay.size() >0) {
                maxMoneyByRateDay = this.getPacketMoney(iPlan, amount, redPacketsByRateDay.get(0));
                for (RedPacket redPacket : redPacketsByRateDay) {
                    double tempMoney = this.getPacketMoney(iPlan, amount, redPacket);
                    if (tempMoney > maxMoneyByRateDay) {
                        maxMoneyByRateDay = tempMoney;
                    }
                }
                for (RedPacket redPacket : redPacketsByRateDay) {
                    double tempMoney = this.getPacketMoney(iPlan, amount, redPacket);
                    if (tempMoney == maxMoneyByRateDay) {
                        maxRedPacketsByRateDay.add(redPacket);
                    }
                }
            }
            RedPacket maxPacket = null;
            if (money >= maxMoneyByRate && money >= maxMoneyByRateDay) {
                if (maxRedPacketsByMoney != null && maxRedPacketsByMoney.size() > 0) {
                    maxPacket = maxRedPacketsByMoney.get(0);
                }
            } else if (money <= maxMoneyByRate && maxMoneyByRateDay <= maxMoneyByRate) {
                if (maxRedPacketsByRate != null && maxRedPacketsByRate.size() > 0) {
                    maxPacket = maxRedPacketsByRate.get(0);
                }
            } else if (money <= maxMoneyByRateDay && maxMoneyByRate <= maxMoneyByRateDay) {
                if (maxRedPacketsByRateDay != null && maxRedPacketsByRateDay.size() > 0) {
                    maxPacket = maxRedPacketsByRateDay.get(0);
                }
            }
            return maxPacket;
        }
        return null;
    }

    /**
     * 获取用户可用的红包
     *
     * @param userId
     * @param investMoney
     * @param iPlan
     * @return
     */
    private List<RedPacket> getUsablePackets(String userId, double investMoney, IPlan iPlan) {
        //查询用户未使用的红包
        RedPacket redPacket = new RedPacket();
        redPacket.setUserId(userId);
        redPacket.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
        redPacket.setUsageDetail(RedPacket.USAGEDETAIL_INVEST);
        redPacket.setInvestMoney(investMoney);
        List<RedPacket> list = this.getRedPacketDetails(redPacket);
        if (list == null || list.size() <= 0) {
            return null;
        }
        //使用红包校验规则选择符条件的红包
        Iterator<RedPacket> iterator = list.iterator();
        while(iterator.hasNext()){
            RedPacket redPacket2 = iterator.next();
            try {
                this.verifyRedPacket(userId, redPacket2.getId(), iPlan, "admin", investMoney);
            } catch (Exception e) {
                iterator.remove();
            }
        }
        return list;
    }
    /**
     * 获取用户可用的红包
     *
     * @param userId
     * @param investMoney
     * @param subject
     * @return
     */
    public List<RedPacket> getUsablePacketSubject(String userId, double investMoney, Subject subject) {
        //查询用户未使用的红包
        RedPacket redPacket = new RedPacket();
        redPacket.setUserId(userId);
        redPacket.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
        redPacket.setUsageDetail(RedPacket.USAGEDETAIL_INVEST);
        redPacket.setInvestMoney(investMoney);
        List<RedPacket> list = this.getRedPacketDetails(redPacket);
        if (list == null || list.size() <= 0) {
            return null;
        }
        //使用红包校验规则选择符条件的红包
        Iterator<RedPacket> iterator = list.iterator();
        while(iterator.hasNext()){
            RedPacket redPacket2 = iterator.next();
            try {
                this.verifyRedPacketsSuject(userId, redPacket2.getId(), subject,"admin", investMoney,"subject");
            } catch (Exception e) {
                iterator.remove();
            }
        }
        return list;
    }
    /**
     * 获取用户可用的红包
     *
     * @param userId    用户id
     * @param iPlan     计划id
     */
    @ProductSlave
    public List<RedPacket> getUsablePackets(String userId, IPlan iPlan, String userSource) {
        try {
            //查询用户未使用的红包
            User user = userService.findByUsername(userId);
            RedPacket redPacket = new RedPacket();
            redPacket.setMobileNumber(user.getMobileNumber());
            redPacket.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
//            redPacket.setUsageDetail(RedPacket.USAGEDETAIL_INVEST);
            List<RedPacket> list = this.getRedPacketCreditDetails(redPacket);
            List<RedPacket> totalList = new ArrayList<>();
            for (RedPacket newRedPacket: list) {
                totalList.add(newRedPacket);
            }
            if (list == null || list.size() <= 0) {
                return null;
            }
            //使用红包校验规则选择符合条件的红包
            Iterator<RedPacket> iterator = list.iterator();
            while(iterator.hasNext()){
                RedPacket redPacket2 = iterator.next();
                try {
                    this.verifyRedPacket(userId, redPacket2.getId(), iPlan, userSource);
                } catch (Exception e) {
                    iterator.remove();
                }
            }
            for (RedPacket useRedPacket:list) {
                //若该项目是活动标
                if (iPlan.getActivityId()!=null) {
                    ActivityMarkConfigure amc = activityMarkConfigureService.findById(iPlan.getActivityId());
                    //活动标配置不能使用红包
                    if(amc.getRedpacketWhether()==null ||amc.getRedpacketWhether()==0){
                        useRedPacket.setUseStatus("unUseAble");
                    } else {
                        useRedPacket.setUseStatus("useAble");
                    }
                } else {
                    useRedPacket.setUseStatus("useAble");
                }
            }
            List<RedPacket> newList = new ArrayList<>();
            for (RedPacket totalRedPacket:totalList){
                if(!list.contains(totalRedPacket)){
                    totalRedPacket.setUseStatus("unUseAble");
                    newList.add(totalRedPacket);
                }
            }
            List<RedPacket> lastList = new ArrayList<>();
            lastList.addAll(list);
            lastList.addAll(newList);
            return lastList;
        }catch (Exception e){
            return null;
        }

    }

    public List<RedPacket> getRedPacketDetails(RedPacket redPacket) {
        if (redPacket == null) {
            throw new IllegalArgumentException("redPacket can not be null");
        }
        return redPacketDao.getRedPacketDetails(redPacket);
    }

    public List<RedPacket> getRedPacketCreditDetails(RedPacket redPacket) {
        if (redPacket == null) {
            throw new IllegalArgumentException("redPacket can not be null");
        }
        return redPacketDao.getRedPacketDetailsNew(redPacket);
    }

    private double getPacketMoney(IPlan iPlan, double investMoney, RedPacket packet) {
        double d = 0;
        double money = investMoney;
        if(packet != null){
            String type = packet.getType();
            double rate = packet.getRate();
            if (23 == packet.getRuleId()) {// 如果红包规则是23则金额最高计算上限是5万
                money = money > 50000 ? 50000 : money;
            }
            if ("rate".equals(type)) {
                d = InterestUtil.getInterestByPeriodMoth(money, rate, iPlan.getTerm(), getIplanRepayType(iPlan.getRepayType()));
            } else if ("money".equals(type) || "deduct".equals(type)) {
                // 固定金额奖励的情况下
                d = packet.getMoney();
            } else if ("rateByDay".equals(type)) {
                int rateDay = packet.getRateDay();
                d = InterestUtil.getInterestByPeriodDay(money, rate, rateDay);
            }
        }
        return d;
    }

    private String getIplanRepayType(String iplanRepayType) {
        switch (iplanRepayType) {
            case IPlan.REPAY_TYPE_OTRP:
                return "一次性到期还本付息";
            case IPlan.REPAY_TYPE_IFPA:
                return "按月付息到期还本";
            case IPlan.REPAY_TYPE_MCEI:
                return "等额本息";
            default:
                return "按月付息到期还本";
        }
    }

    /**
     * 获取金额最大的现金券或利率最大的加息券
     * @param list
     * @param name
     * @return
     */
    private List<RedPacket> getMaxRedPackets(List<RedPacket> list, String name){
        double value = 0;
        List<RedPacket> maxRedPacketsByName = new ArrayList<>();
        if (list != null && list.size() > 0) {
            if (StringUtils.equals(name, "money")) {
                value = list.get(0).getMoney();
                for (RedPacket redPacket : list) {
                    double tempMoney = redPacket.getMoney();
                    if (tempMoney > value) {
                        value = tempMoney;
                    }
                }
                for (RedPacket redPacket : list) {
                    double tempMoney = redPacket.getMoney();
                    if (tempMoney == value) {
                        maxRedPacketsByName.add(redPacket);
                    }
                }
            }
            if (StringUtils.equals(name, "rate")) {
                value = list.get(0).getRate();
                for (RedPacket redPacket : list) {
                    double tempRate = redPacket.getRate();
                    if (tempRate > value) {
                        value = tempRate;
                    }
                }
                for (RedPacket redPacket : list) {
                    double tempRate = redPacket.getRate();
                    if (tempRate == value) {
                        maxRedPacketsByName.add(redPacket);
                    }
                }
            }
        }
        return maxRedPacketsByName;
    }

    //获取红包介绍（比如：投资满5000可用）
    @ProductSlave
    public String getRedPacketInvestMoneyStr(RedPacket red){
        //项目类型限制
        String str = "";
        if(red.getSpecificType() != null && red.getSpecificType() == 2){
            str += "债转项目专用券，";
        }else{
            str += "债转项目不可用，";
            str += createDetail(red.getUseLoanType());
        }
        String investMoneyStr = "投资即可使用";
        if(23==red.getRuleId()){
            investMoneyStr = "投资额超过5万的部分不享受加息";
        }
        if(31==red.getRuleId()){
            investMoneyStr = "满"+df2.format(red.getInvestMoney()/10000)+"万可用,限首次投定期且至少"+red.getInvestCycle()+"月（新手标不可用）";
        }
        //投资金额限制
        if(red.getInvestMoney()>0&&red.getInvestMoney()<10000){
            str += "投满"+df2.format(red.getInvestMoney())+"元可用，";

        }else if(red.getInvestMoney()>=10000){
            str += "投满"+df2.format(red.getInvestMoney()/10000)+"万元可用";
        }
        //投资周期限制
        if(red.getInvestCycle() > 1){
            str += "限"+red.getInvestCycle()+"月及以上项目可用";
        }else if(red.getInvestCycle() == 1){
            str += "限"+red.getInvestCycle()+"月及以上项目(或天标)可用";
        }


        if(!"".equals(str.trim())){
            if(str.endsWith("，")){
                str = str.substring(0,str.length() -1);
            }
            investMoneyStr = str;
        }
        return investMoneyStr;
    }

    @ProductSlave
    public String getRedPacketInvestMoneyStrForIplan(RedPacket red,IPlan iPlan){
        if (iPlan.getActivityId()!=null) {
            ActivityMarkConfigure amc = activityMarkConfigureService.findById(iPlan.getActivityId());
            //活动标配置不能使用红包
            if(amc.getRedpacketWhether()==null ||amc.getRedpacketWhether()==0){
                return "限量爆款项目不可使用加息券";
            }
        }
        //项目类型限制
        String str = "";
        if(red.getSpecificType() != null && red.getSpecificType() == 2){
            str += "债转项目专用券，";
        }else{
            str += "债转项目不可用，";
            str += createDetail(red.getUseLoanType());
        }
        String investMoneyStr = "投资即可使用";
        if(23==red.getRuleId()){
            investMoneyStr = "投资额超过5万的部分不享受加息";
        }
        if(31==red.getRuleId()){
            investMoneyStr = "满"+df2.format(red.getInvestMoney()/10000)+"万可用,限首次投定期且至少"+red.getInvestCycle()+"月（新手标不可用）";
        }
        //投资金额限制
        if(red.getInvestMoney()>0&&red.getInvestMoney()<10000){
            str += "投满"+df2.format(red.getInvestMoney())+"元可用，";

        }else if(red.getInvestMoney()>=10000){
            str += "投满"+df2.format(red.getInvestMoney()/10000)+"万元可用，";
        }
        //投资周期限制
        if(red.getInvestCycle() > 1){
            str += "限"+red.getInvestCycle()+"月及以上项目可用";
        }else if(red.getInvestCycle() == 1){
            str += "限"+red.getInvestCycle()+"月及以上项目(或天标)可用";
        }

        if(!"".equals(str.trim())){
            if(str.endsWith("，")){
                str = str.substring(0,str.length() -1);
            }
            investMoneyStr = str;
        }
        return investMoneyStr;
    }

    private String createDetail(int type){
        if(type == 1){
            return  "新手标不可用，";
        }else if(type == 2){
            return "App投资专享，";
        }else if(type == 3){
            return "App投资专享且新手标不可用，";
        }else{
            return "";
        }
    }
    @ProductSlave
    public InvestRedpacket getReceivedRedPacketAmt(String userId, String loanId, String investRedpacketType) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(loanId)) {
            throw new ProcessException(Error.NDR_0101);
        }
        return redPacketDao.getReceivedRedPacketAmt(userId, loanId, investRedpacketType);
    }

    /**
     * 散标获取红包卷加息金额(调整)
     * @param redPacket
     * @param subject
     * @param money
     * @return
     */
    public double getRedpacketMoneyCommon(RedPacket redPacket,Subject subject, double money){
        String type = redPacket.getType();
        String status = redPacket.getSendStatus();
        double rate = redPacket.getRate();
        double d = 0.0;
        if (23 == redPacket.getRuleId()) {// 如果红包规则是23则金额最高计算上限是5万
            money = money > 50000 ? 50000 : money;
        }
        //普通加息券就是加到锁定期为止

            String repayType=subjectService.getSubjectRepayType(subject.getSubjectId());
            //期数
            int term = subject != null ? subject.getTerm() : 0;
            //天数
            int period = subject != null ? subject.getPeriod() : 0;
            if (type.equals(RedPacket.TYPE_RATE)) {
                if(period<30){
                    d= InterestUtil.getInterestByPeriodDay(money,rate,period);
                }
                else{
                    d= InterestUtil.getInterestByPeriodMoth(money,rate,term,repayType);
                }
            } else if (type.equals(RedPacket.TYPE_RATE_BY_DAY)) {
                //按天加息券，如果天数小于期限数，按加息劵天数加息
                int rateDay = period <= redPacket.getRateDay() ? period : redPacket.getRateDay();
                d = InterestUtil.getInterestByPeriodDay(money, rate, rateDay);
            } else if (RedPacket.TYPE_MONEY.equals(type)) {
                // 固定金额奖励的情况下
               d = redPacket.getMoney();
           }

        return d;
    }

    /**
     * 获取用户可用的红包
     *
     * @param userId    用户id
     */
    public List<RedPacket> getSubjectUsablePackets(String userId) {
        //查询用户未使用的红包
        User user = userService.findByUsername(userId);
        RedPacket redPacket = new RedPacket();
        redPacket.setMobileNumber(user.getMobileNumber());
        redPacket.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
        redPacket.setUsageDetail(RedPacket.USAGEDETAIL_INVEST);
        List<RedPacket> list = this.getRedPacketDetails(redPacket);
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list;
    }

    /**
     * 获取标的最优红包券
     * @param userId
     * @param subject
     * @param amount
     * @param redPacketRule
     * @return
     */
   public RedPacket getBestRedPacketSubject(String userId, Subject subject, double amount, String redPacketRule) {
        if ( StringUtils.isBlank(redPacketRule)
                || StringUtils.isBlank(userId)
                || subject == null
                || amount == 0) {
            return null;
        }
        List<RedPacket> list = getUsablePacketSubject(userId, amount, subject);
        if (list == null || list.size() <= 0) {
            return null;
        }
        List<RedPacket> packetsByTime = new ArrayList<>();
        if (StringUtils.equals(redPacketRule, AutoInvest.REDPACKET_RULE_TIME_FIRST)) {
            String time = list.get(0).getDeadLine();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getDeadLine().equals(time)) {
                    packetsByTime.add(list.get(i));
                }
            }
            RedPacket maxRedPacket = packetsByTime.get(0);
            if (packetsByTime.size() > 1) {
                double money = this.getRedpacketMoneyCommon(maxRedPacket,subject,amount);
                for (RedPacket redPacket : packetsByTime) {
                    double tempMoney = getRedpacketMoneyCommon(redPacket,subject,amount);
                    if (money < tempMoney) {
                        money = tempMoney;
                        maxRedPacket = redPacket;
                    }
                }
            }
            return maxRedPacket;
        }
        if (StringUtils.equals(redPacketRule, AutoInvest.REDPACKET_RULE_MONEY_FIRST)) {
            List<RedPacket> redPacketsByMoney = new ArrayList<>();
            List<RedPacket> redPacketsByRate = new ArrayList<>();
            List<RedPacket> redPacketsByRateDay = new ArrayList<>();
            for (RedPacket redPacket : list) {
                if (StringUtils.equals(redPacket.getType(), RedPacket.TYPE_MONEY) || StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                    redPacketsByMoney.add(redPacket);
                } else if (StringUtils.equals(redPacket.getType(), RedPacket.TYPE_RATE)) {
                    redPacketsByRate.add(redPacket);
                } else if (StringUtils.equals(redPacket.getType(), RedPacket.TYPE_RATE_BY_DAY)) {
                    redPacketsByRateDay.add(redPacket);
                }
            }
            double money = 0;
            List<RedPacket> maxRedPacketsByMoney = this.getMaxRedPackets(redPacketsByMoney, RedPacket.TYPE_MONEY);
            if (maxRedPacketsByMoney.size() > 0) {
                money = maxRedPacketsByMoney.get(0).getMoney();
            }
            double maxMoneyByRate = 0;
            List<RedPacket> maxRedPacketsByRate = this.getMaxRedPackets(redPacketsByRate, RedPacket.TYPE_RATE);
            if (maxRedPacketsByRate.size() >0) {
                maxMoneyByRate = this.getRedpacketMoneyCommon(maxRedPacketsByRate.get(0),subject,amount);
            }
            double maxMoneyByRateDay = 0;
            List<RedPacket> maxRedPacketsByRateDay = new ArrayList<>();
            if (redPacketsByRateDay.size() >0) {
                maxMoneyByRateDay = this.getRedpacketMoneyCommon(redPacketsByRateDay.get(0),subject,amount);
                for (RedPacket redPacket : redPacketsByRateDay) {
                    double tempMoney = this.getRedpacketMoneyCommon(redPacket,subject,amount);
                    if (tempMoney > maxMoneyByRateDay) {
                        maxMoneyByRateDay = tempMoney;
                    }
                }
                for (RedPacket redPacket : redPacketsByRateDay) {
                    double tempMoney = this.getRedpacketMoneyCommon(redPacket,subject,amount);
                    if (tempMoney == maxMoneyByRateDay) {
                        maxRedPacketsByRateDay.add(redPacket);
                    }
                }
            }
            RedPacket maxPacket = null;
            if (money >= maxMoneyByRate && money >= maxMoneyByRateDay) {
                if (maxRedPacketsByMoney != null && maxRedPacketsByMoney.size() > 0) {
                    maxPacket = maxRedPacketsByMoney.get(0);
                }
            } else if (money <= maxMoneyByRate && maxMoneyByRateDay <= maxMoneyByRate) {
                if (maxRedPacketsByRate != null && maxRedPacketsByRate.size() > 0) {
                    maxPacket = maxRedPacketsByRate.get(0);
                }
            } else if (money <= maxMoneyByRateDay && maxMoneyByRate <= maxMoneyByRateDay) {
                if (maxRedPacketsByRateDay != null && maxRedPacketsByRateDay.size() > 0) {
                    maxPacket = maxRedPacketsByRateDay.get(0);
                }
            }
            return maxPacket;
        }
        return null;
    }


    //获取红包卷加息金额(调整)
    public double getRedpacketMoney(RedPacket redPacket,Integer periods,double money,IPlan iPlan) {

        String type = redPacket.getType();
        String status = redPacket.getSendStatus();
        double rate = redPacket.getRate();
        String repayType = iPlan.getRepayType();
        int interestAccrualType = iPlan.getInterestAccrualType();
        if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)){
            periods = iPlan.getDay();
        } else {
            periods = iPlan.getTerm();
            if(iPlanAccountService.isNewIplan(iPlan)){
                periods = iPlan.getExitLockDays()/31==0?1:iPlan.getExitLockDays()/31;
            }
        }
        double d = 0;
        if (23 == redPacket.getRuleId()) {// 如果红包规则是23则金额最高计算上限是5万
            money = money > 50000 ? 50000 : money;
        }
        //普通加息券就是加到锁定期为止
        if (RedPacket.SEND_STATUS_USED.equals(status)){
            if (RedPacket.TYPE_RATE.equals(type)) {
                if (IPlan.REPAY_TYPE_MCEI.equals(repayType)){
                    repayType = "等额本息";
                    d= InterestUtil.getInterestByPeriodMoth(money,rate,periods,repayType);
                }else {
                    if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)){
                        d = InterestUtil.getInterestByPeriodDay(money, rate, periods);
                    } else {
                        d = InterestUtil.getRFCLInterestByPeriodMoth(money, rate, periods);
                    }
                }
            } else if (RedPacket.TYPE_RATE_BY_DAY.equals(type)) {
                //按天加息券，如果天数小于锁定期，按加息劵天数加息
                if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)){
                    periods = periods < redPacket.getRateDay() ? periods : redPacket.getRateDay();
                } else {
                    periods = redPacket.getRateDay();
                }
                d = InterestUtil.getInterestByPeriodDay(money, rate, periods);

            } else if (RedPacket.TYPE_MONEY.equals(type)) {
                // 固定金额奖励的情况下
                d = redPacket.getMoney();
            }
        }
        return d;
    }
    /**
     * 债转获取红包卷加息金额
     * @param redPacket
     * @param id
     * @param money
     * @return
     */
    public double getCreditRedpacketMoney(RedPacket redPacket,Integer id, double money){
        String type = redPacket.getType();
        String status = redPacket.getSendStatus();
        CreditOpening creditOpening = creditOpeningDao.findById(id);
        Credit credit = creditDao.findById(creditOpening.getCreditId());

        double rate = redPacket.getRate();
        double d = 0.0;
        if (23 == redPacket.getRuleId()) {// 如果红包规则是23则金额最高计算上限是5万
            money = money > 50000 ? 50000 : money;
        }
        String repayType=subjectService.getSubjectRepayType(creditOpening.getSubjectId());
        //期数
        int term = credit != null ? credit.getResidualTerm() : 0;
        //天数
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        String endDate = creditOpening.getEndTime().substring(0, 8);
        int period = (int) DateUtil.betweenDays(currentDate, endDate);
        if (type.equals(RedPacket.TYPE_RATE)) {
            if(period<30){
                d= InterestUtil.getInterestByPeriodDay(money,rate,period);
            }
            else{
                d= InterestUtil.getInterestByPeriodMoth(money,rate,term,repayType);
            }
        } else if (type.equals(RedPacket.TYPE_RATE_BY_DAY)) {
            //按天加息券，如果天数小于期限数，按加息劵天数加息
            int rateDay = period <= redPacket.getRateDay() ? period : redPacket.getRateDay();
            d = InterestUtil.getInterestByPeriodDay(money, rate, rateDay);
        } else if (RedPacket.TYPE_MONEY.equals(type)) {
            // 固定金额奖励的情况下
            d = redPacket.getMoney();
        }
        return d;
    }


    /**
     * 获取用户可用的红包
     *
     * @param userId    用户id
     * @param subject     散标id
     */
    @ProductSlave
    public List<RedPacket> getUsablePacketCreditAll(String userId, Subject subject, String userSource,String subjectType) {
        try {
            //查询用户未使用的红包
            User user = userService.findByUsername(userId);
            RedPacket redPacket = new RedPacket();
            redPacket.setMobileNumber(user.getMobileNumber());
            redPacket.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
            redPacket.setUsageDetail(RedPacket.USAGEDETAIL_INVEST);
            List<RedPacket> list = this.getRedPacketCreditDetails(redPacket);
            List<RedPacket> useList = new ArrayList<>();//有用红包
            //没用的红包
            List<RedPacket> unAbleList = new ArrayList<>();
            if (list == null || list.size() <= 0) {
                return null;
            }
            //使用红包校验规则选择符合条件的红包
            Iterator<RedPacket> iterator = list.iterator();
            while(iterator.hasNext()){
                RedPacket redPacket2 = iterator.next();
                try {
                    this.verifyRedPacketSubject(userId, redPacket2.getId(), subject, userSource,subjectType);
                    redPacket2.setUseStatus("useAble");
                    useList.add(redPacket2);
                } catch (Exception e) {
                    //iterator.remove();
                    redPacket2.setUseStatus("unUseAble");
                    unAbleList.add(redPacket2);
                }
            }
            //有用的红包
            /*Iterator<RedPacket> iteratorUse = list.iterator();
            while(iteratorUse.hasNext()){
                RedPacket useRedPacket = iteratorUse.next();
                useRedPacket.setUseStatus("useAble");
                useList.add(useRedPacket);
            }*/

            List<RedPacket> totalList = new ArrayList<>();
            totalList.addAll(useList);
            totalList.addAll(unAbleList);
            return totalList;
        }catch (Exception e){
            return null;
        }

    }
    public void verifyRedPacketSubject(String userId, int redPacketId, Subject subject, String userSource,String subjectType) {
        logger.warn("校验红包是否可用，userId:" + userId + ",redPacketId:" + redPacketId + ",subject:" + subject.toString() + ",userSource:");
        if (StringUtils.isBlank(userId) || redPacketId == 0 || subject == null || StringUtils.isBlank(userSource)) {
            logger.warn("用户: " + userId + "使用红包id为0");
            throw new ProcessException(Error.NDR_0101);
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            logger.warn("用户: " + userId + "不存在");
            throw new ProcessException(Error.NDR_0419);
        }
        String mobileNumber = user.getMobileNumber();
        if (mobileNumber == null) {
            logger.warn("用户：" + userId + "手机号不存在");
            throw new ProcessException(Error.NDR_0431);
        }
        RedPacket redPacket = new RedPacket(redPacketId, mobileNumber, null, null, null,
                null, 0);
        List<RedPacket> redPackets = redPacketDao.getRedPacketsByCondition(redPacket);
        if (redPackets != null && redPackets.size() == 1) {
            redPacket = redPackets.get(0);
        } else {
            logger.warn("用户：" + userId + "使用的红包：" + redPacketId + "不存在");
            throw new ProcessException(Error.NDR_0432);
        }
        if (redPacket != null) {
            if (subject.getActivityId()!=null) {
                ActivityMarkConfigure amc = activityMarkConfigureService.findById(subject.getActivityId());
                //活动标配置不能使用红包
                if(amc.getRedpacketWhether()==null ||amc.getRedpacketWhether()==0){
                    throw new ProcessException(Error.NDR_0500);
                }
            }
            //排除天天赚红包
            if (StringUtils.equals(RedPacket.TYPE_RATELPLAN, redPacket.getType())) {
                throw new ProcessException(Error.NDR_04392);
            }
            //排除天天赚转投月月盈专属红包
            if (redPacket.getSpecificType() != null && redPacket.getSpecificType() == 1) {
                throw new ProcessException(Error.NDR_04394);
            }
            if(StringUtils.isNotBlank(subjectType) && "credit".equals(subjectType)){
                if (redPacket.getSpecificType() == null ) {
                    throw new ProcessException(Error.NDR_0909);
                }

            }
            //如果是散标购买就判断 是否债权购买专享
            if (StringUtils.isNotBlank(subjectType) && "subject".equals(subjectType)) {
                //排除债权购买专属红包
                if (redPacket.getSpecificType() != null && redPacket.getSpecificType() == 2) {
                    throw new ProcessException(Error.NDR_04395);
                }
            }

            /*
            *   useLoanType：1,新手标不可使用，2，APP专享，3，APP专享且新手标不可用
            */
            if (Subject.NEWBIE_ONLY_Y.equals(subject.getNewbieOnly()) && redPacket.getUseLoanType() == 1) {
                logger.warn("用户ID：" + userId + ",项目ID：" + subject.getId()
                        + "redpacketId:" + redPacketId + "此加息券新手标不可使用");
                throw new ProcessException(Error.NDR_0433);
            }
            if (Subject.NEWBIE_ONLY_Y.equals(subject.getNewbieOnly()) && redPacket.getUseLoanType() == 3) {
                logger.warn("用户ID：" + userId + ",项目ID：" + subject.getId()
                        + "redpacketId:" + redPacketId + "此红包券APP投资专享且新手标不可用");
                throw new ProcessException(Error.NDR_0434);
            }
            if (redPacket.getUseLoanType() == 2 && !userSource.contains("ios") && !userSource.contains("android")) {
                logger.warn("用户ID：" + userId + ",项目ID：" + subject.getId()
                        + "redpacketId:" + redPacketId + "此加息券APP专享");
                throw new ProcessException(Error.NDR_0435);
            }
            if (redPacket.getUseLoanType() == 3 && !userSource.contains("ios") && !userSource.contains("android")) {
                logger.warn("用户ID：" + userId + ",项目ID：" + subject.getId()
                        + "redpacketId:" + redPacketId + "此红包券APP投资专享且新手标不可用");
                throw new ProcessException(Error.NDR_0434);
            }
            // 投资周期限制
            if("subject".equals(subjectType)){
                if (redPacket.getInvestCycle() > 0 && redPacket.getInvestCycle() > subject.getTerm()) {
                    logger.warn("用户ID：" + userId + "，项目ID：" + subject.getId()
                            + "redPacketId:" + redPacketId + "此红包券仅限" + redPacket.getInvestCycle() + "个月及以上标的使用");
                    throw new ProcessException(Error.NDR_0760);
                }
            }else{
                CreditOpening creditOpening = creditOpeningDao.findBySubjectIdAndStatus(subject.getSubjectId());
                if(creditOpening != null){
                    Credit credit = creditDao.findById(creditOpening.getCreditId());
                    if (redPacket.getInvestCycle() > 0 && redPacket.getInvestCycle() > credit.getResidualTerm()) {
                        logger.warn("用户ID：" + userId + "，项目ID：" + subject.getId()
                                + "redPacketId:" + redPacketId + "此红包券仅限" + redPacket.getInvestCycle() + "个月及以上债转项目使用");
                        throw new ProcessException(Error.NDR_0908);
                    }
                }else{
                    throw new ProcessException(Error.NDR_0907);
                }
            }
            if (!"unused".equals(redPacket.getSendStatus())) {
                logger.warn("用户ID：" + userId + "，项目ID：" + subject.getId()
                        + "redPacketId:" + redPacketId + "状态不是未使用" + redPacket.toString());
                String status = "被使用";
                if ("expired".equals(redPacket.getSendStatus())) {
                    status = "过期";
                    throw new ProcessException(Error.NDR_0438);
                }
                throw new ProcessException(Error.NDR_0437);
            }
            if (!StringUtils.equals(RedPacket.USAGEDETAIL_INVEST, redPacket.getUsageDetail())) {
                logger.warn("投资不可使用" + redPacketId);
                throw new ProcessException(Error.NDR_0439);
            }
/*            if (money < redPacket.getInvestMoney()) {
                logger.warn("投资金额小于限制金额;投资金额：" + money + ";限制金额：" + redPacket.getInvestMoney());
                throw new ProcessException(Error.NDR_0440);
            }*/
            if (redPacket.getInvestRate() > 0
                    && subject.getInvestRate() != null && subject.getInvestRate().doubleValue() > redPacket.getInvestRate()) {
                logger.warn("投资利率大于限制利率;投资利率：" + subject.getInvestRate() + ";限制利率："
                        + redPacket.getInvestRate());
                throw new ProcessException(Error.NDR_0441);
            }
            if (redPacket.getRuleId() == 31) {
                logger.warn("userId:" + userId + ",红包ID：" + redPacket.getId());
                double newbieUsable = investService.getNewbieUsable(userId,null);
                if (newbieUsable <= 0) {
                    logger.warn("老用户不能使用此类红包券userId:" + userId + "red" + redPacket.toString());
                    throw new ProcessException(Error.NDR_0442);
                }
            }
        } else {
            logger.warn("用户：" + userId + "使用的红包：" + redPacketId + "不存在");
            throw new ProcessException(Error.NDR_0432);
        }
    }

    /**
     * 返回红包信息
     * @param redPacket
     * @return
     */
    public String getRedpackeMsg(RedPacket redPacket) {
        String redpackeMsg="";
        if (RedPacket.TYPE_MONEY.equals(redPacket.getType())) {
            redpackeMsg = ArithUtil.round(redPacket.getMoney(), 2) + "元现金券";
        } else if (RedPacket.TYPE_RATE.equals(redPacket.getType())) {
            redpackeMsg = ArithUtil.round(redPacket.getRate() * 100, 2)
                    + "%加息券";
        } else if (RedPacket.TYPE_DEDUCT.equals(redPacket.getType())) {
            redpackeMsg = ArithUtil.round(redPacket.getMoney(), 2)
                    + "元抵扣券";
        } else if (RedPacket.TYPE_RATE_BY_DAY.equals(redPacket.getType())) {
            redpackeMsg = ArithUtil.round(redPacket.getRate() * 100, 2)
                    + "%" + redPacket.getRateDay() + "天加息券";
        }
        return redpackeMsg;
    }
}
