package com.jiuyi.ndr.service.contract;

import com.jiuyi.ndr.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * Created by drw on 2017/7/17.
 */
@Component
public class AsyncContractTask {

    @Autowired
    private ContractService contractService;

    @Async
    public Future<String> doTask1(Integer iplanAccountId, String userId , User user ) throws InterruptedException{
        contractService.signIPlan(iplanAccountId, userId, user.getRealname(), user.getIdCard(), user.getMobileNumber());
        return new AsyncResult<>("Task1 accomplished!");
    }
}
