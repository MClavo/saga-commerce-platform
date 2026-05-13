package com.mclavo.ecommerce.handler;

import java.util.Map;

public record ErrorDetailResponse(
    Map<String, String> errors
) {}
