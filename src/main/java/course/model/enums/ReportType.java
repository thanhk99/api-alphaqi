package course.model.enums;

import lombok.Getter;

@Getter
public enum ReportType {
    // Parents
    MACRO("Báo cáo vĩ mô"),
    COMPANY_INDUSTRY("Báo cáo Ngành, Công ty"),
    ASSET_MANAGEMENT("Báo cáo Quản lý tài sản"),

    // Children of MACRO
    MACRO_MONEY_MARKET_BOND("Báo cáo Thị trường Tiền tệ & Trái phiếu", MACRO),
    MACRO_TOPICAL("Báo cáo chuyên đề Vĩ mô", MACRO),

    // Children of COMPANY_INDUSTRY
    COMPANY("Báo cáo Công ty", COMPANY_INDUSTRY),
    SECTOR("Báo cáo ngành", COMPANY_INDUSTRY),

    // Children of ASSET_MANAGEMENT
    ASSET_ALLOCATION("Báo cáo Phân bổ tài sản", ASSET_MANAGEMENT),
    WEALTH_MANAGEMENT_TOPICAL("Báo cáo chuyên đề Quản lý tài sản", ASSET_MANAGEMENT),

    // Other top-level types
    INVESTMENT_STRATEGY("Báo cáo chiến lược đầu tư"),
    CIO_REPORT("Báo cáo CIO");

    private final String displayName;
    private final ReportType parent;

    ReportType(String displayName) {
        this(displayName, null);
    }

    ReportType(String displayName, ReportType parent) {
        this.displayName = displayName;
        this.parent = parent;
    }
}
