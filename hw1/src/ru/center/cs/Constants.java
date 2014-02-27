package ru.center.cs;

/**
 * @author Yuri Denison
 * @date 25.02.2014
 */
public class Constants {
    public static final String HEADER_ACTION_TITLE = "replica_action";
    public static final String HEADER_REPLICA_ADDRESS = "replica_address";
    public static final String HEADER_REPLICA_ACTION_REGISTER = "replica_register";
    public static final String HEADER_REPLICA_ACTION_UNREGISTER = "replica_unregister";
    public static final String HEADER_REPLICA_ACTION_DEPRECATED = "replica_deprecated";
    public static final String HEADER_REPLICA_ACTION_SET_ACTIVE = "replica_set_active";
    public static final String HEADER_REPLICA_ACTION_SET_INACTIVE = "replica_set_inactive";
    public static final String HEADER_REPLICA_ACTION_SHUTDOWN = "replica_shutdown";

    public static final String MASTER_WRITE_PATH = "/get-write-replica";
    public static final String MASTER_MANAGE_PATH = "/manage";
    public static final String REPLICA_WRITE_PATH = "/write";
    public static final String REPLICA_MANAGE_PATH = "/manage";

    public static final String REPLICA_ACTIVE_RESPONSE = "OK";
    public static final String REPLICA_INACTIVE_RESPONSE = "KO";

    public static final int DEFAULT_REPLICA_NUMBER = 2;
    public static final int MASTER_DEFAULT_PORT = 8080;
    public static final int REPLICA_DEPRECATION_TIME = 10000;
    public static final int CONNECTION_TIMEOUT = 3000;
}
