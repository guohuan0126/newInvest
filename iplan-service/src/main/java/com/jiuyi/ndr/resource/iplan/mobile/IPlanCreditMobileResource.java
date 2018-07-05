package com.jiuyi.ndr.resource.iplan.mobile;

import com.github.pagehelper.PageInfo;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.subject.BorrowInfo;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppCreditDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.page.PageDataPlus;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.util.DealUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiaolei on 2017/6/16.
 */
@RestController
public class IPlanCreditMobileResource {

    @Autowired
    private CreditService creditService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private SubjectService subjectService;

    @GetMapping("authed/{userId}/{iPlanId}/holding/credits")
    public RestResponse<PageDataPlus> getUserHoldingCredits(@PathVariable("userId") String userId, @PathVariable("iPlanId") Integer iPlanId,
                                                            @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        List<IPlanAppCreditDto> appCreditDtos = new ArrayList<>();
        List<Credit> credits = creditService.getUserHoldingCreditsInSomeIPlanByPageHelper(userId, iPlanId, pageNo, pageSize);
        Integer totalAmt = 0;
        for (Credit credit : credits) {
            IPlanAppCreditDto appCreditDto = new IPlanAppCreditDto();
            appCreditDto.setCreditId(String.valueOf(credit.getId()));
            appCreditDto.setSubjectId(credit.getSubjectId());
            Subject subject = subjectService.findSubjectBySubjectId(credit.getSubjectId());
            if (subject != null) {
                appCreditDto.setSubjectName(subject.getSubjectId());
            }
            appCreditDto.setHoldingPrincipal(String.valueOf(credit.getHoldingPrincipal() / 100.0));
            BorrowInfo borrowInfo = subjectService.getBorrowerInfo(credit.getSubjectId(), credit.getId());
            if (borrowInfo != null) {
                appCreditDto.setBorrowName(DealUtil.dealRealname(borrowInfo.getName()));
                appCreditDto.setBorrowIdCard(DealUtil.dealIdCard(borrowInfo.getIdCard()));
            }
            appCreditDtos.add(appCreditDto);
            totalAmt += credit.getHoldingPrincipal() == null ? 0 : credit.getHoldingPrincipal();
        }
        PageDataPlus<IPlanAppCreditDto> pageData = new PageDataPlus<>();
        PageInfo<Credit> pageInfo = new PageInfo<>(credits);
        pageData.setList(appCreditDtos);
        pageData.setPage(pageInfo.getPageNum());
        pageData.setSize(pageInfo.getSize());
        pageData.setTotalPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        return new RestResponseBuilder<PageDataPlus>().success(pageData);
    }

    @GetMapping("authed/{userId}/{iPlanId}/transferring/credits")
    public RestResponse<PageDataPlus> getUserTransferringCredits(@PathVariable("userId") String userId, @PathVariable("iPlanId") Integer iPlanId,
                                                                 @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        List<IPlanAppCreditDto> appCreditDtos = new ArrayList<>();
        List<CreditOpening> creditOpenings = creditOpeningService.getUserTransferringCreditsInSomeIPlanByPageHelper(userId, iPlanId, pageNo, pageSize);
        Integer totalAmt = 0;
        for (CreditOpening creditOpening : creditOpenings) {
            IPlanAppCreditDto appCreditDto = new IPlanAppCreditDto();
            appCreditDto.setSubjectId(creditOpening.getSubjectId());
            appCreditDto.setHoldingPrincipal(String.valueOf(creditOpening.getTransferPrincipal() / 100.0));
            appCreditDtos.add(appCreditDto);
            totalAmt += creditOpening.getTransferPrincipal() == null ? 0 : creditOpening.getTransferPrincipal();
        }
        PageDataPlus<IPlanAppCreditDto> pageData = new PageDataPlus<>();
        PageInfo<CreditOpening> pageInfo = new PageInfo<>(creditOpenings);
        pageData.setList(appCreditDtos);
        pageData.setPage(pageInfo.getPageNum());
        pageData.setSize(pageInfo.getSize());
        pageData.setTotalPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        return new RestResponseBuilder<PageDataPlus>().success(pageData);
    }

    @GetMapping("authed/{userId}/{iPlanId}/end/credits")
    public RestResponse<PageDataPlus> getUserEndCredits(@PathVariable("userId") String userId, @PathVariable("iPlanId") Integer iPlanId,
                                                        @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        List<IPlanAppCreditDto> appCreditDtos = new ArrayList<>();
        List<Credit> credits = creditService.getUserHoldingCreditsInSomeIPlanByPageHelper(userId, iPlanId, pageNo, pageSize);
        Integer totalAmt = 0;
        for (Credit credit : credits) {
            IPlanAppCreditDto appCreditDto = new IPlanAppCreditDto();
            appCreditDto.setCreditId(String.valueOf(credit.getId()));
            appCreditDto.setSubjectId(credit.getSubjectId());
            appCreditDto.setHoldingPrincipal(String.valueOf(credit.getInitPrincipal() / 100.0));
            appCreditDtos.add(appCreditDto);
            totalAmt += credit.getHoldingPrincipal() == null ? 0 : credit.getHoldingPrincipal();
        }
        PageDataPlus<IPlanAppCreditDto> pageData = new PageDataPlus<>();
        PageInfo<Credit> pageInfo = new PageInfo<>(credits);
        pageData.setList(appCreditDtos);
        pageData.setPage(pageInfo.getPageNum());
        pageData.setSize(pageInfo.getSize());
        pageData.setTotalPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        return new RestResponseBuilder<PageDataPlus>().success(pageData);
    }

}
