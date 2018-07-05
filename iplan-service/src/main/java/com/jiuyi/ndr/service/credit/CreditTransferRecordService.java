package com.jiuyi.ndr.service.credit;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.dto.credit.CreditTransferRecordDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.customannotation.PutRedis;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * 债转记录接口
 *
 * @author 姜广兴
 * @date 2018-04-17
 */
@Service
public class CreditTransferRecordService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CreditTransferRecordService.class);
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private CreditDao creditDao;
    @Autowired
    private UserService userService;
    @Autowired
    private SubjectDao subjectDao;
    private static final DecimalFormat DF_RATE = new DecimalFormat("0.#");
    private static final DecimalFormat DF_AMOUNT = new DecimalFormat("0.00");

    @ProductSlave
    @PutRedis(key = GlobalConfig.CREDIT_RECORD,fieldKey = "#subjectId",expire = 1200)
    public List<CreditTransferRecordDto> getCreditTransferRecords(String subjectId) {
        LOGGER.info("债转记录接口执行开始，subjectId=[{}]", subjectId);
        if (isBlank(subjectId)) {
            LOGGER.error("债转记录接口执行失败，subjectId=[{}]", subjectId);
            throw new ProcessException(Error.NDR_0202.getCode(), Error.NDR_0202.getMessage());
        }
        List<CreditTransferRecordDto> creditTransferRecordDtos = new ArrayList<>();
        Subject subject = subjectDao.findBySubjectId(subjectId);
        if (subject == null) {
            return creditTransferRecordDtos;
        }
        String subjectName = subject.getName();
        //根据subjectId查询所有开放中债权
        List<CreditOpening> creditOpenings = creditOpeningService.getBySubjectId(subjectId);
        if (CollectionUtils.isEmpty(creditOpenings)) {
            return creditTransferRecordDtos;
        }
        //遍历所有开放中债权
        dealCreditOpenings(creditOpenings, creditTransferRecordDtos, subjectName);
        creditTransferRecordDtos = sort(creditTransferRecordDtos);

        return creditTransferRecordDtos;
    }

    /**
     * 处理开放中债权
     *
     * @param creditOpenings
     * @param creditTransferRecordDtos
     * @param subjectName
     */
    private void dealCreditOpenings(List<CreditOpening> creditOpenings, List<CreditTransferRecordDto> creditTransferRecordDtos, String subjectName) {
        creditOpenings.parallelStream().forEach(creditOpening -> {
            //根据targetId查询所有债权
            List<Credit> credits = creditDao.findByTargetId(creditOpening.getId());
            if (creditOpening.getAvailablePrincipal() != 0) {
                Credit credit = new Credit();
                credit.setCreditStatus(Credit.CREDIT_STATUS_HOLDING);
                credit.setTarget(Credit.TARGET_CREDIT);
                credits.add(credit);
            }

            //过滤掉状态为处理中和来源为散标的记录
            Stream<Credit> creditStream = credits.parallelStream().filter(
                    credit -> (credit.getCreditStatus() != null
                            && Credit.CREDIT_STATUS_WAIT != (credit.getCreditStatus())
                            && credit.getTarget() != null
                            && Credit.TARGET_CREDIT == credit.getTarget())
            );
            creditStream.forEach(credit -> dealCredit(creditOpening, credit, creditTransferRecordDtos, subjectName));
        });
    }

    /**
     * 处理债权
     *
     * @param creditOpening
     * @param credit
     * @param creditTransferRecordDtos
     * @param subjectName
     */
    private void dealCredit(CreditOpening creditOpening, Credit credit, List<CreditTransferRecordDto> creditTransferRecordDtos, String subjectName) {
        String transferorId = creditOpening.getTransferorId();
        String userId = credit.getUserId();
        //原债权人
        User transferor = userService.getUserById(transferorId);
        //受让人
        User user = null;
        if (!isBlank(userId)) {
            user = userService.getUserById(userId);
        }
        if (transferor == null) {
            return;
        }
        CreditTransferRecordDto creditTransferRecordDto = new CreditTransferRecordDto();
        creditTransferRecordDto.setSubjectName(subjectName);
        BigDecimal transferDiscount = creditOpening.getTransferDiscount();
        //对用户id脱敏
        creditTransferRecordDto.setSourceCreditUserId(insensitivityUserId(transferorId));
        creditTransferRecordDto.setSourceCreditUser(insensitivityUserName(transferor.getRealname()));
        creditTransferRecordDto.setAcceptUserId(isBlank(userId) ? "" : insensitivityUserId(userId));
        creditTransferRecordDto.setAcceptUser(user != null ? insensitivityUserName(user.getRealname()) : "");
        Integer initPrincipal = credit.getInitPrincipal();
        creditTransferRecordDto.setTransferAmount(DF_AMOUNT.format((initPrincipal != null ? initPrincipal : creditOpening.getAvailablePrincipal()) / 100.0));
        creditTransferRecordDto.setDiscountRate(DF_RATE.format(transferDiscount.multiply(BigDecimal.valueOf(100))) + "%");
        creditTransferRecordDto.setTransferTime(creditOpening.getCreateTime().substring(0, 10));
        creditTransferRecordDto.setAcceptTime(isBlank(credit.getCreateTime()) ? "" : credit.getCreateTime().substring(0, 10));
        creditTransferRecordDto.setAcceptAmount(initPrincipal != null ? DF_AMOUNT.format(transferDiscount.multiply(BigDecimal.valueOf(initPrincipal / 100.0))) : "");
        creditTransferRecordDto.setCreditId(credit.getId() == null ? 0 : credit.getId());
        creditTransferRecordDtos.add(creditTransferRecordDto);
    }

    /**
     * 排序
     *
     * @param creditTransferRecordDtos
     * @return
     */
    private List<CreditTransferRecordDto> sort(List<CreditTransferRecordDto> creditTransferRecordDtos) {
        //根据债权形成时间排序
        final List<CreditTransferRecordDto> finalCtrds = creditTransferRecordDtos;
        //重用Stream
        Supplier<Stream<CreditTransferRecordDto>> streamSupplier = () -> finalCtrds.parallelStream();
        //过滤出没有卖出的部分债权
        List<CreditTransferRecordDto> noBuyers = streamSupplier.get().filter(ctrd -> ctrd.getCreditId() == 0).collect((Collectors.toList()));
        //过滤出卖出的部分债权，按债权形成时间正序排序
        creditTransferRecordDtos = streamSupplier.get().filter(ctrd -> ctrd.getCreditId() > 0)
                .sorted(Comparator.comparing(CreditTransferRecordDto::getCreditId))
                .collect((Collectors.toList()));
        //没有卖出的部分债权，放在集合最后
        creditTransferRecordDtos.addAll(noBuyers);
        return creditTransferRecordDtos;
    }

    private String insensitivityUserId(String userId) {
        return StringUtils.replaceWithSpecialStr(userId, "***", 2, 2);
    }

    private String insensitivityUserName(String userName) {
        return StringUtils.replaceRightWithSpecialStr(userName, "*", 1);
    }

    public static void main(String[] args) {
        System.out.println(DF_AMOUNT.format(0));
        System.out.println(DF_RATE.format(0));
        System.out.println(DF_AMOUNT.format(1.1));
        System.out.println(DF_RATE.format(1.1));
        System.out.println(DF_AMOUNT.format(1.12));
        System.out.println(DF_RATE.format(1.12));
        System.out.println(DF_RATE.format(99.5));
    }
}
