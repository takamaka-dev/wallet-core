/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 *
 * @author Giovanni Antino giovanni.antino@takamaka.io
 */
@JsonInclude(Include.NON_NULL)
public class DefaultInitParameters {

    /**
     * default wallet name
     */
    public static String WALLET_NAME = "default_wallet";

    /**
     * default wallet file extension
     */
    public static String WALLET_EXTENSION = ".wallet";

    /**
     * transaction wallet file extension
     */
    public static String TRANSACTION_EXTENSION = ".trx";

    /**
     * root folder name for the application
     */
    public static String APPLICATION_ROOT_FOLDER_NAME = ".tkm-chain";
    /**
     * 
     */
    public static String DELETED_WALLET_FOLDER = "wallet_removed_by_delete";
    /**
     * test path hotmoka
     */
    public static String HOTMOKA_TEST_FOLDER_NAME = "hotmoka-test";

    public static String HOTMOKA_FILES_FOLDER_NAME = "hotmoka-files";

    /**
     * zero block file number
     */
    public static String ZERO_BLOCK_FILE_NUMBER = "";

    public static int MEM_CACHE_TRANSACTIONS = 15000;

    /**
     * this value establishes in how many parts the unit can be divided
     */
    public static int NUMBER_OF_ZEROS = 9;

    public static int MAX_MEMORY_BLOCK_SIZE = 50;
    //with these two parameters a transaction from 600 bytes scales to 0,08 tokens
    public static BigInteger FEE_SCALE_MULT = new BigInteger("5");
    public static BigInteger FEE_SCALE_DIV = new BigInteger("15");

    public static BigInteger DISK_SCALE = new BigInteger("100");
    public static BigInteger MEM_SCALE = BigInteger.TEN;
    public static BigInteger CPU_SCALE = BigInteger.ONE;
    //public static  BigInteger CPU_SCALE = new BigInteger("1");
    /**
     * define the desired maximum number of client in the network. The minimum
     * target number will be calculated halving this value
     */
    public static String TARGET_CLIENT_NUMBER_MAX = "400";
//    public static  String TARGET_CLIENT_NUMBER_MAX = "4";
    public static int TARGET_CLIENT_NUMBER_MAX_INT = Integer.parseInt(TARGET_CLIENT_NUMBER_MAX);
    public static BigInteger TARGET_CLIENT_NUMBER_MAX_BI = new BigInteger(TARGET_CLIENT_NUMBER_MAX);
    public static BigInteger YEARS_MOORE_LAW = new BigInteger("10");
    public static int SLOT_PER_EPOCH_INT = 24000;
    //public static  int SLOT_PER_EPOCH_INT = 60;
    public static int BLOCK_PENATY_LIMIT = SLOT_PER_EPOCH_INT / (TARGET_CLIENT_NUMBER_MAX_INT / 2);

    public static BigInteger SLOT_PER_EPOCH = new BigInteger("" + SLOT_PER_EPOCH_INT);
    public static BigInteger MAX_ALLOWED_SLOTS_PER_EPOCH = SLOT_PER_EPOCH.divide(TARGET_CLIENT_NUMBER_MAX_BI).multiply(BigInteger.ONE.add(BigInteger.ONE));
    public static int TRANSACTION_LIMIT_MB_PER_BLOCK = 6;
    public static int TRANSACTION_LIMIT_NUMBER_PER_BLOCK = 10000;
    public static int PAYBACK_LIMIT_SIZE_MB = 2;
    public static int PENALTY_BLOCKS_PER_BLOCK_OVER_THE_LIMIT = 2;
    public static BigInteger BLOCK_COINBASE = TkmTK.unitTK(BigInteger.ONE);
    /**
     * transaction validity threshold in milliseconds as long value
     */
    public static long TRANSACTION_VALIDITY_THRESHOLD = 600000L; //10 min
    /**
     * block permanence in broadcast buffer queue validity threshold in
     * milliseconds as long value
     */
    public static long BROADCAST_BUFFER_QUEUE_VALIDITY_THRESHOLD = 600000L; //10 min
    /**
     * fast sync execution limit
     */
    public static long FAST_SYNC_EXECUTION_TIME_SPACE = 3000L; //10 min
    /**
     *
     */
    public static long THREAD_POOL_EXECUTOR_LIMIT_VALUE = 1000L; //1 sec
    public static TimeUnit THREAD_POOL_EXECUTOR_LIMIT_UNIT = TimeUnit.MILLISECONDS; //1 sec

    public static long TKM_BUFFER_QUEUE_VALIDITY_THRESHOLD = 600000L; //10 min
    public static int TKM_BUFFER_QUEUE_TRANSACTIONS_NUMBER = 25000; //10 min
    /**
     *
     */
    public static int REQUEST_PAY_MESSAGE_LIMIT = 200;

    /**
     * number of blocks in boradcast buffer queue
     */
    public static int MAX_NUMBER_OF_BLOCKS_IN_BROADCAST_BUFFER_QUEUE = 50;

    /**
     * Minimum stake bet in unit format. Use {@code TkmTK} before apply
     */
    public static int MINIMUM_STAKE_BET_UNIT = 200;
    /**
     * Minimum balance for staking
     */
    public static BigInteger MINIMUM_BALANCE_OVER_THE_LIMIT = TkmTK.unitTK(200);
    /**
     * temporary seed for VRF
     */
    public static String RIGGED_VRF_SEED = "Simple. I got very bored and depressed, so I went and plugged myself in to its external computer feed. I talked to the computer at great length and explained my view of the Universe to it";
    /**
     * node state name
     */
    public static String NODE_STATE_NAME = "node_state_default";
    /**
     * node wallet name
     */
    public static String NODE_WALLET_NAME = "node_cat_walking_on_the_keyboard";
    /**
     * node wallet index
     */
    public static int NODE_WALLET_INDEX = 0;
    /**
     * node wallet cypher
     */
    public static KeyContexts.WalletCypher NODE_WALLET_CYPHER = KeyContexts.WalletCypher.BCQTESLA_PS_1_R2;
    //public static KeyContexts.WalletCypher NODE_WALLET_CYPHER = KeyContexts.WalletCypher.Ed25519BC;
    /**
     * name for wallets used in forward secure secure node
     */
    public static String TEMPORARY_NODE_WALLET = "temporary_node_wallet_";
    /**
     *
     */
    public static int EPHEMERAL_KEYS_SIZE = 1024;

    public static int LIMIT_PROPOSED_KEYS_NUMBER = 100;

    public static int LIMIT_BACKTRACK = 25;

    public static int LIMIT_PROPOSED_NODE_KEYS = 2;
    /**
     * number of transaction in wallet history
     */
    public static int LIMIT_PROPOSED_HISTORY_TRANSACTIONS_SIZE = 20;

    /**
     * use ED for test and qtesla for productions
     */
    public static KeyContexts.WalletCypher EPHEMERAL_BLOCK_CYPHER = KeyContexts.WalletCypher.BCQTESLA_PS_1;
    //public static  KeyContexts.WalletCypher EPHEMERAL_BLOCK_CYPHER = KeyContexts.WalletCypher.Ed25519BC;
    public static int BLOCK_POOL_LIMIT = 50;
    /**
     * (key,index),(ED25519BC,0),(qteslabc,1)
     */
    public static int[] TO_DATA_LENGTH_WHITELIST = new int[]{FixedParameters.ED25519_B64URL_ADDR_LEN, FixedParameters.QTESLA_B64URL_ADDR_LEN};
    public static KeyContexts.WalletCypher[] PERMITTED_CYPHERS_FOR_MINING = new KeyContexts.WalletCypher[]{
        KeyContexts.WalletCypher.BCQTESLA_PS_1,
        KeyContexts.WalletCypher.BCQTESLA_PS_1_R2
    };

    // PERMITTED_CYPHERS_FOR_MINING
    /**
     * enables the functions usable only in the test environment. In production
     * the flag must be set ALWAYS to false
     */
    public static boolean TEST_FUNCTIONS_ENABLED = true;
    public static boolean SIMULATION_FUNCTIONS_ENABLED = true;
    /**
     * stop the execution of the node at the end of the simulation
     */
    public static boolean simulationFunctionKillAtTarget = false;
    public static boolean simulationFunctionKillAtBootstrap = false;
    public static int simulationFunctionKillAtBootstrapBlockNum = 10;
    public static boolean DUMP_VRF_METADATA = true;
    //public static  Date SIMULATION_DATE = new Date(new Date().getTime() - (1000L * 30L * 50L)); //milisec + sec + slot nr
    //1585091527000
    //public static  Date SIMULATION_DATE = new Date(1585091527000L); //simulate production start
    //public static Date SIMULATION_DATE = new Date(1614600150000L); //simulate production start
    //public static  Date SIMULATION_DATE = new Date(new Date().getTime() - (1000L * 30L * 22560L)); //milisec + sec + slot nr
    public static Date SIMULATION_DATE = new Date(new Date().getTime() - (1000L * 30L * 1000L)); //milisec + sec + slot nr

    public static String BLOCKS_ID_PATTERN_EXPR = "^E[0-9]{1,6}S[0-9]{1,6}$";
    public static Pattern BLOCKS_ID_PATTERN = Pattern.compile(BLOCKS_ID_PATTERN_EXPR);

    public static boolean isReplicaNode = true;
    public static String ELASTICSEARCH_HOST = "localhost";
    public static int ELASTICSEARCH_PORT = 9200;

    public static int DEFAULT_SEARCH_RESULT_LIMIT = 50;
    public static int MAX_SEARCH_RESULT_LIMIT = 5000;
    public static String ALL_SEARCH_RESULTS = "ALL";

    /*
    public static  String LOCAL_BOOKMARK_URL = "http://127.0.0.1:60006/bookmark";
    public static  String DEFAULT_BOOKMARK_URL = "http://takamaka.io:60006/api/bookmark";
     */
    
    public static String BLOCKCHAIN_HISTORY_API = "/blockhistory";
    public static String GET_BLOCK_API = "/getblock";
    
    public static String FULL_ADDRESS_MAIN_LIST = "assignoverflow";
    public static String FULL_ADDRESS_MAIN_LIST_STATS = "mainlist";
    public static String FULL_OVERFLOW_LIST = "registeroverflow";
    public static String REQUEST_STAKE_LIST = "stake/";
    public static String REQUEST_SM_ADDR = "smaddressreference";
    public static String REQUEST_SM_HOTMOKA_REFERENCE = "smhotmokareference";
    public static String REQUEST_APP_VERSION = "version.json";
    public static String REQUEST_BLOCKCHAIN_SETTINGS = "blockchainsettings";

    public static String SIMPLE_WALLET_APPLICATION_NAME = "SimpleWallet";
    public static String SIMPLE_WALLET_APPLICATION_NUMBER = "0.12h";

    public static int QTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS = 4;
    public static int LRBEANS_LIVENESS = 40;
    public static long LRBEANS_VALIDITY_THRESHOLD = 300000L; //5 min

    /*
    public static  BaseurlBean BASEURL_EXPLORER_PRODUCTION = new BaseurlBean(SWInt.i().getMessage("main_net_tag") + " - PRODUCTION EXPLORER", "https://exp.takamaka.dev/");
    public static  BaseurlBean BASEURL_EXPLORER_TEST = new BaseurlBean(SWInt.i().getMessage("test_tag") + " - TEST EXPLORER", "https://testexplorer.takamaka.dev/");
    public static  BaseurlBean BASEURL_EXPLORER_LOCALHOST = new BaseurlBean(SWInt.i().getMessage("local_tag") + " - LOCALHOST EXPLORER", "http://127.0.0.1:5000/");
    public static  BaseurlBean BASEURL_EXPLORER_ALL[] = new BaseurlBean[]{
        BASEURL_EXPLORER_PRODUCTION,
        BASEURL_EXPLORER_TEST,
        BASEURL_EXPLORER_LOCALHOST
    };
    public static  BaseurlBean BASEURL_EXPLORER_DEFAULT = BASEURL_EXPLORER_PRODUCTION;
     */
   
    //
    // SMART CONTRACT
    //
    /**
     * takamaka code jar
     */
    public static String TAKAMAKA_CODE_JAR_RESOURCE = "io-takamaka-code-1.0.0.jar";
    /**
     * key number for vote address
     */
    public static int S_REGISTER_ADDRESS_KEY_NUMBER = 10000;
    public static boolean PRINT_EXCEPTIONS = true;

    public static String REFERENCE_KEYS_DATABASE_FOLDER = "reference_keys_db_folder";
    public static String STATE_DATABASE_FOLDER = "state_db_folder";

    public static String LIVE_STATE_FOLDER = "live_state";
    public static String REFERENCE_KEYS_DATABASE_MULTIPART_LEVELS = "1";
    public static String REFERENCE_KEYS_DATABASE_NAME = "reference_key_database";
    public static String STATE_DATABASE_NAME = "state_database_name";
    public static ConcurrentSkipListMap<String, String> REFERENCE_KEYS_DATABASE_CONFIGURATION;

    public static String psqlSettingsFile = "psql_database_property_file.json";
    public static String psqlTablespacesRootFolder = "psql_tablespaces_root_folder";
    public static String psqlTablespacesBlocksName = "block_table_tablespace";
    public static String psqlTablespacesBlocksFolderName = "psql_tablespace_block_table";
    public static String psqlTablespacesAddressesFolderName = "psql_tablespace_addresses_table";
    public static String psqlTablespacesAddressesName = "addresses_table_tablespace";
    public static String psqlTablespacesBalancesFolderName = "psql_tablespace_balances_table";
    public static String psqlTablespacesBalancesName = "balances_table_tablespace";
    public static String psqlTablespacesStakesFolderName = "psql_tablespace_stake_table";
    public static String psqlTablespacesStakesName = "stake_table_tablespace";
    public static String psqlTablespacesTransactionsFolderName = "psql_tablespace_transaction_table";
    public static String psqlTablespacesTransactionsName = "transaction_table_tablespace";
    public static String psqlTablespacesTemporaryFolderName = "psql_tablespace_temporary_table";
    public static String psqlTablespacesTemporaryName = "temporary_table_tablespace";

    public static int INSERT_BUCKETS_ADDRESSES = 500;
    /**
     * CONNECTION_VALIDITY_TIMEOUT - - The time in seconds to wait for the
     * database operation used to validate the connection to complete. If the
     * timeout period expires before the operation completes, this method
     * returns false. A value of 0 indicates a timeout is not applied to the
     * database operation.
     */
    public static int CONNECTION_VALIDITY_TIMEOUT = 2;

    public int getCONNECTION_VALIDITY_TIMEOUT() {
        return CONNECTION_VALIDITY_TIMEOUT;
    }

    public void setCONNECTION_VALIDITY_TIMEOUT(int CONNECTION_VALIDITY_TIMEOUT) {
        DefaultInitParameters.CONNECTION_VALIDITY_TIMEOUT = CONNECTION_VALIDITY_TIMEOUT;
    }

    public int getINSERT_BUCKETS_ADDRESSES() {
        return INSERT_BUCKETS_ADDRESSES;
    }

    public void setINSERT_BUCKETS_ADDRESSES(int INSERT_BUCKETS_ADDRESSES) {
        DefaultInitParameters.INSERT_BUCKETS_ADDRESSES = INSERT_BUCKETS_ADDRESSES;
    }

    public String getPsqlTablespacesTemporaryFolderName() {
        return psqlTablespacesTemporaryFolderName;
    }

    public void setPsqlTablespacesTemporaryFolderName(String psqlTablespacesTemporaryFolderName) {
        DefaultInitParameters.psqlTablespacesTemporaryFolderName = psqlTablespacesTemporaryFolderName;
    }

    public String getPsqlTablespacesTemporaryName() {
        return psqlTablespacesTemporaryName;
    }

    public void setPsqlTablespacesTemporaryName(String psqlTablespacesTemporaryName) {
        DefaultInitParameters.psqlTablespacesTemporaryName = psqlTablespacesTemporaryName;
    }

    public String getPsqlTablespacesTransactionsFolderName() {
        return psqlTablespacesTransactionsFolderName;
    }

    public void setPsqlTablespacesTransactionsFolderName(String psqlTablespacesTransactionsFolderName) {
        DefaultInitParameters.psqlTablespacesTransactionsFolderName = psqlTablespacesTransactionsFolderName;
    }

    public String getPsqlTablespacesTransactionsName() {
        return psqlTablespacesTransactionsName;
    }

    public void setPsqlTablespacesTransactionsName(String psqlTablespacesTransactionsName) {
        DefaultInitParameters.psqlTablespacesTransactionsName = psqlTablespacesTransactionsName;
    }

    public String getPsqlTablespacesAddressesFolderName() {
        return psqlTablespacesAddressesFolderName;
    }

    public void setPsqlTablespacesAddressesFolderName(String psqlTablespacesAddressesFolderName) {
        DefaultInitParameters.psqlTablespacesAddressesFolderName = psqlTablespacesAddressesFolderName;
    }

    public String getPsqlTablespacesAddressesName() {
        return psqlTablespacesAddressesName;
    }

    public void setPsqlTablespacesAddressesName(String psqlTablespacesAddressesName) {
        DefaultInitParameters.psqlTablespacesAddressesName = psqlTablespacesAddressesName;
    }

    public String getPsqlTablespacesBalancesFolderName() {
        return psqlTablespacesBalancesFolderName;
    }

    public void setPsqlTablespacesBalancesFolderName(String psqlTablespacesBalancesFolderName) {
        DefaultInitParameters.psqlTablespacesBalancesFolderName = psqlTablespacesBalancesFolderName;
    }

    public String getPsqlTablespacesBalancesName() {
        return psqlTablespacesBalancesName;
    }

    public void setPsqlTablespacesBalancesName(String psqlTablespacesBalancesName) {
        DefaultInitParameters.psqlTablespacesBalancesName = psqlTablespacesBalancesName;
    }

    public String getPsqlTablespacesStakesFolderName() {
        return psqlTablespacesStakesFolderName;
    }

    public void setPsqlTablespacesStakesFolderName(String psqlTablespacesStakesFolderName) {
        DefaultInitParameters.psqlTablespacesStakesFolderName = psqlTablespacesStakesFolderName;
    }

    public String getPsqlTablespacesStakesName() {
        return psqlTablespacesStakesName;
    }

    public void setPsqlTablespacesStakesName(String psqlTablespacesStakesName) {
        DefaultInitParameters.psqlTablespacesStakesName = psqlTablespacesStakesName;
    }

    public String getPsqlTablespacesBlocksName() {
        return psqlTablespacesBlocksName;
    }

    public void setPsqlTablespacesBlocksName(String psqlTablespacesBlocksName) {
        DefaultInitParameters.psqlTablespacesBlocksName = psqlTablespacesBlocksName;
    }

    public String getPsqlTablespacesBlocksFolderName() {
        return psqlTablespacesBlocksFolderName;
    }

    public void setPsqlTablespacesBlocksFolderName(String psqlTablespacesBlocksFolderName) {
        DefaultInitParameters.psqlTablespacesBlocksFolderName = psqlTablespacesBlocksFolderName;
    }

    public String getPsqlTablespacesRootFolder() {
        return psqlTablespacesRootFolder;
    }

    public void setPsqlTablespacesRootFolder(String psqlTablespacesRootFolder) {
        DefaultInitParameters.psqlTablespacesRootFolder = psqlTablespacesRootFolder;
    }

    public String getPsqlSettingsFile() {
        return psqlSettingsFile;
    }

    public void setPsqlSettingsFile(String psqlSettingsFile) {
        DefaultInitParameters.psqlSettingsFile = psqlSettingsFile;
    }

    public String getREFERENCE_KEYS_DATABASE_FOLDER() {
        return REFERENCE_KEYS_DATABASE_FOLDER;
    }

    public void setREFERENCE_KEYS_DATABASE_FOLDER(String REFERENCE_KEYS_DATABASE_FOLDER) {
        DefaultInitParameters.REFERENCE_KEYS_DATABASE_FOLDER = REFERENCE_KEYS_DATABASE_FOLDER;
    }

    //spam
    public String getWALLET_NAME() {
        return WALLET_NAME;
    }

    public void setWALLET_NAME(String WALLET_NAME) {
        DefaultInitParameters.WALLET_NAME = WALLET_NAME;
    }

    public String getWALLET_EXTENSION() {
        return WALLET_EXTENSION;
    }

    public void setWALLET_EXTENSION(String WALLET_EXTENSION) {
        DefaultInitParameters.WALLET_EXTENSION = WALLET_EXTENSION;
    }

    public String getTRANSACTION_EXTENSION() {
        return TRANSACTION_EXTENSION;
    }

    public void setTRANSACTION_EXTENSION(String TRANSACTION_EXTENSION) {
        DefaultInitParameters.TRANSACTION_EXTENSION = TRANSACTION_EXTENSION;
    }

    public String getAPPLICATION_ROOT_FOLDER_NAME() {
        return APPLICATION_ROOT_FOLDER_NAME;
    }

    public void setAPPLICATION_ROOT_FOLDER_NAME(String APPLICATION_ROOT_FOLDER_NAME) {
        DefaultInitParameters.APPLICATION_ROOT_FOLDER_NAME = APPLICATION_ROOT_FOLDER_NAME;
    }

    public String getHOTMOKA_TEST_FOLDER_NAME() {
        return HOTMOKA_TEST_FOLDER_NAME;
    }

    public void setHOTMOKA_TEST_FOLDER_NAME(String HOTMOKA_TEST_FOLDER_NAME) {
        DefaultInitParameters.HOTMOKA_TEST_FOLDER_NAME = HOTMOKA_TEST_FOLDER_NAME;
    }

    public String getHOTMOKA_FILES_FOLDER_NAME() {
        return HOTMOKA_FILES_FOLDER_NAME;
    }

    public void setHOTMOKA_FILES_FOLDER_NAME(String HOTMOKA_FILES_FOLDER_NAME) {
        DefaultInitParameters.HOTMOKA_FILES_FOLDER_NAME = HOTMOKA_FILES_FOLDER_NAME;
    }

    public String getZERO_BLOCK_FILE_NUMBER() {
        return ZERO_BLOCK_FILE_NUMBER;
    }

    public void setZERO_BLOCK_FILE_NUMBER(String ZERO_BLOCK_FILE_NUMBER) {
        DefaultInitParameters.ZERO_BLOCK_FILE_NUMBER = ZERO_BLOCK_FILE_NUMBER;
    }

    public int getMEM_CACHE_TRANSACTIONS() {
        return MEM_CACHE_TRANSACTIONS;
    }

    public void setMEM_CACHE_TRANSACTIONS(int MEM_CACHE_TRANSACTIONS) {
        DefaultInitParameters.MEM_CACHE_TRANSACTIONS = MEM_CACHE_TRANSACTIONS;
    }

    public int getNUMBER_OF_ZEROS() {
        return NUMBER_OF_ZEROS;
    }

    public void setNUMBER_OF_ZEROS(int NUMBER_OF_ZEROS) {
        DefaultInitParameters.NUMBER_OF_ZEROS = NUMBER_OF_ZEROS;
    }

    public int getMAX_MEMORY_BLOCK_SIZE() {
        return MAX_MEMORY_BLOCK_SIZE;
    }

    public void setMAX_MEMORY_BLOCK_SIZE(int MAX_MEMORY_BLOCK_SIZE) {
        DefaultInitParameters.MAX_MEMORY_BLOCK_SIZE = MAX_MEMORY_BLOCK_SIZE;
    }

    public BigInteger getFEE_SCALE_MULT() {
        return FEE_SCALE_MULT;
    }

    public void setFEE_SCALE_MULT(BigInteger FEE_SCALE_MULT) {
        DefaultInitParameters.FEE_SCALE_MULT = FEE_SCALE_MULT;
    }

    public BigInteger getFEE_SCALE_DIV() {
        return FEE_SCALE_DIV;
    }

    public void setFEE_SCALE_DIV(BigInteger FEE_SCALE_DIV) {
        DefaultInitParameters.FEE_SCALE_DIV = FEE_SCALE_DIV;
    }

    public BigInteger getDISK_SCALE() {
        return DISK_SCALE;
    }

    public void setDISK_SCALE(BigInteger DISK_SCALE) {
        DefaultInitParameters.DISK_SCALE = DISK_SCALE;
    }

    public BigInteger getMEM_SCALE() {
        return MEM_SCALE;
    }

    public void setMEM_SCALE(BigInteger MEM_SCALE) {
        DefaultInitParameters.MEM_SCALE = MEM_SCALE;
    }

    public BigInteger getCPU_SCALE() {
        return CPU_SCALE;
    }

    public void setCPU_SCALE(BigInteger CPU_SCALE) {
        DefaultInitParameters.CPU_SCALE = CPU_SCALE;
    }

    public String getTARGET_CLIENT_NUMBER_MAX() {
        return TARGET_CLIENT_NUMBER_MAX;
    }

    public void setTARGET_CLIENT_NUMBER_MAX(String TARGET_CLIENT_NUMBER_MAX) {
        DefaultInitParameters.TARGET_CLIENT_NUMBER_MAX = TARGET_CLIENT_NUMBER_MAX;
    }

    public int getTARGET_CLIENT_NUMBER_MAX_INT() {
        return TARGET_CLIENT_NUMBER_MAX_INT;
    }

    public void setTARGET_CLIENT_NUMBER_MAX_INT(int TARGET_CLIENT_NUMBER_MAX_INT) {
        DefaultInitParameters.TARGET_CLIENT_NUMBER_MAX_INT = TARGET_CLIENT_NUMBER_MAX_INT;
    }

    public BigInteger getTARGET_CLIENT_NUMBER_MAX_BI() {
        return TARGET_CLIENT_NUMBER_MAX_BI;
    }

    public void setTARGET_CLIENT_NUMBER_MAX_BI(BigInteger TARGET_CLIENT_NUMBER_MAX_BI) {
        DefaultInitParameters.TARGET_CLIENT_NUMBER_MAX_BI = TARGET_CLIENT_NUMBER_MAX_BI;
    }

    public BigInteger getYEARS_MOORE_LAW() {
        return YEARS_MOORE_LAW;
    }

    public void setYEARS_MOORE_LAW(BigInteger YEARS_MOORE_LAW) {
        DefaultInitParameters.YEARS_MOORE_LAW = YEARS_MOORE_LAW;
    }

    public int getSLOT_PER_EPOCH_INT() {
        return SLOT_PER_EPOCH_INT;
    }

    public void setSLOT_PER_EPOCH_INT(int SLOT_PER_EPOCH_INT) {
        DefaultInitParameters.SLOT_PER_EPOCH_INT = SLOT_PER_EPOCH_INT;
    }

    public int getBLOCK_PENATY_LIMIT() {
        return BLOCK_PENATY_LIMIT;
    }

    public void setBLOCK_PENATY_LIMIT(int BLOCK_PENATY_LIMIT) {
        DefaultInitParameters.BLOCK_PENATY_LIMIT = BLOCK_PENATY_LIMIT;
    }

    public BigInteger getSLOT_PER_EPOCH() {
        return SLOT_PER_EPOCH;
    }

    public void setSLOT_PER_EPOCH(BigInteger SLOT_PER_EPOCH) {
        DefaultInitParameters.SLOT_PER_EPOCH = SLOT_PER_EPOCH;
    }

    public BigInteger getMAX_ALLOWED_SLOTS_PER_EPOCH() {
        return MAX_ALLOWED_SLOTS_PER_EPOCH;
    }

    public void setMAX_ALLOWED_SLOTS_PER_EPOCH(BigInteger MAX_ALLOWED_SLOTS_PER_EPOCH) {
        DefaultInitParameters.MAX_ALLOWED_SLOTS_PER_EPOCH = MAX_ALLOWED_SLOTS_PER_EPOCH;
    }

    public int getTRANSACTION_LIMIT_MB_PER_BLOCK() {
        return TRANSACTION_LIMIT_MB_PER_BLOCK;
    }

    public void setTRANSACTION_LIMIT_MB_PER_BLOCK(int TRANSACTION_LIMIT_MB_PER_BLOCK) {
        DefaultInitParameters.TRANSACTION_LIMIT_MB_PER_BLOCK = TRANSACTION_LIMIT_MB_PER_BLOCK;
    }

    public int getTRANSACTION_LIMIT_NUMBER_PER_BLOCK() {
        return TRANSACTION_LIMIT_NUMBER_PER_BLOCK;
    }

    public void setTRANSACTION_LIMIT_NUMBER_PER_BLOCK(int TRANSACTION_LIMIT_NUMBER_PER_BLOCK) {
        DefaultInitParameters.TRANSACTION_LIMIT_NUMBER_PER_BLOCK = TRANSACTION_LIMIT_NUMBER_PER_BLOCK;
    }

    public int getPAYBACK_LIMIT_SIZE_MB() {
        return PAYBACK_LIMIT_SIZE_MB;
    }

    public void setPAYBACK_LIMIT_SIZE_MB(int PAYBACK_LIMIT_SIZE_MB) {
        DefaultInitParameters.PAYBACK_LIMIT_SIZE_MB = PAYBACK_LIMIT_SIZE_MB;
    }

    public int getPENALTY_BLOCKS_PER_BLOCK_OVER_THE_LIMIT() {
        return PENALTY_BLOCKS_PER_BLOCK_OVER_THE_LIMIT;
    }

    public void setPENALTY_BLOCKS_PER_BLOCK_OVER_THE_LIMIT(int PENALTY_BLOCKS_PER_BLOCK_OVER_THE_LIMIT) {
        DefaultInitParameters.PENALTY_BLOCKS_PER_BLOCK_OVER_THE_LIMIT = PENALTY_BLOCKS_PER_BLOCK_OVER_THE_LIMIT;
    }

    public BigInteger getBLOCK_COINBASE() {
        return BLOCK_COINBASE;
    }

    public void setBLOCK_COINBASE(BigInteger BLOCK_COINBASE) {
        DefaultInitParameters.BLOCK_COINBASE = BLOCK_COINBASE;
    }

    public long getTRANSACTION_VALIDITY_THRESHOLD() {
        return TRANSACTION_VALIDITY_THRESHOLD;
    }

    public void setTRANSACTION_VALIDITY_THRESHOLD(long TRANSACTION_VALIDITY_THRESHOLD) {
        DefaultInitParameters.TRANSACTION_VALIDITY_THRESHOLD = TRANSACTION_VALIDITY_THRESHOLD;
    }

    public long getBROADCAST_BUFFER_QUEUE_VALIDITY_THRESHOLD() {
        return BROADCAST_BUFFER_QUEUE_VALIDITY_THRESHOLD;
    }

    public void setBROADCAST_BUFFER_QUEUE_VALIDITY_THRESHOLD(long BROADCAST_BUFFER_QUEUE_VALIDITY_THRESHOLD) {
        DefaultInitParameters.BROADCAST_BUFFER_QUEUE_VALIDITY_THRESHOLD = BROADCAST_BUFFER_QUEUE_VALIDITY_THRESHOLD;
    }

    public long getFAST_SYNC_EXECUTION_TIME_SPACE() {
        return FAST_SYNC_EXECUTION_TIME_SPACE;
    }

    public void setFAST_SYNC_EXECUTION_TIME_SPACE(long FAST_SYNC_EXECUTION_TIME_SPACE) {
        DefaultInitParameters.FAST_SYNC_EXECUTION_TIME_SPACE = FAST_SYNC_EXECUTION_TIME_SPACE;
    }

    public long getTHREAD_POOL_EXECUTOR_LIMIT_VALUE() {
        return THREAD_POOL_EXECUTOR_LIMIT_VALUE;
    }

    public void setTHREAD_POOL_EXECUTOR_LIMIT_VALUE(long THREAD_POOL_EXECUTOR_LIMIT_VALUE) {
        DefaultInitParameters.THREAD_POOL_EXECUTOR_LIMIT_VALUE = THREAD_POOL_EXECUTOR_LIMIT_VALUE;
    }

    public TimeUnit getTHREAD_POOL_EXECUTOR_LIMIT_UNIT() {
        return THREAD_POOL_EXECUTOR_LIMIT_UNIT;
    }

    public void setTHREAD_POOL_EXECUTOR_LIMIT_UNIT(TimeUnit THREAD_POOL_EXECUTOR_LIMIT_UNIT) {
        DefaultInitParameters.THREAD_POOL_EXECUTOR_LIMIT_UNIT = THREAD_POOL_EXECUTOR_LIMIT_UNIT;
    }

    public long getTKM_BUFFER_QUEUE_VALIDITY_THRESHOLD() {
        return TKM_BUFFER_QUEUE_VALIDITY_THRESHOLD;
    }

    public void setTKM_BUFFER_QUEUE_VALIDITY_THRESHOLD(long TKM_BUFFER_QUEUE_VALIDITY_THRESHOLD) {
        DefaultInitParameters.TKM_BUFFER_QUEUE_VALIDITY_THRESHOLD = TKM_BUFFER_QUEUE_VALIDITY_THRESHOLD;
    }

    public int getTKM_BUFFER_QUEUE_TRANSACTIONS_NUMBER() {
        return TKM_BUFFER_QUEUE_TRANSACTIONS_NUMBER;
    }

    public void setTKM_BUFFER_QUEUE_TRANSACTIONS_NUMBER(int TKM_BUFFER_QUEUE_TRANSACTIONS_NUMBER) {
        DefaultInitParameters.TKM_BUFFER_QUEUE_TRANSACTIONS_NUMBER = TKM_BUFFER_QUEUE_TRANSACTIONS_NUMBER;
    }

    public int getREQUEST_PAY_MESSAGE_LIMIT() {
        return REQUEST_PAY_MESSAGE_LIMIT;
    }

    public void setREQUEST_PAY_MESSAGE_LIMIT(int REQUEST_PAY_MESSAGE_LIMIT) {
        DefaultInitParameters.REQUEST_PAY_MESSAGE_LIMIT = REQUEST_PAY_MESSAGE_LIMIT;
    }

    public int getMAX_NUMBER_OF_BLOCKS_IN_BROADCAST_BUFFER_QUEUE() {
        return MAX_NUMBER_OF_BLOCKS_IN_BROADCAST_BUFFER_QUEUE;
    }

    public void setMAX_NUMBER_OF_BLOCKS_IN_BROADCAST_BUFFER_QUEUE(int MAX_NUMBER_OF_BLOCKS_IN_BROADCAST_BUFFER_QUEUE) {
        DefaultInitParameters.MAX_NUMBER_OF_BLOCKS_IN_BROADCAST_BUFFER_QUEUE = MAX_NUMBER_OF_BLOCKS_IN_BROADCAST_BUFFER_QUEUE;
    }

    public int getMINIMUM_STAKE_BET_UNIT() {
        return MINIMUM_STAKE_BET_UNIT;
    }

    public void setMINIMUM_STAKE_BET_UNIT(int MINIMUM_STAKE_BET_UNIT) {
        DefaultInitParameters.MINIMUM_STAKE_BET_UNIT = MINIMUM_STAKE_BET_UNIT;
    }

    public BigInteger getMINIMUM_BALANCE_OVER_THE_LIMIT() {
        return MINIMUM_BALANCE_OVER_THE_LIMIT;
    }

    public void setMINIMUM_BALANCE_OVER_THE_LIMIT(BigInteger MINIMUM_BALANCE_OVER_THE_LIMIT) {
        DefaultInitParameters.MINIMUM_BALANCE_OVER_THE_LIMIT = MINIMUM_BALANCE_OVER_THE_LIMIT;
    }

    public String getRIGGED_VRF_SEED() {
        return RIGGED_VRF_SEED;
    }

    public void setRIGGED_VRF_SEED(String RIGGED_VRF_SEED) {
        DefaultInitParameters.RIGGED_VRF_SEED = RIGGED_VRF_SEED;
    }

    public String getNODE_STATE_NAME() {
        return NODE_STATE_NAME;
    }

    public void setNODE_STATE_NAME(String NODE_STATE_NAME) {
        DefaultInitParameters.NODE_STATE_NAME = NODE_STATE_NAME;
    }

    public String getNODE_WALLET_NAME() {
        return NODE_WALLET_NAME;
    }

    public void setNODE_WALLET_NAME(String NODE_WALLET_NAME) {
        DefaultInitParameters.NODE_WALLET_NAME = NODE_WALLET_NAME;
    }

    public String getTEMPORARY_NODE_WALLET() {
        return TEMPORARY_NODE_WALLET;
    }

    public void setTEMPORARY_NODE_WALLET(String TEMPORARY_NODE_WALLET) {
        DefaultInitParameters.TEMPORARY_NODE_WALLET = TEMPORARY_NODE_WALLET;
    }

    public int getEPHEMERAL_KEYS_SIZE() {
        return EPHEMERAL_KEYS_SIZE;
    }

    public void setEPHEMERAL_KEYS_SIZE(int EPHEMERAL_KEYS_SIZE) {
        DefaultInitParameters.EPHEMERAL_KEYS_SIZE = EPHEMERAL_KEYS_SIZE;
    }

    public int getLIMIT_PROPOSED_KEYS_NUMBER() {
        return LIMIT_PROPOSED_KEYS_NUMBER;
    }

    public void setLIMIT_PROPOSED_KEYS_NUMBER(int LIMIT_PROPOSED_KEYS_NUMBER) {
        DefaultInitParameters.LIMIT_PROPOSED_KEYS_NUMBER = LIMIT_PROPOSED_KEYS_NUMBER;
    }

    public int getLIMIT_BACKTRACK() {
        return LIMIT_BACKTRACK;
    }

    public void setLIMIT_BACKTRACK(int LIMIT_BACKTRACK) {
        DefaultInitParameters.LIMIT_BACKTRACK = LIMIT_BACKTRACK;
    }

    public int getLIMIT_PROPOSED_NODE_KEYS() {
        return LIMIT_PROPOSED_NODE_KEYS;
    }

    public void setLIMIT_PROPOSED_NODE_KEYS(int LIMIT_PROPOSED_NODE_KEYS) {
        DefaultInitParameters.LIMIT_PROPOSED_NODE_KEYS = LIMIT_PROPOSED_NODE_KEYS;
    }

    public int getLIMIT_PROPOSED_HISTORY_TRANSACTIONS_SIZE() {
        return LIMIT_PROPOSED_HISTORY_TRANSACTIONS_SIZE;
    }

    public void setLIMIT_PROPOSED_HISTORY_TRANSACTIONS_SIZE(int LIMIT_PROPOSED_HISTORY_TRANSACTIONS_SIZE) {
        DefaultInitParameters.LIMIT_PROPOSED_HISTORY_TRANSACTIONS_SIZE = LIMIT_PROPOSED_HISTORY_TRANSACTIONS_SIZE;
    }

    public KeyContexts.WalletCypher getEPHEMERAL_BLOCK_CYPHER() {
        return EPHEMERAL_BLOCK_CYPHER;
    }

    public void setEPHEMERAL_BLOCK_CYPHER(KeyContexts.WalletCypher EPHEMERAL_BLOCK_CYPHER) {
        DefaultInitParameters.EPHEMERAL_BLOCK_CYPHER = EPHEMERAL_BLOCK_CYPHER;
    }

    public int getBLOCK_POOL_LIMIT() {
        return BLOCK_POOL_LIMIT;
    }

    public void setBLOCK_POOL_LIMIT(int BLOCK_POOL_LIMIT) {
        DefaultInitParameters.BLOCK_POOL_LIMIT = BLOCK_POOL_LIMIT;
    }

    public int[] getTO_DATA_LENGTH_WHITELIST() {
        return TO_DATA_LENGTH_WHITELIST;
    }

    public void setTO_DATA_LENGTH_WHITELIST(int[] TO_DATA_LENGTH_WHITELIST) {
        DefaultInitParameters.TO_DATA_LENGTH_WHITELIST = TO_DATA_LENGTH_WHITELIST;
    }

    public KeyContexts.WalletCypher[] getPERMITTED_CYPHERS_FOR_MINING() {
        return PERMITTED_CYPHERS_FOR_MINING;
    }

    public void setPERMITTED_CYPHERS_FOR_MINING(KeyContexts.WalletCypher[] PERMITTED_CYPHERS_FOR_MINING) {
        DefaultInitParameters.PERMITTED_CYPHERS_FOR_MINING = PERMITTED_CYPHERS_FOR_MINING;
    }

    public boolean isTEST_FUNCTIONS_ENABLED() {
        return TEST_FUNCTIONS_ENABLED;
    }

    public void setTEST_FUNCTIONS_ENABLED(boolean TEST_FUNCTIONS_ENABLED) {
        DefaultInitParameters.TEST_FUNCTIONS_ENABLED = TEST_FUNCTIONS_ENABLED;
    }

    public boolean isSIMULATION_FUNCTIONS_ENABLED() {
        return SIMULATION_FUNCTIONS_ENABLED;
    }

    public void setSIMULATION_FUNCTIONS_ENABLED(boolean SIMULATION_FUNCTIONS_ENABLED) {
        DefaultInitParameters.SIMULATION_FUNCTIONS_ENABLED = SIMULATION_FUNCTIONS_ENABLED;
    }

    public boolean isSimulationFunctionKillAtTarget() {
        return simulationFunctionKillAtTarget;
    }

    public void setSimulationFunctionKillAtTarget(boolean simulationFunctionKillAtTarget) {
        DefaultInitParameters.simulationFunctionKillAtTarget = simulationFunctionKillAtTarget;
    }

    public boolean isDUMP_VRF_METADATA() {
        return DUMP_VRF_METADATA;
    }

    public void setDUMP_VRF_METADATA(boolean DUMP_VRF_METADATA) {
        DefaultInitParameters.DUMP_VRF_METADATA = DUMP_VRF_METADATA;
    }

    public Date getSIMULATION_DATE() {
        return SIMULATION_DATE;
    }

    public void setSIMULATION_DATE(Date SIMULATION_DATE) {
        DefaultInitParameters.SIMULATION_DATE = SIMULATION_DATE;
    }

    public String getBLOCKS_ID_PATTERN_EXPR() {
        return BLOCKS_ID_PATTERN_EXPR;
    }

    public void setBLOCKS_ID_PATTERN_EXPR(String BLOCKS_ID_PATTERN_EXPR) {
        DefaultInitParameters.BLOCKS_ID_PATTERN_EXPR = BLOCKS_ID_PATTERN_EXPR;
    }

    public Pattern getBLOCKS_ID_PATTERN() {
        return BLOCKS_ID_PATTERN;
    }

    public void setBLOCKS_ID_PATTERN(Pattern BLOCKS_ID_PATTERN) {
        DefaultInitParameters.BLOCKS_ID_PATTERN = BLOCKS_ID_PATTERN;
    }

    public boolean isIsReplicaNode() {
        return isReplicaNode;
    }

    public void setIsReplicaNode(boolean isReplicaNode) {
        DefaultInitParameters.isReplicaNode = isReplicaNode;
    }

    public String getELASTICSEARCH_HOST() {
        return ELASTICSEARCH_HOST;
    }

    public void setELASTICSEARCH_HOST(String ELASTICSEARCH_HOST) {
        DefaultInitParameters.ELASTICSEARCH_HOST = ELASTICSEARCH_HOST;
    }

    public int getELASTICSEARCH_PORT() {
        return ELASTICSEARCH_PORT;
    }

    public void setELASTICSEARCH_PORT(int ELASTICSEARCH_PORT) {
        DefaultInitParameters.ELASTICSEARCH_PORT = ELASTICSEARCH_PORT;
    }

    public int getSEARCH_RESULT_LIMIT() {
        return DEFAULT_SEARCH_RESULT_LIMIT;
    }

    public void setSEARCH_RESULT_LIMIT(int SEARCH_RESULT_LIMIT) {
        DefaultInitParameters.DEFAULT_SEARCH_RESULT_LIMIT = SEARCH_RESULT_LIMIT;
    }


    public String getFULL_ADDRESS_MAIN_LIST() {
        return FULL_ADDRESS_MAIN_LIST;
    }

    public void setFULL_ADDRESS_MAIN_LIST(String FULL_ADDRESS_MAIN_LIST) {
        DefaultInitParameters.FULL_ADDRESS_MAIN_LIST = FULL_ADDRESS_MAIN_LIST;
    }

    public String getFULL_ADDRESS_MAIN_LIST_STATS() {
        return FULL_ADDRESS_MAIN_LIST_STATS;
    }

    public void setFULL_ADDRESS_MAIN_LIST_STATS(String FULL_ADDRESS_MAIN_LIST_STATS) {
        DefaultInitParameters.FULL_ADDRESS_MAIN_LIST_STATS = FULL_ADDRESS_MAIN_LIST_STATS;
    }

    public String getFULL_OVERFLOW_LIST() {
        return FULL_OVERFLOW_LIST;
    }

    public void setFULL_OVERFLOW_LIST(String FULL_OVERFLOW_LIST) {
        DefaultInitParameters.FULL_OVERFLOW_LIST = FULL_OVERFLOW_LIST;
    }

    public String getREQUEST_STAKE_LIST() {
        return REQUEST_STAKE_LIST;
    }

    public void setREQUEST_STAKE_LIST(String REQUEST_STAKE_LIST) {
        DefaultInitParameters.REQUEST_STAKE_LIST = REQUEST_STAKE_LIST;
    }

    public String getREQUEST_SM_ADDR() {
        return REQUEST_SM_ADDR;
    }

    public void setREQUEST_SM_ADDR(String REQUEST_SM_ADDR) {
        DefaultInitParameters.REQUEST_SM_ADDR = REQUEST_SM_ADDR;
    }

    public String getREQUEST_SM_HOTMOKA_REFERENCE() {
        return REQUEST_SM_HOTMOKA_REFERENCE;
    }

    public void setREQUEST_SM_HOTMOKA_REFERENCE(String REQUEST_SM_HOTMOKA_REFERENCE) {
        DefaultInitParameters.REQUEST_SM_HOTMOKA_REFERENCE = REQUEST_SM_HOTMOKA_REFERENCE;
    }

    public String getREQUEST_APP_VERSION() {
        return REQUEST_APP_VERSION;
    }

    public void setREQUEST_APP_VERSION(String REQUEST_APP_VERSION) {
        DefaultInitParameters.REQUEST_APP_VERSION = REQUEST_APP_VERSION;
    }

    public String getREQUEST_BLOCKCHAIN_SETTINGS() {
        return REQUEST_BLOCKCHAIN_SETTINGS;
    }

    public void setREQUEST_BLOCKCHAIN_SETTINGS(String REQUEST_BLOCKCHAIN_SETTINGS) {
        DefaultInitParameters.REQUEST_BLOCKCHAIN_SETTINGS = REQUEST_BLOCKCHAIN_SETTINGS;
    }

    public String getSIMPLE_WALLET_APPLICATION_NAME() {
        return SIMPLE_WALLET_APPLICATION_NAME;
    }

    public void setSIMPLE_WALLET_APPLICATION_NAME(String SIMPLE_WALLET_APPLICATION_NAME) {
        DefaultInitParameters.SIMPLE_WALLET_APPLICATION_NAME = SIMPLE_WALLET_APPLICATION_NAME;
    }

    public String getSIMPLE_WALLET_APPLICATION_NUMBER() {
        return SIMPLE_WALLET_APPLICATION_NUMBER;
    }

    public void setSIMPLE_WALLET_APPLICATION_NUMBER(String SIMPLE_WALLET_APPLICATION_NUMBER) {
        DefaultInitParameters.SIMPLE_WALLET_APPLICATION_NUMBER = SIMPLE_WALLET_APPLICATION_NUMBER;
    }

    public int getQTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS() {
        return QTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS;
    }

    public void setQTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS(int QTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS) {
        DefaultInitParameters.QTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS = QTESLA_COMPRESSED_ADDRESSES_FOLDER_LEVELS;
    }

    public int getLRBEANS_LIVENESS() {
        return LRBEANS_LIVENESS;
    }

    public void setLRBEANS_LIVENESS(int LRBEANS_LIVENESS) {
        DefaultInitParameters.LRBEANS_LIVENESS = LRBEANS_LIVENESS;
    }

    public long getLRBEANS_VALIDITY_THRESHOLD() {
        return LRBEANS_VALIDITY_THRESHOLD;
    }

    public void setLRBEANS_VALIDITY_THRESHOLD(long LRBEANS_VALIDITY_THRESHOLD) {
        DefaultInitParameters.LRBEANS_VALIDITY_THRESHOLD = LRBEANS_VALIDITY_THRESHOLD;
    }


    public String getTAKAMAKA_CODE_JAR_RESOURCE() {
        return TAKAMAKA_CODE_JAR_RESOURCE;
    }

    public void setTAKAMAKA_CODE_JAR_RESOURCE(String TAKAMAKA_CODE_JAR_RESOURCE) {
        DefaultInitParameters.TAKAMAKA_CODE_JAR_RESOURCE = TAKAMAKA_CODE_JAR_RESOURCE;
    }

    public int getS_REGISTER_ADDRESS_KEY_NUMBER() {
        return S_REGISTER_ADDRESS_KEY_NUMBER;
    }

    public void setS_REGISTER_ADDRESS_KEY_NUMBER(int S_REGISTER_ADDRESS_KEY_NUMBER) {
        DefaultInitParameters.S_REGISTER_ADDRESS_KEY_NUMBER = S_REGISTER_ADDRESS_KEY_NUMBER;
    }

    public boolean isPRINT_EXCEPTIONS() {
        return PRINT_EXCEPTIONS;
    }

    public void setPRINT_EXCEPTIONS(boolean PRINT_EXCEPTIONS) {
        DefaultInitParameters.PRINT_EXCEPTIONS = PRINT_EXCEPTIONS;
    }

    public String getLIVE_STATE_FOLDER() {
        return LIVE_STATE_FOLDER;
    }

    public void setLIVE_STATE_FOLDER(String LIVE_STATE_FOLDER) {
        DefaultInitParameters.LIVE_STATE_FOLDER = LIVE_STATE_FOLDER;
    }

    public String getREFERENCE_KEYS_DATABASE_MULTIPART_LEVELS() {
        return REFERENCE_KEYS_DATABASE_MULTIPART_LEVELS;
    }

    public void setREFERENCE_KEYS_DATABASE_MULTIPART_LEVELS(String REFERENCE_KEYS_DATABASE_MULTIPART_LEVELS) {
        DefaultInitParameters.REFERENCE_KEYS_DATABASE_MULTIPART_LEVELS = REFERENCE_KEYS_DATABASE_MULTIPART_LEVELS;
    }

    public KeyContexts.WalletCypher getNODE_WALLET_CYPHER() {
        return NODE_WALLET_CYPHER;
    }

    public void setNODE_WALLET_CYPHER(KeyContexts.WalletCypher NODE_WALLET_CYPHER) {
        DefaultInitParameters.NODE_WALLET_CYPHER = NODE_WALLET_CYPHER;
    }

    public String getREFERENCE_KEYS_DATABASE_NAME() {
        return REFERENCE_KEYS_DATABASE_NAME;
    }

    public void setREFERENCE_KEYS_DATABASE_NAME(String REFERENCE_KEYS_DATABASE_NAME) {
        DefaultInitParameters.REFERENCE_KEYS_DATABASE_NAME = REFERENCE_KEYS_DATABASE_NAME;
    }

    public int getNODE_WALLET_INDEX() {
        return NODE_WALLET_INDEX;
    }

    public void setNODE_WALLET_INDEX(int NODE_WALLET_INDEX) {
        DefaultInitParameters.NODE_WALLET_INDEX = NODE_WALLET_INDEX;
    }

    public ConcurrentSkipListMap<String, String> getREFERENCE_KEYS_DATABASE_CONFIGURATION() {
        return REFERENCE_KEYS_DATABASE_CONFIGURATION;
    }

    public void setREFERENCE_KEYS_DATABASE_CONFIGURATION(ConcurrentSkipListMap<String, String> REFERENCE_KEYS_DATABASE_CONFIGURATION) {
        DefaultInitParameters.REFERENCE_KEYS_DATABASE_CONFIGURATION = REFERENCE_KEYS_DATABASE_CONFIGURATION;
    }

    public int getSimulationFunctionKillAtBootstrapBlockNum() {
        return simulationFunctionKillAtBootstrapBlockNum;
    }

    public void setSimulationFunctionKillAtBootstrapBlockNum(int simulationFunctionKillAtBootstrapBlockNum) {
        DefaultInitParameters.simulationFunctionKillAtBootstrapBlockNum = simulationFunctionKillAtBootstrapBlockNum;
    }

    public String getSTATE_DATABASE_FOLDER() {
        return STATE_DATABASE_FOLDER;
    }

    public void setSTATE_DATABASE_FOLDER(String STATE_DATABASE_FOLDER) {
        DefaultInitParameters.STATE_DATABASE_FOLDER = STATE_DATABASE_FOLDER;
    }

    public String getSTATE_DATABASE_NAME() {
        return STATE_DATABASE_NAME;
    }

    public void setSTATE_DATABASE_NAME(String STATE_DATABASE_NAME) {
        DefaultInitParameters.STATE_DATABASE_NAME = STATE_DATABASE_NAME;
    }

    public boolean isSimulationFunctionKillAtBootstrap() {
        return simulationFunctionKillAtBootstrap;
    }

    public void setSimulationFunctionKillAtBootstrap(boolean simulationFunctionKillAtBootstrap) {
        DefaultInitParameters.simulationFunctionKillAtBootstrap = simulationFunctionKillAtBootstrap;
    }

}
