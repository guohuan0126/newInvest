package com.jiuyi.ndr.util.redis;

public interface ICustomer {
    void customer(String key, String message) throws RollbackException;
}
