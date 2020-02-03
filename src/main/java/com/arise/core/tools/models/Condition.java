package com.arise.core.tools.models;

public abstract class Condition<DATA_TO_CHECK, DATA_TO_ACCEPT> implements FilterCriteria<DATA_TO_CHECK> {

    public abstract DATA_TO_ACCEPT getPayload();

}
