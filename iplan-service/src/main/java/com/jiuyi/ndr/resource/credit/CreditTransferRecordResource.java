package com.jiuyi.ndr.resource.credit;

import com.jiuyi.ndr.dto.credit.CreditTransferRecordDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.page.PageData;
import com.jiuyi.ndr.service.credit.CreditTransferRecordService;
import com.jiuyi.ndr.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 债转记录接口
 *
 * @author 姜广兴
 * @date 2018-04-17
 */
@RestController
public class CreditTransferRecordResource {
    @Autowired
    private CreditTransferRecordService creditTransferRecordService;

    @GetMapping("/creditTransferRecords/{subjectId}")
    public RestResponse getCreditTransferRecords(@RequestParam("pageNo") int pageNo,
                                                 @RequestParam("pageSize") int pageSize, @PathVariable("subjectId") String subjectId) {
        List<CreditTransferRecordDto> creditTransferRecordDtos = creditTransferRecordService.getCreditTransferRecords(subjectId);
        int size = creditTransferRecordDtos.size();
        PageData pageData = new PageData();
        pageData.setList(new PageUtil().ListSplit(creditTransferRecordDtos, pageNo, pageSize));
        pageData.setPage(pageNo);
        pageData.setSize(pageSize);
        pageData.setTotal(size);
        pageData.setTotalPages(size % pageSize != 0 ? size / pageSize + 1 : size / pageSize);
        return new RestResponseBuilder<>().success(pageData);
    }
}
