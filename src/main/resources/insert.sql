INSERT INTO MCC_CODE (ID, CODE,CAMPAIGN_NAME,MERCHANT_ID,TERMINAL_ID,TERMINAL_NAME,CREATED_DATE,MERCHANT_CATEGORY,MERCHANT_DESCRIPTION)
    VALUES ( MCC_ID_SEQ.nextval, {{code}},'{{campaign}}','{{mid}}','{{tid}}','{{terminal_name}}',SYSDATE,'{{category}}','{{description}}');
