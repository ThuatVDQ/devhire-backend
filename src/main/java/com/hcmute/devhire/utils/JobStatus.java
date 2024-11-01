package com.hcmute.devhire.utils;

public enum JobStatus {
    PENDING, //--(admin approve)-->
    OPEN,
    CLOSED,  //--(hết hạn or recruiter close)--> bonus
    HOT, //member vip)
    REJECTED // --(admin reject)-->
}
