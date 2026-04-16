package ai.po;

import java.util.List;

/**
 * 图片审核结果类
 * 用于存储AI对图片进行多维度审核的评估结果
 */
public class ImageAuditResult {

    /**
     * 整体审核状态
     * 可选值: "pass"(通过), "review"(待复审), "reject"(拒绝)
     */
    private String overallStatus;

    /**
     * 整体评分
     * 数值范围通常为0-100，分数越高风险越低
     */
    private Double overallScore;

    /**
     * 各维度审核详情
     * 包含7个维度的审核结果：色情低俗、暴力恐怖、政治敏感、违法违禁、广告营销、版权IP、公序良俗
     */
    private AuditDimensions dimensions;

    /**
     * 风险标签列表
     * 当检测到风险时，会标记相应的风险标签
     */
    private List<String> riskTags;

    /**
     * 审核建议
     * AI给出的具体审核建议和说明
     */
    private String recommendation;

    /**
     * 审核维度详情内部类
     */
    public static class AuditDimensions {

        /**
         * 色情低俗维度
         */
        private DimensionDetail pornographyVulgarity;

        /**
         * 暴力恐怖维度
         */
        private DimensionDetail violenceTerror;

        /**
         * 政治敏感维度
         */
        private DimensionDetail politicalSensitivity;

        /**
         * 违法违禁维度
         */
        private DimensionDetail illegalContraband;

        /**
         * 广告营销维度
         */
        private DimensionDetail adMarketing;

        /**
         * 版权IP维度
         */
        private DimensionDetail copyrightIp;

        /**
         * 公序良俗维度
         */
        private DimensionDetail publicMorals;

        // Getters and Setters
        public DimensionDetail getPornographyVulgarity() {
            return pornographyVulgarity;
        }

        public void setPornographyVulgarity(DimensionDetail pornographyVulgarity) {
            this.pornographyVulgarity = pornographyVulgarity;
        }

        public DimensionDetail getViolenceTerror() {
            return violenceTerror;
        }

        public void setViolenceTerror(DimensionDetail violenceTerror) {
            this.violenceTerror = violenceTerror;
        }

        public DimensionDetail getPoliticalSensitivity() {
            return politicalSensitivity;
        }

        public void setPoliticalSensitivity(DimensionDetail politicalSensitivity) {
            this.politicalSensitivity = politicalSensitivity;
        }

        public DimensionDetail getIllegalContraband() {
            return illegalContraband;
        }

        public void setIllegalContraband(DimensionDetail illegalContraband) {
            this.illegalContraband = illegalContraband;
        }

        public DimensionDetail getAdMarketing() {
            return adMarketing;
        }

        public void setAdMarketing(DimensionDetail adMarketing) {
            this.adMarketing = adMarketing;
        }

        public DimensionDetail getCopyrightIp() {
            return copyrightIp;
        }

        public void setCopyrightIp(DimensionDetail copyrightIp) {
            this.copyrightIp = copyrightIp;
        }

        public DimensionDetail getPublicMorals() {
            return publicMorals;
        }

        public void setPublicMorals(DimensionDetail publicMorals) {
            this.publicMorals = publicMorals;
        }
    }

    /**
     * 单个审核维度的详细信息
     */
    public static class DimensionDetail {

        /**
         * 该维度的风险评分
         * 数值范围通常为0-100，分数越高风险越低
         */
        private Double score;

        /**
         * 该维度的评分原因说明
         */
        private String reason;

        // Getters and Setters
        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    // Getters and Setters
    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public Double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    public AuditDimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(AuditDimensions dimensions) {
        this.dimensions = dimensions;
    }

    public List<String> getRiskTags() {
        return riskTags;
    }

    public void setRiskTags(List<String> riskTags) {
        this.riskTags = riskTags;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
