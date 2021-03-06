SELECT
STT_PK AS key,
STT_TEST_TYPE AS testType,
STT_BILLING_ITEM_CODE AS billingItemCode,
STT_SETUP_ITEM_CODE AS setupItemCode,
STT_PROCESSING_ORDER AS processingOrder,
STT_PROCESSING_GROUP AS processingGroup,
STT_SORT_ORDER AS sortOrder,
(CASE WHEN STT_ACTIVE_YN = 'Y' THEN 1 ELSE 0 END) AS isActive,
STT_COMMENT AS comments,
STT_PATHOGEN AS pathogen,
STT_ASSAY AS assay
FROM cnprcSrc_srl.SRL_TEST_TYPES