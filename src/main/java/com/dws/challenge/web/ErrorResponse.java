package com.dws.challenge.web;

import java.time.Instant;

public record ErrorResponse(String status, int code, String error, Instant timestamp) {
}