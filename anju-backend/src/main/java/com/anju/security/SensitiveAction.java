package com.anju.security;

import lombok.Getter;

@Getter
public enum SensitiveAction {
    INVOICE_ISSUE("invoice_issue", true),
    INVOICE_REJECT("invoice_reject", true),
    PERMANENT_DELETE("permanent_delete", true),
    USER_DELETE("user_delete", true),
    REFUND_PROCESS("refund_process", true),
    CANCEL_APPOINTMENT("cancel_appointment", false),
    RESCHEDULE_APPOINTMENT("reschedule_appointment", false),
    ROLLBACK_FILE("rollback_file", true);

    private final String code;
    private final boolean requiresSecondaryPassword;

    SensitiveAction(String code, boolean requiresSecondaryPassword) {
        this.code = code;
        this.requiresSecondaryPassword = requiresSecondaryPassword;
    }
}
