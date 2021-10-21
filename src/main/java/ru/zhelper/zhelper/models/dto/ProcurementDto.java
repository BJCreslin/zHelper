package ru.zhelper.zhelper.models.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@Builder
public class ProcurementDto {
    @NotNull
    @NotBlank
    private String fzNumber;
    @NotNull
    @NotBlank
    private String uin;
    @NotNull
    @NotBlank
    private String objectOf;
    @NotNull
    @NotBlank
    private String publisherName;
    private String contractPrice;
    @NotNull
    @NotBlank
    private String procedureType;
    private String stage;
    private String linkOnPlacement;
    private String applicationDeadline;
    private String applicationSecure;
    private String contractSecure;
    private String restrictions;
    private String lastUpdatedFromEIS;
    private String dateOfPlacement;
    private String dateOfAuction;
    private String timeOfAuction;
    private String timeZone;
    private String etpName;
    private String etpUrl;
    private String summingUpDate;
}
