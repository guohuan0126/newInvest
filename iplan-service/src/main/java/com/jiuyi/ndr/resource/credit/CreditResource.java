package com.jiuyi.ndr.resource.credit;

import com.github.pagehelper.PageInfo;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.dto.credit.CreditDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.page.PageDataPlus;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiaolei on 2017/6/8.
 */
@RestController
public class CreditResource {

    @Autowired
    public CreditService creditService;
    @Autowired
    public CreditOpeningService creditOpeningService;

    @GetMapping("/{userId}/{iPlanId}/holding/credits")
    public RestResponse<PageDataPlus> getUserHoldingCredits(@PathVariable("userId") String userId, @PathVariable("iPlanId") Integer iPlanId,
                                                            @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        List<CreditDto> creditDtos = new ArrayList<>();
        List<Credit> credits = creditService.getUserHoldingCreditsInSomeIPlanByPageHelper(userId, iPlanId, pageNo, pageSize);
        Integer totalAmt = 0;
        for (Credit credit : credits) {
            CreditDto creditDto = new CreditDto();
            creditDto.setCreditId(String.valueOf(credit.getId()));
            creditDto.setSubjectId(credit.getSubjectId());
            creditDto.setHoldingPrincipal(credit.getHoldingPrincipal() / 100.0);
            creditDto.setStartTime(DateUtil.parseDateTime(credit.getStartTime(), DateUtil.DATE_TIME_FORMATTER_17).format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            if (StringUtils.isEmpty(credit.getEndTime())) {
                creditDto.setEndTime("----/--/--");
            } else {
                creditDto.setEndTime(DateUtil.parseDate(credit.getEndTime().substring(0, 8), DateUtil.DATE_TIME_FORMATTER_8).format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            }
            creditDtos.add(creditDto);
            totalAmt += credit.getHoldingPrincipal() == null ? 0 : credit.getHoldingPrincipal();
        }
        PageDataPlus<CreditDto> pageData = new PageDataPlus<>();
        PageInfo<Credit> pageInfo = new PageInfo<>(credits);
        pageData.setList(creditDtos);
        pageData.setPage(pageInfo.getPageNum());
        pageData.setSize(pageInfo.getSize());
        pageData.setTotalPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        return new RestResponseBuilder<PageDataPlus>().success(pageData);
    }

    @GetMapping("/{userId}/{iPlanId}/transferring/credits")
    public RestResponse<PageDataPlus> getUserTransferringCredits(@PathVariable("userId") String userId, @PathVariable("iPlanId") Integer iPlanId,
                                                                 @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        List<CreditDto> creditDtos = new ArrayList<>();
        List<CreditOpening> creditOpenings = creditOpeningService.getUserTransferringCreditsInSomeIPlanByPageHelper(userId, iPlanId, pageNo, pageSize);
        Integer totalAmt = 0;
        for (CreditOpening creditOpening : creditOpenings) {
            CreditDto creditDto = new CreditDto();
            creditDto.setSubjectId(creditOpening.getSubjectId());
            creditDto.setHoldingPrincipal(creditOpening.getTransferPrincipal() / 100.0);
            Credit credit = creditService.getById(creditOpening.getCreditId());
            creditDto.setStartTime(DateUtil.parseDateTime(credit.getStartTime(), DateUtil.DATE_TIME_FORMATTER_17).format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            if (StringUtils.isEmpty(credit.getEndTime())) {
                creditDto.setEndTime("----/--/--");
            } else {
                creditDto.setEndTime(DateUtil.parseDate(credit.getEndTime().substring(0, 8), DateUtil.DATE_TIME_FORMATTER_8).format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            }
            creditDtos.add(creditDto);
            totalAmt += credit.getHoldingPrincipal() == null ? 0 : credit.getHoldingPrincipal();
        }
        PageDataPlus<CreditDto> pageData = new PageDataPlus<>();
        PageInfo<CreditOpening> pageInfo = new PageInfo<>(creditOpenings);
        pageData.setList(creditDtos);
        pageData.setPage(pageInfo.getPageNum());
        pageData.setSize(pageInfo.getSize());
        pageData.setTotalPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        return new RestResponseBuilder<PageDataPlus>().success(pageData);
    }

    @GetMapping("/{userId}/{iPlanId}/end/credits")
    public RestResponse<PageDataPlus> getUserEndCredits(@PathVariable("userId") String userId, @PathVariable("iPlanId") Integer iPlanId,
                                                        @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        List<CreditDto> creditDtos = new ArrayList<>();
        List<Credit> credits = creditService.getUserHoldingCreditsInSomeIPlanByPageHelper(userId, iPlanId, pageNo, pageSize);
        Integer totalAmt = 0;
        for (Credit credit : credits) {
            CreditDto creditDto = new CreditDto();
            creditDto.setCreditId(String.valueOf(credit.getId()));
            creditDto.setSubjectId(credit.getSubjectId());
            creditDto.setHoldingPrincipal(credit.getInitPrincipal() / 100.0);
            creditDto.setStartTime(DateUtil.parseDateTime(credit.getStartTime(), DateUtil.DATE_TIME_FORMATTER_17).format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            if (StringUtils.isEmpty(credit.getEndTime())) {
                creditDto.setEndTime("----/--/--");
            } else {
                creditDto.setEndTime(DateUtil.parseDate(credit.getEndTime().substring(0, 8), DateUtil.DATE_TIME_FORMATTER_8).format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            }
            creditDtos.add(creditDto);
            totalAmt += credit.getHoldingPrincipal() == null ? 0 : credit.getHoldingPrincipal();
        }
        PageDataPlus<CreditDto> pageData = new PageDataPlus<>();
        PageInfo<Credit> pageInfo = new PageInfo<>(credits);
        pageData.setList(creditDtos);
        pageData.setPage(pageInfo.getPageNum());
        pageData.setSize(pageInfo.getSize());
        pageData.setTotalPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        return new RestResponseBuilder<PageDataPlus>().success(pageData);
    }

}
