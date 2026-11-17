package cn.iocoder.yudao.module.data.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 错误码常量类
 */
public class ErrorCodeConstants {

    // ========== 设计属性 1-001 ==========
    public static final ErrorCode DESIGN_ATTRIBUTE_NOT_EXISTS = new ErrorCode(1001, "设计属性不存在");

    // ========== 故障信息不存在 1002 ==========
    public static final ErrorCode FAULT_INFORMATION_NOT_EXISTS = new ErrorCode(1002, "故障信息不存在");

    // ========== 保障属性不存在 1003 ==========
    public static final ErrorCode GUARANTEE_ATTRIBUTES_NOT_EXISTS = new ErrorCode(1003, "保障属性不存在");

    // ========== 标识属性不存在 1004 ==========
    public static final ErrorCode IDENTIFICATION_ATTRIBUTE_NOT_EXISTS = new ErrorCode(1004, "标识属性不存在");

    // ========== 机型属性不存在 1005 ==========
    public static final ErrorCode MEMORY_ATTRIBUTE_NOT_EXISTS = new ErrorCode(1005, "机型属性不存在");

    // ========== 产品不存在 1006 ==========
    public static final ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1006, "产品不存在");

    // ========== 供应属性不存在 1007 ==========
    public static final ErrorCode SUPPLY_ATTRIBUTE_NOT_EXISTS = new ErrorCode(1007, "供应属性不存在");

    // ========== 客户信息不存在 1008==========
    public static final ErrorCode CUSTOMER_INFO_NOT_EXISTS = new ErrorCode(1008, "客户信息不存在");

    // ========== 培训计划不存在 1009 ==========
    public static final ErrorCode TRAINING_PLAN_NOT_EXISTS = new ErrorCode(1009, "培训计划不存在");

    // ========== 培训资料管理不存在 1010 ==========
    public static final ErrorCode TRAINING_MATERIALS_NOT_EXISTS = new ErrorCode(1010, "培训资料管理不存在");

    // ========== 审批历史不存在 1011 ==========
    public static final ErrorCode APPROVAL_HISTORY_NOT_EXISTS = new ErrorCode(1011, "审批历史不存在");

    // ========== 文档管理不存在 1012 ==========
    public static final ErrorCode DOCUMENT_MANAGEMENT_NOT_EXISTS = new ErrorCode(1012, "文档管理不存在");

    // ========== 培训前准备不存在 1013==========
    public static final ErrorCode PRE_TRAINING_PREPARATION_NOT_EXISTS = new ErrorCode(1013, "培训前准备不存在");

    // ========== 培训签到不存在 1014 ==========
    public static final ErrorCode TRAINING_ATTENDANCE_NOT_EXISTS = new ErrorCode(1014, "培训签到不存在");

    // ========== 培训管理不存在 1015 ==========
    public static final ErrorCode TRAINING_MANAGEMENT_NOT_EXISTS = new ErrorCode(1015, "培训管理不存在");

    // ========== 培训记录不存在 1016 ==========
    public static final ErrorCode TRAINING_RECORD_NOT_EXISTS = new ErrorCode(1016, "培训记录不存在");

    // ========== 培训需求不存在 1017 ==========
    public static final ErrorCode TRAINING_REQUIREMENTS_NOT_EXISTS = new ErrorCode(1017, "培训需求不存在");

    // ========== 购买产品信息不存在 1018 ==========
    public static final ErrorCode PURCHASE_PRODUCT_INFO_NOT_EXISTS = new ErrorCode(1018, "购买产品信息不存在");

    // ========== 飞行性能数据管理不存在 1019 ==========
    public static final ErrorCode RELIABILITY_DATA_METRICS_NOT_EXISTS = new ErrorCode(1019, "可靠性指标不存在");
    // ========== 文档管理 TODO 补充编号 ==========
    public static final ErrorCode DOC_DIRECTORY_NOT_EXISTS = new ErrorCode(1026, "文档管理不存在");
    public static final ErrorCode DOC_DIRECTORY_EXITS_CHILDREN = new ErrorCode(1021, "存在存在子文档管理，无法删除");
    public static final ErrorCode DOC_DIRECTORY_PARENT_NOT_EXITS = new ErrorCode(1022,"父级文档管理不存在");
    public static final ErrorCode DOC_DIRECTORY_PARENT_ERROR = new ErrorCode(1023, "不能设置自己为父文档管理");
    public static final ErrorCode DOC_DIRECTORY_NAME_DUPLICATE = new ErrorCode(1024, "已经存在该目录名称 （如文档管理，培训资料等）的文档管理");
    public static final ErrorCode DOC_DIRECTORY_PARENT_IS_CHILD = new ErrorCode(1025, "不能设置自己的子DocDirectory为父DocDirectory");

    // ========== 飞行复现不存在 1025 ==========
    public static final ErrorCode FLIGHT_DATA_NOT_EXISTS = new ErrorCode(1025, "飞行复现不存在");

    // ========== QAR数据不存在 1026 ==========
    public static final ErrorCode QAR_DATA_NOT_EXISTS = new ErrorCode(1026, "QAR数据不存在");

    // ========== 培训考核 TODO 补充编号 ==========
    public static final ErrorCode TRAINING_ASSESSMENT_NOT_EXISTS = new ErrorCode(1027, "培训考核不存在");

    // ========== 培训经历 TODO 补充编号 ==========
    public static final ErrorCode EXPERIENCE_NOT_EXISTS = new ErrorCode(1028, "培训经历不存在");

    // ========== 外置表单配置不存在 1029 ==========
    public static final ErrorCode EXTERNAL_FORM_NOT_EXISTS = new ErrorCode(1029, "外置表单配置不存在");

    // ========== 客户服务问题不存在 1020 ==========
    public static final ErrorCode CUSTOMER_SERVICE_ISSUES_NOT_EXISTS = new ErrorCode(1020, "客户服务问题不存在");

    //导入失败
    public static final ErrorCode IMPORT_FAILED = new ErrorCode(1030, "product 去重关键字段缺失：产品件号/标牌版本/序列号 必须都有值");

    //找不到投产基线
    public static final ErrorCode NOT_FOUNT_PRODUCTION_BASELINE = new ErrorCode(1050, "未找到 sheet：投产基线");
    //表头为空
    public static final ErrorCode HEADER_IS_EMPTY = new ErrorCode(1051, "sheet[投产基线] 的第2行表头为空");
    //导入失败
    public static final ErrorCode IMPORT_FAILED_ONE = new ErrorCode(1052, "导入失败");


    //产品档案不存在
    public static final ErrorCode PRODUCT_FILE_NOT_EXISTS = new ErrorCode(1031, "产品档案不存在");

    //文件不是有效的SCORM课件包: 缺少 imsmanifest.xml 或不符合 SCORM 规范
    public static final ErrorCode FILE_NOT_SCORM_PACKAGE = new ErrorCode(1032, "文件不是有效的SCORM课件包: 缺少 imsmanifest.xml 或不符合 SCORM 规范");
    // ========== QAR数据不存在 1032 ==========
    public static final ErrorCode DATA_NOT_EXISTS = new ErrorCode(1033, "QAR数据不存在");

    // ========== QAR映射信息不存在 1034 ==========
    public static final ErrorCode INFO_NOT_EXISTS = new ErrorCode(1034, "QAR映射信息不存在");

    // ========== 仅允许发起单条数据的启动指令 1035 ==========
    public static final ErrorCode ONLY_SINGLE_QAR_RECORD = new ErrorCode(1035, "仅允许发起单条数据的启动指令");

    // ========== 可靠性数据收集不存在 1036 ==========
    public static final ErrorCode RELIABILITY_DATA_NOT_EXISTS = new ErrorCode(1036, "可靠性数据收集不存在");

    public static final ErrorCode TRAINING_ATTENDANCE_ASSESSMENT_NOT_EXISTS = new ErrorCode(1037, "培训签到考核证书不存在");
    public static final ErrorCode PPQL_NOT_EXISTS = new ErrorCode(1038, "培训前准备问题不存在");

    // ========== 客户编号已存在 1039 ==========
    public static final ErrorCode CUSTOMER_ID_EXISTS = new ErrorCode(1039, "客户编号已存在");

    public static final ErrorCode PRODUCT_BOM_NOT_EXISTS = new ErrorCode(1040, "产品BOM不存在");

    public static final ErrorCode QAR_NOT_EXISTS = new ErrorCode(1041, "qar数据管理不存在");
    public static final ErrorCode QAR_FILE_IMPORT_FAILED = new ErrorCode(1042, "QAR记录已创建(id={})，但文件入库失败: {}");
    /** MySQL 未启用「从本机加载数据」时 LOAD DATA LOCAL 会报错 */
    public static final ErrorCode QAR_LOAD_LOCAL_DISABLED = new ErrorCode(1043, "QAR 大文件入库失败：MySQL 未启用「从本机加载数据」。请确认：1) 在 MySQL 实际使用的 my.ini 的 [mysqld] 下添加 local_infile=1（或 local-infile=1）；2) 保存后必须重启 MySQL 服务；3) 重启后登录 MySQL 执行 SHOW GLOBAL VARIABLES LIKE 'local_infile' 确认 Value=ON。若仍为 OFF，在命令行执行 mysql --help 查看「Default options」里加载的配置文件路径，确保修改的是该文件。应用侧 JDBC 已配置 allowLoadLocalInfile=true。");

    public static final ErrorCode ATTRIBUTE_NOT_EXISTS = new ErrorCode(1044, "产品属性不存在");
    // 私有构造函数防止实例化
    private ErrorCodeConstants() {
    }
}