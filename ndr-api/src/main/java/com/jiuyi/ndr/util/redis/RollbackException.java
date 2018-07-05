package com.jiuyi.ndr.util.redis;

import redis.clients.jedis.exceptions.JedisException;

public class RollbackException extends JedisException{

    private static final long serialVersionUID = 3590091498452695999L;

    public RollbackException(String message) {
        super(message);
    }

    public RollbackException(String message, Throwable cause) {
        super(message, cause);
    }

    public RollbackException(Throwable cause) {
        super(cause);
    }
}
