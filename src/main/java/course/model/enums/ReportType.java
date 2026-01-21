package course.model.enums;

import lombok.Getter;

@Getter
public enum ReportType {
    MACRO("Báo cáo vĩ mô"),
    INVESTMENT_STRATEGY("Báo cáo chiến lược đầu tư"),
    COMPANY_INDUSTRY("Báo cáo công ty, ngành"),
    ASSET_MANAGEMENT("Báo cáo quản lý tài sản");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }
}
