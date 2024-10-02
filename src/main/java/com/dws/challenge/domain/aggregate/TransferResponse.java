package com.dws.challenge.domain.aggregate;

import com.dws.challenge.domain.valueobject.TransferStatus;
import java.math.BigDecimal;

/**
 * Transfer response DTO.
 * @param status Indicates if transfer was a SUCCESS or FAILURE.
 * @param transferredAmount amount to be transferred.
 */
public record TransferResponse(TransferStatus status, BigDecimal transferredAmount) {
}